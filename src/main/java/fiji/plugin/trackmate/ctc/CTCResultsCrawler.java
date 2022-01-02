package fiji.plugin.trackmate.ctc;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.ctc.CTCResults.Builder;

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

	public static void main( final String[] args ) throws IOException
	{
		final String resultsFolder = "/Users/tinevez/Projects/JYTinevez/TrackMate-StarDist/CellMigration/";
		final CTCResultsCrawler crawler = new CTCResultsCrawler( Logger.DEFAULT_LOGGER );
		crawler.crawl( resultsFolder );
	}
}
