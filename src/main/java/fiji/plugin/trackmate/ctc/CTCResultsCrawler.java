package fiji.plugin.trackmate.ctc;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.CTCResults.Builder;
import net.imglib2.util.ValuePair;

public class CTCResultsCrawler
{

	private final Logger batchLogger;

	private final Map< String, CTCResults > tables;

	public CTCResultsCrawler( final Logger batchLogger )
	{
		this.batchLogger = batchLogger;
		this.tables = new HashMap<>();
	}

	public void reset()
	{
		tables.clear();
	}

	public String printReport()
	{
		final StringBuilder str = new StringBuilder();
		for ( final CTCMetricsDescription desc : CTCMetricsDescription.values() )
		{
			final ValuePair< String, Integer > pair = bestFor( desc );
			final CTCResults results = get( pair.getA() );
			if ( results == null )
			{
				str.append( "There is no good configuration for " + desc.description() );
			}
			else
			{
				final String s = results.printLine( pair.getB() );
				str.append( String.format( "Best configuration for %s with a score of %.3f\n",
						desc.description(), 
						results.getMetrics( pair.getB() ).get( desc ) ) );
				str.append( s );
			}
			str.append( "\n________________________________\n" );
		}

		return str.toString();
	}

	public ValuePair< String, Integer > bestFor( final CTCMetricsDescription desc )
	{
		final BiFunction< Double, Double, Boolean > betterThan;
		double best;
		int bestLine = -1;
		String bestCSVFile = null;
		if ( desc == CTCMetricsDescription.TIM
				|| desc == CTCMetricsDescription.DETECTION_TIME
				|| desc == CTCMetricsDescription.TRACKING_TIME )
		{
			// Faster is better.
			betterThan = ( val, b ) -> val < b;
			best = Double.POSITIVE_INFINITY;
		}
		else
		{
			// Higher score is better.
			betterThan = ( val, b ) -> val > b;
			best = Double.NEGATIVE_INFINITY;
		}

		for ( final String csvFile : tables.keySet() )
		{
			final CTCResults results = tables.get( csvFile );
			final int line = results.bestFor( desc );
			if ( line < 0 )
				continue;
			final CTCMetrics m = results.getMetrics( line );
			final double val = m.get( desc );
			if ( betterThan.apply( val, best ) )
			{
				best = val;
				bestLine = line;
				bestCSVFile = csvFile;
			}
		}
		return new ValuePair<>( bestCSVFile, bestLine );
	}

	public CTCResults get( final String csvFile )
	{
		return tables.get( csvFile );
	}

	public final void crawl( final String resultsFolder ) throws IOException
	{
		final List< String > csvFiles = findFiles( resultsFolder, "csv" );
		for ( final String csvFile : csvFiles )
		{
			try (CSVReader csvReader = new CSVReaderBuilder( new FileReader( csvFile ) ).build())
			{
				final String[] readHeader = csvReader.readNext();
				if ( !CTCResults.isCTCHeader( readHeader ) )
				{
					batchLogger.log( String.format(
							"CSV file %s is not a CTC results file. Skipping.\n", csvFile ) );
					continue;
				}

				final Builder builder = CTCResults.create().addHeader( readHeader );
				String[] line;
				while ( ( line = csvReader.readNext() ) != null )
					builder.addFromCSV( line );

				final CTCResults results = builder.get();
				tables.put( csvFile, results );
			}
			catch ( final IOException | CsvValidationException e )
			{
				batchLogger.error( "Cannot open CSV file " + csvFile + " for reading:\n" + e.getMessage() );
				e.printStackTrace();
			}
		}

	}

	private static final List< String > findFiles( final String folder, final String fileExtension ) throws IOException
	{
		final Path path = Paths.get( folder );
		if ( !Files.isDirectory( path ) )
			throw new IllegalArgumentException( "Path must be a directory!" );

		try (Stream< Path > walk = Files.walk( path ))
		{
			return walk
					.filter( p -> !Files.isDirectory( p ) )
					.map( p -> p.toString().toLowerCase() )
					.filter( f -> f.endsWith( fileExtension ) )
					.collect( Collectors.toList() );
		}
	}

	public boolean isSettingsPresent( final Settings settings )
	{
		for ( final CTCResults results : tables.values() )
		{
			// Test detector.
			final int ntests = results.size();
			LINE: for ( int i = 0; i < ntests; i++ )
			{
				final String detectorKey = results.getDetector( i );
				if ( !detectorKey.equals( settings.detectorFactory.getKey() ) )
					continue;
				// Detectors are equal.

				final Map< String, Object > ds = settings.detectorSettings;
				final Map< String, String > dp = results.getDetectorParams( i );
				for ( final String key : ds.keySet() )
				{
					final Object o1 = ds.get( key );
					final String o2 = dp.get( key );
					if ( !o1.toString().equals( o2 ) )
						continue LINE;
				}
				// Detector params are equal.

				final String trackerKey = results.getTracker( i );
				if ( !trackerKey.equals( settings.trackerFactory.getKey() ) )
					continue;
				// Tracker are equal.

				final Map< String, Object > ts = settings.trackerSettings;
				final Map< String, String > tp = results.getTrackerParams( i );
				for ( final String key : ts.keySet() )
				{
					final Object o1 = ts.get( key );
					final String o2 = tp.get( key );
					if ( !o1.toString().equals( o2 ) )
						continue LINE;
				}
				// Tracker params are equal.

				return true;
			}
		}
		return false;
	}
}
