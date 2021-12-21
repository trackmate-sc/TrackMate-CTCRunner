package fiji.plugin.trackmate.ctc.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import fiji.plugin.trackmate.ctc.ui.DoubleParamSweepModel.RangeType;
import fiji.plugin.trackmate.gui.Fonts;

public class DoubleRangeSweepPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final JTextField tfValues;

	private final DoubleParamSweepModel defaultValues;

	private final JRadioButton rdbtnRane;

	private final JFormattedTextField ftfFromValue;

	private final JFormattedTextField ftfToValue;

	private final SpinnerNumberModel spinnerNumberModel;

	private final JCheckBox chckbxLog;

	private final JFormattedTextField ftfFixedValue;

	private final JRadioButton rdbtnManualRange;

	private final JRadioButton rdbtnFixed;

	public DoubleRangeSweepPanel( final DoubleParamSweepModel val )
	{
		this.defaultValues = val;
		setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 80, 0, 0, 80, 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblParamName = new JLabel( val.paramName );
		final GridBagConstraints gbcLblParamName = new GridBagConstraints();
		gbcLblParamName.gridwidth = 9;
		gbcLblParamName.insets = new Insets( 0, 0, 5, 0 );
		gbcLblParamName.gridx = 0;
		gbcLblParamName.gridy = 0;
		add( lblParamName, gbcLblParamName );

		rdbtnRane = new JRadioButton( "Range of values", val.type == RangeType.LIN_RANGE || val.type == RangeType.LOG_RANGE );
		final GridBagConstraints gbcRdbtnSweep = new GridBagConstraints();
		gbcRdbtnSweep.anchor = GridBagConstraints.WEST;
		gbcRdbtnSweep.gridwidth = 4;
		gbcRdbtnSweep.insets = new Insets( 0, 0, 5, 5 );
		gbcRdbtnSweep.gridx = 0;
		gbcRdbtnSweep.gridy = 1;
		add( rdbtnRane, gbcRdbtnSweep );

		chckbxLog = new JCheckBox( "Logarithmic scale", val.type == RangeType.LOG_RANGE );
		final GridBagConstraints gbcChckbxLog = new GridBagConstraints();
		gbcChckbxLog.anchor = GridBagConstraints.EAST;
		gbcChckbxLog.gridwidth = 12;
		gbcChckbxLog.insets = new Insets( 0, 0, 5, 0 );
		gbcChckbxLog.gridx = 5;
		gbcChckbxLog.gridy = 1;
		add( chckbxLog, gbcChckbxLog );

		final GridBagConstraints gbcLblFrom = new GridBagConstraints();
		gbcLblFrom.insets = new Insets( 0, 0, 5, 5 );
		gbcLblFrom.anchor = GridBagConstraints.EAST;
		gbcLblFrom.gridx = 0;
		gbcLblFrom.gridy = 2;
		add( new JLabel( "From" ), gbcLblFrom );

		ftfFromValue = new JFormattedTextField( Double.valueOf( val.min ) );
		ftfFromValue.setHorizontalAlignment( SwingConstants.TRAILING );
		ftfFromValue.setColumns( 6 );
		final GridBagConstraints gbcFtfFromValue = new GridBagConstraints();
		gbcFtfFromValue.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfFromValue.insets = new Insets( 0, 0, 5, 5 );
		gbcFtfFromValue.gridx = 1;
		gbcFtfFromValue.gridy = 2;
		add( ftfFromValue, gbcFtfFromValue );

		final GridBagConstraints gbcLblUnit1 = new GridBagConstraints();
		gbcLblUnit1.insets = new Insets( 0, 0, 5, 5 );
		gbcLblUnit1.gridx = 2;
		gbcLblUnit1.gridy = 2;
		add( new JLabel( val.units ), gbcLblUnit1 );

		final GridBagConstraints gbcLblTo = new GridBagConstraints();
		gbcLblTo.anchor = GridBagConstraints.EAST;
		gbcLblTo.insets = new Insets( 0, 0, 5, 5 );
		gbcLblTo.gridx = 3;
		gbcLblTo.gridy = 2;
		add( new JLabel( "to" ), gbcLblTo );

		ftfToValue = new JFormattedTextField( Double.valueOf( val.max ) );
		ftfToValue.setHorizontalAlignment( SwingConstants.TRAILING );
		ftfToValue.setColumns( 6 );
		final GridBagConstraints gbcFtfToValue = new GridBagConstraints();
		gbcFtfToValue.insets = new Insets( 0, 0, 5, 5 );
		gbcFtfToValue.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfToValue.gridx = 4;
		gbcFtfToValue.gridy = 2;
		add( ftfToValue, gbcFtfToValue );

		final GridBagConstraints gbcLblUnit2 = new GridBagConstraints();
		gbcLblUnit2.insets = new Insets( 0, 0, 5, 5 );
		gbcLblUnit2.gridx = 5;
		gbcLblUnit2.gridy = 2;
		add( new JLabel( val.units ), gbcLblUnit2 );

		final GridBagConstraints gbcLblIn = new GridBagConstraints();
		gbcLblIn.insets = new Insets( 0, 0, 5, 5 );
		gbcLblIn.gridx = 6;
		gbcLblIn.gridy = 2;
		add( new JLabel( "in" ), gbcLblIn );

		spinnerNumberModel = new SpinnerNumberModel( val.nSteps, 2, 100, 1 );
		final JSpinner spinnerNSteps = new JSpinner( spinnerNumberModel );
		final GridBagConstraints gbcSpinnerNSteps = new GridBagConstraints();
		gbcSpinnerNSteps.insets = new Insets( 0, 0, 5, 5 );
		gbcSpinnerNSteps.gridx = 7;
		gbcSpinnerNSteps.gridy = 2;
		add( spinnerNSteps, gbcSpinnerNSteps );

		final GridBagConstraints gbcLblSteps = new GridBagConstraints();
		gbcLblSteps.insets = new Insets( 0, 0, 5, 0 );
		gbcLblSteps.gridx = 8;
		gbcLblSteps.gridy = 2;
		add( new JLabel( "steps" ), gbcLblSteps );

		rdbtnManualRange = new JRadioButton( "Manual range" );
		final GridBagConstraints gbc_rdbtnManualRange = new GridBagConstraints();
		gbc_rdbtnManualRange.anchor = GridBagConstraints.WEST;
		gbc_rdbtnManualRange.gridwidth = 9;
		gbc_rdbtnManualRange.insets = new Insets( 0, 0, 5, 0 );
		gbc_rdbtnManualRange.gridx = 0;
		gbc_rdbtnManualRange.gridy = 3;
		add( rdbtnManualRange, gbc_rdbtnManualRange );

		tfValues = new JTextField( DoubleParamSweepModel.str( val.getRange() ) );
		tfValues.setHorizontalAlignment( SwingConstants.CENTER );
		tfValues.setBorder( null );
		tfValues.setEditable( false );
		final GridBagConstraints gbc_tfValues = new GridBagConstraints();
		gbc_tfValues.gridwidth = 9;
		gbc_tfValues.insets = new Insets( 0, 0, 5, 0 );
		gbc_tfValues.fill = GridBagConstraints.BOTH;
		gbc_tfValues.gridx = 0;
		gbc_tfValues.gridy = 4;
		add( tfValues, gbc_tfValues );
		tfValues.setColumns( 10 );

		rdbtnFixed = new JRadioButton( "Fixed value", val.type == RangeType.FIXED );
		final GridBagConstraints gbcRdbtnFixed = new GridBagConstraints();
		gbcRdbtnFixed.anchor = GridBagConstraints.WEST;
		gbcRdbtnFixed.gridwidth = 4;
		gbcRdbtnFixed.insets = new Insets( 0, 0, 0, 5 );
		gbcRdbtnFixed.gridx = 0;
		gbcRdbtnFixed.gridy = 5;
		add( rdbtnFixed, gbcRdbtnFixed );

		ftfFixedValue = new JFormattedTextField( Double.valueOf( val.min ) );
		ftfFixedValue.setHorizontalAlignment( SwingConstants.TRAILING );
		ftfFixedValue.setColumns( 6 );
		final GridBagConstraints gbcFtfFixedValue = new GridBagConstraints();
		gbcFtfFixedValue.insets = new Insets( 0, 0, 0, 5 );
		gbcFtfFixedValue.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfFixedValue.gridx = 4;
		gbcFtfFixedValue.gridy = 5;
		add( ftfFixedValue, gbcFtfFixedValue );

		final GridBagConstraints gbcLblUnit3 = new GridBagConstraints();
		gbcLblUnit3.insets = new Insets( 0, 0, 0, 5 );
		gbcLblUnit3.gridx = 5;
		gbcLblUnit3.gridy = 5;
		add( new JLabel( val.units ), gbcLblUnit3 );

		// Fonts.
		GuiUtils.changeFont( this, Fonts.SMALL_FONT );
		lblParamName.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );

		// Focus.
		fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( ftfFromValue );
		fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( ftfToValue );
		fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( ftfFixedValue );

		// Radiu buttons.
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( rdbtnFixed );
		buttonGroup.add( rdbtnRane );
		buttonGroup.add( rdbtnManualRange );

		// Listeners.
		final PropertyChangeListener pcl = e -> updateRange();
		ftfFromValue.addPropertyChangeListener( "value", pcl );
		ftfToValue.addPropertyChangeListener( "value", pcl );
		ftfFixedValue.addPropertyChangeListener( "value", pcl );
		final ChangeListener cl = e -> updateRange();
		spinnerNumberModel.addChangeListener( cl );
		final ItemListener il = e -> updateRange();
		rdbtnFixed.addItemListener( il );
		rdbtnRane.addItemListener( il );
		rdbtnManualRange.addItemListener( il );
		chckbxLog.addItemListener( il );
	}

	private void updateRange()
	{
		final DoubleParamSweepModel vals = getModel();
		tfValues.setText( DoubleParamSweepModel.str( vals.getRange() ) );
	}

	private DoubleParamSweepModel getModel()
	{
		final double min = ( ( Number ) ftfFromValue.getValue() ).doubleValue();
		final double max = ( ( Number ) ftfToValue.getValue() ).doubleValue();
		final double fixed = ( ( Number ) ftfFixedValue.getValue() ).doubleValue();
		RangeType type;
		if ( rdbtnRane.isSelected() )
		{
			if ( chckbxLog.isSelected() )
				type = RangeType.LOG_RANGE;
			else
				type = RangeType.LIN_RANGE;
		}
		else if ( rdbtnFixed.isSelected() )
			type = RangeType.FIXED;
		else
			type = RangeType.MANUAL;

		return DoubleParamSweepModel.create()
				.paramName( defaultValues.paramName )
				.units( defaultValues.units )
				.rangeType( type )
				.min( type == RangeType.FIXED ? fixed : min )
				.max( max )
				.nSteps( ( ( Number ) spinnerNumberModel.getValue() ).intValue() )
//				.rangeType( parseRange() ) // TODO
				.get();
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final JFrame frame = new JFrame();
		frame.getContentPane().add( new DoubleRangeSweepPanel( DoubleParamSweepModel.create().get() ) );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}

}
