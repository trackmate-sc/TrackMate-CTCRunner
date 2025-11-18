/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.helper.ui;

import static fiji.plugin.trackmate.helper.ui.components.GuiUtils.HELPER_ICON;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import fiji.plugin.trackmate.helper.HelperRunner;
import fiji.plugin.trackmate.helper.HelperRunner.Builder;
import fiji.plugin.trackmate.helper.TrackingMetricsType;
import fiji.plugin.trackmate.helper.ctc.CTCTrackingMetricsType;
import fiji.plugin.trackmate.helper.model.ParameterSweepModel;
import fiji.plugin.trackmate.helper.model.ParameterSweepModelIO;
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
		frame.setIconImage( HELPER_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.setSize( 350, 550 );
		frame.setLocationRelativeTo( null );

		gui.btnCancel.addActionListener( e -> frame.dispose() );
		gui.btnOK.addActionListener( e -> {
			final ImagePlus imp;
			final boolean impOpen = WindowManager.getImageCount() > 0;
			final boolean ctcSelected = gui.isCTCSelected();
			final String gtPath = gui.tfGTPath.getText();

			String units;
			if ( impOpen )
			{
				final String imName = ( String ) gui.cmbboxImp.getSelectedItem();
				imp = WindowManager.getImage( imName );
				if ( imp == null )
				{
					IJ.error( "TrackMate-Helper", "Could not find opened image with name " + imName );
					return;
				}
				units = imp.getCalibration().getUnit();
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
				units = imp.getCalibration().getUnit();
			}
			imp.killRoi(); // Remove ROI to avoid issues with CTC and tracking.

			frame.dispose();
			final TrackingMetricsType type = ctcSelected
					? new CTCTrackingMetricsType()
					: new SPTTrackingMetricsType( gui.getSPTMaxPairingDistance(), units );

			final File modelFile = ParameterSweepModelIO.makeSettingsFileForGTPath( gtPath );
			if ( !modelFile.exists() )
				ParameterSweepModelIO.saveTo( modelFile, new ParameterSweepModel() );

			final File saveFolder = modelFile.getParentFile();

			final Builder builder = HelperRunner.create();
			final HelperRunner runner = builder
					.trackingMetricsType( type )
					.groundTruth( gtPath )
					.image( imp )
					.runSettings( ParameterSweepModelIO.makeSettingsFileForGTPath( gtPath ).getAbsolutePath() )
					.savePath( saveFolder.getAbsolutePath() )
					.get();

			if ( runner == null )
			{
				final String msg = builder.getErrorMessage();
				final String wrappedMessage = "<html><body style='width: 300px;'>"
						+ msg
								.replaceAll( "\n", "<p><p>" )
								.replaceAll( "/", "/<wbr>" )
						+ "</body></html>";
				final String title = "TrackMate Helper";
				JOptionPane.showMessageDialog( frame, wrappedMessage, title, JOptionPane.ERROR_MESSAGE, HELPER_ICON );
				return;
			}

			final ParameterSweepController controller = new ParameterSweepController( runner );
			controller.show();

		} );
		frame.setVisible( true );
	}
}
