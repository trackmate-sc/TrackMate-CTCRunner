package fiji.plugin.trackmate.ctc.ui;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.ctc.TrackMateParameterSweepPlugin;
import net.imagej.ImageJ;

public class PluginTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
//		Debug.runPlugIn( TrackMateParameterSweepPlugin.class.getCanonicalName(), null, false );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final ImageJ ij = new ImageJ();
		ij.launch( args );
		new TrackMateParameterSweepPlugin().run( null );
	}
}
