package fiji.plugin.trackmate.ctc.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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

import fiji.plugin.trackmate.ctc.ui.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.gui.Fonts;

public class RangeSweepPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final JTextField tfValues;

	private final NumberParamSweepModel defaultValues;

	private final JRadioButton rdbtnRane;

	private final JFormattedTextField ftfFromValue;

	private final JFormattedTextField ftfToValue;

	private final JCheckBox chckbxLog;

	private final JFormattedTextField ftfFixedValue;

	private final JRadioButton rdbtnManualRange;

	private final JRadioButton rdbtnFixed;

	private final JSpinner spinnerNSteps;

	public RangeSweepPanel( final NumberParamSweepModel val )
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
		gbcChckbxLog.gridwidth = 4;
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

		ftfFromValue = new JFormattedTextField( val.min );
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

		ftfToValue = new JFormattedTextField( val.max );
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

		final SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel( val.nSteps, 2, 100, 1 );
		spinnerNSteps = new JSpinner( spinnerNumberModel );
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

		tfValues = new JTextField( Arrays.toString( val.getRange() ) );
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

		ftfFixedValue = new JFormattedTextField( val.min );
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

		// Radio buttons.
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( rdbtnFixed );
		buttonGroup.add( rdbtnRane );
		buttonGroup.add( rdbtnManualRange );

		// Enable / Disable.
		update();

		// Listeners.
		final PropertyChangeListener pcl = e -> update();
		ftfFromValue.addPropertyChangeListener( "value", pcl );
		ftfToValue.addPropertyChangeListener( "value", pcl );
		ftfFixedValue.addPropertyChangeListener( "value", pcl );
		final ChangeListener cl = e -> update();
		spinnerNumberModel.addChangeListener( cl );
		final ItemListener il = e -> update();
		rdbtnFixed.addItemListener( il );
		rdbtnRane.addItemListener( il );
		rdbtnManualRange.addItemListener( il );
		chckbxLog.addItemListener( il );
		final ActionListener al = e -> update();
		tfValues.addActionListener( al );
		final FocusAdapter fa = new FocusAdapter()
		{
			@Override
			public void focusLost( final java.awt.event.FocusEvent e )
			{
				update();
			}
		};
		tfValues.addFocusListener( fa );
	}

	private void update()
	{
		final NumberParamSweepModel vals = getModel();

		final String rangeStr = DoubleParamSweepModel.str( vals.getRange() );
		if ( !rangeStr.equals( tfValues.getText() ) )
			tfValues.setText( rangeStr );

		switch ( vals.type )
		{
		case FIXED:
			ftfFixedValue.setEnabled( true );
			ftfFromValue.setEnabled( false );
			ftfToValue.setEnabled( false );
			spinnerNSteps.setEnabled( false );
			chckbxLog.setEnabled( false );
			tfValues.setEditable( false );
			tfValues.setBackground( getBackground() );
			break;
		case LIN_RANGE:
		case LOG_RANGE:
			ftfFixedValue.setEnabled( false );
			ftfFromValue.setEnabled( true );
			ftfToValue.setEnabled( true );
			spinnerNSteps.setEnabled( true );
			chckbxLog.setEnabled( true );
			tfValues.setEditable( false );
			tfValues.setBackground( getBackground() );
			break;
		case MANUAL:
			ftfFixedValue.setEnabled( false );
			ftfFromValue.setEnabled( false );
			ftfToValue.setEnabled( false );
			spinnerNSteps.setEnabled( false );
			chckbxLog.setEnabled( false );
			tfValues.setEditable( true );
			tfValues.setBackground( ftfFixedValue.getBackground() );
			break;
		default:
			throw new IllegalArgumentException( "Unknown range type: " + vals.type );
		}
	}

	private NumberParamSweepModel getModel()
	{
		final Number min = ( ( Number ) ftfFromValue.getValue() );
		final Number max = ( ( Number ) ftfToValue.getValue() );
		final Number fixed = ( ( Number ) ftfFixedValue.getValue() );
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

		if ( defaultValues instanceof DoubleParamSweepModel )
		{
			return DoubleParamSweepModel.create()
					.paramName( defaultValues.paramName )
					.units( defaultValues.units )
					.rangeType( type )
					.min( type == RangeType.FIXED ? fixed.doubleValue() : min.doubleValue() )
					.max( max.doubleValue() )
					.nSteps( ( ( Number ) spinnerNSteps.getValue() ).intValue() )
					.manualRange( parseRange() )
					.get();
		}
		else
		{
			final Double[] range = parseRange();
			final Integer[] arr = new Integer[ range.length ];
			for ( int i = 0; i < range.length; i++ )
				arr[ i ] = range[ i ].intValue();

			return IntParamSweepModel.create()
					.paramName( defaultValues.paramName )
					.units( defaultValues.units )
					.rangeType( type )
					.min( type == RangeType.FIXED ? fixed.intValue() : min.intValue() )
					.max( max.intValue() )
					.nSteps( ( ( Number ) spinnerNSteps.getValue() ).intValue() )
					.manualRange( arr )
					.get();
		}
	}

	private Double[] parseRange()
	{
		final String str = tfValues.getText();
		final List< Double > vals = new ArrayList<>();
		try (final Scanner scanner = new Scanner( str ))
		{
			scanner.useDelimiter( "\\s*,\\s*|\\s+" );
			while ( scanner.hasNext() )
			{
				if ( scanner.hasNextDouble() )
				{
					final double val = scanner.nextDouble();
					vals.add( Double.valueOf( val ) );
				}
				else
				{
					scanner.next();
				}
			}
		}
		final Double[] arr = new Double[ vals.size() ];
		for ( int i = 0; i < vals.size(); i++ )
			arr[ i ] = vals.get( i ).doubleValue();

		return arr;
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final JFrame frame1 = new JFrame();
		frame1.getContentPane().add( new RangeSweepPanel( DoubleParamSweepModel.create().get() ) );
		frame1.pack();
		frame1.setLocationRelativeTo( null );
		frame1.setVisible( true );

		final JFrame frame2 = new JFrame();
		frame2.getContentPane().add( new RangeSweepPanel( IntParamSweepModel.create().get() ) );
		frame2.pack();
		frame2.setLocationRelativeTo( null );
		frame2.setVisible( true );
	}
}
