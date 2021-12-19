package fiji.plugin.trackmate.ctc.ui;

import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.gui.Icons.CANCEL_ICON;
import static fiji.plugin.trackmate.gui.Icons.EXECUTE_ICON;
import static fiji.plugin.trackmate.gui.Icons.TRACKMATE_ICON_16x16;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.itextpdf.text.Font;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.features.track.TrackBranchingAnalyzer;
import fiji.plugin.trackmate.gui.components.LogPanel;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;
import ij.ImagePlus;

public class ParameterSweepPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final JTextField tfGroundTruth;

	private final JTabbedPane tabbedPane;

	private final EverythingDisablerAndReenabler enabler;

	public ParameterSweepPanel( final ImagePlus imp )
	{
		enabler = new EverythingDisablerAndReenabler( this, new Class[] { JLabel.class } );

		setLayout( new BorderLayout( 5, 5 ) );

		/*
		 * Tabbed pane.
		 */

		this.tabbedPane = new JTabbedPane( JTabbedPane.TOP );
		tabbedPane.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		add( tabbedPane, BorderLayout.CENTER );

		final LogPanel panelLog = new LogPanel();
		tabbedPane.addTab( "Log", null, panelLog, null );

		final List< FeatureFilter > spotFilters = new ArrayList<>(); // TODO
		final FilterConfigPanel panelSpotFilters = new FilterConfigPanel( TrackMateObject.SPOTS, spotFilters, Spot.QUALITY, imp );
		tabbedPane.addTab( "Spot filters", null, panelSpotFilters, null );

		final List< FeatureFilter > trackFilters = new ArrayList<>(); // TODO
		final FilterConfigPanel panelTrackFilters = new FilterConfigPanel( TrackMateObject.TRACKS, trackFilters, TrackBranchingAnalyzer.NUMBER_SPOTS, imp );
		tabbedPane.addTab( "Track filters", null, panelTrackFilters, null );

		/*
		 * Top panel.
		 */

		final JPanel topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout( 5, 5 ) );
		add( topPanel, BorderLayout.NORTH );

		/*
		 * Title panel.
		 */

		final JPanel panelTitle = new JPanel();
		panelTitle.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		topPanel.add( panelTitle, BorderLayout.NORTH );
		final GridBagLayout gblPanelTitle = new GridBagLayout();
		gblPanelTitle.columnWidths = new int[] { 137, 0 };
		gblPanelTitle.rowHeights = new int[] { 14, 0, 0, 0 };
		gblPanelTitle.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gblPanelTitle.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		panelTitle.setLayout( gblPanelTitle );

		final JLabel lblTitle = new JLabel( "TrackMate parameter sweep" );
		lblTitle.setIcon( TRACKMATE_ICON_16x16 );
		lblTitle.setFont( BIG_FONT );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.insets = new Insets( 0, 0, 5, 0 );
		gbcLblTitle.fill = GridBagConstraints.VERTICAL;
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		panelTitle.add( lblTitle, gbcLblTitle );

		final JLabel lblDoc = new JLabel( "Doc" );
		lblDoc.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblDoc = new GridBagConstraints();
		gbcLblDoc.insets = new Insets( 0, 0, 5, 0 );
		gbcLblDoc.fill = GridBagConstraints.BOTH;
		gbcLblDoc.gridx = 0;
		gbcLblDoc.gridy = 1;
		panelTitle.add( lblDoc, gbcLblDoc );

		final GridBagConstraints gbcSeparator = new GridBagConstraints();
		gbcSeparator.fill = GridBagConstraints.BOTH;
		gbcSeparator.gridx = 0;
		gbcSeparator.gridy = 2;
		panelTitle.add( new JSeparator(), gbcSeparator );

		/*
		 * Checkbox panel. Select detectors and tracker to include in the sweep.
		 */

		final JPanel panelChkboxes = new JPanel();
		panelChkboxes.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gblPanelChkboxes = new GridBagLayout();
		gblPanelChkboxes.columnWidths = new int[] { 0, 0, 0 };
		gblPanelChkboxes.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblPanelChkboxes.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gblPanelChkboxes.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelChkboxes.setLayout( gblPanelChkboxes );

		final JLabel lblDetectors = new JLabel( "Detectors" );
		lblDetectors.setFont( FONT.deriveFont( Font.BOLD ) );
		final GridBagConstraints gbcLblDetectors = new GridBagConstraints();
		gbcLblDetectors.anchor = GridBagConstraints.WEST;
		gbcLblDetectors.insets = new Insets( 0, 0, 5, 5 );
		gbcLblDetectors.gridx = 0;
		gbcLblDetectors.gridy = 0;
		panelChkboxes.add( lblDetectors, gbcLblDetectors );

		final JLabel lblTrackers = new JLabel( "Trackers" );
		lblTrackers.setFont( FONT.deriveFont( Font.BOLD ) );
		final GridBagConstraints gbcLblTrackers = new GridBagConstraints();
		gbcLblTrackers.anchor = GridBagConstraints.WEST;
		gbcLblTrackers.insets = new Insets( 0, 0, 5, 0 );
		gbcLblTrackers.gridx = 1;
		gbcLblTrackers.gridy = 0;
		panelChkboxes.add( lblTrackers, gbcLblTrackers );

		final JCheckBox chckbxLoGDetector = new JCheckBox( "LoG detector" );
		chckbxLoGDetector.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxLoGDetector = new GridBagConstraints();
		gbcChckbxLoGDetector.anchor = GridBagConstraints.WEST;
		gbcChckbxLoGDetector.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxLoGDetector.gridx = 0;
		gbcChckbxLoGDetector.gridy = 1;
		panelChkboxes.add( chckbxLoGDetector, gbcChckbxLoGDetector );

		final JCheckBox chckbxSimpleLAPTracker = new JCheckBox( "Simple LAP tracker" );
		chckbxSimpleLAPTracker.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxSimpleLAPTracker = new GridBagConstraints();
		gbcChckbxSimpleLAPTracker.anchor = GridBagConstraints.WEST;
		gbcChckbxSimpleLAPTracker.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxSimpleLAPTracker.gridx = 1;
		gbcChckbxSimpleLAPTracker.gridy = 1;
		panelChkboxes.add( chckbxSimpleLAPTracker, gbcChckbxSimpleLAPTracker );

		final JCheckBox chckbxDoGDetector = new JCheckBox( "DoG  detector" );
		chckbxDoGDetector.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxDoGDetector = new GridBagConstraints();
		gbcChckbxDoGDetector.anchor = GridBagConstraints.WEST;
		gbcChckbxDoGDetector.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxDoGDetector.gridx = 0;
		gbcChckbxDoGDetector.gridy = 2;
		panelChkboxes.add( chckbxDoGDetector, gbcChckbxDoGDetector );

		final JCheckBox chckbxLAPTracker = new JCheckBox( "LAP tracker" );
		chckbxLAPTracker.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxLAPTracker = new GridBagConstraints();
		gbcChckbxLAPTracker.anchor = GridBagConstraints.WEST;
		gbcChckbxLAPTracker.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxLAPTracker.gridx = 1;
		gbcChckbxLAPTracker.gridy = 2;
		panelChkboxes.add( chckbxLAPTracker, gbcChckbxLAPTracker );

		final JCheckBox chckbxMaskDetector = new JCheckBox( "Mask detector" );
		chckbxMaskDetector.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxMaskDetector = new GridBagConstraints();
		gbcChckbxMaskDetector.anchor = GridBagConstraints.WEST;
		gbcChckbxMaskDetector.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxMaskDetector.gridx = 0;
		gbcChckbxMaskDetector.gridy = 3;
		panelChkboxes.add( chckbxMaskDetector, gbcChckbxMaskDetector );

		final JCheckBox chckbxKalmanTracker = new JCheckBox( "Kalman tracker" );
		chckbxKalmanTracker.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxKalmanTracker = new GridBagConstraints();
		gbcChckbxKalmanTracker.anchor = GridBagConstraints.WEST;
		gbcChckbxKalmanTracker.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxKalmanTracker.gridx = 1;
		gbcChckbxKalmanTracker.gridy = 3;
		panelChkboxes.add( chckbxKalmanTracker, gbcChckbxKalmanTracker );

		final JCheckBox chckbxThresholdDetector = new JCheckBox( "Threshold detector" );
		chckbxThresholdDetector.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxThresholdDetector = new GridBagConstraints();
		gbcChckbxThresholdDetector.anchor = GridBagConstraints.WEST;
		gbcChckbxThresholdDetector.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxThresholdDetector.gridx = 0;
		gbcChckbxThresholdDetector.gridy = 4;
		panelChkboxes.add( chckbxThresholdDetector, gbcChckbxThresholdDetector );

		final JCheckBox chckbxOverlapTracker = new JCheckBox( "Overlap tracker" );
		chckbxOverlapTracker.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxOverlapTracker = new GridBagConstraints();
		gbcChckbxOverlapTracker.anchor = GridBagConstraints.WEST;
		gbcChckbxOverlapTracker.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxOverlapTracker.gridx = 1;
		gbcChckbxOverlapTracker.gridy = 4;
		panelChkboxes.add( chckbxOverlapTracker, gbcChckbxOverlapTracker );

		final JCheckBox chckbxLabelImgDetector = new JCheckBox( "Label image detector" );
		chckbxLabelImgDetector.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxLabelImgDetector = new GridBagConstraints();
		gbcChckbxLabelImgDetector.anchor = GridBagConstraints.WEST;
		gbcChckbxLabelImgDetector.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxLabelImgDetector.gridx = 0;
		gbcChckbxLabelImgDetector.gridy = 5;
		panelChkboxes.add( chckbxLabelImgDetector, gbcChckbxLabelImgDetector );

		final JCheckBox chckbxNNTracker = new JCheckBox( "Nearest-Neighbor tracker" );
		chckbxNNTracker.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxNNTracker = new GridBagConstraints();
		gbcChckbxNNTracker.anchor = GridBagConstraints.WEST;
		gbcChckbxNNTracker.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxNNTracker.gridx = 1;
		gbcChckbxNNTracker.gridy = 5;
		panelChkboxes.add( chckbxNNTracker, gbcChckbxNNTracker );

		final JCheckBox chckbxMorphoLibJDetector = new JCheckBox( "MorphoLibJ detector" );
		chckbxMorphoLibJDetector.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxMorphoLibJDetector = new GridBagConstraints();
		gbcChckbxMorphoLibJDetector.anchor = GridBagConstraints.WEST;
		gbcChckbxMorphoLibJDetector.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxMorphoLibJDetector.gridx = 0;
		gbcChckbxMorphoLibJDetector.gridy = 6;
		panelChkboxes.add( chckbxMorphoLibJDetector, gbcChckbxMorphoLibJDetector );

		final GridBagConstraints gbcSeparator6 = new GridBagConstraints();
		gbcSeparator6.anchor = GridBagConstraints.NORTH;
		gbcSeparator6.fill = GridBagConstraints.HORIZONTAL;
		gbcSeparator6.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator6.gridx = 1;
		gbcSeparator6.gridy = 6;
		panelChkboxes.add( new JSeparator(), gbcSeparator6 );

		final JCheckBox chckbxWekaDetector = new JCheckBox( "Weka detector" );
		chckbxWekaDetector.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxWekaDetector = new GridBagConstraints();
		gbcChckbxWekaDetector.anchor = GridBagConstraints.WEST;
		gbcChckbxWekaDetector.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxWekaDetector.gridx = 0;
		gbcChckbxWekaDetector.gridy = 7;
		panelChkboxes.add( chckbxWekaDetector, gbcChckbxWekaDetector );

		final JCheckBox chckbxIlastikDetector = new JCheckBox( "Ilastik detector" );
		chckbxIlastikDetector.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxIlastikDetector = new GridBagConstraints();
		gbcChckbxIlastikDetector.anchor = GridBagConstraints.WEST;
		gbcChckbxIlastikDetector.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxIlastikDetector.gridx = 0;
		gbcChckbxIlastikDetector.gridy = 8;
		panelChkboxes.add( chckbxIlastikDetector, gbcChckbxIlastikDetector );

		final JCheckBox chckbxStarDistDetector = new JCheckBox( "StarDist detector" );
		chckbxStarDistDetector.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxStarDistDetector = new GridBagConstraints();
		gbcChckbxStarDistDetector.anchor = GridBagConstraints.WEST;
		gbcChckbxStarDistDetector.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxStarDistDetector.gridx = 1;
		gbcChckbxStarDistDetector.gridy = 8;
		panelChkboxes.add( chckbxStarDistDetector, gbcChckbxStarDistDetector );

		final JCheckBox chckbxCellposeDetector = new JCheckBox( "Cellpose detector" );
		chckbxCellposeDetector.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxCellposeDetector = new GridBagConstraints();
		gbcChckbxCellposeDetector.anchor = GridBagConstraints.WEST;
		gbcChckbxCellposeDetector.insets = new Insets( 0, 0, 0, 5 );
		gbcChckbxCellposeDetector.gridx = 0;
		gbcChckbxCellposeDetector.gridy = 9;
		panelChkboxes.add( chckbxCellposeDetector, gbcChckbxCellposeDetector );

		final JCheckBox chckbxStarDistCustom = new JCheckBox( "StarDist custom model" );
		chckbxStarDistCustom.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxStarDistCustom = new GridBagConstraints();
		gbcChckbxStarDistCustom.anchor = GridBagConstraints.WEST;
		gbcChckbxStarDistCustom.gridx = 1;
		gbcChckbxStarDistCustom.gridy = 9;
		panelChkboxes.add( chckbxStarDistCustom, gbcChckbxStarDistCustom );

		/*
		 * Path panel. Set image and ground-truth path, plus other options.
		 */

		final JPanel panelPath = new JPanel();
		panelPath.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gblPanelPath = new GridBagLayout();
		gblPanelPath.columnWidths = new int[] { 0, 0, 0 };
		gblPanelPath.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblPanelPath.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gblPanelPath.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelPath.setLayout( gblPanelPath );

		final JLabel lblSourceImage = new JLabel( "Source image:" );
		lblSourceImage.setFont( FONT.deriveFont( Font.BOLD ) );
		final GridBagConstraints gbcLblSourceImage = new GridBagConstraints();
		gbcLblSourceImage.gridwidth = 2;
		gbcLblSourceImage.anchor = GridBagConstraints.WEST;
		gbcLblSourceImage.insets = new Insets( 0, 0, 5, 0 );
		gbcLblSourceImage.gridx = 0;
		gbcLblSourceImage.gridy = 0;
		panelPath.add( lblSourceImage, gbcLblSourceImage );

		final JLabel lblImageName = new JLabel( imp.getShortTitle() );
		lblImageName.setFont( SMALL_FONT );
		final GridBagConstraints gbc_lblImageName = new GridBagConstraints();
		gbc_lblImageName.fill = GridBagConstraints.BOTH;
		gbc_lblImageName.gridwidth = 2;
		gbc_lblImageName.insets = new Insets( 0, 0, 5, 5 );
		gbc_lblImageName.gridx = 0;
		gbc_lblImageName.gridy = 1;
		panelPath.add( lblImageName, gbc_lblImageName );

		final GridBagConstraints gbcSeparator1 = new GridBagConstraints();
		gbcSeparator1.fill = GridBagConstraints.BOTH;
		gbcSeparator1.gridwidth = 2;
		gbcSeparator1.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator1.gridx = 0;
		gbcSeparator1.gridy = 2;
		panelPath.add( new JSeparator(), gbcSeparator1 );

		final JLabel lblGroundTruth = new JLabel( "Path to CTC ground-truth folder:" );
		lblGroundTruth.setFont( FONT.deriveFont( Font.BOLD ) );
		final GridBagConstraints gbcLblGroundTruth = new GridBagConstraints();
		gbcLblGroundTruth.insets = new Insets( 0, 0, 5, 5 );
		gbcLblGroundTruth.anchor = GridBagConstraints.WEST;
		gbcLblGroundTruth.gridx = 0;
		gbcLblGroundTruth.gridy = 3;
		panelPath.add( lblGroundTruth, gbcLblGroundTruth );

		final JButton btnBrowseGT = new JButton( "Browse" );
		btnBrowseGT.setFont( SMALL_FONT );
		final GridBagConstraints gbcBtnBrowseGT = new GridBagConstraints();
		gbcBtnBrowseGT.insets = new Insets( 0, 0, 5, 0 );
		gbcBtnBrowseGT.gridx = 1;
		gbcBtnBrowseGT.gridy = 3;
		panelPath.add( btnBrowseGT, gbcBtnBrowseGT );

		tfGroundTruth = new JTextField();
		tfGroundTruth.setFont( SMALL_FONT );
		final GridBagConstraints gbcTfGroundTruth = new GridBagConstraints();
		gbcTfGroundTruth.insets = new Insets( 0, 0, 5, 0 );
		gbcTfGroundTruth.gridwidth = 2;
		gbcTfGroundTruth.fill = GridBagConstraints.HORIZONTAL;
		gbcTfGroundTruth.gridx = 0;
		gbcTfGroundTruth.gridy = 4;
		panelPath.add( tfGroundTruth, gbcTfGroundTruth );
		tfGroundTruth.setColumns( 10 );

		final GridBagConstraints gbcSeparator2 = new GridBagConstraints();
		gbcSeparator2.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator2.fill = GridBagConstraints.BOTH;
		gbcSeparator2.gridwidth = 2;
		gbcSeparator2.gridx = 0;
		gbcSeparator2.gridy = 5;
		panelPath.add( new JSeparator(), gbcSeparator2 );

		final JCheckBox chckbxSaveTrackMateFile = new JCheckBox( "Save TrackMate file for every test" );
		chckbxSaveTrackMateFile.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxSaveTrackMateFile = new GridBagConstraints();
		gbcChckbxSaveTrackMateFile.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxSaveTrackMateFile.gridwidth = 2;
		gbcChckbxSaveTrackMateFile.anchor = GridBagConstraints.WEST;
		gbcChckbxSaveTrackMateFile.gridx = 0;
		gbcChckbxSaveTrackMateFile.gridy = 6;
		panelPath.add( chckbxSaveTrackMateFile, gbcChckbxSaveTrackMateFile );

		final GridBagConstraints gbcSeparator4 = new GridBagConstraints();
		gbcSeparator4.fill = GridBagConstraints.BOTH;
		gbcSeparator4.gridwidth = 2;
		gbcSeparator4.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator4.gridx = 0;
		gbcSeparator4.gridy = 7;
		panelPath.add( new JSeparator(), gbcSeparator4 );

		final JPanel panelButtons = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panelButtons.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );
		final GridBagConstraints gbcPanelButtons = new GridBagConstraints();
		gbcPanelButtons.anchor = GridBagConstraints.SOUTH;
		gbcPanelButtons.gridwidth = 2;
		gbcPanelButtons.fill = GridBagConstraints.HORIZONTAL;
		gbcPanelButtons.gridx = 0;
		gbcPanelButtons.gridy = 8;
		panelPath.add( panelButtons, gbcPanelButtons );

		final JButton btnStop = new JButton( "Stop" );
		btnStop.setFont( SMALL_FONT );
		btnStop.setIcon( CANCEL_ICON );
		panelButtons.add( btnStop );

		final JButton btnRun = new JButton( "Run" );
		btnRun.setFont( SMALL_FONT );
		btnRun.setIcon( EXECUTE_ICON );

		panelButtons.add( btnRun );

		/*
		 * The split-pane that contains the path panel and the checkbox panel.
		 */

		final JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, panelPath, panelChkboxes );
		splitPane.setFont( FONT );
		splitPane.setDividerSize( 10 );
		splitPane.setOneTouchExpandable( true );
		splitPane.setBorder( null );
		topPanel.add( splitPane, BorderLayout.CENTER );

		/*
		 * Bottom of the top panel. A simple title and a separator line.
		 */

		final JPanel panelSweepConfig = new JPanel();
		panelSweepConfig.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		topPanel.add( panelSweepConfig, BorderLayout.SOUTH );
		final GridBagLayout gblPanelSweepConfig = new GridBagLayout();
		gblPanelSweepConfig.columnWidths = new int[] { 231, 0 };
		gblPanelSweepConfig.rowHeights = new int[] { 0, 14, 0 };
		gblPanelSweepConfig.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gblPanelSweepConfig.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelSweepConfig.setLayout( gblPanelSweepConfig );

		final GridBagConstraints gbcSeparator3 = new GridBagConstraints();
		gbcSeparator3.fill = GridBagConstraints.BOTH;
		gbcSeparator3.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator3.gridx = 0;
		gbcSeparator3.gridy = 0;
		panelSweepConfig.add( new JSeparator(), gbcSeparator3 );

		final JLabel lblParamSweep = new JLabel( "Parameter sweep configuration" );
		lblParamSweep.setFont( FONT.deriveFont( Font.BOLD ) );
		final GridBagConstraints gbcLblParamSweep = new GridBagConstraints();
		gbcLblParamSweep.anchor = GridBagConstraints.NORTHWEST;
		gbcLblParamSweep.gridx = 0;
		gbcLblParamSweep.gridy = 1;
		panelSweepConfig.add( lblParamSweep, gbcLblParamSweep );

		/*
		 * Wire some listeners.
		 */

		// Browse buttons.
		btnBrowseGT.addActionListener( e -> browseGroundTruthPath() );

		/*
		 * Default values.
		 */

		// TODO
	}

	private void browseGroundTruthPath()
	{
		enabler.disable();
		try
		{
			final File file = FileChooser.chooseFile(
					this,
					tfGroundTruth.getText(),
					null,
					"Browse to the ground truth folder",
					DialogType.LOAD,
					SelectionMode.DIRECTORIES_ONLY );
			if ( file != null )
				tfGroundTruth.setText( file.getAbsolutePath() );
		}
		finally
		{
			enabler.reenable();
		}
	}
}
