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
package fiji.plugin.trackmate.helper.ui.components;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel.RangeType;

public class BooleanRangeSweepPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final BooleanParamSweepModel values;

	private final JRadioButton rdbtnTestAll;

	private final JRadioButton rdbtnFixed;

	private final JRadioButton rdbtnTrue;

	private final JRadioButton rdbtnFalse;

	public BooleanRangeSweepPanel( final BooleanParamSweepModel values )
	{
		this.values = values;
		setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 80, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblParamName = new JLabel( values.getParamName() );
		final GridBagConstraints gbcLblParamName = new GridBagConstraints();
		gbcLblParamName.gridwidth = 3;
		gbcLblParamName.insets = new Insets( 0, 0, 5, 0 );
		gbcLblParamName.gridx = 0;
		gbcLblParamName.gridy = 0;
		add( lblParamName, gbcLblParamName );

		rdbtnTestAll = new JRadioButton( "Test true and false", values.getRangeType() == RangeType.TEST_ALL );
		final GridBagConstraints gbc_rdbtnTestAll = new GridBagConstraints();
		gbc_rdbtnTestAll.gridwidth = 3;
		gbc_rdbtnTestAll.anchor = GridBagConstraints.WEST;
		gbc_rdbtnTestAll.insets = new Insets( 0, 0, 5, 5 );
		gbc_rdbtnTestAll.gridx = 0;
		gbc_rdbtnTestAll.gridy = 1;
		add( rdbtnTestAll, gbc_rdbtnTestAll );

		rdbtnFixed = new JRadioButton( "Fixed value", values.getRangeType() == RangeType.FIXED );
		final GridBagConstraints gbcRdbtnFixed = new GridBagConstraints();
		gbcRdbtnFixed.anchor = GridBagConstraints.WEST;
		gbcRdbtnFixed.insets = new Insets( 0, 0, 0, 5 );
		gbcRdbtnFixed.gridx = 0;
		gbcRdbtnFixed.gridy = 2;
		add( rdbtnFixed, gbcRdbtnFixed );

		rdbtnTrue = new JRadioButton( "true", values.getFixedValue() );
		final GridBagConstraints gbc_rdbtnTrue = new GridBagConstraints();
		gbc_rdbtnTrue.insets = new Insets( 0, 0, 0, 5 );
		gbc_rdbtnTrue.gridx = 1;
		gbc_rdbtnTrue.gridy = 2;
		add( rdbtnTrue, gbc_rdbtnTrue );

		rdbtnFalse = new JRadioButton( "false", !values.getFixedValue() );
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
		final ItemListener il = new ItemListener()
		{

			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				// Only fire once for the one who gets selected.
				if ( e.getStateChange() == ItemEvent.SELECTED )
					update();
			}
		};
		rdbtnFixed.addItemListener( il );
		rdbtnTestAll.addItemListener( il );
		rdbtnFalse.addItemListener( il );
		rdbtnTrue.addItemListener( il );
	}

	private void update()
	{
		// Update model.
		final RangeType type = ( rdbtnTestAll.isSelected() ) ? RangeType.TEST_ALL : RangeType.FIXED;
		values.rangeType( type )
				.fixedValue( rdbtnTrue.isSelected() );

		// Update UI.
		switch ( values.getRangeType() )
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
			throw new IllegalArgumentException( "Unknown range type: " + values.getRangeType() );
		}
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final BooleanParamSweepModel model = new BooleanParamSweepModel();
		model.listeners().add( () -> System.out.println( model ) );

		final JFrame frame = new JFrame();
		frame.getContentPane().add( new BooleanRangeSweepPanel( model ) );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
