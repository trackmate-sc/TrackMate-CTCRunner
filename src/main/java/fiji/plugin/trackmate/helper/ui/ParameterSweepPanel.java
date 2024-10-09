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
import static fiji.plugin.trackmate.gui.Icons.CANCEL_ICON;
import static fiji.plugin.trackmate.gui.Icons.EXECUTE_ICON;
import static fiji.plugin.trackmate.gui.Icons.REVERT_ICON;
import static fiji.plugin.trackmate.gui.Icons.TRACKMATE_ICON_16x16;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.features.track.TrackBranchingAnalyzer;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.components.LogPanel;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.trackmate.helper.ResultsCrawler;
import fiji.plugin.trackmate.helper.model.AbstractSweepModelBase.ModelListener;
import fiji.plugin.trackmate.helper.model.ParameterSweepModel;
import fiji.plugin.trackmate.helper.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.helper.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.helper.ui.filters.SpotFilterConfigPanel;
import fiji.plugin.trackmate.helper.ui.filters.TrackFilterConfigPanel;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import ij.ImagePlus;

public class ParameterSweepPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private static final String DOC_LINK = "https://imagej.net/plugins/trackmate/extensions/trackmate-helper";

	private static final String DOC_STR = "<html><small><a href=" + DOC_LINK + ">Documentation</a></small></html>";

	final JTabbedPane tabbedPane;

	/**
	 * List of enablers that can disable/enable the parts of the UI that should
	 * not be touched while a parameter sweep is runing.
	 */
	final List< EverythingDisablerAndReenabler > enablers;

	private final SpotFilterConfigPanel panelSpotFilters;

	private final TrackFilterConfigPanel panelTrackFilters;

	final JButton btnRun;

	final JButton btnStop;

	final JButton btnReset;

	final JSlider sliderChannel;

	final JCheckBox chckbxSaveTrackMateFile;

	final Logger logger;

	final ResultsCrawler crawler;


	public ParameterSweepPanel(
			final ImagePlus imp,
			final ParameterSweepModel model,
			final ResultsCrawler crawler,
			final String gtPath )
	{
		this.crawler = crawler;
		this.enablers = new ArrayList<>();
		setLayout( new BorderLayout( 5, 5 ) );

		/*
		 * Tabbed pane.
		 */

		this.tabbedPane = new JTabbedPane( JTabbedPane.TOP );
		tabbedPane.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );

		final LogPanel panelLog = new LogPanel();
		panelLog.getTextPane().setFont( Fonts.SMALL_FONT_MONOSPACED );
		this.logger = panelLog.getLogger();
		tabbedPane.addTab( "Log", null, panelLog, null );

		final CrawlerResultsPanel bestParamsPanel = new CrawlerResultsPanel( crawler, imp );
		tabbedPane.addTab( "Best params", null, bestParamsPanel, null );

		panelSpotFilters = new SpotFilterConfigPanel( TrackMateObject.SPOTS, Spot.QUALITY, imp, model );
		tabbedPane.addTab( "Spot filters", null, panelSpotFilters, null );
		// Enabler.
		enablers.add( new EverythingDisablerAndReenabler( panelSpotFilters, new Class[] { JLabel.class } ) );

		panelTrackFilters = new TrackFilterConfigPanel( TrackMateObject.TRACKS, TrackBranchingAnalyzer.NUMBER_SPOTS, imp, model );
		tabbedPane.addTab( "Track filters", null, panelTrackFilters, null );
		// Enabler.
		enablers.add( new EverythingDisablerAndReenabler( panelTrackFilters, new Class[] { JLabel.class } ) );

		/*
		 * Top panel.
		 */

		final JPanel topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout( 5, 5 ) );
		// Enabler for the top panel.
		enablers.add( new EverythingDisablerAndReenabler( topPanel, new Class[] { JLabel.class } ) );

		/*
		 * Title panel.
		 */

		final JPanel panelTitle = new JPanel();
		panelTitle.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gblPanelTitle = new GridBagLayout();
		gblPanelTitle.columnWidths = new int[] { 137, 0 };
		gblPanelTitle.rowHeights = new int[] { 14, 0, 0, 0, 0 };
		gblPanelTitle.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gblPanelTitle.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		panelTitle.setLayout( gblPanelTitle );

		final JLabel lblTitle = new JLabel( "<html><center>TrackMate Helper <small>v"
				+ VersionUtils.getVersion( ParameterSweepPanel.class )
				+ "</small></center></html>" );
		lblTitle.setIcon( TRACKMATE_ICON_16x16 );
		lblTitle.setFont( BIG_FONT );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.insets = new Insets( 0, 0, 5, 0 );
		gbcLblTitle.fill = GridBagConstraints.BOTH;
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		panelTitle.add( lblTitle, gbcLblTitle );

		final JLabel lblDoc = new JLabel( "<html>"
				+ "Runs automated parameter sweeps on an image using TrackMate, "
				+ "and compute the tracking metrics on all the results."
				+ "</html>" );
		lblDoc.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblDoc = new GridBagConstraints();
		gbcLblDoc.insets = new Insets( 0, 0, 5, 0 );
		gbcLblDoc.fill = GridBagConstraints.BOTH;
		gbcLblDoc.gridx = 0;
		gbcLblDoc.gridy = 1;
		panelTitle.add( lblDoc, gbcLblDoc );

		final JLabel lblUrl = new JLabel( DOC_STR );
		lblUrl.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final java.awt.event.MouseEvent e )
			{
				try
				{
					Desktop.getDesktop().browse( new URI( DOC_LINK ) );
				}
				catch ( URISyntaxException | IOException ex )
				{
					ex.printStackTrace();
				}
			}
		} );

		final GridBagConstraints gbcLblUrl = new GridBagConstraints();
		gbcLblUrl.anchor = GridBagConstraints.EAST;
		gbcLblUrl.insets = new Insets( 0, 0, 5, 0 );
		gbcLblUrl.gridx = 0;
		gbcLblUrl.gridy = 2;
		panelTitle.add( lblUrl, gbcLblUrl );

		final GridBagConstraints gbcSeparator = new GridBagConstraints();
		gbcSeparator.fill = GridBagConstraints.BOTH;
		gbcSeparator.gridx = 0;
		gbcSeparator.gridy = 3;
		gbcSeparator.anchor = GridBagConstraints.SOUTH;
		panelTitle.add( new JSeparator(), gbcSeparator );

		/*
		 * Checkbox panel. Select detectors and tracker to include in the sweep.
		 */

		final JPanel panelChkboxes = new JPanel();
		panelChkboxes.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gblPanelChkboxes = new GridBagLayout();
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

		// Try to balance detector and tracker checkboxes.
		final int nDetectors = model.detectorModels().size();
		final int nTrackers = model.trackerModels().size();
		final int nRows = ( nDetectors + nTrackers + 2 ) / 2;

		// Add detector checkboxes.
		final String spaceUnits = imp.getCalibration().getUnit();
		final String timeUnits = imp.getCalibration().getTimeUnit();
		final GridBagConstraints c1 = new GridBagConstraints();
		c1.anchor = GridBagConstraints.WEST;
		c1.insets = new Insets( 0, 0, 5, 5 );
		c1.gridx = 0;
		c1.gridy = 1;
		boolean addSeparator = true;
		for ( final DetectorSweepModel dm : model.detectorModels() )
		{
			final String name = dm.getName();
			final boolean active = model.isActive( name );
			final JCheckBox chkbox = new JCheckBox( name, active );
			chkbox.setFont( SMALL_FONT );
			final ModuleParameterSweepPanel panel = new ModuleParameterSweepPanel( dm, spaceUnits, timeUnits );
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
			if ( c1.gridy > nRows )
			{
				// Switch to the other column, aligning with the last row.
				c1.gridx = 1;
				c1.gridy = nRows - ( nDetectors - c1.gridy );
				addSeparator = false;
			}
			// Enabler.
			enablers.add( new EverythingDisablerAndReenabler( panel, new Class[] { JLabel.class } ) );
		}
		c1.fill = GridBagConstraints.HORIZONTAL;
		if ( addSeparator )
			panelChkboxes.add( new JSeparator(), c1 );

		// Add tracker checkboxes.
		addSeparator = true;
		final GridBagConstraints c2 = new GridBagConstraints();
		c2.anchor = GridBagConstraints.WEST;
		c2.insets = new Insets( 0, 0, 5, 5 );
		c2.gridx = 1;
		c2.gridy = 1;
		for ( final TrackerSweepModel tm : model.trackerModels() )
		{
			final String name = tm.getName();
			final boolean active = model.isActive( name );
			final JCheckBox chkbox = new JCheckBox( name, active );
			chkbox.setFont( SMALL_FONT );
			final ModuleParameterSweepPanel panel = new ModuleParameterSweepPanel( tm, spaceUnits, timeUnits );
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
			if ( c2.gridy > nRows )
			{
				// Switch to the other column, aligning with the last row.
				c2.gridx = 0;
				c2.gridy = nRows - ( nTrackers - c2.gridy );
				addSeparator = false;
			}
			// Enabler.
			enablers.add( new EverythingDisablerAndReenabler( panel, new Class[] { JLabel.class } ) );
		}
		c2.fill = GridBagConstraints.HORIZONTAL;
		if ( addSeparator )
			panelChkboxes.add( new JSeparator(), c2 );

		/*
		 * Path panel. Show image and ground-truth path, set other options.
		 */

		final JPanel panelPath = new JPanel();
		panelPath.setPreferredSize( new Dimension( 200, 300 ) );
		panelPath.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gblPanelPath = new GridBagLayout();
		gblPanelPath.columnWidths = new int[] { 0, 0, 0 };
		gblPanelPath.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblPanelPath.columnWeights = new double[] { 1., 0., Double.MIN_VALUE };
		gblPanelPath.rowWeights = new double[] { 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 1., Double.MIN_VALUE };
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
		lblImageName.setMaximumSize( new Dimension( 200, 14 ) );
		lblImageName.setMinimumSize( new Dimension( 200, 14 ) );
		lblImageName.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblImageName = new GridBagConstraints();
		gbcLblImageName.fill = GridBagConstraints.BOTH;
		gbcLblImageName.gridwidth = 2;
		gbcLblImageName.insets = new Insets( 0, 0, 5, 0 );
		gbcLblImageName.gridx = 0;
		gbcLblImageName.gridy = 1;
		panelPath.add( lblImageName, gbcLblImageName );

		final JLabel lblSegmentInChannel = new JLabel( "Detection in channel:" );
		lblSegmentInChannel.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblDetectionChannel = new GridBagConstraints();
		gbcLblDetectionChannel.anchor = GridBagConstraints.WEST;
		gbcLblDetectionChannel.gridwidth = 2;
		gbcLblDetectionChannel.insets = new Insets( 0, 0, 5, 0 );
		gbcLblDetectionChannel.gridx = 0;
		gbcLblDetectionChannel.gridy = 2;
		panelPath.add( lblSegmentInChannel, gbcLblDetectionChannel );

		final JPanel panelChannel = new JPanel();
		final GridBagConstraints gbcPanelChannel = new GridBagConstraints();
		gbcPanelChannel.gridwidth = 2;
		gbcPanelChannel.insets = new Insets( 0, 0, 0, 0 );
		gbcPanelChannel.fill = GridBagConstraints.HORIZONTAL;
		gbcPanelChannel.gridx = 0;
		gbcPanelChannel.gridy = 3;
		panelPath.add( panelChannel, gbcPanelChannel );

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

		final JTextField lblGroundTruthPath = new JTextField( gtPath );
		lblGroundTruthPath.setEditable( false );
		lblGroundTruthPath.setFont( SMALL_FONT );
		final GridBagConstraints gbcTfGroundTruth = new GridBagConstraints();
		gbcTfGroundTruth.insets = new Insets( 0, 0, 5, 0 );
		gbcTfGroundTruth.gridwidth = 2;
		gbcTfGroundTruth.fill = GridBagConstraints.HORIZONTAL;
		gbcTfGroundTruth.gridx = 0;
		gbcTfGroundTruth.gridy = 6;
		panelPath.add( lblGroundTruthPath, gbcTfGroundTruth );

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
		panelButtons.setLayout( new BoxLayout( panelButtons, BoxLayout.LINE_AXIS ) );

		final GridBagConstraints gbcPanelButtons = new GridBagConstraints();
		gbcPanelButtons.anchor = GridBagConstraints.NORTH;
		gbcPanelButtons.gridwidth = 2;
		gbcPanelButtons.fill = GridBagConstraints.HORIZONTAL;
		gbcPanelButtons.gridx = 0;
		gbcPanelButtons.gridy = 10;
		panelPath.add( panelButtons, gbcPanelButtons );

		btnReset = new JButton( "Reset parameters" );
		btnReset.setFont( SMALL_FONT );
		btnReset.setIcon( REVERT_ICON );
		panelButtons.add( btnReset );
		panelButtons.add( Box.createHorizontalGlue() );

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
		splitPane.setResizeWeight( 0.5 );
		splitPane.setFont( FONT );
		splitPane.setDividerSize( 10 );
		splitPane.setOneTouchExpandable( true );
		splitPane.setBorder( null );
		topPanel.add( splitPane, BorderLayout.CENTER );

		/*
		 * Bottom of the top panel. A simple title and a separator line.
		 */

		final JPanel panelSweepConfig = new JPanel();
		final JLabel lblParamSweep = new JLabel( "Parameter sweep configuration" );
		lblParamSweep.setFont( FONT.deriveFont( Font.BOLD ) );
		panelSweepConfig.add( lblParamSweep );

		/*
		 * Put everything in a vertical split pane.
		 */

		add( panelTitle, BorderLayout.NORTH );
		final JScrollPane scrollPane = new JScrollPane( topPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setBorder( null );
		final JSplitPane mainPanel = new JSplitPane( JSplitPane.VERTICAL_SPLIT, false,
				scrollPane,
				tabbedPane );
		mainPanel.setResizeWeight( 0.5 );
		mainPanel.setFont( FONT );
		mainPanel.setDividerSize( 10 );
		mainPanel.setOneTouchExpandable( true );
		mainPanel.setBorder( null );
		add( mainPanel, BorderLayout.CENTER );
		add( panelSweepConfig, BorderLayout.SOUTH );

		/*
		 * Wire some listeners.
		 */

		// Count the number of different settings.
		final ModelListener l1 = () -> {
			final int count = model.count();
			String str = "Parameter sweep configuration  -  ";
			if ( count == 0 )
				str += "Please select at least one detector and one tracker.";
			else if ( count == 1 )
				str += "One settings to test.";
			else
				str += String.format( "Will generate %d different settings to test.", count );
			lblParamSweep.setText( str );
		};
		model.listeners().add( l1 );
		l1.modelChanged();
	}
}
