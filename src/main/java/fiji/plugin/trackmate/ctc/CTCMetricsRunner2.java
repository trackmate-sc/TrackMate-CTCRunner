package fiji.plugin.trackmate.ctc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.function.BiFunction;

import org.scijava.Context;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.action.CTCExporter;
import fiji.plugin.trackmate.action.CTCExporter.ExportType;
import fiji.plugin.trackmate.util.TMUtils;
import ij.ImagePlus;
import net.imglib2.util.ValuePair;

public class CTCMetricsRunner2
{

	/**
	 * Input image.
	 */
	private final ImagePlus imp;

	/**
	 * Logger to supervise the batch.
	 */
	private Logger batchLogger = Logger.DEFAULT_LOGGER;

	/**
	 * Logger to pass to TrackMate instances.
	 */
	private Logger trackmateLogger = Logger.VOID_LOGGER;

	/**
	 * CTC processor instance.
	 */
	private final CTCMetricsProcessor ctc;

	/**
	 * Path to ground truth folder.
	 */
	private final String gtPath;

	/**
	 * Where to save metrics results file and temp images.
	 */
	private final Path resultsRootPath;

	public CTCMetricsRunner2( final ImagePlus imp, final String gtPath, final Context context )
	{
		assert imp != null;
		this.imp = imp;
		this.gtPath = gtPath;
		this.resultsRootPath = Paths.get( gtPath ).getParent();
		final int logLevel = 0; // silence CTC logging.
		this.ctc = new CTCMetricsProcessor( context, logLevel );
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
				|| !trackmate.computeSpotFeatures( true )
				|| !trackmate.execSpotFiltering( true ) )
		{
			batchLogger.error( "Error in the detection step:\n" + trackmate.getErrorMessage() );
			return null;
		}
		final long end = System.currentTimeMillis();
		final double detectionTiming = ( end - start ) / 1000.;

		batchLogger.log( String.format( "Detection done in %.1f s.\n", ( end - start ) / 1e3f ) );
		batchLogger.log( String.format( "Found %d spots.\n", trackmate.getModel().getSpots().getNSpots( false ) ) );

		return new ValuePair<>( trackmate, detectionTiming );
	}

	public double execTracking( final TrackMate trackmate )
	{
		batchLogger.log( "Executing tracking.\n" );
		batchLogger.log( "Configured tracker: " );
		batchLogger.log( trackmate.getSettings().trackerFactory.getName(), Logger.BLUE_COLOR );
		batchLogger.log( " with settings:\n" );
		batchLogger.log( TMUtils.echoMap( trackmate.getSettings().trackerSettings, 2 ) );

		final long start = System.currentTimeMillis();
		if ( !trackmate.checkInput()
				|| !trackmate.execTracking()
				|| !trackmate.computeEdgeFeatures( true )
				|| !trackmate.computeTrackFeatures( true )
				|| !trackmate.execTrackFiltering( true ) )
		{
			System.err.println( "Error in tracking step:\n" + trackmate.getErrorMessage() );
			return Double.NaN;
		}
		final long end = System.currentTimeMillis();
		final double trackingTiming = ( end - start ) / 1000.;

		batchLogger.log( String.format( "Tracking done in %.1f s.\n", trackingTiming ) );
		final TrackModel trackModel = trackmate.getModel().getTrackModel();
		final int nTracks = trackModel.nTracks( false );
		final IntSummaryStatistics stats = trackModel.unsortedTrackIDs( false ).stream()
				.mapToInt( id -> trackModel.trackSpots( id ).size() )
				.summaryStatistics();
		batchLogger.log( "Found " + nTracks + " tracks.\n" );
		batchLogger.log( String.format( "  - avg size: %.1f spots.\n", stats.getAverage() ) );
		batchLogger.log( String.format( "  - min size: %d spots.\n", stats.getMin() ) );
		batchLogger.log( String.format( "  - max size: %d spots.\n", stats.getMax() ) );

		return trackingTiming;
	}

	public void performCTCMetricsMeasurements( final TrackMate trackmate, final double detectionTiming, final double trackingTiming )
	{
		batchLogger.log( "Exporting as CTC results.\n" );
		final Settings settings = trackmate.getSettings();
		final File csvFile = findSuitableCSVFile( settings );
		final String[] csvHeader1 = toCSVHeader( settings );

		final int id = CTCExporter.getAvailableDatasetID( resultsRootPath.toString() );
		try
		{
			// Export to CTC files.
			final String resultsFolder = CTCExporter.exportTrackingData( resultsRootPath.toString(), id, ExportType.RESULTS, trackmate, trackmateLogger );

			// Perform CTC measurements.
			batchLogger.log( "Performing CTC metrics measurements.\n" );
			final CTCMetrics m = ctc.process( gtPath, resultsFolder );
			// Add timing measurements.
			final CTCMetrics metrics = m.copyEdit()
					.detectionTime( detectionTiming )
					.trackingTime( trackingTiming )
					.tim( detectionTiming + trackingTiming )
					.get();
			batchLogger.log( "CTC metrics:\n" );
			batchLogger.log( metrics.toString() + '\n' );

			// Write to CSV.
			final String[] line1 = toCSVLine( settings, csvHeader1 );
			final String[] line = metrics.concatWithCSVLine( line1 );

			try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile, true ),
					CSVWriter.DEFAULT_SEPARATOR,
					CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END ))
			{
				csvWriter.writeNext( line );
			}

			// Delete CTC export folder.
			deleteFolder( resultsFolder );
		}
		catch ( final IOException | IllegalArgumentException e )
		{
			batchLogger.error( "Could not export tracking data to CTC files:\n" + e.getMessage() + '\n' );
		}
	}

	private File findSuitableCSVFile( final Settings settings )
	{
		final String imFileName = imp.getShortTitle();
		// Prepare CSV headers.
		final String[] csvHeader1 = toCSVHeader( settings );
		final String[] csvHeader = CTCMetrics.concatWithCSVHeader( csvHeader1 );

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

	private static final File getCSVFile( final String resultsRootPath, final String imageName, final int id )
	{
		final Path csvFilePath = Paths.get( resultsRootPath, nameGenWithID.apply( imageName, id ) );
		return csvFilePath.toFile();
	}

	private final boolean csvFileIsCompatible( final Settings settings, final File csvFile )
	{
		// Prepare CSV headers.
		final String[] csvHeader1 = toCSVHeader( settings );
		final String[] csvHeader = CTCMetrics.concatWithCSVHeader( csvHeader1 );

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

	private static final String[] toCSVHeader( final Settings settings )
	{
		final int nDetectorParams = settings.detectorSettings.size();
		final int nTrackerParams = settings.trackerSettings.size();
		final int nCols = 1 + nDetectorParams + 1 + nTrackerParams;
		final String[] out = new String[ nCols ];

		int i = 0;
		out[ i++ ] = "DETECTOR";
		for ( final String key : settings.detectorSettings.keySet() )
			out[ i++ ] = key;
		out[ i++ ] = "TRACKER";
		for ( final String key : settings.trackerSettings.keySet() )
			out[ i++ ] = key;
		return out;
	}

	private static final String[] toCSVLine( final Settings settings, final String[] csvHeader )
	{
		final int nDetectorParams = settings.detectorSettings.size();
		final int nTrackerParams = settings.trackerSettings.size();
		final int nCols = 1 + nDetectorParams + 1 + nTrackerParams;
		final String[] out = new String[ nCols ];

		int i = 0;
		out[ i++ ] = settings.detectorFactory.getKey();
		for ( int j = 0; j < nDetectorParams; j++ )
		{
			out[ i ] = settings.detectorSettings.get( csvHeader[ i ] ).toString();
			i++;
		}
		out[ i++ ] = settings.trackerFactory.getKey();
		for ( int j = 0; j < nTrackerParams; j++ )
		{
			out[ i ] = settings.trackerSettings.get( csvHeader[ i ] ).toString();
			i++;
		}
		return out;
	}

	private static final void deleteFolder( final String folder )
	{
		final Path path = Paths.get( folder );
		try
		{
			Files.walkFileTree( path, new SimpleFileVisitor< Path >()
			{
				@Override
				public FileVisitResult visitFile( final Path file, final BasicFileAttributes attrs ) throws IOException
				{
					Files.delete( file );
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory( final Path dir, final IOException e ) throws IOException
				{
					if ( e == null )
					{
						Files.delete( dir );
						return FileVisitResult.CONTINUE;
					}
					throw e;
				}
			} );
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( "Failed to delete " + path, e );
		}
	}

	public void setBatchLogger( final Logger batchLogger )
	{
		this.batchLogger = batchLogger;
	}

	public void setTrackmateLogger( final Logger trackmateLogger )
	{
		this.trackmateLogger = trackmateLogger;
	}

	private static final BiFunction< String, Integer, String > nameGenWithID = ( imName, i ) -> String.format( "%s_CTCMetrics_%02d.csv", imName, i );

}
