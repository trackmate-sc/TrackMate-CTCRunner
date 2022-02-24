package fiji.plugin.trackmate.batcher.ui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BatcherModelIO
{

	private static File defaultSaveFile = new File( new File( System.getProperty( "user.home" ), ".trackmate" ), "trackmatebatchersettings.json" );

	public static void saveTo( final File modelFile, final BatcherModel model )
	{
		final String str = toJson( model );

		if ( !modelFile.exists() )
			modelFile.getParentFile().mkdirs();

		try (FileWriter writer = new FileWriter( modelFile ))
		{
			writer.append( str );
		}
		catch ( final IOException e )
		{
			System.err.println( "Could not write the settings to " + modelFile );
			e.printStackTrace();
		}
	}

	public static void saveToDefault( final BatcherModel model )
	{
		saveTo( defaultSaveFile, model );
	}

	public static BatcherModel readFrom( final File modelFile )
	{
		if ( !modelFile.exists() )
		{
			final BatcherModel model = new BatcherModel();
			saveTo( modelFile, model );
			return model;
		}

		try (FileReader reader = new FileReader( modelFile ))
		{
			final String str = Files.lines( Paths.get( modelFile.getAbsolutePath() ) )
					.collect( Collectors.joining( System.lineSeparator() ) );

			return fromJson( str );
		}
		catch ( final IOException e )
		{}
		return new BatcherModel();
	}

	public static BatcherModel readFromDefault()
	{
		return readFrom( defaultSaveFile );
	}

	private static Gson getGson()
	{
		final GsonBuilder builder = new GsonBuilder();
		return builder.setPrettyPrinting().create();
	}

	public static String toJson( final BatcherModel model )
	{
		return getGson().toJson( model );
	}

	public static BatcherModel fromJson( final String str )
	{
		final BatcherModel model = getGson().fromJson( str, BatcherModel.class );
		return model;
	}
}
