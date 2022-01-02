package fiji.plugin.trackmate.ctc;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.ctc.CTCResults.Builder;

public class CTCResultsCrawler
{

	private final String resultsFolder;

	private final Logger batchLogger;

	public CTCResultsCrawler( final String resultsFolder, final Logger batchLogger )
	{
		this.resultsFolder = resultsFolder;
		this.batchLogger = batchLogger;
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
}
