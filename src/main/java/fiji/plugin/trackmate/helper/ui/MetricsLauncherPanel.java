package fiji.plugin.trackmate.helper.ui;

import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.helper.ui.HelperLauncherPanel.GT_PATH_KEY;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.scijava.prefs.PrefService;
import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.ui.HelperLauncherPanel.SetFileDropTarget;
import fiji.plugin.trackmate.helper.ui.components.MetricsChooserPanel;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;
import fiji.plugin.trackmate.util.JLabelLogger;
import fiji.plugin.trackmate.util.TMUtils;

public class MetricsLauncherPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	final JTextField tfInputPath;

	final JTextField tfGTPath;

	final JButton btnCancel;

	final JButton btnOK;

	private final MetricsChooserPanel metricsChooserPanel;

	private Logger logger;

	public MetricsLauncherPanel()
	{
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] { 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 0., 1. };
		gridBagLayout.rowHeights = new int[] { 0, 0, 15, 200, 15 };
		gridBagLayout.columnWeights = new double[] { 1., 0. };
		setLayout( gridBagLayout );

		final Image im = Icons.TRACKMATE_ICON.getImage();
		final Image newimg = im.getScaledInstance( 32, 32, java.awt.Image.SCALE_SMOOTH );
		final ImageIcon icon = new ImageIcon( newimg );

		final JLabel lblTitle = new JLabel( "TrackMate tracking metrics", icon, JLabel.LEADING );
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

		metricsChooserPanel = new MetricsChooserPanel();
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

		final JLabel lblPleaseSelectTestFile = new JLabel( "<html>"
				+ "Please select the TrackMate file or folder containing "
				+ "the files on which the metrics will be measured."
				+ "<html>" );
		lblPleaseSelectTestFile.setFont( FONT );
		final GridBagConstraints gbcLblPleaseSelectTestFile = new GridBagConstraints();
		gbcLblPleaseSelectTestFile.fill = GridBagConstraints.BOTH;
		gbcLblPleaseSelectTestFile.gridwidth = 2;
		gbcLblPleaseSelectTestFile.insets = new Insets( 0, 0, 5, 0 );
		gbcLblPleaseSelectTestFile.anchor = GridBagConstraints.WEST;
		gbcLblPleaseSelectTestFile.gridx = 0;
		gbcLblPleaseSelectTestFile.gridy = 5;
		add( lblPleaseSelectTestFile, gbcLblPleaseSelectTestFile );

		final JLabel lblInputPath = new JLabel( "Input path:" );
		lblInputPath.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblInputPath = new GridBagConstraints();
		gbcLblInputPath.gridwidth = 2;
		gbcLblInputPath.insets = new Insets( 0, 0, 5, 0 );
		gbcLblInputPath.anchor = GridBagConstraints.WEST;
		gbcLblInputPath.gridx = 0;
		gbcLblInputPath.gridy = 6;
		add( lblInputPath, gbcLblInputPath );

		tfInputPath = new JTextField();
		tfInputPath.setFont( SMALL_FONT );
		final GridBagConstraints gbcTfImagePath = new GridBagConstraints();
		gbcTfImagePath.insets = new Insets( 0, 0, 5, 5 );
		gbcTfImagePath.fill = GridBagConstraints.HORIZONTAL;
		gbcTfImagePath.gridx = 0;
		gbcTfImagePath.gridy = 7;
		add( tfInputPath, gbcTfImagePath );
		tfInputPath.setColumns( 10 );

		final JButton btnBrowseInput = new JButton( "Browse" );
		btnBrowseInput.setFont( SMALL_FONT );
		final GridBagConstraints gbcBtnBrowseInput = new GridBagConstraints();
		gbcBtnBrowseInput.insets = new Insets( 0, 0, 5, 0 );
		gbcBtnBrowseInput.gridx = 1;
		gbcBtnBrowseInput.gridy = 7;
		add( btnBrowseInput, gbcBtnBrowseInput );

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

		final JLabelLogger labelLogger = new JLabelLogger();
		labelLogger.setFont( SMALL_FONT );
		final GridBagConstraints gbcLogger = new GridBagConstraints();
		gbcLogger.anchor = GridBagConstraints.SOUTHEAST;
		gbcLogger.fill = GridBagConstraints.BOTH;
		gbcLogger.gridwidth = 2;
		gbcLogger.gridx = 0;
		gbcLogger.gridy = 12;
		add( labelLogger, gbcLogger );

		final JPanel panelButtons = new JPanel();
		final GridBagConstraints gbcPanelButtons = new GridBagConstraints();
		gbcPanelButtons.anchor = GridBagConstraints.SOUTHEAST;
		gbcPanelButtons.gridwidth = 2;
		gbcPanelButtons.gridx = 0;
		gbcPanelButtons.gridy = 13;
		add( panelButtons, gbcPanelButtons );

		btnCancel = new JButton( "Cancel" );
		btnCancel.setFont( SMALL_FONT );
		panelButtons.add( btnCancel );

		btnOK = new JButton( "OK" );
		btnOK.setFont( SMALL_FONT );
		panelButtons.add( btnOK );

		/*
		 * Listeners & co.
		 */
		
		this.logger = labelLogger.getLogger();
		final PrefService prefService = TMUtils.getContext().getService( PrefService.class );

		fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( tfInputPath );
		fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( tfGTPath );

		final Runnable storeGtPath = () -> prefService.put( getClass(), GT_PATH_KEY, tfGTPath.getText() );
		final Runnable storeInputPath = () -> prefService.put( getClass(), INPUT_PATH_KEY, tfInputPath.getText() );

		tfInputPath.addActionListener( e -> storeInputPath.run() );
		final FocusAdapter faIm = new FocusAdapter()
		{
			@Override
			public void focusLost( final java.awt.event.FocusEvent e )
			{
				storeInputPath.run();
			}
		};
		tfInputPath.addFocusListener( faIm );

		tfGTPath.addActionListener( e -> storeGtPath.run() );
		final FocusAdapter faGt = new FocusAdapter()
		{
			@Override
			public void focusLost( final java.awt.event.FocusEvent e )
			{
				storeGtPath.run();
				readPixelUnits();
			}
		};
		tfGTPath.addFocusListener( faGt );

		btnBrowseInput.addActionListener( e -> {
			final File file = FileChooser.chooseFile( this, tfInputPath.getText(), null,
					"Select an input file or folder", DialogType.LOAD, SelectionMode.FILES_AND_DIRECTORIES );
			if ( file == null )
				return;

			tfInputPath.setText( file.getAbsolutePath() );
			storeInputPath.run();
		} );

		btnBrowseGT.addActionListener( e -> {
			final String dialogTitle = metricsChooserPanel.isCTCSelected()
					? "Select a CTC ground-truth folder."
					: "Select a SPT ground-truth XML file.";
			final SelectionMode selectionMode = metricsChooserPanel.isCTCSelected()
					? SelectionMode.DIRECTORIES_ONLY
					: SelectionMode.FILES_ONLY;
			final File file = FileChooser.chooseFile( this, tfGTPath.getText(), null, dialogTitle, DialogType.LOAD, selectionMode );
			if ( file == null )
				return;

			tfGTPath.setText( file.getAbsolutePath() );
			storeGtPath.run();
			readPixelUnits();
		} );

		tfInputPath.setDropTarget( new SetFileDropTarget( tfInputPath, storeInputPath ) );
		tfGTPath.setDropTarget( new SetFileDropTarget( tfGTPath, storeGtPath ) );

		/*
		 * Default values.
		 */

		final String lastUsedImagePathFolder = prefService.get( getClass(), INPUT_PATH_KEY, System.getProperty( "user.home" ) );
		tfInputPath.setText( lastUsedImagePathFolder );

		final String lastUsedGtPath = prefService.get( getClass(), GT_PATH_KEY, System.getProperty( "user.home" ) );
		tfGTPath.setText( lastUsedGtPath );
		readPixelUnits();
	}

	private void readPixelUnits()
	{
		logger.log( "" );
		if ( !isCTCSelected() )
		{
			metricsChooserPanel.setUnits( "image units" );
			// Try to read the pixel units.
			final TmXmlReader reader = new TmXmlReader( new File( tfGTPath.getText() ) );
			try
			{
				final Model model = reader.getModel();
				if ( model != null )
					metricsChooserPanel.setUnits( model.getSpaceUnits() );
			}
			catch ( final Exception ex )
			{}
		}
	}

	public boolean isCTCSelected()
	{
		return metricsChooserPanel.isCTCSelected();
	}

	public double getSPTMaxPairingDistance()
	{
		return metricsChooserPanel.getSPTMaxPairingDistance();
	}

	public String getUnits()
	{
		return metricsChooserPanel.getUnits();
	}

	private static final String INPUT_PATH_KEY = "INPUT_PATH";

}
