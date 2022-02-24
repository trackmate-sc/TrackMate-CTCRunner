package fiji.plugin.trackmate.batcher;

import fiji.plugin.trackmate.batcher.ui.BatcherController;
import fiji.plugin.trackmate.gui.GuiUtils;
import ij.plugin.PlugIn;
import net.imagej.ImageJ;

public class TrackMateBatcherPlugin implements PlugIn
{

	@Override
	public void run( final String arg )
	{
		GuiUtils.setSystemLookAndFeel();
		final BatcherController controller = new BatcherController();
		controller.show();
	}

	public static void main( final String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );

		new TrackMateBatcherPlugin().run( null );
	}
}
