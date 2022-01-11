/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
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
package fiji.plugin.trackmate.ctc.ui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import fiji.plugin.trackmate.ctc.model.parameter.ArrayParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.ArrayParamSweepModel.RangeType;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.Icons;

public class ArrayRangeSweepPanel< T > extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final JRadioButton rdbtnTestAll;

	private final JRadioButton rdbtnListValues;

	private final JRadioButton rdbtnFixed;

	private final JComboBox< T > cmbboxFixedValue;

	private final JPanel panelListValues;

	private final JComboBox< T > cmbboxValueList;

	private final JButton btnAdd;

	private final JLabel lblValuesList;

	private final ArrayParamSweepModel< T > values;

	private final JButton btnRemove;

	public ArrayRangeSweepPanel( final ArrayParamSweepModel< T > val )
	{
		this.values = val;
		setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		setLayout( gridBagLayout );

		final JLabel lblParamName = new JLabel( val.getParamName() );
		final GridBagConstraints gbcLblParamName = new GridBagConstraints();
		gbcLblParamName.gridwidth = 2;
		gbcLblParamName.insets = new Insets( 0, 0, 5, 0 );
		gbcLblParamName.gridx = 0;
		gbcLblParamName.gridy = 0;
		add( lblParamName, gbcLblParamName );

		rdbtnTestAll = new JRadioButton( "All values", val.getRangeType() == RangeType.TEST_ALL );
		final GridBagConstraints gbc_rdbtnTestAll = new GridBagConstraints();
		gbc_rdbtnTestAll.anchor = GridBagConstraints.WEST;
		gbc_rdbtnTestAll.gridwidth = 2;
		gbc_rdbtnTestAll.insets = new Insets( 0, 0, 5, 0 );
		gbc_rdbtnTestAll.gridx = 0;
		gbc_rdbtnTestAll.gridy = 1;
		add( rdbtnTestAll, gbc_rdbtnTestAll );

		rdbtnListValues = new JRadioButton( "Set of values", val.getRangeType() == RangeType.LIST );
		final GridBagConstraints gbc_rdbtnManualRange = new GridBagConstraints();
		gbc_rdbtnManualRange.anchor = GridBagConstraints.WEST;
		gbc_rdbtnManualRange.gridwidth = 2;
		gbc_rdbtnManualRange.insets = new Insets( 0, 0, 5, 0 );
		gbc_rdbtnManualRange.gridx = 0;
		gbc_rdbtnManualRange.gridy = 2;
		add( rdbtnListValues, gbc_rdbtnManualRange );

		panelListValues = new JPanel();
		final GridBagConstraints gbc_panelListValues = new GridBagConstraints();
		gbc_panelListValues.gridwidth = 2;
		gbc_panelListValues.insets = new Insets( 0, 0, 5, 0 );
		gbc_panelListValues.fill = GridBagConstraints.BOTH;
		gbc_panelListValues.gridx = 0;
		gbc_panelListValues.gridy = 3;
		add( panelListValues, gbc_panelListValues );
		panelListValues.setLayout( new BoxLayout( panelListValues, BoxLayout.X_AXIS ) );

		cmbboxValueList = new JComboBox<>( new Vector<>( val.getAllValues() ) );
		cmbboxValueList.setPreferredSize( new Dimension( 150, 24 ) );
		cmbboxValueList.setMaximumSize( new Dimension( 150, 24 ) );
		btnAdd = new JButton();
		btnAdd.setIcon( Icons.ADD_ICON );
		btnAdd.setMaximumSize( new Dimension( 30, 30 ) );
		btnAdd.setBorder( null );
		btnAdd.setBorderPainted( false );
		btnAdd.setContentAreaFilled( false );
		btnRemove = new JButton();
		btnRemove.setIcon( Icons.REMOVE_ICON );
		btnRemove.setMaximumSize( new Dimension( 30, 30 ) );
		btnRemove.setBorder( null );
		btnRemove.setContentAreaFilled( false );

		panelListValues.add( cmbboxValueList );
		panelListValues.add( Box.createHorizontalStrut( 5 ) );
		panelListValues.add( btnAdd );
		panelListValues.add( Box.createHorizontalStrut( 5 ) );
		panelListValues.add( btnRemove );
		panelListValues.add( Box.createHorizontalStrut( 5 ) );
		panelListValues.add( Box.createHorizontalGlue() );

		lblValuesList = new JLabel();
		panelListValues.add( lblValuesList );

		rdbtnFixed = new JRadioButton( "Fixed value", val.getRangeType() == RangeType.FIXED );
		final GridBagConstraints gbcRdbtnFixed = new GridBagConstraints();
		gbcRdbtnFixed.anchor = GridBagConstraints.WEST;
		gbcRdbtnFixed.insets = new Insets( 0, 0, 0, 5 );
		gbcRdbtnFixed.gridx = 0;
		gbcRdbtnFixed.gridy = 4;
		add( rdbtnFixed, gbcRdbtnFixed );

		cmbboxFixedValue = new JComboBox<>( new Vector<>( val.getAllValues() ) );
		cmbboxFixedValue.setSelectedItem( val.getFixedValue() );
		cmbboxFixedValue.setFont( Fonts.SMALL_FONT );
		cmbboxFixedValue.setPreferredSize( new Dimension( 150, 24 ) );
		final GridBagConstraints gbcFtfFixedValue = new GridBagConstraints();
		gbcFtfFixedValue.anchor = GridBagConstraints.EAST;
		gbcFtfFixedValue.gridx = 1;
		gbcFtfFixedValue.gridy = 4;
		add( cmbboxFixedValue, gbcFtfFixedValue );

		// Fonts.
		GuiUtils.changeFont( this, Fonts.SMALL_FONT );
		lblParamName.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );

		// Radio buttons.
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( rdbtnFixed );
		buttonGroup.add( rdbtnTestAll );
		buttonGroup.add( rdbtnListValues );

		// Enable / Disable.
		update();

		// Listeners.
		btnAdd.addActionListener( e -> addEnum() );
		btnRemove.addActionListener( e -> removeEnum() );
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
		rdbtnListValues.addItemListener( il );
		final ActionListener al = e -> update();
		cmbboxFixedValue.addActionListener( al );
		cmbboxValueList.addActionListener( al );
	}

	private void removeEnum()
	{
		if ( values.getSelection().isEmpty() )
			return;
		values.removeValue( values.getSelection().iterator().next() );
		update();
	}

	@SuppressWarnings( "unchecked" )
	private void addEnum()
	{
		values.addValue( ( T ) cmbboxValueList.getSelectedItem() );
		update();
	}

	@SuppressWarnings( "unchecked" )
	private void update()
	{
		// Update model.
		RangeType type;
		if ( rdbtnListValues.isSelected() )
			type = RangeType.LIST;
		else if ( rdbtnFixed.isSelected() )
			type = RangeType.FIXED;
		else
			type = RangeType.TEST_ALL;
		values.rangeType( type ).fixedValue( ( T ) cmbboxFixedValue.getSelectedItem() );

		// Update UI.
		final String rangeStr = str( values.getRange() );
		lblValuesList.setText( rangeStr );

		switch ( values.getRangeType() )
		{
		case FIXED:
			cmbboxFixedValue.setEnabled( true );
			cmbboxValueList.setEnabled( false );
			btnAdd.setEnabled( false );
			btnRemove.setEnabled( false );
			break;
		case TEST_ALL:
			cmbboxFixedValue.setEnabled( false );
			cmbboxValueList.setEnabled( false );
			btnAdd.setEnabled( false );
			btnRemove.setEnabled( false );
			break;
		case LIST:
			cmbboxFixedValue.setEnabled( false );
			cmbboxValueList.setEnabled( true );
			btnAdd.setEnabled( true );
			btnRemove.setEnabled( true );
			break;
		default:
			throw new IllegalArgumentException( "Unknown range type: " + values.getRangeType() );
		}
	}

	private String str( final List< T > range )
	{
		if ( range.isEmpty() )
			return "[]";

		final StringBuilder str = new StringBuilder();
		str.append( "[ " );
		str.append( String.format( "%s", range.get( 0 ) ) );
		for ( int i = 1; i < range.size(); i++ )
			str.append( String.format( ", %s", range.get( i ) ) );

		str.append( " ]" );
		return str.toString();
	}

	public static void main( final String[] args )
	{
		final ArrayParamSweepModel< RangeType > model = new ArrayParamSweepModel<>( RangeType.values() )
				.paramName( "Test enum" )
				.rangeType( RangeType.LIST )
				.addValue( RangeType.FIXED )
				.addValue( RangeType.TEST_ALL );

		final JFrame frame = new JFrame();
		frame.getContentPane().add( new ArrayRangeSweepPanel<>( model ) );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
