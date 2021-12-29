package fiji.plugin.trackmate.ctc;

import fiji.plugin.trackmate.ctc.ui.ParameterSweepController;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class TrackMateParameterSweepPlugin implements PlugIn
{

	private ParameterSweepController controller;

	@Override
	public void run( final String arg )
	{
		final ImagePlus imp;
		if ( arg == null || arg.isEmpty() )
		{
			imp = WindowManager.getCurrentImage();
		}
		else
		{
			imp = IJ.openImage( arg );
			imp.show();
		}

		controller = new ParameterSweepController( imp );
		controller.show();
	}

	public ParameterSweepController getController()
	{
		return controller;
	}
}
