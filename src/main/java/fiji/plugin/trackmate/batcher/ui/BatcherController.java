/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
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
package fiji.plugin.trackmate.batcher.ui;

import java.awt.event.WindowAdapter;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.Cancelable;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.batcher.BatcherUtils;
import fiji.plugin.trackmate.batcher.RunParamModel;
import fiji.plugin.trackmate.batcher.TrackMateBatcher;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.util.TMUtils;
import ij.Prefs;
import net.imagej.ImageJ;

public class BatcherController implements Cancelable
{

	private final BatcherModel model;

	private final BatcherPanel gui;

	private String cancelReason;

	private TrackMateBatcher runner;

	public BatcherController()
	{
		// Settings persistence.
		this.model = BatcherModelIO.readFromDefault();
		model.listeners().add( () -> BatcherModelIO.saveToDefault( model ) );

		this.gui = new BatcherPanel( model );
		gui.btnRun.addActionListener( e -> run() );
		gui.btnCancel.addActionListener( e -> cancel( "User pressed the cancel button." ) );
		gui.btnCancel.setVisible( false );

		// Listen to file list changes.
		model.getFileListModel().listeners().add( () -> logFileList() );
	}

	private void logFileList()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				final List< String > list = model.getFileListModel().getList();
				final Set< Path > paths = BatcherUtils.collectRegularFiles( list );
				final Set< Path > imageFiles = BatcherUtils.filterImageFiles( paths );

				gui.logger.log( "\n_______________________________\n" );
				gui.logger.log( "Input list points to " + paths.size() + " files,\n" );
				gui.logger.log( "among which there are " + imageFiles.size() + " candidate image files:\n" );
				for ( final Path im : imageFiles )
					gui.logger.log( " - " + im.toString() + '\n' );
			}
		}.start();
	}

	private void run()
	{
		cancelReason = null;
		gui.btnRun.setVisible( false );
		gui.btnCancel.setVisible( true );
		gui.btnCancel.setEnabled( true );
		new Thread( "TrackMate Batcher runner thread" )
		{
			@Override
			public void run()
			{
				try
				{
					final Set< Path > files = BatcherUtils.collectRegularFiles( model.getFileListModel().getList() );
					final Set< Path > inputPaths = BatcherUtils.filterImageFiles( files );
					final Settings settings = model.getTrackMateReadConfigModel().getSettings();
					final DisplaySettings displaySettings = model.getTrackMateReadConfigModel().getDisplaySettings();
					final RunParamModel runParams = model.getRunParamModel();
					runner = new TrackMateBatcher( inputPaths, settings, displaySettings, runParams, gui.logger );
					runner.setNumThreads( Prefs.getThreads() );

					if ( !runner.checkInput() )
					{
						gui.logger.error( runner.getErrorMessage() + '\n' );
						return;
					}
					if ( !runner.process() )
					{
						gui.logger.error( runner.getErrorMessage() + '\n' );
						return;
					}
				}
				catch ( final Exception e )
				{
					e.printStackTrace();
					gui.logger.error( "\nError running the batch:\n" );
					gui.logger.error( e.getMessage() + '\n' );
				}
				finally
				{
					gui.btnRun.setVisible( true );
					gui.btnCancel.setVisible( false );
				}
			}
		}.start();
	}

	public void show()
	{
		// It still cannot stand the Metal L&F...
		fiji.plugin.trackmate.gui.GuiUtils.setSystemLookAndFeel();
		final JFrame frame = new JFrame( "TrackMate Batcher" );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final java.awt.event.WindowEvent e )
			{
				cancel( "User closed the batcher window." );
			}
		} );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}

	@Override
	public void cancel( final String cancelReason )
	{
		gui.btnCancel.setEnabled( false );
		gui.logger.log( TMUtils.getCurrentTimeString() + " - " + cancelReason + '\n' );
		if ( runner != null )
			runner.cancel( cancelReason );
		this.cancelReason = cancelReason;
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}

	@Override
	public boolean isCanceled()
	{
		return cancelReason != null;
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );

		GuiUtils.setSystemLookAndFeel();
		final BatcherController controller = new BatcherController();
		controller.show();
	}
}
