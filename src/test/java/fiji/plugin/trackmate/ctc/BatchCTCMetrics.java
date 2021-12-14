package fiji.plugin.trackmate.ctc;

import java.io.BufferedReader;
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
import java.util.Map;
import java.util.function.BiFunction;

import org.scijava.Context;
import org.scijava.log.LogService;

import com.opencsv.CSVWriter;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.CTCExporter;
import fiji.plugin.trackmate.action.CTCExporter.ExportType;
import fiji.plugin.trackmate.ctc.CTCMetricsProcessor.CTCMetrics;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.stardist.StarDistDetectorFactory;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.sparselap.SimpleSparseLAPTrackerFactory;
import ij.IJ;
import ij.ImagePlus;

/**
 * Performs batch tracking and CTC metrics meaurements to find the optimal
 * settings of a tracking configuration.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class BatchCTCMetrics
{

	public static void main( final String[] args ) throws IOException
	{
		try (final Context context = new Context())
		{
			final String sourceImagePath = "C:\\Users\\tinevez\\Google Drive\\Writing\\TrackMate-DL-paper-materials\\Revision_1\\QuantitativeComparison\\CellMigration\\R1_cell migration R1 - Position 60_XY1562686156_Z0_T00_C1.tif";
			final String groundTruthPath = "C:\\Users\\tinevez\\Google Drive\\Writing\\TrackMate-DL-paper-materials\\Revision_1\\QuantitativeComparison\\CellMigration\\02_GT";
			final String resultsRootPath = "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\CellMigration";
			final String imFileName = "CellMigration";

			/*
			 * Create base settings.
			 */

			final ImagePlus imp = IJ.openImage( sourceImagePath );
			final Settings settings = new Settings( imp );

			// Base detector.
			settings.detectorFactory = new StarDistDetectorFactory<>();
			settings.detectorSettings = settings.detectorFactory.getDefaultSettings();

			// Base tracker.
			settings.trackerFactory = new SimpleSparseLAPTrackerFactory();
			settings.trackerSettings = settings.trackerFactory.getDefaultSettings();

			// Base filter.
			settings.initialSpotFilterValue = 0.;

			// Analyzers.
			settings.addAllAnalyzers();

			/*
			 * Logging thingies.
			 */

			// Logger to supervise the batch.
			final Logger batchLogger = Logger.DEFAULT_LOGGER;
			// Logger to pass to TrackMate instances.
			final Logger trackmateLogger = Logger.VOID_LOGGER;
			// LogService
			final LogService logService = context.getService( LogService.class );
			final int logLevel = 0; // silence CTC logging.
			logService.setLevel( logLevel );

			/*
			 * CTC measures.
			 */

			final CTCMetricsProcessor ctc = new CTCMetricsProcessor( context, logLevel );

			/*
			 * Parameter span.
			 */

			final double[] maxLinkingDistances = new double[] { 5., 10., 15., 20. };
			final int[] maxFrameGaps = new int[] { 0, 2, 3, 4 };

			/*
			 * Main loop. We vary only the tracking part.
			 */

			// Create CSV.
			final String[] csvHeader1 = toCSVHeader( settings );
			final String[] csvHeader = CTCMetrics.concatWithCSVHeader( csvHeader1 );
			final File csvFile = getAvailableCSVFile( resultsRootPath, imFileName );
			try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile ),
					CSVWriter.DEFAULT_SEPARATOR,
					CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END ))
			{
				// CSV header.
				csvWriter.writeNext( csvHeader );
			}

			/*
			 * Detection part.
			 */

			// For timing.
			final double detectionTiming;

			// Did we have already an existing detection file?
			final File trackmateFile = new File( resultsRootPath, "TrackMate_" + settings.detectorFactory.getKey() + ".xml" );
			final File timingFile = new File( resultsRootPath, "TrackMate_" + settings.detectorFactory.getKey() + "_timing.txt" );
			final TrackMate trackmate;
			if ( trackmateFile.exists() )
			{
				batchLogger.log( "Reading detection from " + trackmateFile + '\n' );
				final TmXmlReader reader = new TmXmlReader( trackmateFile );
				if ( !reader.isReadingOk() )
				{
					batchLogger.error( reader.getErrorMessage() );
					return;
				}
				final Model model = reader.getModel();
				trackmate = new TrackMate( model, settings );
				trackmate.getModel().setLogger( trackmateLogger );

				// Read timing.
				batchLogger.log( "Reading timing from " + timingFile + '\n' );
				try(BufferedReader  timingReader = new BufferedReader( new FileReader( timingFile )))
				{
					final String line = timingReader.readLine();
					detectionTiming = Double.parseDouble( line );
				}

				batchLogger.log( "Reading done. Timing = " + detectionTiming + " s.\n" );
			}
			else
			{
				final long start = System.currentTimeMillis();
				trackmate = new TrackMate( settings );
				trackmate.getModel().setLogger( trackmateLogger );
				if ( !trackmate.checkInput()
						|| !trackmate.execDetection()
						|| !trackmate.execInitialSpotFiltering()
						|| !trackmate.computeSpotFeatures( true )
						|| !trackmate.execSpotFiltering( true ) )
				{
					System.err.println( "Error in the detection step:\n" + trackmate.getErrorMessage() );
					return;
				}
				final long end = System.currentTimeMillis();
				detectionTiming = ( end - start ) / 1000.;

				// Write tmp file so that we don't have to redo the
				// tracking.
				batchLogger.log( "Saving detection to file " + trackmateFile + '\n' );
				final TmXmlWriter writer = new TmXmlWriter( trackmateFile, trackmateLogger );
				writer.appendLog( batchLogger.toString() );
				writer.appendModel( trackmate.getModel() );
				writer.appendSettings( trackmate.getSettings() );
				writer.writeToFile();

				// Write timing.
				try (FileWriter timingWriter = new FileWriter( timingFile ))
				{
					timingWriter.write( "" + detectionTiming );
				}
			}

			/*
			 * Tracking part.
			 */

			final Map< String, Object > ts = settings.trackerSettings;
			for ( final int frameGap : maxFrameGaps )
			{
				for ( final double mld : maxLinkingDistances )
				{
					batchLogger.log( "Performing tracking.\n" );
					ts.put( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, frameGap );
					ts.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, mld );
					ts.put( TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, mld );

					final long start = System.currentTimeMillis();
					if ( !trackmate.checkInput()
							|| !trackmate.execTracking()
							|| !trackmate.computeEdgeFeatures( true )
							|| !trackmate.computeTrackFeatures( true )
							|| !trackmate.execTrackFiltering( true ) )
					{
						System.err.println( "Error in tracking step:\n" + trackmate.getErrorMessage() );
						continue;
					}
					final long end = System.currentTimeMillis();
					final double trackingTiming = ( end - start ) / 1000.;
					batchLogger.log( String.format( "Tracking time: %.1f s\n", trackingTiming ) );

					/*
					 * Export to CTC files.
					 */

					trackmateLogger.log( "Exporting as CTC results.\n" );
					final int id = CTCExporter.getAvailableDatasetID( resultsRootPath );
					final String resultsFolder = CTCExporter.exportTrackingData( resultsRootPath, id, ExportType.RESULTS, trackmate, trackmateLogger );

					/*
					 * Perform CTC measurements.
					 */

					final CTCMetrics metrics = ctc.process( groundTruthPath, resultsFolder );
					batchLogger.log( metrics.toString() + '\n' );

					// Write to CSV.
					final String[] line1 = toCSVLine( settings, csvHeader1 );
					final String[] line = metrics.concatWithCSVLine( line1, detectionTiming, trackingTiming );

					try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile, true ),
							CSVWriter.DEFAULT_SEPARATOR,
							CSVWriter.NO_QUOTE_CHARACTER,
							CSVWriter.DEFAULT_ESCAPE_CHARACTER,
							CSVWriter.DEFAULT_LINE_END ))
					{
						csvWriter.writeNext( line );
					}

					/*
					 * Delete CTC export folder.
					 */
					deleteFolder( resultsFolder );
				}
			}
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}

	private static final BiFunction< String, Integer, String > nameGen = ( imName, i ) -> String.format( "%s_CTCMetrics_%02d.csv", imName, i );

	public static File getAvailableCSVFile( final String resultsRootPath, final String imageName )
	{
		int i = 1;
		Path savePath1 = Paths.get( resultsRootPath, nameGen.apply( imageName, i ) );
		while ( Files.exists( savePath1 ) )
		{
			i++;
			savePath1 = Paths.get( resultsRootPath, nameGen.apply( imageName, i ) );
		}
		return savePath1.toFile();
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

}
