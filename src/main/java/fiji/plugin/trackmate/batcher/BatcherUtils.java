package fiji.plugin.trackmate.batcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fiji.plugin.trackmate.TrackMate;
import loci.formats.ImageReader;


public class BatcherUtils
{

	public static final Set< Path > collectRegularFiles( final List< String > list )
	{
		final Set< Path > paths = new LinkedHashSet<>();
		for ( final String pathStr : list )
		{
			final Path path = Paths.get( pathStr );
			if ( !Files.exists( path ) )
				continue;

			if ( Files.isDirectory( path ) )
			{
				try (final Stream< Path > children = Files.list( path ))
				{
					final List< Path > collect = children.filter( Files::isRegularFile ).collect( Collectors.toList() );
					paths.addAll( collect );
				}
				catch ( final IOException e )
				{}
				continue;
			}

			paths.add( path );
		}
		return paths;
	}

	public static final Set< Path > filterImageFiles( final Set< Path > paths )
	{
		// Test which are images. Thanks Curtis.
		final Set< Path > imageFiles = new LinkedHashSet<>();
		try (final ImageReader r = new ImageReader())
		{
			final boolean allowOpen = false;
			for ( final Path path : paths )
			{
				final boolean isImageFile = r.isThisType( path.toString(), allowOpen );
				if ( isImageFile )
					imageFiles.add( path );
			}
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
		return imageFiles;
	}

	private BatcherUtils()
	{}

	public static void toExcelWorkbook( final TrackMate trackmate )
	{
		// TODO
	}
}
