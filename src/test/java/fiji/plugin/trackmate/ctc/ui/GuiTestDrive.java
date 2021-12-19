package fiji.plugin.trackmate.ctc.ui;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;

public class GuiTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final ImageJ ij = new ImageJ();
		ij.launch( args );
		final ImagePlus imp = IJ.openImage( "samples/FakeTracks.tif" );
		imp.show();
		new ParameterSweepController( imp ).show();
	}
}
