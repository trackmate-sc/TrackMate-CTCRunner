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

import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.batcher.RunParamModel;
import fiji.plugin.trackmate.batcher.RunParamModel.RunParamListener;
import fiji.plugin.trackmate.batcher.exporter.ExporterParam;
import fiji.plugin.trackmate.gui.displaysettings.StyleElements;
import fiji.plugin.trackmate.gui.displaysettings.StyleElements.BooleanElement;
import fiji.plugin.trackmate.gui.displaysettings.StyleElements.DoubleElement;
import fiji.plugin.trackmate.gui.displaysettings.StyleElements.IntElement;
import fiji.plugin.trackmate.gui.displaysettings.StyleElements.StyleElement;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;
import net.imagej.ImageJ;

public class RunBatchPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final JTextField tfOutputPath;

	private final RunParamModel model;

	public RunBatchPanel( final RunParamModel model )
	{
		this.model = model;
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblOutputLocation = new JLabel( "Save output:" );
		lblOutputLocation.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblOutputLocation = new GridBagConstraints();
		gbcLblOutputLocation.insets = new Insets( 0, 0, 5, 0 );
		gbcLblOutputLocation.fill = GridBagConstraints.HORIZONTAL;
		gbcLblOutputLocation.gridx = 0;
		gbcLblOutputLocation.gridy = 0;
		add( lblOutputLocation, gbcLblOutputLocation );

		final JRadioButton rdbtnSame = new JRadioButton( "In input image folder." );
		rdbtnSame.setFont( SMALL_FONT );
		final GridBagConstraints gbcRdbtnSame = new GridBagConstraints();
		gbcRdbtnSame.insets = new Insets( 0, 0, 5, 0 );
		gbcRdbtnSame.anchor = GridBagConstraints.WEST;
		gbcRdbtnSame.gridx = 0;
		gbcRdbtnSame.gridy = 1;
		add( rdbtnSame, gbcRdbtnSame );

		final JPanel panelTo = new JPanel();
		final GridBagConstraints gbcPanelTo = new GridBagConstraints();
		gbcPanelTo.insets = new Insets( 0, 0, 5, 0 );
		gbcPanelTo.fill = GridBagConstraints.BOTH;
		gbcPanelTo.gridx = 0;
		gbcPanelTo.gridy = 2;
		add( panelTo, gbcPanelTo );
		panelTo.setLayout( new BoxLayout( panelTo, BoxLayout.X_AXIS ) );

		final JRadioButton rdbtnTo = new JRadioButton( "To:" );
		rdbtnTo.setFont( SMALL_FONT );
		panelTo.add( rdbtnTo );

		tfOutputPath = new JTextField();
		tfOutputPath.setColumns( 10 );
		tfOutputPath.setFont( SMALL_FONT );
		panelTo.add( Box.createHorizontalStrut( 5 ) );
		panelTo.add( tfOutputPath );

		final JButton btnBrowse = new JButton( "Browse" );
		btnBrowse.setFont( SMALL_FONT );
		panelTo.add( Box.createHorizontalStrut( 5 ) );
		panelTo.add( btnBrowse );

		final JLabel lblExport = new JLabel( "Export:" );
		lblExport.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblExport = new GridBagConstraints();
		gbcLblExport.insets = new Insets( 0, 0, 5, 0 );
		gbcLblExport.anchor = GridBagConstraints.WEST;
		gbcLblExport.gridx = 0;
		gbcLblExport.gridy = 3;
		add( lblExport, gbcLblExport );

		/*
		 * 'Automatic' UI generation for the exporters.
		 */

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets( 0, 0, 5, 0 );
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 4;

		final Set< String > exporterKeys = model.getExporterKeys();
		for ( final String exporterKey : exporterKeys )
		{
			final List< String > exportables = model.getExportables( exporterKey );
			final List< JCheckBox > chkboxes = new ArrayList<>();
			for ( final String exportable : exportables )
			{
				final JPanel panel = new JPanel();
				panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );

				final BooleanElement bel = StyleElements.booleanElement( exportable,
						() -> model.isExportActive( exportable ),
						b -> model.setExportActive( exportable, b ) );
				final JCheckBox chkbox = StyleElements.linkedCheckBox( bel, exportable );
				chkboxes.add( chkbox );
				chkbox.setFont( SMALL_FONT );
				panel.add( chkbox );
				panel.add( Box.createHorizontalGlue() );

				add( panel, gbc );
				gbc.gridy++;
			}
			final List< ExporterParam > params = model.getExporterExtraParameters( exporterKey );
			if ( params != null && !params.isEmpty() )
			{
				final JPanel panel = new JPanel();
				panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
				panel.add( Box.createHorizontalGlue() );
				for ( final ExporterParam param : params )
				{

					final JComponent component;
					final StyleElement el = param.element( () -> model.notifyListeners() );
					if ( IntElement.class.isAssignableFrom( el.getClass() ) )
					{
						component = StyleElements.linkedSpinner( ( IntElement ) el );
					}
					else if ( DoubleElement.class.isAssignableFrom( el.getClass() ) )
					{
						component = StyleElements.linkedFormattedTextField( ( DoubleElement ) el );
					}
					else
					{
						throw new IllegalArgumentException( "Do not know how to handle extra parameters of type " + el );
					}
					component.setFont( SMALL_FONT );
					final JLabel lbl = new JLabel( param.name() + ":" );
					lbl.setFont( SMALL_FONT );
					panel.add( Box.createHorizontalStrut( 5 ) );
					panel.add( lbl );
					panel.add( component );
				}
				add( panel, gbc );
				gbc.gridy++;

				/*
				 * Listen to all checkboxes being unselected to disable the
				 * panel.
				 */
				final ActionListener al = e -> {
					boolean enabled = false;
					for ( final JCheckBox chkbox : chkboxes )
					{
						if ( chkbox.isSelected() )
						{
							enabled = true;
							break;
						}
					}
					setPanelEnabled( panel, enabled );
				};
				al.actionPerformed( null );
				for ( final JCheckBox chkbox : chkboxes )
					chkbox.addActionListener( al );

			}
		}

		/*
		 * Listeners and co.
		 */

		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( rdbtnTo );
		buttonGroup.add( rdbtnSame );
		// Radio buttons listener.
		final ItemListener il = new ItemListener()
		{

			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				// Only fire once for the one who gets selected.
				if ( e.getStateChange() == ItemEvent.SELECTED )
				{
					tfOutputPath.setEnabled( rdbtnTo.isSelected() );
					btnBrowse.setEnabled( rdbtnTo.isSelected() );
					model.setSaveToInputFolder( !rdbtnTo.isSelected() );
				}
			}
		};
		rdbtnSame.addItemListener( il );
		rdbtnTo.addItemListener( il );

		tfOutputPath.addActionListener( e -> model.setOutputFolderPath( tfOutputPath.getText() ) );
		final FocusAdapter fa = new FocusAdapter()
		{
			@Override
			public void focusLost( final java.awt.event.FocusEvent e )
			{
				model.setOutputFolderPath( tfOutputPath.getText() );
			}
		};
		tfOutputPath.addFocusListener( fa );

		final EverythingDisablerAndReenabler enabler = new EverythingDisablerAndReenabler( this, new Class[] { JLabel.class } );
		btnBrowse.addActionListener( e -> {
			enabler.disable();
			try
			{
				final File file = FileChooser.chooseFile(
						this,
						tfOutputPath.getText(),
						null,
						"Browse to a folder",
						DialogType.SAVE,
						SelectionMode.DIRECTORIES_ONLY );
				if ( file != null )
				{
					tfOutputPath.setText( file.getAbsolutePath() );
					model.setOutputFolderPath( file.getAbsolutePath() );
				}
			}
			finally
			{
				enabler.reenable();
			}
		} );

		final RunParamListener l = () -> {
			rdbtnSame.setSelected( model.isSaveToInputFolder() );
			rdbtnTo.setSelected( !model.isSaveToInputFolder() );
			tfOutputPath.setText( model.getOutputFolderPath() );
		};
		model.listeners().add( l );
		l.runParamChanged();

		// DnD.
		tfOutputPath.setDropTarget( new AddFilesDropTarget() );
		setDropTarget( new AddFilesDropTarget() );
	}

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
				if ( !list.isEmpty() )
				{
					tfOutputPath.setText( list.get( 0 ) );
					model.setOutputFolderPath( tfOutputPath.getText() );
				}
			}
			catch ( final Exception ex )
			{
				ex.printStackTrace();
			}
		}
	}

	private static void setPanelEnabled( final JPanel panel, final boolean isEnabled )
	{
		panel.setEnabled( isEnabled );
		final Component[] components = panel.getComponents();
		for ( final Component component : components )
		{
			if ( component instanceof JPanel )
				setPanelEnabled( ( JPanel ) component, isEnabled );
			component.setEnabled( isEnabled );
		}
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );

		final RunParamModel model = new RunParamModel();
		model.listeners().add( () -> System.out.println( model ) );

		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final JPanel panel = new RunBatchPanel( model );
		final JFrame frame = new JFrame();
		frame.getContentPane().add( panel );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
