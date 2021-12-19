package fiji.plugin.trackmate.ctc.ui;

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
import ij.ImagePlus;

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
	}

	public void refreshFeatures( final ImagePlus imp )
	{
		// TODO
	}

	public FeatureFilter getFilter()
	{
		return new FeatureFilter(
				( String ) cmbboxFeatureKeys.getSelectedItem(),
				( ( Number ) ftfThreshold.getValue() ).doubleValue(),
				rdnbtnAbove.isSelected() );
	}
}
