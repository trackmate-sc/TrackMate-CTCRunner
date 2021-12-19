package fiji.plugin.trackmate.ctc.ui;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.Context;

import ij.IJ;
import net.imagej.ImageJ;

public class GuiTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );
		final Context context = ij.context();
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		IJ.openImage( "samples/FakeTracks.tif" ).show();
		new ParameterSweepController( context ).show();
	}
}
