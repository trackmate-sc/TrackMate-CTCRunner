/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.helper;

import static fiji.plugin.trackmate.helper.TrackingMetricsTable.echoFilters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.Cancelable;

import com.google.gson.JsonParseException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.helper.ctc.CTCTrackingMetricsType;
import fiji.plugin.trackmate.helper.model.ParameterSweepModel;
import fiji.plugin.trackmate.helper.model.ParameterSweepModelIO;
import fiji.plugin.trackmate.helper.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.helper.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.helper.spt.SPTTrackingMetricsType;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.util.NestedIterator;
import fiji.plugin.trackmate.util.TMUtils;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.util.ValuePair;

public class HelperRunner implements Runnable, Cancelable
{

	private static final int TRACK_FILTER_LOOP = 0;

	private static final int TRACKER_SETTINGS_LOOP = 1;

	private static final int SPOT_FILTER_LOOP = 2;

	private static final int DETECTOR_SETTINGS_LOOP = 3;

	private static final int FINISHED = 4;

	private final String gtPath;

	private final ImagePlus imp;

	private final ParameterSweepModel model;

	private final String savePath;

	private Logger batchLogger;

	private Logger trackmateLogger;

	private final TrackingMetricsType type;

	private String cancelReason;

	private final ResultsCrawler crawler;

	private boolean saveTrackMateFiles;

	private final String modelPath;

	private HelperRunner(
			final TrackingMetricsType type,
			final String gtPath,
			final ImagePlus imp,
			final ParameterSweepModel model,
			final String modelPath,
			final String savePath,
			final Logger batchLogger,
			final Logger trackmateLogger,
			final boolean saveTrackMateFiles )
	{
		this.type = type;
		this.gtPath = gtPath;
		this.imp = imp;
		this.model = model;
		this.modelPath = modelPath;
		this.savePath = savePath;
		this.batchLogger = batchLogger;
		this.trackmateLogger = trackmateLogger;
		this.saveTrackMateFiles = saveTrackMateFiles;
		this.crawler = new ResultsCrawler( type, batchLogger );
		crawler.reset();
		try
		{
			crawler.crawl( savePath );
		}
		catch ( final IOException e )
		{
			batchLogger.error( "Error while crawling the folder " + savePath + " for CSV results file:\n" );
			batchLogger.error( e.getMessage() + '\n' );
			e.printStackTrace();
			return;
		}
		crawler.watch( savePath );
	}

	/**
	 * Exposes the crawler of this runner that monitors result files appearing
	 * in the save folder.
	 *
	 * @return the {@link ResultsCrawler}.
	 */
	public ResultsCrawler getCrawler()
	{
		return crawler;
	}

	/**
	 * Exposes the settings model that will be used by this runner.
	 *
	 * @return the {@link ParameterSweepModel}.
	 */
	public ParameterSweepModel getModel()
	{
		return model;
	}

	/**
	 * Returns the path to the file that stores the settings model that will be
	 * used by this runner. This path is used to save the model to a file when
	 * modified.
	 *
	 * @return the path to the settings model.
	 */
	public String getModelPath()
	{
		return modelPath;
	}

	/**
	 * Exposes the input image this runner is configured to run on.
	 *
	 * @return the image.
	 */
	public ImagePlus getImage()
	{
		return imp;
	}

	/**
	 * Returns the path to the ground-truth file or folder this runner is
	 * configured to use.
	 *
	 * @return the path to the ground-truth.
	 */
	public String getGroundTruthPath()
	{
		return gtPath;
	}

	/**
	 * Returns the type of tracking metrics this runner is configured to use.
	 *
	 * @return the tracking metrics type.
	 */
	public TrackingMetricsType getType()
	{
		return type;
	}

	/**
	 * Sets the logger to use to log progress of the run.
	 *
	 * @param batchLogger
	 *            a {@link Logger} instance.
	 */
	public void setBatchLogger( final Logger batchLogger )
	{
		this.batchLogger = batchLogger;
	}

	/**
	 * Sets the logger to use to log individual tracking tests. This is only
	 * useful for debugging.
	 *
	 * @param trackmateLogger
	 *            a {@link Logger} instance.
	 */
	public void setTrackmateLogger( final Logger trackmateLogger )
	{
		this.trackmateLogger = trackmateLogger;
	}

	/**
	 * Sets whether TrackMate results XML files will be saved for every test.
	 * <p>
	 * This can consume a lot off disk space if there are many tests to run.
	 *
	 * @param saveTrackMateFiles
	 *            whether TrackMate XML files will be saved for every test.
	 */
	public void setSaveTrackMateFiles( final boolean saveTrackMateFiles )
	{
		this.saveTrackMateFiles = saveTrackMateFiles;
	}

	@Override
	public void run()
	{
		cancelReason = null;
		final IterationData iterationData = new IterationData();

		iterationData.count = model.count();
		iterationData.progress = 0;

		final MetricsRunner runner = type.runner( gtPath, savePath );
		runner.setBatchLogger( batchLogger );
		runner.setTrackmateLogger( trackmateLogger );
		iterationData.runner = runner;

		final Settings base = new Settings( imp );

		loopDetectorSettings( base, iterationData );
	}

	/**
	 * Iterates over possible several detector configurations, and adds them to
	 * the specified base settings, then loop over spot filter configurations.
	 *
	 * @param base
	 *            the {@link Settings} base. Must be fully configured except for
	 *            detector settings, spot filters, tracker settings and track
	 *            filters.
	 * @param iterationData
	 *            the iteration data.
	 * @return
	 * @return
	 * @return an <code>int</code> value that determines what should be the next
	 *         iteration step:
	 *         <ol start="4">
	 *         <li>if the iteration should stop.
	 *         </ol>
	 */
	private int loopDetectorSettings( final Settings base, final IterationData iterationData )
	{
		MAIN_LOOP: for ( final DetectorSweepModel detectorModel : model.getActiveDetectors() )
		{
			final Iterator< Settings > detectorIterator = detectorModel.iterator( base );
			while ( detectorIterator.hasNext() )
			{
				if ( isCanceled() )
					break MAIN_LOOP;

				final Settings settings = detectorIterator.next();

				iterationData.detectionDone = false;
				iterationData.trackmate = null;
				iterationData.detectionTiming = Double.NaN;

				final int val = loopSpotFilterSettings( settings, iterationData );
				if ( val > DETECTOR_SETTINGS_LOOP )
					return val;
			}
		}
		return FINISHED;
	}

	/**
	 * Iterates over possible several spot filter configurations, and adds them
	 * to the specified base settings, then loop over tracker configurations.
	 *
	 * @param base
	 *            the {@link Settings} base. Must be fully configured except for
	 *            spot filters, tracker settings and track filters.
	 * @param iterationData
	 *            the iteration data.
	 * @return
	 * @return an <code>int</code> value that determines what should be the next
	 *         iteration step:
	 *         <ol start="3">
	 *         <li>if the next iteration should be on detector settings.
	 *         <li>if detectors can be skipped and the iteration should stop.
	 *         </ol>
	 */
	private int loopSpotFilterSettings( final Settings base, final IterationData iterationData )
	{
		if ( model.spotFilterModels().isEmpty() )
		{
			// No spot filter, we can skip to iterating on tracker settings.
			base.clearSpotFilters();
			final int val = loopTrackerSettings( base, iterationData );
			if ( val > SPOT_FILTER_LOOP )
				return val;
			return DETECTOR_SETTINGS_LOOP;
		}

		final Iterator< List< FeatureFilter > > spotFilterIterator = new NestedIterator<>( model.spotFilterModels() );
		while ( spotFilterIterator.hasNext() )
		{
			if ( isCanceled() )
				break;

			base.clearSpotFilters();
			final List< FeatureFilter > ffs = spotFilterIterator.next();
			ffs.forEach( base::addSpotFilter );

			final int val = loopTrackerSettings( base, iterationData );
			if ( val > SPOT_FILTER_LOOP )
				return val;
		}
		return DETECTOR_SETTINGS_LOOP;
	}

	/**
	 * Iterates over possible several tracker configurations, and adds them to
	 * the specified base settings, then loop over track filters configurations.
	 *
	 * @param base
	 *            the {@link Settings} base. Must be fully configured except for
	 *            tracker settings and track filters.
	 * @param iterationData
	 *            the iteration data.
	 * @return an <code>int</code> value that determines what should be the next
	 *         iteration step:
	 *         <ol start="2">
	 *         <li>if the next iteration should be on spot filters.
	 *         <li>if spot filters can be skipped and the next iteration should
	 *         be on detector settings.
	 *         <li>if detectors can be skipped and the iteration should stop.
	 *         </ol>
	 */
	private int loopTrackerSettings( final Settings base, final IterationData iterationData )
	{
		MAIN_LOOP: for ( final TrackerSweepModel trackerModel : model.getActiveTracker() )
		{
			final Iterator< Settings > trackerIterator = trackerModel.iterator( base );
			while ( trackerIterator.hasNext() )
			{
				if ( isCanceled() )
					break MAIN_LOOP;

				iterationData.trackingDone = false;
				iterationData.trackingTiming = Double.NaN;

				final Settings settings = trackerIterator.next();
				final int val = loopTrackFilterSettings( settings, iterationData );
				if ( val > TRACKER_SETTINGS_LOOP )
					return val;
			}
		}
		return SPOT_FILTER_LOOP;
	}

	/**
	 * Iterates over possible several track filter configurations, and adds them
	 * to the specified base settings, then execute the tracking and metrics
	 * measurements.
	 *
	 * @param base
	 *            the {@link Settings} base. Must be fully configured except for
	 *            track filters.
	 * @param iterationData
	 *            the iteration data.
	 * @return an <code>int</code> value that determines what should be the next
	 *         iteration step:
	 *         <ol start="1">
	 *         <li>if the next iteration should be on tracker settings.
	 *         <li>if trackers can be skipped and the next iteration should be
	 *         on spot filters.
	 *         <li>if spot filters can be skipped and the next iteration should
	 *         be on detector settings.
	 *         <li>if detectors can be skipped and the iteration should stop.
	 *         </ol>
	 */
	private int loopTrackFilterSettings( final Settings base, final IterationData iterationData )
	{
		if ( model.trackFilterModels().isEmpty() )
		{
			// No track filter, we can skip to iterating on tracker settings.
			base.clearTrackFilters();
			final int val = execTracking( base, iterationData );
			if ( val > TRACK_FILTER_LOOP )
				return val;
			return TRACKER_SETTINGS_LOOP;
		}

		final Iterator< List< FeatureFilter > > trackFilterIterator = new NestedIterator<>( model.trackFilterModels() );
		while ( trackFilterIterator.hasNext() )
		{
			if ( isCanceled() )
				break;

			base.clearTrackFilters();
			final List< FeatureFilter > ffs = trackFilterIterator.next();
			ffs.forEach( base::addTrackFilter );

			final int val = execTracking( base, iterationData );
			if ( val > TRACK_FILTER_LOOP )
				return val;
		}
		return TRACKER_SETTINGS_LOOP;

	}

	/**
	 * Execute the full tracking process using the fully configured
	 * {@link Settings}.
	 *
	 * @param settings
	 *            the settings to use to run TrackMate.
	 * @param iterationData
	 *            the iteration data.
	 * @return an <code>int</code> value that determines what should be the next
	 *         iteration step:
	 *         <ol start="0">
	 *         <li>if the next iteration should be along track filters.
	 *         <li>if track filters can be skipped and the next iteration should
	 *         be on tracker settings.
	 *         <li>if trackers can be skipped and the next iteration should be
	 *         on spot filters.
	 *         <li>if spot filters can be skipped and the next iteration should
	 *         be on detector settings.
	 *         <li>if detectors can be skipped and the iteration should stop.
	 *         </ol>
	 */
	private int execTracking( final Settings base, final IterationData iterationData )
	{
		batchLogger.setProgress( ( double ) ++iterationData.progress / iterationData.count );
		batchLogger.log( "________________________________________\n" );

		if ( crawler.isSettingsPresent( base ) )
		{
			batchLogger.log( "Settings for detector " + base.detectorFactory.getKey() + " with parameters:\n" );
			batchLogger.log( TMUtils.echoMap( base.detectorSettings, 2 ) );
			batchLogger.log( "and tracker " + base.trackerFactory.getKey() + " with parameters:\n" );
			batchLogger.log( TMUtils.echoMap( base.trackerSettings, 2 ) );
			if ( base.getSpotFilters().isEmpty() )
			{
				batchLogger.log( "without spot filter,\n" );
			}
			else
			{
				batchLogger.log( "and with spot filters:\n" );
				batchLogger.log( echoFilters( base.getSpotFilters() ) );
			}
			if ( base.getTrackFilters().isEmpty() )
			{
				batchLogger.log( "without track filter,\n" );
			}
			else
			{
				batchLogger.log( "and with track filters:\n" );
				batchLogger.log( echoFilters( base.getTrackFilters() ) );
			}
			batchLogger.log( "were already tested. Skipping.\n" );
			return TRACK_FILTER_LOOP;
		}

		/*
		 * PERFORM DETECTION IF WE NEED.
		 */

		if ( !iterationData.detectionDone )
		{
			batchLogger.log( "\n________________________________________\n" );
			batchLogger.log( TMUtils.getCurrentTimeString() + "\n" );
			batchLogger.setStatus( base.detectorFactory.getName() );

			final ValuePair< TrackMate, Double > detectionResult = iterationData.runner.execDetection( base );
			iterationData.trackmate = detectionResult.getA();
			iterationData.detectionTiming = detectionResult.getB();
			iterationData.detectionDone = true;

			// Detection failed?
			if ( null == iterationData.trackmate )
			{
				batchLogger.error( "Error running TrackMate with these parameters.\nSkipping.\n" );
				iterationData.progress += model.countTrackerSettings() * model.countSpotFilterSettings() * model.countTrackFilterSettings();
				batchLogger.setProgress( ( double ) ++iterationData.progress / iterationData.count );
				return DETECTOR_SETTINGS_LOOP;
			}
			if ( iterationData.trackmate.getModel().getSpots().getNSpots( false ) == 0 )
			{
				batchLogger.log( "Settings result in having 0 after detection.\nSkipping.\n" );
				iterationData.progress += model.countTrackerSettings() * model.countTrackFilterSettings();
				batchLogger.setProgress( ( double ) ++iterationData.progress / iterationData.count );
				return SPOT_FILTER_LOOP;
			}
		}
		else
		{
			/*
			 * Detection has been done already. We just need to make a new
			 * TrackMate with the current settings we iterate to.
			 */
			final Model tmModel = iterationData.trackmate.getModel();
			iterationData.trackmate = new TrackMate( tmModel, base );
		}

		if ( !iterationData.trackingDone )
		{
			/*
			 * PERFORM SPOT FILTERING.
			 */

			iterationData.runner.execSpotFiltering( iterationData.trackmate );
			// Got 0 spots to track?
			if ( iterationData.trackmate.getModel().getSpots().getNSpots( true ) == 0 )
			{
				batchLogger.log( "Settings result in having 0 spots to track.\nSkipping.\n" );
				iterationData.progress += model.countTrackerSettings() * model.countTrackFilterSettings();
				batchLogger.setProgress( ( double ) ++iterationData.progress / iterationData.count );
				return SPOT_FILTER_LOOP;
			}

			/*
			 * PERFORM TRACKING.
			 */

			batchLogger.setStatus(
					iterationData.trackmate.getSettings().detectorFactory.getName()
							+ " + "
							+ iterationData.trackmate.getSettings().trackerFactory.getName()
							+ String.format( " - %.1f%%", 100. * iterationData.progress / iterationData.count ) );

			iterationData.trackingTiming = iterationData.runner.execTracking( iterationData.trackmate );
			if ( Double.isNaN( iterationData.trackingTiming ) )
			{
				// Tracking failed, we iterate to the next tracking settings.
				iterationData.progress += model.countTrackFilterSettings();
				batchLogger.setProgress( ( double ) ++iterationData.progress / iterationData.count );
				return TRACKER_SETTINGS_LOOP;
			}
			iterationData.trackingDone = true;
		}
		else
		{
			/*
			 * Spot filtering and tracking have been done already. We just loop
			 * to the next track filter settings.
			 */
		}

		/*
		 * PERFORM TRACK FILTERING.
		 */

		iterationData.runner.execTrackFiltering( iterationData.trackmate );

		/*
		 * PERFORM METRICS MEASUREMENTS.
		 */

		iterationData.runner.performAndSaveMetricsMeasurements(
				iterationData.trackmate,
				iterationData.detectionTiming,
				iterationData.trackingTiming );

		// Save TrackMate file if required.
		if ( saveTrackMateFiles )
		{
			final String nameGen = "TrackMate_%s_%s_%03d.xml";
			int i = 1;
			File trackmateFile;
			do
			{
				trackmateFile = new File( savePath,
						String.format( nameGen,
								iterationData.trackmate.getSettings().detectorFactory.getKey(),
								iterationData.trackmate.getSettings().trackerFactory.getKey(),
								i++ ) );
			}
			while ( trackmateFile.exists() );

			final TmXmlWriter writer = new TmXmlWriter( trackmateFile, Logger.VOID_LOGGER );
			writer.appendModel( iterationData.trackmate.getModel() );
			writer.appendSettings( iterationData.trackmate.getSettings() );
			writer.appendGUIState( "ConfigureViews" );
			try
			{
				writer.writeToFile();
				batchLogger.log( "Saved results to TrackMate file: " + trackmateFile + "\n" );
			}
			catch ( final IOException e )
			{
				batchLogger.error( e.getMessage() );
				e.printStackTrace();
			}
		}
		return TRACK_FILTER_LOOP;
	}

	@Override
	public void cancel( final String cancelReason )
	{
		batchLogger.log( TMUtils.getCurrentTimeString() + " - " + cancelReason + '\n' );
		this.cancelReason = cancelReason;
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}

	@Override
	public boolean isCanceled()
	{
		return cancelReason != null;
	}

	/*
	 * BUILDER
	 */

	public static final Builder create()
	{
		return new Builder();
	}

	public static final class Builder
	{
		private String gtPath;

		private ImagePlus imp;

		private String imagePath;

		private String runSettingsPath;

		private String savePath;

		private Logger batchLogger = Logger.DEFAULT_LOGGER;

		private Logger trackmateLogger = Logger.VOID_LOGGER;

		private TrackingMetricsType type;

		private String typeStr;

		private boolean saveTrackMateFiles = false;

		private String errorMessage;

		private double maxDist = Double.NaN;

		/**
		 * Sets the tracking metrics type to use.
		 *
		 * @param type
		 *            the tracking metrics type.
		 * @return this builder.
		 */
		public Builder trackingMetricsType( final TrackingMetricsType type )
		{
			if ( this.type != type )
				this.type = type;

			return this;
		}

		/**
		 * Sets the tracking metrics type to use with a string. Currently
		 * supported types are "CTC" (Cell-Tracking-Challenge) and "SPT"
		 * (Single-Particle Tracking Challenge).
		 *
		 * @param type
		 *            the tracking metrics type as a string.
		 * @return this builder.
		 * @throws IllegalArgumentException
		 *             if the type string is not "CTC" or "SPT".
		 */
		public Builder trackingMetricsType( final String type )
		{
			if ( type == null )
				this.type = null;

			this.typeStr = type;
			return this;
		}

		/**
		 * Sets the path to the ground-truth file or folder.
		 * <p>
		 * The files need to comply to the tracking metrics.
		 *
		 * @param groundTruth
		 *            path to the ground-truth file or folder.
		 * @return this builder.
		 * @see #trackingMetricsType(String)
		 * @see #trackingMetricsType(TrackingMetricsType)
		 */
		public Builder groundTruth( final String groundTruth )
		{
			if ( groundTruth == null || !groundTruth.equals( gtPath ) )
				this.gtPath = groundTruth;

			return this;
		}

		/**
		 * Sets the path to the helper runner configuration file.
		 * <p>
		 * The config file is used to specify what parameters to test. It is a
		 * JSon file named <code>helperrunnersettings.json</code>. Use the GUI
		 * to set it.
		 *
		 * @param runSettings
		 *            path to the helper runner configuration file.
		 * @return this builder.
		 */
		public Builder runSettings( final String runSettings )
		{
			if ( runSettings == null || !runSettings.equals( this.runSettingsPath ) )
				this.runSettingsPath = runSettings;

			return this;
		}

		/**
		 * Sets the path to the folder in which runner results will be saved.
		 *
		 * @param savePath
		 *            path to the save folder.
		 * @return this builder.
		 */
		public Builder savePath( final String savePath )
		{
			if ( savePath == null || !savePath.equals( this.savePath ) )
				this.savePath = savePath;

			return this;
		}

		/**
		 * Sets the path to the image file to use as input.
		 *
		 * @param imagePath
		 *            path to the image file to use as input.
		 * @return this builder.
		 */
		public Builder imagePath( final String imagePath )
		{
			if ( imagePath == null || !imagePath.equals( this.imagePath ) )
				this.savePath = imagePath;

			return this;
		}

		/**
		 * Sets the path to the image to use as input.
		 *
		 * @param image
		 *            path to the image to use as input.
		 * @return this builder.
		 */
		public Builder image( final ImagePlus image )
		{
			if ( image == null || image != this.imp )
				this.imp = image;

			return this;
		}

		/**
		 * Sets the logger to use to log progress of the run.
		 *
		 * @param batchLogger
		 *            a {@link Logger} instance.
		 * @return this builder.
		 */
		public Builder batchLogger( final Logger batchLogger )
		{
			if ( this.batchLogger != batchLogger )
				this.batchLogger = batchLogger;

			return this;
		}

		/**
		 * Sets the logger to use to log individual tracking tests. This is only
		 * useful for debugging.
		 *
		 * @param trackmateLogger
		 *            a {@link Logger} instance.
		 * @return this builder.
		 */
		public Builder trackmateLogger( final Logger trackmateLogger )
		{
			if ( this.trackmateLogger != trackmateLogger )
				this.trackmateLogger = trackmateLogger;

			return this;
		}

		/**
		 * Sets whether TrackMate results XML files will be saved for every
		 * test.
		 * <p>
		 * This can consume a lot off disk space if there are many tests to run.
		 *
		 * @param saveTrackMateFiles
		 *            whether TrackMate XML files will be saved for every test.
		 * @return this builder
		 */
		public Builder saveTrackMateFiles( final boolean saveTrackMateFiles )
		{
			this.saveTrackMateFiles = saveTrackMateFiles;
			return this;
		}

		/**
		 * Sets the max pairing distance to use with the SPT metrics. The max
		 * pairing distance if the distance in physical units below which a
		 * candidate detection and a ground-truth detection will be considered
		 * matching.
		 * <p>
		 * Only affect the SPT metrics type, if it is specified via the string
		 * method {@link #trackingMetricsType(String)} of this builder.
		 *
		 * @param maxDist
		 *            the max pairing distance.
		 * @return this builder.
		 */
		public Builder sptMetricsMaxPairingDistance( final double maxDist )
		{
			this.maxDist = maxDist;
			return this;
		}

		public HelperRunner get()
		{
			boolean ok = true;
			errorMessage = null;
			final StringBuilder str = new StringBuilder();

			// Tracking metric type.
			if ( type == null )
			{
				// If the type is unspecified, take it from the string.
				if ( typeStr == null )
				{
					ok = false;
					str.append( "Please specify a tracking metric type.\n" );
				}
				else
				{
					if ( typeStr.equals( "CTC" ) )
						this.type = new CTCTrackingMetricsType();
					else if ( typeStr.equals( "SPT" ) )
					{
						if ( Double.isNaN( maxDist ) )
						{
							str.append( "Max pairing distance for SPT metrics has not been set.\n" );
							ok = false;
						}
						this.type = new SPTTrackingMetricsType( maxDist, "image units" );
					}
					else
					{
						ok = false;
						str.append( "Unknown tracking metric type: " + type + '\n' );
					}
				}
			}

			// Path to ground truth.
			if ( gtPath == null )
			{
				ok = false;
				str.append( "Please specify the path to a ground-truth file or folder.\n" );
			}

			// Path to runner config file.
			if ( runSettingsPath == null )
			{
				// Not specified, take it from the gtPath.
				if ( gtPath != null )
				{
					final File runSettingFile = ParameterSweepModelIO.makeSettingsFileForGTPath( gtPath );
					runSettingsPath = runSettingFile.getAbsolutePath();
				}
			}
			ParameterSweepModel model = null;
			if ( runSettingsPath == null )
			{
				ok = false;
				str.append( "Please specify the path to the helper runner settings file.\n" );
			}
			else
			{
				try (FileReader reader = new FileReader( runSettingsPath ))
				{
					final String lines = Files.lines( Paths.get( runSettingsPath ) )
							.collect( Collectors.joining( System.lineSeparator() ) );

					model = ParameterSweepModelIO.fromJson( lines );
				}
				catch ( final JsonParseException e )
				{
					final String msg = "Error when reading TrackMate-Helper parameter file. "
							+ "One class is not found in your Fiji installation: "
							+ "\n"
							+ e.getMessage()
							+ "\n"
							+ "It is likely an error with conflicting versions and upgrades "
							+ "of modules. You may fix this error by removing the existing "
							+ "TrackMate-Helper parameter file in: "
							+ "\n"
							+ runSettingsPath;
					ok = false;
					str.append( msg );
				}
				catch ( final FileNotFoundException e )
				{
					ok = false;
					str.append( "Could not find the helper runner settings file: " + runSettingsPath + '\n' );
				}
				catch ( final IOException e )
				{
					ok = false;
					str.append( "Could not read the helper runner settings file: " + runSettingsPath
							+ ":\n" + e.getMessage() + '\n' );
				}
			}

			// Save path.
			if ( savePath == null && gtPath != null )
				savePath = new File( gtPath ).getParent();

			if ( savePath != null && !new File( savePath ).canWrite() )
			{
				ok = false;
				str.append( "Cannot write to results folder: " + savePath + '\n' );
			}

			// Input image.
			if ( imp == null )
			{
				// Try to load input image from path.
				if ( imagePath == null )
				{
					ok = false;
					str.append( "Please specify an input image or a path to the input image.\n" );
				}
				else
				{
					imp = IJ.openImage( imagePath );
					if ( imp == null )
					{
						ok = false;
						str.append( "Could not open image file " + imagePath + '\n' );
					}
				}
			}

			// Finally, create.
			if ( !ok )
			{
				errorMessage = str.toString();
				return null;
			}

			return new HelperRunner(
					type,
					gtPath,
					imp,
					model,
					runSettingsPath,
					savePath,
					batchLogger,
					trackmateLogger,
					saveTrackMateFiles );
		}

		public String getErrorMessage()
		{
			return errorMessage;
		}
	}

	/**
	 * Data class representing the data that is passed between the parameter
	 * sweep iterations.
	 */
	private static class IterationData
	{

		public double trackingTiming;

		public boolean trackingDone;

		public MetricsRunner runner;

		public double detectionTiming;

		public TrackMate trackmate;

		public boolean detectionDone;

		public int progress;

		public int count;

	}
}
