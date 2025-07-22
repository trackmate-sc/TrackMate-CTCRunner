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
import static fiji.plugin.trackmate.gui.Icons.BIN_CLOSED_ICON;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.FocusAdapter;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
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

	private final EverythingDisablerAndReenabler enabler;

	private final JPanel mainPanel;

	private final JPanel buttonPanel;

	private final FileListModel model;

	public FileListPanel( final FileListModel model )
	{
		this.model = model;
		model.listeners().add( this::refresh );
		this.enabler = new EverythingDisablerAndReenabler( this, new Class[] { JLabel.class } );
		setLayout( new BorderLayout( 0, 0 ) );

		final JScrollPane scrollPane = new JScrollPane();
		this.add( scrollPane );
		scrollPane.setPreferredSize( new java.awt.Dimension( 250, 389 ) );
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setBorder( null );
		scrollPane.getVerticalScrollBar().setUnitIncrement( 16 );

		mainPanel = new JPanel( new GridBagLayout() );
		scrollPane.setViewportView( mainPanel );

		final JButton btnAdd = new JButton();
		btnAdd.setIcon( ADD_ICON );
		btnAdd.setPreferredSize( new java.awt.Dimension( 24, 24 ) );
		btnAdd.setSize( 24, 24 );
		btnAdd.setMinimumSize( new java.awt.Dimension( 24, 24 ) );
		buttonPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		buttonPanel.add( btnAdd );

		/*
		 * Listeners & co.
		 */

		btnAdd.addActionListener( e -> model.add( System.getProperty( "user.home" ) ) );
		setDropTarget( new AddFilesDropTarget() );
		refresh();
	}

	/**
	 * Refreshes the panel when the model changes.
	 */
	private void refresh()
	{
		mainPanel.removeAll();

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		mainPanel.add( buttonPanel, gbc );

		final List< String > list = model.getList();
		for ( int i = 0; i < list.size(); i++ )
		{
			final String str = list.get( i );
			final int index = i;
			final FileBoxPanel panel = new FileBoxPanel( str, ( s ) -> model.set( index, s ) );
			panel.btnRemove.addActionListener( e -> model.remove( index ) );
			if ( list.size() > 1 )
				panel.btnRemove.setVisible( true );
			else if ( list.size() == 1 && i == 0 )
				panel.btnRemove.setVisible( false );

			gbc.gridy++;
			mainPanel.add( Box.createVerticalStrut( 5 ), gbc );
			gbc.gridy++;
			mainPanel.add( panel, gbc );
		}

		gbc.gridy++;
		gbc.weighty = 1.0;
		mainPanel.add( Box.createVerticalGlue(), gbc );

		mainPanel.revalidate();
		mainPanel.repaint();
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
				model.addAll( list );
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

		private final Consumer< String > refresher;

		public FileBoxPanel( final String str, final Consumer< String > refresher )
		{
			this.refresher = refresher;

			setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );
			btnRemove = new JButton( BIN_CLOSED_ICON );
			final int w = 30;
			btnRemove.setPreferredSize( new java.awt.Dimension( w, w ) );
			btnRemove.setSize( w, w );
			btnRemove.setMinimumSize( new java.awt.Dimension( w, w ) );
			btnRemove.setContentAreaFilled( false );
			btnRemove.setBorderPainted( false );
			btnRemove.setFocusPainted( false );
			btnRemove.setOpaque( false );
			add( btnRemove );

			add( Box.createHorizontalStrut( 5 ) );
			tfStr = new JTextField( str );
			tfStr.setFont( SMALL_FONT );
			add( tfStr );

			add( Box.createHorizontalStrut( 5 ) );
			final JButton btnBrowse = new JButton( "Browse" );
			btnBrowse.setFont( SMALL_FONT );
			btnBrowse.addActionListener( e -> browse() );
			add( btnBrowse );

			setMinimumSize( new java.awt.Dimension( w, w ) );
			setPreferredSize( new java.awt.Dimension( 100, w ) );
			setMaximumSize( new java.awt.Dimension( 6000, w ) );
			setBorder( BorderFactory.createEmptyBorder( 2, 5, 2, 5 ) );

			// Listeners.
			fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( tfStr );

			tfStr.addActionListener( e -> refresher.accept( tfStr.getText() ) );
			final FocusAdapter fa = new FocusAdapter()
			{
				@Override
				public void focusLost( final java.awt.event.FocusEvent e )
				{
					refresher.accept( tfStr.getText() );
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
					refresher.accept( tfStr.getText() );
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
