/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
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
package fiji.plugin.trackmate.batcher.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.scijava.listeners.Listeners;

public class FileListModel
{

	public interface FileListlListener
	{
		public void fileListChanged();
	}

	private final List< String > fileList = new ArrayList<>();

	private final transient Listeners.List< FileListlListener > listeners;

	public FileListModel()
	{
		this.listeners = new Listeners.SynchronizedList<>();
	}

	public Listeners.List< FileListlListener > listeners()
	{
		return listeners;
	}

	public List< String > getList()
	{
		return new ArrayList<>( fileList );
	}

	public List< Path > getPaths()
	{
		final List< Path > paths = new ArrayList<>( fileList.size() );
		for ( final String str : fileList )
			paths.add( Paths.get( str ) );
		return paths;
	}

	public FileListModel removeAll()
	{
		if ( !fileList.isEmpty() )
		{
			fileList.clear();
			notifyListeners();
		}
		return this;
	}

	public FileListModel addAll( final List< String > strs )
	{
		if ( !strs.isEmpty() )
		{
			fileList.addAll( strs );
			notifyListeners();
		}
		return this;
	}

	public FileListModel setAll( final List< String > strs )
	{
		final ArrayList< String > copy = new ArrayList<>( fileList );
		fileList.clear();
		fileList.addAll( strs );
		if ( !copy.equals( fileList ) )
			notifyListeners();
		return this;

	}

	public FileListModel add( final String string )
	{
		fileList.add( string );
		notifyListeners();
		return this;
	}

	public FileListModel remove( final int index )
	{
		if ( fileList.size() < 2 || index < 0 || index >= fileList.size() )
			return this;

		fileList.remove( index );
		notifyListeners();
		return this;
	}

	public FileListModel set( final int id, final String text )
	{
		if ( !fileList.get( id ).equals( text ) )
		{
			fileList.set( id, text );
			notifyListeners();
		}
		return this;
	}

	@Override
	public String toString()
	{
		final List< Path > paths = getPaths();
		int nDontExist = 0;
		final List< Path > regularFiles = new ArrayList<>();
		for ( final Path path : paths )
		{
			if ( !Files.exists( path ) )
			{
				nDontExist++;
				continue;
			}
			if ( Files.isDirectory( path ) )
			{
				final List< Path > files = getFilesOf( path );
				regularFiles.addAll( files );
				continue;
			}
			regularFiles.add( path );
		}
		final int nFiles = regularFiles.size();

		final StringBuilder str = new StringBuilder( super.toString() );
		str.append( String.format( "\n - %3d paths specified.", paths.size() ) );
		str.append( String.format( "\n - %3d are non existent.", nDontExist ) );
		str.append( String.format( "\n - %3d files total.", nFiles ) );
		return str.toString();
	}

	private static List< Path > getFilesOf( final Path path )
	{
		try (Stream< Path > paths = Files.list( path ))
		{
			return paths.filter( Files::isRegularFile ).collect( Collectors.toList() );

		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	private void notifyListeners()
	{
		for ( final FileListlListener l : listeners.list )
			l.fileListChanged();
	}
}
