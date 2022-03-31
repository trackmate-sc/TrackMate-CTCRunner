package fiji.plugin.trackmate.helper.ui;

import javax.swing.JFrame;

import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.TrackingMetricsType;
import fiji.plugin.trackmate.helper.ctc.CTCTrackingMetricsType;
import fiji.plugin.trackmate.helper.spt.SPTTrackingMetricsType;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

public class HelperLauncherController
{

	public HelperLauncherController()
	{
		final HelperLauncherPanel gui = new HelperLauncherPanel();
		final JFrame frame = new JFrame( "TrackMate-Helper Launcher" );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.setSize( 350, 550 );
		frame.setLocationRelativeTo( null );

		gui.btnCancel.addActionListener( e -> frame.dispose() );
		gui.btnOK.addActionListener( e -> {
			final ImagePlus imp;
			final boolean impOpen = WindowManager.getImageCount() > 0;
			final boolean ctcSelected = gui.rdbtnCTC.isSelected();
			final String gtPath = gui.tfGTPath.getText();

			if ( impOpen )
			{
				final String imName = ( String ) gui.cmbboxImp.getSelectedItem();
				imp = WindowManager.getImage( imName );
				if ( imp == null )
				{
					IJ.error( "TrackMate-Helper", "Could not find opened image with name " + imName );
					return;
				}
			}
			else
			{
				final String imagePath = gui.tfImagePath.getText();
				imp = IJ.openImage( imagePath );
				if ( imp == null )
				{
					IJ.error( "TrackMate-Helper", "Could not open image file " + imagePath );
					return;
				}
				imp.show();
			}

			frame.dispose();
			final TrackingMetricsType type = ctcSelected
					? new CTCTrackingMetricsType()
					: new SPTTrackingMetricsType();
			final ParameterSweepController controller = new ParameterSweepController( imp, gtPath, type );
			controller.show();
		} );

		// It still cannot stand the Metal L&F...
		fiji.plugin.trackmate.gui.GuiUtils.setSystemLookAndFeel();
		frame.setVisible( true );
	}
}
