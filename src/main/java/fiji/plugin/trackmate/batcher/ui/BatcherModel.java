package fiji.plugin.trackmate.batcher.ui;

import org.scijava.listeners.Listeners;

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
