package fiji.plugin.trackmate.ctc.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.model.AbstractSweepModel;
import fiji.plugin.trackmate.ctc.model.AbstractSweepModel.ModelListener;
import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.ArrayParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.InfoParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.NumberParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.StringRangeParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.ArrayRangeSweepPanel;
import fiji.plugin.trackmate.ctc.ui.components.BooleanRangeSweepPanel;
import fiji.plugin.trackmate.ctc.ui.components.InfoPanel;
import fiji.plugin.trackmate.ctc.ui.components.NumberRangeSweepPanel;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepPanel;
import fiji.plugin.trackmate.gui.Fonts;

/**
 * Panel that lets the user configure a parameter sweep over a TrackMate module
 * (detector or tracker).
 * 
 * @author Jean-Yves Tinevez
 */
public class ModuleParameterSweepPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public ModuleParameterSweepPanel( final AbstractSweepModel< ? > model, final String spaceUnits, final String timeUnits )
	{
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		setLayout( new BorderLayout() );

		// Top panel.
		final JPanel topPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		final JLabel lblInfo = new JLabel( " " );
		lblInfo.setFont( Fonts.SMALL_FONT );
		topPanel.add( lblInfo );
		add( topPanel, BorderLayout.NORTH );

		// Main panel.
		final JPanel mainPanel = new JPanel();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 40, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		mainPanel.setLayout( gridBagLayout );

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTH;

		// Model components.
		for ( final AbstractParamSweepModel< ? > cm : model.getModels().values() )
		{
			final JPanel p = createPanelFor( cm, spaceUnits, timeUnits );
			mainPanel.add( p, c );
			c.gridy++;

			mainPanel.add( new JSeparator(), c );
			c.gridy++;
		}

		final int n = c.gridy == 0 ? 1 : c.gridy;
		gridBagLayout.rowWeights = new double[ n ];
		gridBagLayout.rowWeights[ n - 1 ] = Double.MIN_VALUE;

		// Scroll pane.
		final JScrollPane scrollPane = new JScrollPane( mainPanel );
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.getVerticalScrollBar().setUnitIncrement( 16 );

		add( scrollPane, BorderLayout.CENTER );

		final ModelListener infoListener = () -> {
			final Iterator< Settings > it = model.iterator( new Settings(), 1 );
			int ns = 0;
			while ( it.hasNext() )
			{
				it.next();
				ns++;
			}
			final String str = ( ns == 1 )
					? "Sweep over one setting for this detector."
					: String.format( "Sweep over %d different settings for this detector.", ns );
			lblInfo.setText( str );
		};
		infoListener.modelChanged();
		model.listeners().add( infoListener );
	}

	private static final JPanel createPanelFor( final AbstractParamSweepModel< ? > cm, final String spaceUnits, final String timeUnits )
	{
		if ( cm instanceof BooleanParamSweepModel )
			return new BooleanRangeSweepPanel( ( BooleanParamSweepModel ) cm );
		else if ( cm instanceof NumberParamSweepModel )
			return new NumberRangeSweepPanel( ( NumberParamSweepModel ) cm, spaceUnits, timeUnits );
		else if ( cm instanceof StringRangeParamSweepModel )
			return new StringRangeParamSweepPanel( ( StringRangeParamSweepModel ) cm );
		else if ( cm instanceof ArrayParamSweepModel< ? > )
			return new ArrayRangeSweepPanel<>( ( ArrayParamSweepModel< ? > ) cm );
		else if ( cm instanceof InfoParamSweepModel )
			return new InfoPanel( ( InfoParamSweepModel ) cm );
		else
			throw new IllegalArgumentException( "Do not know how to create a panel for model: " + cm.getClass() );
	}
}
