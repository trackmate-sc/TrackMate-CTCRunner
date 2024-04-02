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

import org.scijava.Cancelable;

import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.HelperRunner;
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
}
