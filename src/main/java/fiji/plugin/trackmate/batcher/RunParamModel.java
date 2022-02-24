/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
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
