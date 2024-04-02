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

import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import fiji.plugin.trackmate.features.FeatureFilter;

public class FilterPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private static final Dimension panelSize = new java.awt.Dimension( 250, 30 );

	private static final Dimension panelMaxSize = new java.awt.Dimension( 1000, 30 );

	private JComboBox< String > cmbboxFeatureKeys;

	private JFormattedTextField ftfThreshold;

	private JRadioButton rdnbtnAbove;

	public FilterPanel( final Map< String, String > keyNames, final FeatureFilter filter )
	{
		setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );
		setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		setPreferredSize( panelSize );
		setMaximumSize( panelMaxSize );

		final ComboBoxModel< String > cmbboxFeatureNameModel = new DefaultComboBoxModel<>( keyNames.keySet().toArray( new String[] {} ) );
		cmbboxFeatureKeys = new JComboBox<>( cmbboxFeatureNameModel );
		cmbboxFeatureKeys.setRenderer( new DefaultListCellRenderer()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent( final JList< ? > list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
			{
				final JLabel lbl = ( JLabel ) super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
				lbl.setText( keyNames.get( value ) );
				return lbl;
			}
		} );
		cmbboxFeatureKeys.setFont( SMALL_FONT );
		add( cmbboxFeatureKeys );
		add( Box.createHorizontalStrut( 10 ) );

		ftfThreshold = new JFormattedTextField( Double.valueOf( filter.value ) );
		ftfThreshold.setColumns( 6 );
		ftfThreshold.setHorizontalAlignment( JFormattedTextField.RIGHT );
		ftfThreshold.setFont( SMALL_FONT );
		add( ftfThreshold );
		add( Box.createHorizontalStrut( 10 ) );

		rdnbtnAbove = new JRadioButton( "Above" );
		rdnbtnAbove.setFont( SMALL_FONT );
		add( rdnbtnAbove );

		final JRadioButton rdbtnBelow = new JRadioButton( "Below" );
		rdbtnBelow.setFont( SMALL_FONT );
		add( rdbtnBelow );
	
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( rdbtnBelow );
		buttonGroup.add( rdnbtnAbove );

		// Set default values.
		cmbboxFeatureKeys.setSelectedItem( filter.feature );
		rdnbtnAbove.setSelected( filter.isAbove );
		rdbtnBelow.setSelected( !filter.isAbove );
	}

	public FeatureFilter getFilter()
	{
		return new FeatureFilter(
				( String ) cmbboxFeatureKeys.getSelectedItem(),
				( ( Number ) ftfThreshold.getValue() ).doubleValue(),
				rdnbtnAbove.isSelected() );
	}
}
