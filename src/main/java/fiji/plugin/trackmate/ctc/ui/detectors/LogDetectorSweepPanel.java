package fiji.plugin.trackmate.ctc.ui.detectors;

import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.BooleanRangeSweepPanel;
import fiji.plugin.trackmate.ctc.ui.components.NumberRangeSweepPanel;

public class LogDetectorSweepPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final JLabel lblLog;

	private final AbstractSweepModel model;

	public LogDetectorSweepPanel( final LogDetectorSweepModel model )
	{
		this( model, "Laplacian of Gaussian detector" );
	}

	protected LogDetectorSweepPanel( final LogDetectorSweepModel model, final String title )
	{
		this.model = model;
		model.listeners().add( () -> update() );

		setLayout( new BorderLayout( 0, 0 ) );

		final JPanel mainPanel = new JPanel();
		mainPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gblMainPanel = new GridBagLayout();
		gblMainPanel.columnWeights = new double[] { 1.0 };
		gblMainPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		mainPanel.setLayout( gblMainPanel );

		final JLabel lblTitle = new JLabel( title );
		lblTitle.setHorizontalAlignment( SwingConstants.CENTER );
		lblTitle.setFont( BIG_FONT );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.insets = new Insets( 0, 0, 5, 0 );
		gbcLblTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		mainPanel.add( lblTitle, gbcLblTitle );

		lblLog = new JLabel( "log" );
		lblLog.setHorizontalAlignment( SwingConstants.LEFT );
		lblLog.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblLog = new GridBagConstraints();
		gbcLblLog.fill = GridBagConstraints.BOTH;
		gbcLblLog.insets = new Insets( 0, 0, 5, 0 );
		gbcLblLog.gridx = 0;
		gbcLblLog.gridy = 1;
		mainPanel.add( lblLog, gbcLblLog );

		final GridBagConstraints gbcSeparator3 = new GridBagConstraints();
		gbcSeparator3.fill = GridBagConstraints.BOTH;
		gbcSeparator3.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator3.gridx = 0;
		gbcSeparator3.gridy = 2;
		mainPanel.add( new JSeparator(), gbcSeparator3 );

		final GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.insets = new Insets( 0, 0, 5, 0 );
		gbc1.anchor = GridBagConstraints.NORTH;
		gbc1.gridx = 0;
		gbc1.gridy = 3;
		final NumberRangeSweepPanel estimatedDiameterParamUI = new NumberRangeSweepPanel( model.estimatedDiameterParam );
		mainPanel.add( estimatedDiameterParamUI, gbc1 );

		final GridBagConstraints gbcSeparator = new GridBagConstraints();
		gbcSeparator.fill = GridBagConstraints.BOTH;
		gbcSeparator.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator.gridx = 0;
		gbcSeparator.gridy = 4;
		mainPanel.add( new JSeparator(), gbcSeparator );

		final GridBagConstraints gbc3 = new GridBagConstraints();
		gbc3.fill = GridBagConstraints.HORIZONTAL;
		gbc3.insets = new Insets( 0, 0, 5, 0 );
		gbc3.anchor = GridBagConstraints.NORTH;
		gbc3.gridx = 0;
		gbc3.gridy = 5;
		final NumberRangeSweepPanel thresholdParamUI = new NumberRangeSweepPanel( model.thresholdParam );
		mainPanel.add( thresholdParamUI, gbc3 );

		final GridBagConstraints gbcSeparator1 = new GridBagConstraints();
		gbcSeparator1.fill = GridBagConstraints.BOTH;
		gbcSeparator1.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator1.gridx = 0;
		gbcSeparator1.gridy = 6;
		mainPanel.add( new JSeparator(), gbcSeparator1 );

		final GridBagConstraints gbc5 = new GridBagConstraints();
		gbc5.fill = GridBagConstraints.HORIZONTAL;
		gbc5.insets = new Insets( 0, 0, 5, 0 );
		gbc5.gridx = 0;
		gbc5.gridy = 7;

		final BooleanRangeSweepPanel subpixelParamUI = new BooleanRangeSweepPanel( model.subpixelLocalizationParam );
		mainPanel.add( subpixelParamUI, gbc5 );

		final GridBagConstraints gbcSeparator2 = new GridBagConstraints();
		gbcSeparator2.fill = GridBagConstraints.BOTH;
		gbcSeparator2.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator2.gridx = 0;
		gbcSeparator2.gridy = 8;
		mainPanel.add( new JSeparator(), gbcSeparator2 );

		final GridBagConstraints gbc7 = new GridBagConstraints();
		gbc7.fill = GridBagConstraints.BOTH;
		gbc7.anchor = GridBagConstraints.WEST;
		gbc7.gridx = 0;
		gbc7.gridy = 9;
		final BooleanRangeSweepPanel medianFilterParamUI = new BooleanRangeSweepPanel( model.useMedianParam );
		mainPanel.add( medianFilterParamUI, gbc7 );

		final JScrollPane scrollPane = new JScrollPane( mainPanel );
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		add( scrollPane );
	}

	private void update()
	{
		lblLog.setText( String.format( "Testing %d parameter sets.",
				model.generateSettings( new Settings(), 1 ).size() ) );
	}
}
