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
package fiji.plugin.trackmate.batcher.ui;

import org.scijava.listeners.Listeners;
import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.batcher.RunParamModel;

public class BatcherModel
{

	private transient Listeners.List< BatcherModelListener > listeners;

	public interface BatcherModelListener
	{
		public void batcherModelChanged();
	}

	private final FileListModel fileListModel;

	private final TrackMateReadConfigModel trackMateReadConfigModel;

	private final RunParamModel runParamModel;

	public final String fileVersion = VersionUtils.getVersion( BatcherModel.class );

	public BatcherModel()
	{
		this.fileListModel = new FileListModel();
		this.trackMateReadConfigModel = new TrackMateReadConfigModel();
		this.runParamModel = new RunParamModel();
	}

	public FileListModel getFileListModel()
	{
		return fileListModel;
	}

	public RunParamModel getRunParamModel()
	{
		return runParamModel;
	}

	public TrackMateReadConfigModel getTrackMateReadConfigModel()
	{
		return trackMateReadConfigModel;
	}

	public Listeners.List< BatcherModelListener > listeners()
	{
		if ( listeners == null )
		{
			listeners = new Listeners.SynchronizedList<>();
			// Register sub-listeners.
			fileListModel.listeners().add( () -> notifyListeners() );
			trackMateReadConfigModel.listeners().add( () -> notifyListeners() );
			runParamModel.listeners().add( () -> notifyListeners() );
		}
		return listeners;
	}

	private void notifyListeners()
	{
		for ( final BatcherModelListener l : listeners.list )
			l.batcherModelChanged();
	}
}
