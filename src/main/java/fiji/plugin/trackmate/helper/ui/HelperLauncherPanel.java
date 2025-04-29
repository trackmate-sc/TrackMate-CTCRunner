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

import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.FocusAdapter;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.scijava.prefs.PrefService;
import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.ui.components.MetricsChooserPanel;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;
import fiji.plugin.trackmate.util.TMUtils;
import ij.ImagePlus;
import ij.WindowManager;

public class HelperLauncherPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	final JTextField tfImagePath;

	final JTextField tfGTPath;

	final JButton btnOK;

	final JButton btnCancel;

	final JComboBox< String > cmbboxImp;

	private MetricsChooserPanel metricsChooserPanel;

	public HelperLauncherPanel()
	{
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 24, 31, 24, 0, 0, 0, 0, 24, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final Image im = Icons.TRACKMATE_ICON.getImage();
		final Image newimg = im.getScaledInstance( 32, 32, java.awt.Image.SCALE_SMOOTH );
		final ImageIcon icon = new ImageIcon( newimg );

		final JLabel lblTitle = new JLabel( "TrackMate-Helper launcher", icon, JLabel.LEADING );
		lblTitle.setFont( BIG_FONT );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.gridwidth = 2;
		gbcLblTitle.insets = new Insets( 0, 0, 5, 0 );
		gbcLblTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		add( lblTitle, gbcLblTitle );

		final JLabel lblVersion = new JLabel( "v" + VersionUtils.getVersion( getClass() ) );
		lblVersion.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblVersion = new GridBagConstraints();
		gbcLblVersion.anchor = GridBagConstraints.WEST;
		gbcLblVersion.gridwidth = 2;
		gbcLblVersion.insets = new Insets( 0, 0, 5, 0 );
		gbcLblVersion.gridx = 0;
		gbcLblVersion.gridy = 1;
		add( lblVersion, gbcLblVersion );

		final GridBagConstraints gbcSeparator = new GridBagConstraints();
		gbcSeparator.gridwidth = 2;
		gbcSeparator.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator.fill = GridBagConstraints.BOTH;
		gbcSeparator.gridx = 0;
		gbcSeparator.gridy = 2;
		add( new JSeparator(), gbcSeparator );

		this.metricsChooserPanel = new MetricsChooserPanel();
		final GridBagConstraints gbcMetricsPanel = new GridBagConstraints();
		gbcMetricsPanel.gridwidth = 2;
		gbcMetricsPanel.insets = new Insets( 0, 0, 5, 0 );
		gbcMetricsPanel.fill = GridBagConstraints.BOTH;
		gbcMetricsPanel.gridx = 0;
		gbcMetricsPanel.gridy = 3;
		add( metricsChooserPanel, gbcMetricsPanel );

		final GridBagConstraints gbcSeparator1 = new GridBagConstraints();
		gbcSeparator1.gridwidth = 2;
		gbcSeparator1.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator1.fill = GridBagConstraints.BOTH;
		gbcSeparator1.gridx = 0;
		gbcSeparator1.gridy = 4;
		add( new JSeparator(), gbcSeparator1 );

		final JLabel lblPleaseSelectImage = new JLabel( "Please select an image to run the tracking on." );
		lblPleaseSelectImage.setFont( FONT );
		final GridBagConstraints gbcLblPleaseSelectImage = new GridBagConstraints();
		gbcLblPleaseSelectImage.gridwidth = 2;
		gbcLblPleaseSelectImage.insets = new Insets( 0, 0, 5, 0 );
		gbcLblPleaseSelectImage.anchor = GridBagConstraints.WEST;
		gbcLblPleaseSelectImage.gridx = 0;
		gbcLblPleaseSelectImage.gridy = 5;
		add( lblPleaseSelectImage, gbcLblPleaseSelectImage );

		final JLabel lblImagePath = new JLabel( "Path to the source image:" );
		lblImagePath.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblImagePath = new GridBagConstraints();
		gbcLblImagePath.gridwidth = 2;
		gbcLblImagePath.insets = new Insets( 0, 0, 5, 0 );
		gbcLblImagePath.anchor = GridBagConstraints.WEST;
		gbcLblImagePath.gridx = 0;
		gbcLblImagePath.gridy = 6;
		add( lblImagePath, gbcLblImagePath );

		tfImagePath = new JTextField();
		tfImagePath.setFont( SMALL_FONT );
		final GridBagConstraints gbcTfImagePath = new GridBagConstraints();
		gbcTfImagePath.insets = new Insets( 0, 0, 5, 5 );
		gbcTfImagePath.fill = GridBagConstraints.HORIZONTAL;
		gbcTfImagePath.gridx = 0;
		gbcTfImagePath.gridy = 7;
		add( tfImagePath, gbcTfImagePath );
		tfImagePath.setColumns( 10 );

		final JButton btnBrowseImage = new JButton( "Browse" );
		btnBrowseImage.setFont( SMALL_FONT );
		final GridBagConstraints gbcBtnBrowseImage = new GridBagConstraints();
		gbcBtnBrowseImage.insets = new Insets( 0, 0, 5, 0 );
		gbcBtnBrowseImage.gridx = 1;
		gbcBtnBrowseImage.gridy = 7;
		add( btnBrowseImage, gbcBtnBrowseImage );

		cmbboxImp = new JComboBox<>();
		cmbboxImp.setFont( SMALL_FONT );
		final GridBagConstraints gbcCmbboxImp = new GridBagConstraints();
		gbcCmbboxImp.insets = new Insets( 0, 0, 5, 0 );
		gbcCmbboxImp.gridwidth = 2;
		gbcCmbboxImp.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxImp.gridx = 0;
		gbcCmbboxImp.gridy = 8;
		add( cmbboxImp, gbcCmbboxImp );

		final GridBagConstraints gbcSeparator3 = new GridBagConstraints();
		gbcSeparator3.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator3.gridwidth = 2;
		gbcSeparator3.fill = GridBagConstraints.BOTH;
		gbcSeparator3.gridx = 0;
		gbcSeparator3.gridy = 9;
		add( new JSeparator(), gbcSeparator3 );

		final JLabel lblBrowseGroundTruth = new JLabel( "Please browse to the ground truth file or folder:" );
		lblBrowseGroundTruth.setFont( FONT );
		final GridBagConstraints gbcLblBrowseGroundTruth = new GridBagConstraints();
		gbcLblBrowseGroundTruth.insets = new Insets( 0, 0, 5, 0 );
		gbcLblBrowseGroundTruth.anchor = GridBagConstraints.WEST;
		gbcLblBrowseGroundTruth.gridwidth = 2;
		gbcLblBrowseGroundTruth.gridx = 0;
		gbcLblBrowseGroundTruth.gridy = 10;
		add( lblBrowseGroundTruth, gbcLblBrowseGroundTruth );

		tfGTPath = new JTextField();
		tfGTPath.setFont( SMALL_FONT );
		final GridBagConstraints gbcTfGTPath = new GridBagConstraints();
		gbcTfGTPath.insets = new Insets( 0, 0, 5, 5 );
		gbcTfGTPath.fill = GridBagConstraints.HORIZONTAL;
		gbcTfGTPath.gridx = 0;
		gbcTfGTPath.gridy = 11;
		add( tfGTPath, gbcTfGTPath );
		tfGTPath.setColumns( 10 );

		final JButton btnBrowseGT = new JButton( "Browse" );
		btnBrowseGT.setFont( SMALL_FONT );
		final GridBagConstraints gbcBtnBrowseGT = new GridBagConstraints();
		gbcBtnBrowseGT.insets = new Insets( 0, 0, 5, 0 );
		gbcBtnBrowseGT.gridx = 1;
		gbcBtnBrowseGT.gridy = 11;
		add( btnBrowseGT, gbcBtnBrowseGT );

		final JPanel panelButtons = new JPanel();
		final GridBagConstraints gbcPanelButtons = new GridBagConstraints();
		gbcPanelButtons.anchor = GridBagConstraints.SOUTHEAST;
		gbcPanelButtons.gridwidth = 2;
		gbcPanelButtons.gridx = 0;
		gbcPanelButtons.gridy = 12;
		add( panelButtons, gbcPanelButtons );

		btnCancel = new JButton( "Cancel" );
		btnCancel.setFont( SMALL_FONT );
		panelButtons.add( btnCancel );

		btnOK = new JButton( "OK" );
		btnOK.setFont( SMALL_FONT );
		panelButtons.add( btnOK );

		final PrefService prefService = TMUtils.getContext().getService( PrefService.class );

		/*
		 * ImagePlus or path to ImagePlus.
		 */

		// Do we have an image opened?
		final boolean impOpen = WindowManager.getImageCount() > 0;
		if ( impOpen )
		{
			final int[] windowIDs = WindowManager.getIDList();
			final String[] windowTitles = new String[ windowIDs.length ];
			for ( int i = 0; i < windowIDs.length; i++ )
			{
				final ImagePlus image = WindowManager.getImage( windowIDs[ i ] );
				windowTitles[ i ] = image == null ? "unknwon_" + i : image.getTitle();
			}
			cmbboxImp.setModel( new DefaultComboBoxModel<>( windowTitles ) );
		}
		cmbboxImp.setVisible( impOpen );
		lblImagePath.setVisible( !impOpen );
		tfImagePath.setVisible( !impOpen );
		btnBrowseImage.setVisible( !impOpen );

		/*
		 * Listeners & co.
		 */

		fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( tfImagePath );
		fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( tfGTPath );

		final Runnable storeGtPath = () -> prefService.put( getClass(), GT_PATH_KEY, tfGTPath.getText() );
		final Runnable storeImagePath = () -> prefService.put( getClass(), IMAGE_PATH_KEY, tfImagePath.getText() );

		tfImagePath.addActionListener( e -> storeImagePath.run() );
		final FocusAdapter faIm = new FocusAdapter()
		{
			@Override
			public void focusLost( final java.awt.event.FocusEvent e )
			{
				storeImagePath.run();
			}
		};
		tfImagePath.addFocusListener( faIm );

		tfGTPath.addActionListener( e -> storeGtPath.run() );
		final FocusAdapter faGt = new FocusAdapter()
		{
			@Override
			public void focusLost( final java.awt.event.FocusEvent e )
			{
				storeGtPath.run();
			}
		};
		tfGTPath.addFocusListener( faGt );

		btnBrowseImage.addActionListener( e -> {
			final File file = FileChooser.chooseFile( this, tfImagePath.getText(), null,
					"Select an image file", DialogType.LOAD, SelectionMode.FILES_ONLY );
			if ( file == null )
				return;

			tfImagePath.setText( file.getAbsolutePath() );
			storeImagePath.run();
		} );

		btnBrowseGT.addActionListener( e -> {
			final String dialogTitle = metricsChooserPanel.isCTCSelected()
					? "Select a CTC ground-truth folder or TrackMate file."
					: "Select a SPT ground-truth XML file.";
			final SelectionMode selectionMode = metricsChooserPanel.isCTCSelected()
					? SelectionMode.FILES_AND_DIRECTORIES
					: SelectionMode.FILES_ONLY;
			final File file = FileChooser.chooseFile( this, tfGTPath.getText(), null, dialogTitle, DialogType.LOAD, selectionMode );
			if ( file == null )
				return;

			tfGTPath.setText( file.getAbsolutePath() );
			storeGtPath.run();
		} );

		tfImagePath.setDropTarget( new SetFileDropTarget( tfImagePath, storeImagePath ) );
		tfGTPath.setDropTarget( new SetFileDropTarget( tfGTPath, storeGtPath ) );

		/*
		 * Default values.
		 */

		final String lastUsedImagePathFolder = prefService.get( getClass(), IMAGE_PATH_KEY, System.getProperty( "user.home" ) );
		tfImagePath.setText( lastUsedImagePathFolder );

		final String lastUsedGtPath = prefService.get( getClass(), GT_PATH_KEY, System.getProperty( "user.home" ) );
		tfGTPath.setText( lastUsedGtPath );
	}

	/**
	 * Returns <code>true</code> if the CTC metrics are selected. If
	 * <code>false</code>, the SPT metrics are selected.
	 *
	 * @return <code>true</code> if the CTC metrics are selected
	 */
	public boolean isCTCSelected()
	{
		return metricsChooserPanel.isCTCSelected();
	}

	static class SetFileDropTarget extends DropTarget
	{

		private final JTextField tf;

		private final Runnable store;

		public SetFileDropTarget( final JTextField tf, final Runnable store )
		{
			this.tf = tf;
			this.store = store;
		}

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
					tf.setText( list.get( 0 ) );
					store.run();
				}
			}
			catch ( final Exception ex )
			{
				ex.printStackTrace();
			}
		}
	}

	static final String GT_PATH_KEY = "GT_FOLDER";

	private static final String IMAGE_PATH_KEY = "IMAGE_PATH";

	public double getSPTMaxPairingDistance()
	{
		return metricsChooserPanel.getSPTMaxPairingDistance();
	}


}
