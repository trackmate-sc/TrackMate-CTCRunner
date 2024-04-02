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
}
