package fiji.plugin.trackmate.batcher.ui;

import java.io.BufferedReader;
import java.io.FileReader;
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
		fileList.clear();
		fileList.addAll( strs );
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

		final List< Path > xmlFiles = filterXMLFiles( regularFiles );
		final List< Path > tmFiles = filterTrackMateFiles( xmlFiles );
		final int nFiles = regularFiles.size();
		final int nXMLFiles = xmlFiles.size();
		final int nTMFiles = tmFiles.size();

		final StringBuilder str = new StringBuilder( super.toString() );
		str.append( String.format( "\n - %3d paths specified.", paths.size() ) );
		str.append( String.format( "\n - %3d are non existent.", nDontExist ) );
		str.append( String.format( "\n - %3d files.", nFiles ) );
		str.append( String.format( "\n - %3d XML files.", nXMLFiles ) );
		str.append( String.format( "\n - %3d TrackMate files.", nTMFiles ) );
		return str.toString();
	}

	private static List< Path > filterTrackMateFiles( final List< Path > xmlFiles )
	{
		final List< Path > tmFiles = new ArrayList<>();
		for ( final Path path : xmlFiles )
		{
			/*
			 * We do this by plain text matching. Using the SAX parser is way
			 * too long.
			 */

			try (BufferedReader br = new BufferedReader( new FileReader( path.toFile() ) ))
			{
				final String line1 = br.readLine();
				if ( !line1.toLowerCase().startsWith( "<?xml" ) )
					continue;
				final String line2 = br.readLine();
				if ( !line2.toLowerCase().startsWith( "<trackmate" ) )
					continue;

				tmFiles.add( path );
			}
			catch ( final IOException e1 )
			{
				continue;
			}
		}
		return tmFiles;
	}

	private static List< Path > filterXMLFiles( final List< Path > files )
	{
		return files
				.stream()
				.filter( path -> path.toString().toLowerCase().endsWith( ".xml" ) )
				.collect( Collectors.toList() );
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

	protected void notifyListeners()
	{
		for ( final FileListlListener l : listeners.list )
			l.fileListChanged();
	}
}
