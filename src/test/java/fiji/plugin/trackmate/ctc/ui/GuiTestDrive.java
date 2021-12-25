package fiji.plugin.trackmate.ctc.ui;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.ctc.TrackMateParameterSweepPlugin;
import net.imagej.ImageJ;

public class GuiTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final ImageJ ij = new ImageJ();
		ij.launch( args );
		new TrackMateParameterSweepPlugin().run( "samples/MAX_Merged.tif" );
	}
}
