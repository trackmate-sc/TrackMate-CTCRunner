/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2024 TrackMate developers.
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

import java.awt.event.WindowAdapter;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.scijava.Cancelable;

import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.HelperRunner;
import fiji.plugin.trackmate.helper.HelperRunner.Builder;
import fiji.plugin.trackmate.helper.ResultsCrawler;
import fiji.plugin.trackmate.helper.model.ParameterSweepModel;
import fiji.plugin.trackmate.helper.model.ParameterSweepModelIO;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import ij.ImagePlus;

public class ParameterSweepController implements Cancelable
{

	private final ParameterSweepPanel gui;

	private final JFrame frame;

	private final HelperRunner runner;

	public ParameterSweepController( final HelperRunner runner )
	{
		this.runner = runner;

		final ImagePlus imp = runner.getImage();
		final ParameterSweepModel model = runner.getModel();
		final ResultsCrawler crawler = runner.getCrawler();
		final String gtPath = runner.getGroundTruthPath();

		gui = new ParameterSweepPanel( imp, model, crawler, gtPath );
		runner.setBatchLogger( gui.logger );

		gui.btnRun.addActionListener( e -> run() );
		gui.btnStop.addActionListener( e -> cancel( "User pressed the stop button." ) );
		gui.btnStop.setVisible( false );
		gui.btnReset.addActionListener( e -> resetParameters() );

		// Save on model modification.
		model.listeners().add( () -> {
			gui.refresh();
			ParameterSweepModelIO.saveTo( new File( runner.getModelPath() ), model );
		} );

		frame = new JFrame( "TrackMate Helper" );
		// Stop crawling when closing the window.
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final java.awt.event.WindowEvent e )
			{
				crawler.stopWatching();
			}
		} );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.setSize( 600, 700 );
		frame.setLocationRelativeTo( null );
	}

	private void resetParameters()
	{
		final String msg = "This will remove all settings for the parameter sweep "
				+ "configuration with this dataset. Effectively, this will be done by "
				+ "deleting the file:"
				+ "<p>"
				+ "<p>"
				+ runner.getModelPath()
				+ "<p>"
				+ "<p>"
				+ "and relaunching this user interface. Are you sure?";
		final String title = "Reset parameters";
		final int answer = JOptionPane.showConfirmDialog( frame, toHtml( msg ), title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, Icons.TRACKMATE_ICON );
		if ( answer != JOptionPane.YES_OPTION )
			return;

		// Remove file.
		final File file = new File( runner.getModelPath() );
		if ( file.exists() )
			file.delete();
		if ( file.exists() )
		{
			final String msg2 = "Could not delete the file: " + file;
			JOptionPane.showMessageDialog( frame, toHtml( msg2 ), title, JOptionPane.ERROR_MESSAGE, Icons.TRACKMATE_ICON );
			return;
		}

		// Save the default to file.
		ParameterSweepModelIO.saveTo( file, new ParameterSweepModel() );

		// Close this UI.
		frame.dispose();

		// Make a new runner, copying old values.
		final String gtPath = runner.getGroundTruthPath();
		final File saveFolder = file.getParentFile();
		final Builder builder = HelperRunner.create();
		final HelperRunner newRunner = builder
				.trackingMetricsType( runner.getType() )
				.groundTruth( gtPath )
				.image( runner.getImage() )
				.runSettings( file.getAbsolutePath() )
				.savePath( saveFolder.getAbsolutePath() )
				.get();
		if ( newRunner == null )
		{
			final String msg3 = builder.getErrorMessage();
			JOptionPane.showMessageDialog( frame, toHtml( msg3 ), title, JOptionPane.ERROR_MESSAGE, Icons.TRACKMATE_ICON );
			return;
		}

		final ParameterSweepController controller = new ParameterSweepController( newRunner );
		controller.show();
	}

	private void run()
	{
		// Refresh model :(
		gui.refresh();
		gui.enablers.forEach( EverythingDisablerAndReenabler::disable );
		gui.btnRun.setVisible( false );
		gui.btnStop.setVisible( true );
		gui.btnStop.setEnabled( true );
		gui.logger.setProgress( 0. );

		final int targetChannel = gui.sliderChannel.getValue();
		runner.setTargetChannel( targetChannel );

		final boolean saveEachTime = gui.chckbxSaveTrackMateFile.isSelected();
		runner.setSaveTrackMateFiles( saveEachTime );

		gui.tabbedPane.setSelectedIndex( 0 );
		new Thread( "TrackMate Helper runner thread" )
		{
			@Override
			public void run()
			{
				try
				{
					runner.run();
				}
				finally
				{
					gui.btnRun.setVisible( true );
					gui.btnStop.setVisible( false );
					gui.enablers.forEach( EverythingDisablerAndReenabler::reenable );
				}
			}
		}.start();
	}

	public void show()
	{
		frame.setVisible( true );
	}

	@Override
	public void cancel( final String cancelReason )
	{
		gui.btnStop.setEnabled( false );
		runner.cancel( cancelReason );
	}

	@Override
	public String getCancelReason()
	{
		return runner.getCancelReason();
	}

	@Override
	public boolean isCanceled()
	{
		return runner.getCancelReason() != null;
	}

	public static final String toHtml( final String msg )
	{
		return "<html><body style='width: 300px;'>"
				+ msg
						.replaceAll( "\n", "<p><p>" )
						.replaceAll( "/", "/<wbr>" )
						.replaceAll( "\\\\", "/<wbr>" )
				+ "</body></html>";
	}
}
