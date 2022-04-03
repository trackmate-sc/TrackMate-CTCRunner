package fiji.plugin.trackmate.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.scijava.Cancelable;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.helper.ctc.CTCTrackingMetricsType;
import fiji.plugin.trackmate.helper.model.ParameterSweepModel;
import fiji.plugin.trackmate.helper.model.ParameterSweepModelIO;
import fiji.plugin.trackmate.helper.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.helper.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.helper.spt.SPTTrackingMetricsType;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.util.TMUtils;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.util.ValuePair;

public class HelperRunner implements Runnable, Cancelable
{

	private final String gtPath;

	private final ImagePlus imp;

	private final ParameterSweepModel model;

	private int targetChannel;

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
			final int targetChannel,
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
		this.targetChannel = targetChannel;
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
	 * Sets the index of the channel in the input image to use for tracking.
	 * Channels are here 1-numbered, meaning that "1" is the first available
	 * channel.
	 * 
	 * @param targetChannel
	 *            the channel index
	 */
	public void setTargetChannel( final int targetChannel )
	{
		this.targetChannel = targetChannel;
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
		final int count = model.count();

		final MetricsRunner runner = type.runner( gtPath, savePath );
		runner.setBatchLogger( batchLogger );
		runner.setTrackmateLogger( trackmateLogger );

		final Settings base = new Settings( imp );
		base.setSpotFilters( model.getSpotFilters() );
		base.setTrackFilters( model.getTrackFilters() );
		int progress = 0;

		DETECTOR_SETTINGS_LOOP: for ( final DetectorSweepModel detectorModel : model.getActiveDetectors() )
		{
			final Iterator< Settings > dit = detectorModel.iterator( base, targetChannel );
			while ( dit.hasNext() )
			{
				final Settings ds = dit.next();
				if ( isCanceled() )
					return;

				boolean detectionDone = false;
				TrackMate trackmate = null;
				double detectionTiming = Double.NaN;

				for ( final TrackerSweepModel trackerModel : model.getActiveTracker() )
				{
					final Iterator< Settings > tit = trackerModel.iterator( ds, targetChannel );
					while ( tit.hasNext() )
					{
						final Settings dts = tit.next();
						if ( isCanceled() )
							return;

						batchLogger.setProgress( ( double ) ++progress / count );
						batchLogger.log( "________________________________________\n" );

						if ( crawler.isSettingsPresent( dts ) )
						{
							batchLogger.log( "Settings for detector " + dts.detectorFactory.getKey() + " with parameters:\n" );
							batchLogger.log( TMUtils.echoMap( dts.detectorSettings, 2 ) );
							batchLogger.log( "and tracker " + dts.trackerFactory.getKey() + " with parameters:\n" );
							batchLogger.log( TMUtils.echoMap( dts.trackerSettings, 2 ) );
							batchLogger.log( "were already tested. Skipping.\n" );
							continue;
						}

						if ( !detectionDone )
						{
							batchLogger.log( "\n________________________________________\n" );
							batchLogger.log( TMUtils.getCurrentTimeString() + "\n" );
							batchLogger.setStatus( ds.detectorFactory.getName() );

							final ValuePair< TrackMate, Double > detectionResult = runner.execDetection( ds );
							trackmate = detectionResult.getA();
							detectionTiming = detectionResult.getB();
							detectionDone = true;

							// Detection failed?
							if ( null == trackmate )
							{
								batchLogger.error( "Error running TrackMate with these parameters.\nSkipping.\n" );
								progress += model.countTrackerSettings();
								batchLogger.setProgress( ( double ) ++progress / count );
								continue DETECTOR_SETTINGS_LOOP;
							}
							// Got 0 spots to track?
							if ( trackmate.getModel().getSpots().getNSpots( true ) == 0 )
							{
								batchLogger.log( "Settings result in having 0 spots to track.\nSkipping.\n" );
								progress += model.countTrackerSettings();
								batchLogger.setProgress( ( double ) ++progress / count );
								continue DETECTOR_SETTINGS_LOOP;
							}
						}

						final Settings settings = trackmate.getSettings();
						settings.trackerFactory = dts.trackerFactory;
						settings.trackerSettings = dts.trackerSettings;
						batchLogger.setStatus( settings.detectorFactory.getName() + " + " + settings.trackerFactory.getName() );

						// Exec tracking.
						final double trackingTiming = runner.execTracking( trackmate );

						// Perform and save metrics measurements.
						runner.performMetricsMeasurements( trackmate, detectionTiming, trackingTiming );

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
												settings.detectorFactory.getKey(),
												settings.trackerFactory.getKey(),
												i++ ) );
							}
							while ( trackmateFile.exists() );

							final TmXmlWriter writer = new TmXmlWriter( trackmateFile, Logger.VOID_LOGGER );
							writer.appendModel( trackmate.getModel() );
							writer.appendSettings( trackmate.getSettings() );
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
					}
				}
			}
		}
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

		private int targetChannel = 1;

		private String savePath;

		private Logger batchLogger = Logger.DEFAULT_LOGGER;

		private Logger trackmateLogger = Logger.VOID_LOGGER;

		private TrackingMetricsType type;

		private String typeStr;
		
		private boolean saveTrackMateFiles = false;

		private String errorMessage;

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
		 * Sets the index of the channel in the input image to use for tracking.
		 * Channels are here 1-numbered, meaning that "1" is the first available
		 * channel.
		 * 
		 * @param targetChannel
		 *            the channel index
		 * @return this builder.
		 */
		public Builder targetChannel( final int targetChannel )
		{
			if ( this.targetChannel != targetChannel )
				this.targetChannel = targetChannel;

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
						this.type = new SPTTrackingMetricsType();
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
					targetChannel,
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
}
