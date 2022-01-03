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
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;

import com.itextpdf.text.Font;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.ctc.CTCResultsCrawler;
import fiji.plugin.trackmate.ctc.ui.components.FilterConfigPanel;
import fiji.plugin.trackmate.ctc.ui.detectors.DetectorSweepModel;
import fiji.plugin.trackmate.ctc.ui.trackers.TrackerSweepModel;
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

	final JTabbedPane tabbedPane;

	final EverythingDisablerAndReenabler enabler;

	private final FilterConfigPanel panelSpotFilters;

	private final FilterConfigPanel panelTrackFilters;

	private final ParameterSweepModel model;

	final JButton btnRun;

	final JButton btnStop;

	final JSlider sliderChannel;

	final JCheckBox chckbxSaveTrackMateFile;

	final BestParamsPanel bestParamsPanel;

	final Logger logger;

	final CTCResultsCrawler crawler;

	public ParameterSweepPanel( final ParameterSweepModel model, final CTCResultsCrawler crawler )
	{
		this.model = model;
		this.crawler = crawler;
		final ImagePlus imp = model.getImage();
		enabler = new EverythingDisablerAndReenabler( this, new Class[] {
				JLabel.class,
				JTabbedPane.class,
				LogPanel.class,
				JTextArea.class,
				JTextPane.class,
				JScrollPane.class,
				JScrollBar.class,
				JViewport.class } );

		setLayout( new BorderLayout( 5, 5 ) );

		/*
		 * Tabbed pane.
		 */

		this.tabbedPane = new JTabbedPane( JTabbedPane.TOP );
		tabbedPane.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		add( tabbedPane, BorderLayout.CENTER );

		final LogPanel panelLog = new LogPanel();
		this.logger = panelLog.getLogger();
		tabbedPane.addTab( "Log", null, panelLog, null );

		bestParamsPanel = new BestParamsPanel( crawler );
		tabbedPane.addTab( "Best params", null, bestParamsPanel, null );

		panelSpotFilters = new FilterConfigPanel( TrackMateObject.SPOTS, Spot.QUALITY, imp, model.spotFilters() );
		tabbedPane.addTab( "Spot filters", null, panelSpotFilters, null );

		panelTrackFilters = new FilterConfigPanel( TrackMateObject.TRACKS, TrackBranchingAnalyzer.NUMBER_SPOTS, imp, model.trackFilters() );
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

		// Add detector checkboxes.
		final GridBagConstraints c1 = new GridBagConstraints();
		c1.anchor = GridBagConstraints.WEST;
		c1.insets = new Insets( 0, 0, 5, 5 );
		c1.gridx = 0;
		c1.gridy = 1;
		for ( final DetectorSweepModel dm : model.detectorModels() )
		{
			final String name = dm.name;
			final boolean active = model.isActive( name );
			final JCheckBox chkbox = new JCheckBox( name, active );
			chkbox.setFont( SMALL_FONT );
			final SweepPanel panel = new SweepPanel( dm );
			final ActionListener al = l -> {
				if ( chkbox.isSelected() )
				{
					tabbedPane.addTab( name, null, panel, null );
					model.setActive( name, true );
				}
				else
				{
					tabbedPane.remove( panel );
					model.setActive( name, false );
				}
			};
			chkbox.addActionListener( al );
			al.actionPerformed( null );
			panelChkboxes.add( chkbox, c1 );

			c1.gridy++;
			if ( c1.gridy > 9 )
			{
				c1.gridy = 8;
				c1.gridx = 1;
			}
		}

		// Add tracker checkboxes.
		final GridBagConstraints c2 = new GridBagConstraints();
		c2.anchor = GridBagConstraints.WEST;
		c2.insets = new Insets( 0, 0, 5, 5 );
		c2.gridx = 1;
		c2.gridy = 1;
		for ( final TrackerSweepModel tm : model.trackerModels() )
		{
			final String name = tm.name;
			final boolean active = model.isActive( name );
			final JCheckBox chkbox = new JCheckBox( name, active );
			chkbox.setFont( SMALL_FONT );
			final SweepPanel panel = new SweepPanel( tm );
			final ActionListener al = e -> {
				if ( chkbox.isSelected() )
				{
					tabbedPane.addTab( name, null, panel, null );
					model.setActive( name, true );
				}
				else
				{
					tabbedPane.remove( panel );
					model.setActive( name, false );
				}
			};
			chkbox.addActionListener( al );
			al.actionPerformed( null );
			panelChkboxes.add( chkbox, c2 );
			c2.gridy++;
		}
		c2.fill = GridBagConstraints.HORIZONTAL;
		panelChkboxes.add( new JSeparator(), c2 );

		/*
		 * Path panel. Set image and ground-truth path, plus other options.
		 */

		final JPanel panelPath = new JPanel();
		panelPath.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gblPanelPath = new GridBagLayout();
		gblPanelPath.columnWidths = new int[] { 0, 0, 0 };
		gblPanelPath.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblPanelPath.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gblPanelPath.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
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
		gbc_lblImageName.insets = new Insets( 0, 0, 5, 0 );
		gbc_lblImageName.gridx = 0;
		gbc_lblImageName.gridy = 1;
		panelPath.add( lblImageName, gbc_lblImageName );

		final JLabel lblSegmentInChannel = new JLabel( "Detection in channel:" );
		lblSegmentInChannel.setFont( SMALL_FONT );
		final GridBagConstraints gbc_lblDetectioonChannel = new GridBagConstraints();
		gbc_lblDetectioonChannel.anchor = GridBagConstraints.WEST;
		gbc_lblDetectioonChannel.gridwidth = 2;
		gbc_lblDetectioonChannel.insets = new Insets( 0, 0, 5, 0 );
		gbc_lblDetectioonChannel.gridx = 0;
		gbc_lblDetectioonChannel.gridy = 2;
		panelPath.add( lblSegmentInChannel, gbc_lblDetectioonChannel );

		final JPanel panelChannel = new JPanel();
		final GridBagConstraints gbc_panelChannel = new GridBagConstraints();
		gbc_panelChannel.gridwidth = 2;
		gbc_panelChannel.insets = new Insets( 0, 0, 0, 0 );
		gbc_panelChannel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelChannel.gridx = 0;
		gbc_panelChannel.gridy = 3;
		panelPath.add( panelChannel, gbc_panelChannel );

		sliderChannel = new JSlider();
		panelChannel.add( sliderChannel );
		sliderChannel.setMaximum( 60 );
		sliderChannel.setMaximum( imp.getNChannels() );
		sliderChannel.setMinimum( 1 );
		sliderChannel.setValue( imp.getChannel() );

		final JLabel labelChannel = new JLabel( "1" );
		labelChannel.setFont( SMALL_FONT );
		panelChannel.add( labelChannel );

		final GridBagConstraints gbcSeparator1 = new GridBagConstraints();
		gbcSeparator1.fill = GridBagConstraints.BOTH;
		gbcSeparator1.gridwidth = 2;
		gbcSeparator1.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator1.gridx = 0;
		gbcSeparator1.gridy = 4;
		panelPath.add( new JSeparator(), gbcSeparator1 );

		final JLabel lblGroundTruth = new JLabel( "Path to CTC ground-truth folder:" );
		lblGroundTruth.setFont( FONT.deriveFont( Font.BOLD ) );
		final GridBagConstraints gbcLblGroundTruth = new GridBagConstraints();
		gbcLblGroundTruth.insets = new Insets( 0, 0, 5, 5 );
		gbcLblGroundTruth.anchor = GridBagConstraints.WEST;
		gbcLblGroundTruth.gridx = 0;
		gbcLblGroundTruth.gridy = 5;
		panelPath.add( lblGroundTruth, gbcLblGroundTruth );

		final JButton btnBrowseGT = new JButton( "Browse" );
		btnBrowseGT.setFont( SMALL_FONT );
		final GridBagConstraints gbcBtnBrowseGT = new GridBagConstraints();
		gbcBtnBrowseGT.insets = new Insets( 0, 0, 5, 0 );
		gbcBtnBrowseGT.gridx = 1;
		gbcBtnBrowseGT.gridy = 5;
		panelPath.add( btnBrowseGT, gbcBtnBrowseGT );

		tfGroundTruth = new JTextField();
		tfGroundTruth.setFont( SMALL_FONT );
		final GridBagConstraints gbcTfGroundTruth = new GridBagConstraints();
		gbcTfGroundTruth.insets = new Insets( 0, 0, 5, 0 );
		gbcTfGroundTruth.gridwidth = 2;
		gbcTfGroundTruth.fill = GridBagConstraints.HORIZONTAL;
		gbcTfGroundTruth.gridx = 0;
		gbcTfGroundTruth.gridy = 6;
		panelPath.add( tfGroundTruth, gbcTfGroundTruth );
		tfGroundTruth.setColumns( 10 );

		final GridBagConstraints gbcSeparator2 = new GridBagConstraints();
		gbcSeparator2.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator2.fill = GridBagConstraints.BOTH;
		gbcSeparator2.gridwidth = 2;
		gbcSeparator2.gridx = 0;
		gbcSeparator2.gridy = 7;
		panelPath.add( new JSeparator(), gbcSeparator2 );

		chckbxSaveTrackMateFile = new JCheckBox( "Save TrackMate file for every test" );
		chckbxSaveTrackMateFile.setFont( SMALL_FONT );
		final GridBagConstraints gbcChckbxSaveTrackMateFile = new GridBagConstraints();
		gbcChckbxSaveTrackMateFile.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxSaveTrackMateFile.gridwidth = 2;
		gbcChckbxSaveTrackMateFile.anchor = GridBagConstraints.WEST;
		gbcChckbxSaveTrackMateFile.gridx = 0;
		gbcChckbxSaveTrackMateFile.gridy = 8;
		panelPath.add( chckbxSaveTrackMateFile, gbcChckbxSaveTrackMateFile );

		final GridBagConstraints gbcSeparator4 = new GridBagConstraints();
		gbcSeparator4.fill = GridBagConstraints.BOTH;
		gbcSeparator4.gridwidth = 2;
		gbcSeparator4.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator4.gridx = 0;
		gbcSeparator4.gridy = 9;
		panelPath.add( new JSeparator(), gbcSeparator4 );

		final JPanel panelButtons = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panelButtons.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );
		final GridBagConstraints gbcPanelButtons = new GridBagConstraints();
		gbcPanelButtons.anchor = GridBagConstraints.SOUTH;
		gbcPanelButtons.gridwidth = 2;
		gbcPanelButtons.fill = GridBagConstraints.HORIZONTAL;
		gbcPanelButtons.gridx = 0;
		gbcPanelButtons.gridy = 10;
		panelPath.add( panelButtons, gbcPanelButtons );

		btnStop = new JButton( "Stop" );
		btnStop.setFont( SMALL_FONT );
		btnStop.setIcon( CANCEL_ICON );
		panelButtons.add( btnStop );

		btnRun = new JButton( "Run" );
		btnRun.setFont( SMALL_FONT );
		btnRun.setIcon( EXECUTE_ICON );

		panelButtons.add( btnRun );

		/*
		 * Deal with channels: the slider and channel labels are only visible if
		 * we find more than one channel.
		 */
		final int nChannels = imp.getNChannels();

		if ( nChannels <= 1 )
		{
			lblSegmentInChannel.setVisible( false );
			labelChannel.setVisible( false );
			sliderChannel.setVisible( false );
		}
		else
		{
			lblSegmentInChannel.setVisible( true );
			labelChannel.setVisible( true );
			sliderChannel.setVisible( true );
		}
		sliderChannel.addChangeListener( e -> labelChannel.setText( "" + sliderChannel.getValue() ) );

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
		// Count the number of different settings.
		model.listeners().add( () -> {
			final int count = model.count();
			String str = "Parameter sweep configuration  -  ";
			if ( count == 0 )
				str += "Please select at least one detector and one tracker.";
			else if ( count == 1 )
				str += "One settings to test.";
			else
				str += String.format( "Will generate %d different settings to test.", count );
			lblParamSweep.setText( str );
		} );
		model.notifyListeners();
	}

	void refresh()
	{
		// Forced to do that because of how we set the filters.
		model.setSpotFilters( panelSpotFilters.getFeatureFilters() );
		model.setTrackFilters( panelTrackFilters.getFeatureFilters() );
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
				setGroundTruthPath( file );
		}
		finally
		{
			enabler.reenable();
		}
	}

	public void setGroundTruthPath( final File file )
	{
		tfGroundTruth.setText( file.getAbsolutePath() );
		model.setGroundTruthPath( file.getParent() );
		crawler.reset();
		try
		{
			crawler.crawl( file.getParent() );
			bestParamsPanel.update();
		}
		catch ( final IOException e )
		{
			logger.error( "Error while crawling the folder " + file.getParent() + " for CSV results file:\n" );
			logger.error( e.getMessage() );
			e.printStackTrace();
		}
	}

	public String getGroundThruthPath()
	{
		return tfGroundTruth.getText();
	}
}
