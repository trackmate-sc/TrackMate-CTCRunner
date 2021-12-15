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
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;

import org.scijava.Context;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.CTCExporter;
import fiji.plugin.trackmate.action.CTCExporter.ExportType;
import fiji.plugin.trackmate.ctc.CTCMetricsProcessor.CTCMetrics;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.util.ValuePair;

public class CTCMetricsRunner
{

	/**
	 * Input image.
	 */
	private final ImagePlus imp;

	/**
	 * Logger to supervise the batch.
	 */
	private final Logger batchLogger = Logger.DEFAULT_LOGGER;

	/**
	 * Logger to pass to TrackMate instances.
	 */
	private final Logger trackmateLogger = Logger.VOID_LOGGER;

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

	public CTCMetricsRunner( final String imagePath, final String gtPath, final Context context )
	{
		this.imp = IJ.openImage( imagePath );
		this.gtPath = gtPath;
		this.resultsRootPath = Paths.get( imagePath ).getParent();
		assert imp != null;
		final int logLevel = 0; // silence CTC logging.
		this.ctc = new CTCMetricsProcessor( context, logLevel );
	}

	public ValuePair< TrackMate, Double > getOrExecDetection( final SpotDetectorFactoryBase< ? > detectorFactory, final Map< String, Object > detectorSettings )
	{
		final Settings settings = new Settings( imp );
		settings.detectorFactory = detectorFactory;
		settings.detectorSettings = detectorSettings;
		settings.addAllAnalyzers();
		settings.initialSpotFilterValue = 0.;

		// For timing.
		double detectionTiming = Double.NaN;

		// Did we have already an existing detection file?
		final File trackmateFile = new File( resultsRootPath.toFile(), "TrackMate_" + settings.detectorFactory.getKey() + ".xml" );
		final File timingFile = new File( resultsRootPath.toFile(), "TrackMate_" + settings.detectorFactory.getKey() + "_timing.txt" );
		final TrackMate trackmate;
		if ( trackmateFile.exists() )
		{
			batchLogger.log( "Reading detection from " + trackmateFile + '\n' );
			final TmXmlReader reader = new TmXmlReader( trackmateFile );
			if ( !reader.isReadingOk() )
			{
				batchLogger.error( reader.getErrorMessage() );
				return null;
			}
			final Model model = reader.getModel();
			trackmate = new TrackMate( model, settings );
			trackmate.getModel().setLogger( trackmateLogger );

			// Read timing.
			batchLogger.log( "Reading timing from " + timingFile + '\n' );
			try (BufferedReader timingReader = new BufferedReader( new FileReader( timingFile ) ))
			{
				final String line = timingReader.readLine();
				detectionTiming = Double.parseDouble( line );
			}
			catch ( final IOException e )
			{
				batchLogger.error( "Could not read timing file:\n" + e.getMessage() + '\n' );
				e.printStackTrace();
			}

			batchLogger.log( "Reading done. Timing = " + detectionTiming + " s.\n" );
		}
		else
		{
			final long start = System.currentTimeMillis();
			trackmate = new TrackMate( settings );
			trackmate.getModel().setLogger( trackmateLogger );
			if ( !trackmate.execDetection()
					|| !trackmate.execInitialSpotFiltering()
					|| !trackmate.computeSpotFeatures( true )
					|| !trackmate.execSpotFiltering( true ) )
			{
				System.err.println( "Error in the detection step:\n" + trackmate.getErrorMessage() );
				return null;
			}
			final long end = System.currentTimeMillis();
			detectionTiming = ( end - start ) / 1000.;

			// Write tmp file so that we don't have to redo the tracking.
			batchLogger.log( "Saving detection to file " + trackmateFile + '\n' );
			final TmXmlWriter writer = new TmXmlWriter( trackmateFile, trackmateLogger );
			writer.appendLog( batchLogger.toString() );
			writer.appendModel( trackmate.getModel() );
			writer.appendSettings( trackmate.getSettings() );
			try
			{
				writer.writeToFile();
			}
			catch ( final IOException e )
			{
				batchLogger.error( "Could not write TrackMate file:\n" + e.getMessage() + '\n' );
				e.printStackTrace();
			}

			// Write timing.
			try (FileWriter timingWriter = new FileWriter( timingFile ))
			{
				timingWriter.write( "" + detectionTiming );
			}
			catch ( final IOException e )
			{
				batchLogger.error( "Could not write timing file:\n" + e.getMessage() + '\n' );
				e.printStackTrace();
			}
		}
		return new ValuePair<>( trackmate, detectionTiming );
	}

	public double execTracking( final TrackMate trackmate, final SpotTrackerFactory trackerFactory, final Map< String, Object > trackerSettings )
	{
		trackmate.getSettings().trackerFactory = trackerFactory;
		trackmate.getSettings().trackerSettings = trackerSettings;

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
		batchLogger.log( String.format( "Tracking time: %.1f s\n", trackingTiming ) );

		return trackingTiming;
	}

	public void performCTCMetricsMeasurements( final TrackMate trackmate, final double detectionTiming, final double trackingTiming )
	{
		trackmateLogger.log( "Exporting as CTC results.\n" );
		final Settings settings = trackmate.getSettings();
		final File csvFile = findSuitableCSVFile( settings );
		final String[] csvHeader1 = toCSVHeader( settings );

		final int id = CTCExporter.getAvailableDatasetID( resultsRootPath.toString() );
		try
		{
			// Export to CTC files.
			final String resultsFolder = CTCExporter.exportTrackingData( resultsRootPath.toString(), id, ExportType.RESULTS, trackmate, trackmateLogger );

			// Perform CTC measurements.
			final CTCMetrics metrics = ctc.process( gtPath, resultsFolder );
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

			// Delete CTC export folder.
			deleteFolder( resultsFolder );
		}
		catch ( final IOException e )
		{
			batchLogger.error( "Could not export tracking data to CTC files:\n" + e.getMessage() + '\n' );
			e.printStackTrace();
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

	private static final BiFunction< String, Integer, String > nameGenWithID = ( imName, i ) -> String.format( "%s_CTCMetrics_%02d.csv", imName, i );

}
