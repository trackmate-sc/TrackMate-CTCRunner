package fiji.plugin.trackmate.batcher.ui;

public class BatcherModel
{

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
}
