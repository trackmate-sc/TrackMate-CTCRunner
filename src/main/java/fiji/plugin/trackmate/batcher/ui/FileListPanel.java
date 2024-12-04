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
package fiji.plugin.trackmate.batcher.ui;

import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.gui.Icons.ADD_ICON;
import static fiji.plugin.trackmate.gui.Icons.BIN_ICON;
import static fiji.plugin.trackmate.gui.Icons.REMOVE_ICON;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.FocusAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;

public class FileListPanel extends JPanel
{

	private final List< FileBoxPanel > fileBoxes = new ArrayList<>();

	private final EverythingDisablerAndReenabler enabler;

	private final Runnable refresher;

	private final JPanel mainPanel;

	private final JPanel buttonPanel;

	public FileListPanel( final FileListModel model )
	{
		this.enabler = new EverythingDisablerAndReenabler( this, new Class[] { JLabel.class } );
		this.refresher = new Runnable()
		{
			@Override
			public void run()
			{
				final List< String > strs = new ArrayList<>( fileBoxes.size() );
				for ( final FileBoxPanel fb : fileBoxes )
				{
					final String str = fb.tfStr.getText();
					strs.add( str );
				}
				model.setAll( strs );
			}
		};
		setLayout( new BorderLayout( 0, 0 ) );

		final JScrollPane scrollPane = new JScrollPane();
		this.add( scrollPane );
		scrollPane.setPreferredSize( new java.awt.Dimension( 250, 389 ) );
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setBorder( null );
		scrollPane.getVerticalScrollBar().setUnitIncrement( 16 );

		mainPanel = new JPanel();
		final BoxLayout jPanelAllThresholdsLayout = new BoxLayout( mainPanel, BoxLayout.Y_AXIS );
		mainPanel.setLayout( jPanelAllThresholdsLayout );
		scrollPane.setViewportView( mainPanel );

		buttonPanel = new JPanel();
		buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.LINE_AXIS ) );

		final JButton btnAdd = new JButton();
		btnAdd.setIcon( ADD_ICON );
		btnAdd.setPreferredSize( new java.awt.Dimension( 24, 24 ) );
		btnAdd.setSize( 24, 24 );
		btnAdd.setMinimumSize( new java.awt.Dimension( 24, 24 ) );

		final JButton btnClearAll = new JButton();
		btnClearAll.setIcon( BIN_ICON );
		btnClearAll.setToolTipText( "Clear all file paths" );
		btnClearAll.setPreferredSize( new java.awt.Dimension( 24, 24 ) );
		btnClearAll.setSize( 24, 24 );
		btnClearAll.setMinimumSize( new java.awt.Dimension( 24, 24 ) );

		buttonPanel.add( btnAdd );
		buttonPanel.add( Box.createHorizontalGlue() );
		buttonPanel.add( btnClearAll );

		/*
		 * Default values.
		 */

		mainPanel.add( buttonPanel );
		for ( final String string : model.getList() )
			addFileBox( string );
		if ( fileBoxes.size() > 1 )
			fileBoxes.forEach( sp -> sp.btnRemove.setVisible( true ) );
		else if ( fileBoxes.size() > 0 )
			fileBoxes.get( 0 ).btnRemove.setVisible( false );

		/*
		 * Listeners & co.
		 */

		btnAdd.addActionListener( e -> addFileBox() );
		btnClearAll.addActionListener( e -> clearAllFiles() );
		setDropTarget( new AddFilesDropTarget() );
	}

	public void addFileBox()
	{
		addFileBox( System.getProperty( "user.home" ) );
	}

	private void addFileBox( final String str )
	{
		final FileBoxPanel panel = new FileBoxPanel( str );
		addFileBox( panel );
		refresher.run();
	}

	private void addFileBoxes( final List< String > files )
	{
		for ( final String str : files )
		{
			final FileBoxPanel panel = new FileBoxPanel( str );
			addFileBox( panel );
		}
		refresher.run();
	}

	private void addFileBox( final FileBoxPanel panel )
	{
		mainPanel.remove( buttonPanel );

		final Component strut = Box.createVerticalStrut( 5 );
		fileBoxes.add( panel );
		mainPanel.add( panel );
		mainPanel.add( strut );
		mainPanel.add( buttonPanel );

		if ( fileBoxes.size() > 1 )
			fileBoxes.forEach( sp -> sp.btnRemove.setVisible( true ) );

		panel.btnRemove.addActionListener( e -> removeStringPanel( panel, strut ) );
		mainPanel.revalidate();
	}

	private void clearAllFiles()
	{
		mainPanel.removeAll();
		mainPanel.add( buttonPanel );
		fileBoxes.clear();
		mainPanel.revalidate();
		mainPanel.repaint();
		refresher.run();
	}

	private void removeStringPanel( final FileBoxPanel stringPanel, final Component strut )
	{
		fileBoxes.remove( stringPanel );

		if ( fileBoxes.size() < 2 )
			fileBoxes.forEach( sp -> sp.btnRemove.setVisible( false ) );
		mainPanel.remove( strut );
		mainPanel.remove( stringPanel );
		mainPanel.revalidate();
		mainPanel.repaint();
		refresher.run();
	}

	private static final long serialVersionUID = 1L;

	/*
	 * PRIVATE CLASSES.
	 */

	private class AddFilesDropTarget extends DropTarget
	{

		private static final long serialVersionUID = 1L;

		@Override
		public synchronized void drop( final DropTargetDropEvent evt )
		{
			try
			{
				evt.acceptDrop( DnDConstants.ACTION_COPY );
				@SuppressWarnings( "unchecked" )
				final List< File > droppedFiles = ( List< File > ) evt.getTransferable().getTransferData( DataFlavor.javaFileListFlavor );
				final List< String > list = droppedFiles.stream().map( File::getAbsolutePath ).collect( Collectors.toList() );
				addFileBoxes( list );
			}
			catch ( final Exception ex )
			{
				ex.printStackTrace();
			}
		}
	}

	private class FileBoxPanel extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final JButton btnRemove;

		private final JTextField tfStr;

		public FileBoxPanel( final String str )
		{
			setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );

			btnRemove = new JButton();
			add( btnRemove );
			btnRemove.setIcon( REMOVE_ICON );
			btnRemove.setFont( SMALL_FONT );
			btnRemove.setPreferredSize( new java.awt.Dimension( 24, 24 ) );
			btnRemove.setSize( 24, 24 );
			btnRemove.setMinimumSize( new java.awt.Dimension( 24, 24 ) );

			add( Box.createHorizontalStrut( 5 ) );
			tfStr = new JTextField( str );
			tfStr.setFont( SMALL_FONT );
			add( tfStr );

			add( Box.createHorizontalStrut( 5 ) );
			final JButton btnBrowse = new JButton( "Browse" );
			btnBrowse.setFont( SMALL_FONT );
			btnBrowse.addActionListener( e -> browse() );
			add( btnBrowse );

			setMinimumSize( new java.awt.Dimension( 24, 24 ) );
			setPreferredSize( new java.awt.Dimension( 100, 24 ) );
			setMaximumSize( new java.awt.Dimension( 6000, 30 ) );
			setBorder( BorderFactory.createEmptyBorder( 2, 5, 2, 5 ) );

			// Listeners.
			fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( tfStr );
			tfStr.addActionListener( e -> refresher.run() );
			final FocusAdapter fa = new FocusAdapter()
			{
				@Override
				public void focusLost( final java.awt.event.FocusEvent e )
				{
					refresher.run();
				}
			};
			tfStr.addFocusListener( fa );
		}

		private void browse()
		{
			enabler.disable();
			try
			{
				final File file = FileChooser.chooseFile(
						this,
						tfStr.getText(),
						null,
						"Browse to a file",
						DialogType.LOAD,
						SelectionMode.FILES_AND_DIRECTORIES );
				if ( file != null )
				{
					tfStr.setText( file.getAbsolutePath() );
					refresher.run();
				}
			}
			finally
			{
				enabler.reenable();
			}
		}

	}

	public static void main( final String[] args )
	{
		final FileListModel model = new FileListModel();
		model.listeners().add( () -> new Thread()
		{
			@Override
			public void run()
			{
				System.out.println( model );
			}
		}.start() );
		final FileListPanel panel = new FileListPanel( model );
		final JFrame frame = new JFrame();
		frame.getContentPane().add( panel );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
