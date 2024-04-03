package fiji.plugin.trackmate.helper.ui.components;

import static fiji.plugin.trackmate.gui.Fonts.FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.scijava.prefs.PrefService;

import fiji.plugin.trackmate.helper.ctc.CTCTrackingMetricsType;
import fiji.plugin.trackmate.helper.spt.SPTTrackingMetricsType;
import fiji.plugin.trackmate.util.TMUtils;
import net.imagej.ImageJ;

public class MetricsChooserPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final JRadioButton rdbtnCTC;

	private final JFormattedTextField ftfMaxDist;

	private final JLabel lblUnits;

	public MetricsChooserPanel()
	{
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0 };
		setLayout( gridBagLayout );

		final JLabel lblChooseMetrics = new JLabel( "Metrics to use:" );
		lblChooseMetrics.setFont( FONT );
		final GridBagConstraints gbcLblChooseMetrics = new GridBagConstraints();
		gbcLblChooseMetrics.gridwidth = 2;
		gbcLblChooseMetrics.anchor = GridBagConstraints.WEST;
		gbcLblChooseMetrics.insets = new Insets( 0, 0, 5, 0 );
		gbcLblChooseMetrics.gridx = 0;
		gbcLblChooseMetrics.gridy = 0;
		add( lblChooseMetrics, gbcLblChooseMetrics );

		rdbtnCTC = new JRadioButton( "Cell-Tracking challenge (CTC)" );
		rdbtnCTC.setFont( SMALL_FONT );
		final GridBagConstraints gbcRdbtnCTC = new GridBagConstraints();
		gbcRdbtnCTC.gridwidth = 2;
		gbcRdbtnCTC.anchor = GridBagConstraints.WEST;
		gbcRdbtnCTC.insets = new Insets( 0, 0, 5, 0 );
		gbcRdbtnCTC.gridx = 0;
		gbcRdbtnCTC.gridy = 1;
		add( rdbtnCTC, gbcRdbtnCTC );

		final JRadioButton rdbtnSPT = new JRadioButton( "Single-Particle Tracking challenge (SPT)" );
		rdbtnSPT.setFont( SMALL_FONT );
		final GridBagConstraints gbcRdbtnSPT = new GridBagConstraints();
		gbcRdbtnSPT.gridwidth = 2;
		gbcRdbtnSPT.insets = new Insets( 0, 0, 5, 0 );
		gbcRdbtnSPT.anchor = GridBagConstraints.WEST;
		gbcRdbtnSPT.gridx = 0;
		gbcRdbtnSPT.gridy = 2;
		add( rdbtnSPT, gbcRdbtnSPT );

		final JLabel lblMetricsDescription = new JLabel();
		lblMetricsDescription.setFont( SMALL_FONT.deriveFont( Font.ITALIC ) );
		final GridBagConstraints gbcLblMetricsDescription = new GridBagConstraints();
		gbcLblMetricsDescription.anchor = GridBagConstraints.SOUTH;
		gbcLblMetricsDescription.fill = GridBagConstraints.HORIZONTAL;
		gbcLblMetricsDescription.gridwidth = 2;
		gbcLblMetricsDescription.insets = new Insets( 0, 0, 5, 0 );
		gbcLblMetricsDescription.gridx = 0;
		gbcLblMetricsDescription.gridy = 3;
		add( lblMetricsDescription, gbcLblMetricsDescription );

		final JLabel lblUrl = new JLabel( " " );
		lblUrl.setFont( SMALL_FONT );
		lblUrl.setForeground( Color.BLUE );
		final GridBagConstraints gbcLblUrl = new GridBagConstraints();
		gbcLblUrl.anchor = GridBagConstraints.NORTH;
		gbcLblUrl.gridwidth = 2;
		gbcLblUrl.fill = GridBagConstraints.HORIZONTAL;
		gbcLblUrl.insets = new Insets( 0, 0, 5, 0 );
		gbcLblUrl.gridx = 0;
		gbcLblUrl.gridy = 4;
		add( lblUrl, gbcLblUrl );

		final JPanel panelConfigParams = new JPanel( new BorderLayout() );
		final GridBagConstraints gbcPanelConfigParams = new GridBagConstraints();
		gbcPanelConfigParams.anchor = GridBagConstraints.NORTH;
		gbcPanelConfigParams.gridwidth = 2;
		gbcPanelConfigParams.insets = new Insets( 0, 0, 5, 0 );
		gbcPanelConfigParams.fill = GridBagConstraints.BOTH;
		gbcPanelConfigParams.gridx = 0;
		gbcPanelConfigParams.gridy = 5;
		add( panelConfigParams, gbcPanelConfigParams );

		/*
		 * Config panel.
		 */

		final JPanel ctcParamPanel = new JPanel(); // Empty
		final JPanel sptParamPanel = new JPanel();
		sptParamPanel.setLayout( new BoxLayout( sptParamPanel, BoxLayout.LINE_AXIS ) );
		final JLabel lblMaxDist = new JLabel( "Max distance for pairing:" );
		lblMaxDist.setFont( SMALL_FONT );
		sptParamPanel.add( lblMaxDist );
		sptParamPanel.add( Box.createHorizontalStrut( 5 ) );
		ftfMaxDist = new JFormattedTextField( new DecimalFormat( "0.00" ) );
		ftfMaxDist.setColumns( 9 );
		ftfMaxDist.setFont( SMALL_FONT );
		ftfMaxDist.setHorizontalAlignment( JTextField.RIGHT );
		ftfMaxDist.setMaximumSize( new Dimension( 100, 40 ) );
		sptParamPanel.add( ftfMaxDist );
		sptParamPanel.add( Box.createHorizontalStrut( 5 ) );
		lblUnits = new JLabel( "image units" );
		lblUnits.setFont( SMALL_FONT );
		sptParamPanel.add( lblUnits );
		sptParamPanel.add( Box.createHorizontalGlue() );

		/*
		 * Listeners & co.
		 */

		final PrefService prefService = TMUtils.getContext().getService( PrefService.class );

		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( rdbtnSPT );
		buttonGroup.add( rdbtnCTC );

		final ItemListener changeMetrics = e -> {
			// Help and selection.
			final String doc = rdbtnCTC.isSelected() ? DOC_CTC : DOC_SPT;
			lblMetricsDescription.setText( doc );
			final String url = rdbtnCTC.isSelected() ? URL_CTC : URL_SPT;
			lblUrl.setText( url );

			// Config panel.
			final JPanel paramPanel = rdbtnCTC.isSelected() ? ctcParamPanel : sptParamPanel;
			panelConfigParams.removeAll();
			panelConfigParams.add( paramPanel, BorderLayout.CENTER );

			prefService.put( getClass(), METRICS_TYPE_KEY, rdbtnCTC.isSelected() );
		};

		rdbtnCTC.addItemListener( changeMetrics );
		lblUrl.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final java.awt.event.MouseEvent e )
			{
				try
				{
					final String link = rdbtnCTC.isSelected() ? LINK_CTC : LINK_SPT;
					Desktop.getDesktop().browse( new URI( link ) );
				}
				catch ( URISyntaxException | IOException ex )
				{
					ex.printStackTrace();
				}
			}
		} );
		fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( ftfMaxDist );

		final boolean ctcSelected = prefService.getBoolean( getClass(), METRICS_TYPE_KEY, true );
		rdbtnCTC.setSelected( ctcSelected );
		rdbtnSPT.setSelected( !ctcSelected );
		changeMetrics.itemStateChanged( null );

		ftfMaxDist.setValue( Double.valueOf( 1.0 ) );
	}

	void setUnits( final String units )
	{
		lblUnits.setText( units );
	}

	/**
	 * Returns the value of the max pairing distance specified in this panel,
	 * used for the SPT metrics.
	 * 
	 * @return the value of the max pairing distance
	 */
	public double getSPTMaxPairingDistance()
	{
		return ( ( Number ) ftfMaxDist.getValue() ).doubleValue();
	}

	/**
	 * Returns <code>true</code> if the CTC metrics are selected. If
	 * <code>false</code>, the SPT metrics are selected.
	 * 
	 * @return <code>true</code> if the CTC metrics are selected
	 */
	public boolean isCTCSelected()
	{
		return rdbtnCTC.isSelected();
	}

	public static final void main( final String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );
		final JFrame frame = new JFrame();
		frame.add( new MetricsChooserPanel() );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}

	private static final String METRICS_TYPE_KEY = "METRICS_TYPE";

	private static final String DOC_CTC = CTCTrackingMetricsType.INFO;

	private static final String DOC_SPT = SPTTrackingMetricsType.INFO;

	protected static final String LINK_CTC = CTCTrackingMetricsType.URL;

	protected static final String LINK_SPT = SPTTrackingMetricsType.URL;

	private static final String URL_CTC = "<html><small><a href="
			+ LINK_CTC
			+ ">Ulman, Maška, et al. 2017</a></small></html>";

	private static final String URL_SPT = "<html><small><a href="
			+ LINK_SPT
			+ ">Chenouard, Smal, de Chaumont, Maška, et al. 2014</a></small></html>";


}
