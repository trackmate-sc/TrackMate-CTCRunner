
package fiji.plugin.trackmate.batcher.ui;

import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.batcher.RunParamModel;
import fiji.plugin.trackmate.batcher.RunParamModel.RunParamListener;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;

public class RunBatchPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final JTextField tfOutputPath;

	public RunBatchPanel( final RunParamModel model )
	{
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

		final JCheckBox chckbxTrackMateFile = new JCheckBox( "TrackMate file." );
		chckbxTrackMateFile.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxTrackMateFile = new GridBagConstraints();
		gbcChckbxTrackMateFile.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxTrackMateFile.anchor = GridBagConstraints.WEST;
		gbcChckbxTrackMateFile.gridx = 0;
		gbcChckbxTrackMateFile.gridy = 4;
		add( chckbxTrackMateFile, gbcChckbxTrackMateFile );

		final JCheckBox chckbxSpotTable = new JCheckBox( "Spot table (CSV)." );
		chckbxSpotTable.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxSpotTable = new GridBagConstraints();
		gbcChckbxSpotTable.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxSpotTable.anchor = GridBagConstraints.WEST;
		gbcChckbxSpotTable.gridx = 0;
		gbcChckbxSpotTable.gridy = 5;
		add( chckbxSpotTable, gbcChckbxSpotTable );

		final JCheckBox chckbxEdgeTable = new JCheckBox( "Edge table (CSV)." );
		chckbxEdgeTable.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxEdgeTable = new GridBagConstraints();
		gbcChckbxEdgeTable.anchor = GridBagConstraints.WEST;
		gbcChckbxEdgeTable.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxEdgeTable.gridx = 0;
		gbcChckbxEdgeTable.gridy = 6;
		add( chckbxEdgeTable, gbcChckbxEdgeTable );

		final JCheckBox chckbxTrackTable = new JCheckBox( "Track table (CSV)." );
		chckbxTrackTable.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxTrackTable = new GridBagConstraints();
		gbcChckbxTrackTable.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxTrackTable.anchor = GridBagConstraints.WEST;
		gbcChckbxTrackTable.gridx = 0;
		gbcChckbxTrackTable.gridy = 7;
		add( chckbxTrackTable, gbcChckbxTrackTable );

		final JCheckBox chckbxTables = new JCheckBox( "The 3 tables (XLSX)." );
		chckbxTables.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxNewCheckBox = new GridBagConstraints();
		gbcChckbxNewCheckBox.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbcChckbxNewCheckBox.gridx = 0;
		gbcChckbxNewCheckBox.gridy = 8;
		add( chckbxTables, gbcChckbxNewCheckBox );

		final JPanel panel = new JPanel();
		final GridBagConstraints gbcPanel = new GridBagConstraints();
		gbcPanel.anchor = GridBagConstraints.NORTH;
		gbcPanel.insets = new Insets( 0, 0, 5, 0 );
		gbcPanel.fill = GridBagConstraints.HORIZONTAL;
		gbcPanel.gridx = 0;
		gbcPanel.gridy = 9;
		add( panel, gbcPanel );
		panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );

		final JCheckBox chckbxMovie = new JCheckBox( "Movie (uncompressed AVI)." );
		panel.add( chckbxMovie );
		chckbxMovie.setFont( SMALL_FONT );

		final Component horizontalGlue = Box.createHorizontalGlue();
		panel.add( horizontalGlue );

		final SpinnerNumberModel spinnerModel = new SpinnerNumberModel( 10, 1, 300, 1 );
		final JSpinner spinnerFPS = new JSpinner( spinnerModel );
		spinnerFPS.setMaximumSize( new Dimension( 80, 20 ) );
		spinnerFPS.setFont( SMALL_FONT );
		panel.add( spinnerFPS );

		final JLabel lblFps = new JLabel( "fps" );
		lblFps.setFont( SMALL_FONT );
		panel.add( lblFps );

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

		chckbxTrackMateFile.addActionListener( e -> model.setExportTrackMateFile( chckbxTrackMateFile.isSelected() ) );
		chckbxSpotTable.addActionListener( e -> model.setExportSpotTable( chckbxSpotTable.isSelected() ) );
		chckbxEdgeTable.addActionListener( e -> model.setExportEdgeTable( chckbxEdgeTable.isSelected() ) );
		chckbxTrackTable.addActionListener( e -> model.setExportTrackTable( chckbxTrackTable.isSelected() ) );
		chckbxTables.addActionListener( e -> model.setExportAllTables( chckbxTables.isSelected() ) );
		chckbxMovie.addActionListener( e -> model.setExportAVIMovie( chckbxMovie.isSelected() ) );
		spinnerFPS.addChangeListener( e -> model.setMovieFps( ( ( Number ) spinnerModel.getValue() ).intValue() ) );

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
			chckbxTrackMateFile.setSelected( model.isExportTrackMateFile() );
			chckbxSpotTable.setSelected( model.isExportSpotTable() );
			chckbxEdgeTable.setSelected( model.isExportEdgeTable() );
			chckbxTrackTable.setSelected( model.isExportTrackTable() );
			chckbxTables.setSelected( model.isExportAllTables() );
			chckbxMovie.setSelected( model.isExportAVIMovie() );
			spinnerModel.setValue( Integer.valueOf( model.getMovieFps() ) );
		};
		model.listeners().add( l );
		l.runParamChanged();
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
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
