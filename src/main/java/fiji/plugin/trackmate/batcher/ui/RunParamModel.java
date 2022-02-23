package fiji.plugin.trackmate.batcher.ui;

import org.scijava.listeners.Listeners;

public class RunParamModel
{

	private boolean saveToInputFolder = true;

	private String outputFolderPath = System.getProperty( "user.home" );

	private boolean exportTrackMateFile = true;

	private boolean exportSpotTable = false;

	private boolean exportEdgeTable = false;

	private boolean exportTrackTable = false;

	private boolean exportAllTables = false;

	private boolean exportAVIMovie = false;

	private int fps = 10;

	private final transient Listeners.List< RunParamListener > listeners;

	public interface RunParamListener
	{
		public void runParamChanged();
	}

	public RunParamModel()
	{
		this.listeners = new Listeners.SynchronizedList<>();
	}

	public boolean isSaveToInputFolder()
	{
		return saveToInputFolder;
	}

	public void setSaveToInputFolder( final boolean saveToInputFolder )
	{
		if ( this.saveToInputFolder != saveToInputFolder )
		{
			this.saveToInputFolder = saveToInputFolder;
			notifyListeners();
		}
	}

	public String getOutputFolderPath()
	{
		return outputFolderPath;
	}

	public void setOutputFolderPath( final String outputFolderPath )
	{
		if ( !this.outputFolderPath.equals( outputFolderPath ) )
		{
			this.outputFolderPath = outputFolderPath;
			notifyListeners();
		}
	}

	public boolean isExportTrackMateFile()
	{
		return exportTrackMateFile;
	}

	public void setExportTrackMateFile( final boolean exportTrackMateFile )
	{
		if ( this.exportTrackMateFile != exportTrackMateFile )
		{
			this.exportTrackMateFile = exportTrackMateFile;
			notifyListeners();
		}
	}

	public boolean isExportSpotTable()
	{
		return exportSpotTable;
	}

	public void setExportSpotTable( final boolean exportSpotTable )
	{
		if ( this.exportSpotTable != exportSpotTable )
		{
			this.exportSpotTable = exportSpotTable;
			notifyListeners();
		}
	}

	public boolean isExportEdgeTable()
	{
		return exportEdgeTable;
	}

	public void setExportEdgeTable( final boolean exportEdgeTable )
	{
		if ( this.exportEdgeTable != exportEdgeTable )
		{
			this.exportEdgeTable = exportEdgeTable;
			notifyListeners();
		}
	}

	public boolean isExportTrackTable()
	{
		return exportTrackTable;
	}

	public void setExportTrackTable( final boolean exportTrackTable )
	{
		if ( this.exportTrackTable != exportTrackTable )
		{
			this.exportTrackTable = exportTrackTable;
			notifyListeners();
		}
	}

	public boolean isExportAllTables()
	{
		return exportAllTables;
	}

	public void setExportAllTables( final boolean exportAllTables )
	{
		if ( this.exportAllTables != exportAllTables )
		{
			this.exportAllTables = exportAllTables;
			notifyListeners();
		}
	}

	public boolean isExportAVIMovie()
	{
		return exportAVIMovie;
	}

	public void setExportAVIMovie( final boolean exportAVIMovie )
	{
		if ( this.exportAVIMovie != exportAVIMovie )
		{
			this.exportAVIMovie = exportAVIMovie;
			notifyListeners();
		}
	}

	public int getMovieFps()
	{
		return fps;
	}

	public void setMovieFps( final int fps )
	{
		if ( this.fps != fps )
		{
			this.fps = fps;
			notifyListeners();
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() );
		final String[][] args = new String[][] {
				{ "saveToInputFolder", "" + saveToInputFolder },
				{ "outputFolderPath", "" + outputFolderPath },
				{ "exportTrackMateFile", "" + exportTrackMateFile },
				{ "exportSpotTable", "" + exportSpotTable },
				{ "exportEdgeTable", "" + exportEdgeTable },
				{ "exportTrackTable", "" + exportTrackTable },
				{ "exportAllTables", "" + exportAllTables },
				{ "exportAVIMovie", "" + exportAVIMovie },
				{ "fps", "" + fps }
		};
		for ( final String[] arg : args )
			str.append( String.format( "\n - %-20s: %s", arg[ 0 ], arg[ 1 ] ) );

		return str.toString();
	}

	public Listeners.List< RunParamListener > listeners()
	{
		return listeners;
	}

	private void notifyListeners()
	{
		for ( final RunParamListener l : listeners.list )
			l.runParamChanged();
	}
}
