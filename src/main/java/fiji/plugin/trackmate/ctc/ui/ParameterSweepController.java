package fiji.plugin.trackmate.ctc.ui;

import javax.swing.JFrame;

import fiji.plugin.trackmate.gui.Icons;
import ij.ImagePlus;

public class ParameterSweepController
{

	private final ParameterSweepPanel gui;

	private final JFrame frame;

	public ParameterSweepController( final ImagePlus imp )
	{
		gui = new ParameterSweepPanel( imp );
		frame = new JFrame( "TrackMate parameter sweep" );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.pack();
		frame.setLocationRelativeTo( null );
	}

	public void show()
	{
		frame.setVisible( true );
	}
}
