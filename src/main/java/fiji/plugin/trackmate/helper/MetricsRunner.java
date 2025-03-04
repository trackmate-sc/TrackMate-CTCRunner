/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2024 TrackMate developers.
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.function.BiFunction;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.util.TMUtils;
import net.imglib2.util.ValuePair;

public abstract class MetricsRunner
{

	/**
	 * Logger to supervise the batch.
	 */
	protected Logger batchLogger = Logger.DEFAULT_LOGGER;

	/**
	 * Logger to pass to TrackMate instances.
	 */
	protected Logger trackmateLogger = Logger.VOID_LOGGER;

	protected final TrackingMetricsType type;

	/**
	 * Where to save metrics results files.
	 */
	protected final Path resultsRootPath;

	/**
	 * Generator for CSV file names.
	 */
	private final BiFunction< String, Integer, String > nameGenWithID;

	public MetricsRunner( final Path resultsRootPath, final TrackingMetricsType type )
	{
		this.resultsRootPath = resultsRootPath;
		this.type = type;
		this.nameGenWithID = ( imName, i ) -> String.format( "%s_" + type.csvSuffix() + "_%02d.csv", imName, i );
	}

	/**
	 * Performs the tracking metrics measurements for the tracks in the model in
	 * the specified TrackMate instance, against the ground truth given at
	 * construction. Metric values are returned as a {@link TrackingMetrics}.
	 *
	 * @param trackmate
	 *            the tracks on which to measure tracking metrics.
	 * @return the metric values.
	 */
	public abstract TrackingMetrics performMetricsMeasurements( final TrackMate trackmate ) throws MetricsComputationErrorException;

	/**
	 * Performs the tracking metrics measurements for the tracks in the model in
	 * the specified TrackMate instance, against the ground truth given at
	 * construction, and save the results in an adequate CSV file.
	 *
	 * @param trackmate
	 *            the tracks on which to measure tracking metrics.
	 * @param detectionTiming
	 *            the metric measuring the detection time.
	 * @param trackingTiming
	 *            the metric measuring the tracking time.
	 */
	public void performAndSaveMetricsMeasurements( final TrackMate trackmate, final double detectionTiming, final double trackingTiming )
	{
		final Settings settings = trackmate.getSettings();
		final File csvFile = findSuitableCSVFile( settings );
		final String[] csvHeader1 = toCSVHeader( settings );

		try
		{
			final TrackingMetrics metrics = performMetricsMeasurements( trackmate );

			// Add timing measurements.
			metrics.set( TrackingMetricsType.TIM, detectionTiming + trackingTiming );
			metrics.set( TrackingMetricsType.DETECTION_TIME, detectionTiming );
			metrics.set( TrackingMetricsType.TRACKING_TIME, trackingTiming );

			batchLogger.log( "SPT metrics:\n" );
			batchLogger.log( metrics.toString() + '\n' );
			writeResults( csvFile, metrics, settings, csvHeader1 );
		}
		catch ( final MetricsComputationErrorException | IllegalArgumentException e )
		{
			writeFailedResults( csvFile, settings, csvHeader1 );
		}
	}

	public ValuePair< TrackMate, Double > execDetection( final Settings settings )
	{
		batchLogger.log( "Executing detection.\n" );
		batchLogger.log( "Configured detector: " );
		batchLogger.log( settings.detectorFactory.getName(), Logger.BLUE_COLOR );
		batchLogger.log( " with settings:\n" );
		batchLogger.log( TMUtils.echoMap( settings.detectorSettings, 2 ) );

		final long start = System.currentTimeMillis();
		final TrackMate trackmate = new TrackMate( settings );
		trackmate.getModel().setLogger( trackmateLogger );
		if ( !trackmate.execDetection()
				|| !trackmate.execInitialSpotFiltering()
				|| !trackmate.computeSpotFeatures( true ) )
		{
			batchLogger.error( "Error in the detection step:\n" + trackmate.getErrorMessage() );
			return null;
		}
		final long end = System.currentTimeMillis();
		final double detectionTiming = ( end - start ) / 1000.;

		batchLogger.log( String.format( "Detection done in %.1f s.\n", ( end - start ) / 1e3f ) );
		return new ValuePair<>( trackmate, detectionTiming );
	}

	public TrackMate execSpotFiltering( final TrackMate trackmate )
	{
		batchLogger.log( "Executing spot filtering.\n" );
		final Settings settings = trackmate.getSettings();
		final List< FeatureFilter > spotFilters = settings.getSpotFilters();
		if ( spotFilters.isEmpty() )
		{
			batchLogger.log( "No spot filters configured.\n" );
		}
		else
		{
			batchLogger.log( " with spot filters:\n" );
			batchLogger.log( TrackingMetricsTable.echoFilters( spotFilters ), Logger.BLUE_COLOR );
		}

		trackmate.getModel().setLogger( trackmateLogger );
		if ( !trackmate.execSpotFiltering( true ) )
		{
			batchLogger.error( "Error in the spot filtering step:\n" + trackmate.getErrorMessage() );
			return null;
		}

		final int nVisibleSpots = trackmate.getModel().getSpots().getNSpots( true );
		final int nTotalSpots = trackmate.getModel().getSpots().getNSpots( false );
		batchLogger.log( String.format( "Found %d visible spots over %d in total.\n",
				nVisibleSpots, nTotalSpots ) );

		if ( nVisibleSpots == 0 )
		{
			final File csvFile = findSuitableCSVFile( settings );
			final String[] csvHeader1 = toCSVHeader( settings );
			writeFailedResults( csvFile, settings, csvHeader1 );
		}
		return trackmate;
	}

	public double execTracking( final TrackMate trackmate )
	{
		batchLogger.log( "Executing tracking.\n" );
		batchLogger.log( "Configured detector: " );
		batchLogger.log( trackmate.getSettings().detectorFactory.getName(), Logger.BLUE_COLOR );
		batchLogger.log( " with settings:\n" );
		batchLogger.log( TMUtils.echoMap( trackmate.getSettings().detectorSettings, 2 ) );
		batchLogger.log( "Configured tracker: " );
		batchLogger.log( trackmate.getSettings().trackerFactory.getName(), Logger.BLUE_COLOR );
		batchLogger.log( " with settings:\n" );
		batchLogger.log( TMUtils.echoMap( trackmate.getSettings().trackerSettings, 2 ) );

		final long start = System.currentTimeMillis();
		if ( !trackmate.checkInput()
				|| !trackmate.execTracking()
				|| !trackmate.computeEdgeFeatures( true )
				|| !trackmate.computeTrackFeatures( true ) )
		{
			System.err.println( "Error in tracking step:\n" + trackmate.getErrorMessage() );
			return Double.NaN;
		}
		final long end = System.currentTimeMillis();
		final double trackingTiming = ( end - start ) / 1000.;

		batchLogger.log( String.format( "Tracking done in %.1f s.\n", trackingTiming ) );
		return trackingTiming;
	}

	public void execTrackFiltering( final TrackMate trackmate )
	{
		batchLogger.log( "Executing track filtering.\n" );
		final List< FeatureFilter > trackFilters = trackmate.getSettings().getTrackFilters();
		if ( trackFilters.isEmpty() )
		{
			batchLogger.log( "No track filters configured.\n" );
		}
		else
		{
			batchLogger.log( " with track filters:\n" );
			batchLogger.log( TrackingMetricsTable.echoFilters( trackFilters ), Logger.BLUE_COLOR );
		}
		if ( !trackmate.execTrackFiltering( true ) )
		{
			batchLogger.error( "Error in the track filtering step:\n" + trackmate.getErrorMessage() );
			return;
		}
		final TrackModel trackModel = trackmate.getModel().getTrackModel();
		final IntSummaryStatistics stats = trackModel.unsortedTrackIDs( true ).stream()
				.mapToInt( id -> trackModel.trackSpots( id ).size() )
				.summaryStatistics();
		batchLogger.log( "Found " + trackModel.nTracks( true ) + " visible tracks over "
				+ trackModel.nTracks( false ) + " in total.\n" );
		batchLogger.log( String.format( "  - avg size: %.1f spots.\n", stats.getAverage() ) );
		batchLogger.log( String.format( "  - min size: %d spots.\n", stats.getMin() ) );
		batchLogger.log( String.format( "  - max size: %d spots.\n", stats.getMax() ) );
	}

	private File findSuitableCSVFile( final Settings settings )
	{
		final String imFileName;
		if ( settings.imp == null )
			imFileName = "";
		else
			imFileName = settings.imp.getShortTitle();

		// Prepare CSV headers.
		final String[] csvHeader1 = toCSVHeader( settings );
		final String[] csvHeader = type.concatWithHeader( csvHeader1 );

		// Init.
		int i = 0;

		while ( i < 100 )
		{
			i++;
			final File csvFile = getCSVFile( resultsRootPath.toString(), imFileName, i );

			// Does the target CSV file exist?
			if ( !csvFile.exists() )
			{
				try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile ),
						CSVWriter.DEFAULT_SEPARATOR,
						CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END ))
				{
					// CSV header.
					csvWriter.writeNext( csvHeader );
				}
				catch ( final IOException e )
				{
					batchLogger.error( "Cannot open CSV file " + csvFile + " for writing:\n" + e.getMessage() );
					e.printStackTrace();
				}
				batchLogger.log( "CSV file " + csvFile + " does not exist. Created it.\n" );
				return csvFile;
			}

			// If yes, is it compatible for appending?
			if ( csvFileIsCompatible( settings, csvFile ) )
			{
				batchLogger.log( "Found a compatible CSV file for appending: " + csvFile + '\n' );
				return csvFile;
			}
		}

		batchLogger.error( "Could not create a proper CSV file in : " + resultsRootPath + '\n' );
		return null;
	}

	/**
	 * Appends a line to the specified CSV file with the specified tracking
	 * metrics. This method will also add the timing metrics to the specified
	 * tracking metrics.
	 *
	 * @param csvFile
	 *            the CSV file to append to.
	 * @param metrics
	 *            the metrics to append.
	 * @param detectionTiming
	 *            the detection time in seconds.
	 * @param trackingTiming
	 *            the tracking time in seconds.
	 * @param settings
	 *            the tracking settings used to generate these metrics values.
	 * @param csvHeader
	 *            the header name for each setting value.
	 */
	private void writeResults(
			final File csvFile,
			final TrackingMetrics metrics,
			final Settings settings,
			final String[] csvHeader )
	{
		// Write to CSV.
		final String[] line1 = toCSVLine( settings, csvHeader );
		final String[] line = metrics.concatWithCSVLine( line1 );

		try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile, true ),
				CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER,
				CSVWriter.DEFAULT_LINE_END ))
		{
			csvWriter.writeNext( line );
		}
		catch ( final IOException e )
		{
			writeFailedResults( csvFile, settings, csvHeader );
		}
	}

	/**
	 * Appends a line in the results file where all metrics results are
	 * <code>NaN</code>s along with the settings values.
	 * <p>
	 * This signals that the settings values result in a failed tracking
	 * results. As the settings values are logged, they won't be retried.
	 *
	 * @param csvFile
	 *            the CSV file to append the line to.
	 * @param settings
	 *            the {@link Settings} that led to the failed tracking.
	 * @param csvHeader
	 *            the header name for each setting value.
	 */
	private void writeFailedResults( final File csvFile, final Settings settings, final String[] csvHeader )
	{
		// Write default values to CSV.
		final String[] settingsValueColumns = toCSVLine( settings, csvHeader );
		// all NaNs.
		final TrackingMetrics metrics = new TrackingMetrics( type );
		final String[] line = metrics.concatWithCSVLine( settingsValueColumns );
		try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile, true ),
				CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER,
				CSVWriter.DEFAULT_LINE_END ))
		{
			csvWriter.writeNext( line );
		}
		catch ( final IOException e1 )
		{
			batchLogger.error( "Could not write failed results to CSV file:\n" + e1.getMessage() + '\n' );
			e1.printStackTrace();
		}
	}

	private final boolean csvFileIsCompatible( final Settings settings, final File csvFile )
	{
		// Prepare CSV headers.
		final String[] csvHeader1 = toCSVHeader( settings );
		final String[] csvHeader = type.concatWithHeader( csvHeader1 );

		try (CSVReader csvReader = new CSVReaderBuilder( new FileReader( csvFile ) ).build())
		{
			final String[] readHeader = csvReader.readNext();
			return Arrays.equals( csvHeader, readHeader );
		}
		catch ( final IOException | CsvValidationException e )
		{
			batchLogger.error( "Cannot open CSV file " + csvFile + " for reading:\n" + e.getMessage() );
			e.printStackTrace();
		}
		return false;
	}

	public void setBatchLogger( final Logger batchLogger )
	{
		this.batchLogger = batchLogger;
	}

	public void setTrackmateLogger( final Logger trackmateLogger )
	{
		this.trackmateLogger = trackmateLogger;
	}

	private final File getCSVFile( final String resultsRootPath, final String imageName, final int id )
	{
		final Path csvFilePath = Paths.get( resultsRootPath, nameGenWithID.apply( imageName, id ) );
		return csvFilePath.toFile();
	}

	private static final String[] toCSVHeader( final Settings settings )
	{
		final int nDetectorParams = settings.detectorSettings.size();
		final int nTrackerParams = settings.trackerSettings.size();
		final int nSpotFilters = settings.getSpotFilters().size();
		final int nTrackFilters = settings.getTrackFilters().size();
		final int nCols = 1 + nDetectorParams + nSpotFilters + 1 + nTrackerParams + nTrackFilters;
		final String[] out = new String[ nCols ];

		int i = 0;
		out[ i++ ] = "DETECTOR";
		for ( final String key : settings.detectorSettings.keySet() )
			out[ i++ ] = key;
		out[ i++ ] = "TRACKER";
		for ( final String key : settings.trackerSettings.keySet() )
			out[ i++ ] = key;
		for ( final FeatureFilter sf : settings.getSpotFilters() )
			out[ i++ ] = "SPOT_FILTER_ON_" + sf.feature;
		for ( final FeatureFilter tf : settings.getTrackFilters() )
			out[ i++ ] = "TRACK_FILTER_ON_" + tf.feature;
		return out;
	}

	private static final String[] toCSVLine( final Settings settings, final String[] csvHeader )
	{
		final int nDetectorParams = settings.detectorSettings.size();
		final int nTrackerParams = settings.trackerSettings.size();
		final int nSpotFilters = settings.getSpotFilters().size();
		final int nTrackFilters = settings.getTrackFilters().size();
		final int nCols = 1 + nDetectorParams + nSpotFilters + 1 + nTrackerParams + nTrackFilters;
		final String[] out = new String[ nCols ];

		int i = 0;
		out[ i++ ] = settings.detectorFactory.getKey();
		for ( int j = 0; j < nDetectorParams; j++ )
		{
			final Object val = settings.detectorSettings.get( csvHeader[ i ] );
			out[ i ] = ( null == val ? "" : val.toString() );
			i++;
		}
		out[ i++ ] = settings.trackerFactory.getKey();
		for ( int j = 0; j < nTrackerParams; j++ )
		{
			final Object val = settings.trackerSettings.get( csvHeader[ i ] );
			out[ i ] = ( null == val ? "" : val.toString() );
			i++;
		}
		for ( final FeatureFilter sf : settings.getSpotFilters() )
		{
			out[ i++ ] = "" + ( ( sf.isAbove ) ? '>' : '<' ) + sf.value;
		}
		for ( final FeatureFilter sf : settings.getTrackFilters() )
		{
			out[ i++ ] = "" + ( ( sf.isAbove ) ? '>' : '<' ) + sf.value;
		}

		return out;
	}

	public static class MetricsComputationErrorException extends Exception
	{

		private static final long serialVersionUID = 1L;

	}
}
