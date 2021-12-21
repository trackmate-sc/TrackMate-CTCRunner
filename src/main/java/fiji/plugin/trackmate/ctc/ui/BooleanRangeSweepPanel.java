package fiji.plugin.trackmate.ctc.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import fiji.plugin.trackmate.ctc.ui.BooleanParamSweepModel.RangeType;
import fiji.plugin.trackmate.gui.Fonts;

public class BooleanRangeSweepPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final BooleanParamSweepModel defaultValues;

	private final JRadioButton rdbtnTestAll;

	private final JRadioButton rdbtnFixed;

	private final JRadioButton rdbtnTrue;

	private final JRadioButton rdbtnFalse;

	public BooleanRangeSweepPanel( final BooleanParamSweepModel val )
	{
		this.defaultValues = val;
		setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 80, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblParamName = new JLabel( val.paramName );
		final GridBagConstraints gbcLblParamName = new GridBagConstraints();
		gbcLblParamName.gridwidth = 3;
		gbcLblParamName.insets = new Insets( 0, 0, 5, 0 );
		gbcLblParamName.gridx = 0;
		gbcLblParamName.gridy = 0;
		add( lblParamName, gbcLblParamName );

		rdbtnTestAll = new JRadioButton( "Test true and false", val.type == RangeType.TEST_ALL );
		final GridBagConstraints gbc_rdbtnTestAll = new GridBagConstraints();
		gbc_rdbtnTestAll.gridwidth = 3;
		gbc_rdbtnTestAll.anchor = GridBagConstraints.WEST;
		gbc_rdbtnTestAll.insets = new Insets( 0, 0, 5, 5 );
		gbc_rdbtnTestAll.gridx = 0;
		gbc_rdbtnTestAll.gridy = 1;
		add( rdbtnTestAll, gbc_rdbtnTestAll );

		rdbtnFixed = new JRadioButton( "Fixed value", val.type == RangeType.FIXED );
		final GridBagConstraints gbcRdbtnFixed = new GridBagConstraints();
		gbcRdbtnFixed.anchor = GridBagConstraints.WEST;
		gbcRdbtnFixed.insets = new Insets( 0, 0, 0, 5 );
		gbcRdbtnFixed.gridx = 0;
		gbcRdbtnFixed.gridy = 2;
		add( rdbtnFixed, gbcRdbtnFixed );

		rdbtnTrue = new JRadioButton( "true", val.fixedValue );
		final GridBagConstraints gbc_rdbtnTrue = new GridBagConstraints();
		gbc_rdbtnTrue.insets = new Insets( 0, 0, 0, 5 );
		gbc_rdbtnTrue.gridx = 1;
		gbc_rdbtnTrue.gridy = 2;
		add( rdbtnTrue, gbc_rdbtnTrue );

		rdbtnFalse = new JRadioButton( "false", !val.fixedValue );
		final GridBagConstraints gbc_rdbtnFalse = new GridBagConstraints();
		gbc_rdbtnFalse.gridx = 2;
		gbc_rdbtnFalse.gridy = 2;
		add( rdbtnFalse, gbc_rdbtnFalse );

		// Fonts.
		GuiUtils.changeFont( this, Fonts.SMALL_FONT );
		lblParamName.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );

		// Radio buttons.
		final ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add( rdbtnFixed );
		buttonGroup1.add( rdbtnTestAll );
		final ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add( rdbtnFalse );
		buttonGroup2.add( rdbtnTrue );

		// Enable / Disable.
		update();

		// Listeners.
		final ItemListener il = e -> update();
		rdbtnFixed.addItemListener( il );
		rdbtnTestAll.addItemListener( il );
	}

	private void update()
	{
		final BooleanParamSweepModel vals = getModel();
		switch ( vals.type )
		{
		case FIXED:
			rdbtnFalse.setEnabled( true );
			rdbtnTrue.setEnabled( true );
			break;
		case TEST_ALL:
			rdbtnFalse.setEnabled( false );
			rdbtnTrue.setEnabled( false );
			break;
		default:
			throw new IllegalArgumentException( "Unknown range type: " + vals.type );
		}
	}

	private BooleanParamSweepModel getModel()
	{
		final RangeType type = ( rdbtnTestAll.isSelected() ) ? RangeType.TEST_ALL : RangeType.FIXED;
		return BooleanParamSweepModel.create()
				.paramName( defaultValues.paramName )
				.rangeType( type )
				.fixedValue( rdbtnTrue.isSelected() )
				.get();
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final JFrame frame = new JFrame();
		frame.getContentPane().add( new BooleanRangeSweepPanel( BooleanParamSweepModel.create().get() ) );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
