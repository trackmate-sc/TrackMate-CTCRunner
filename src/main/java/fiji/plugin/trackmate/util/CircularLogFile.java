package fiji.plugin.trackmate.util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import fiji.plugin.trackmate.Logger;

public class CircularLogFile extends Logger
{
	private final String logFilePath;

	private final int maxLines;

	public CircularLogFile( final String logFilePath, final int maxLines )
	{
		this.logFilePath = logFilePath;
		this.maxLines = maxLines;
	}

	@Override
	public void log( final String message, final Color color )
	{
		appendToLog( message );
	}

	@Override
	public void error( final String message )
	{
		appendToLog( message );
	}

	@Override
	public void setProgress( final double val )
	{
		// Ignored.
	}

	@Override
	public void setStatus( final String status )
	{
		// Ignored.
	}

	public void appendToLog( final String logEntry )
	{
		try
		{
			// Create the log file if it does not exist
			if ( !Files.exists( Paths.get( logFilePath ) ) )
				Files.createFile( Paths.get( logFilePath ) );

			// Check if the log file exceeds the maximum number of lines
			final int currentLines = countLinesInFile( logFilePath );
			if ( currentLines >= maxLines )
				truncateLogFile( logFilePath, currentLines - maxLines + 1 );

			// Append the new log entry
			Files.write( Paths.get( logFilePath ), ( logEntry + System.lineSeparator() ).getBytes(), StandardOpenOption.APPEND );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	private static int countLinesInFile( final String filePath ) throws IOException
	{
		try (BufferedReader reader = new BufferedReader( new FileReader( filePath ) ))
		{
			int lines = 0;
			while ( reader.readLine() != null )
				lines++;

			return lines;
		}
	}

	private static void truncateLogFile( final String filePath, final int linesToRemove ) throws IOException
	{
		List< String > lines = Files.readAllLines( Paths.get( filePath ) );
		if ( lines.size() > linesToRemove )
		{
			lines = lines.subList( linesToRemove, lines.size() );
			Files.write( Paths.get( filePath ), lines );
		}
	}
}
