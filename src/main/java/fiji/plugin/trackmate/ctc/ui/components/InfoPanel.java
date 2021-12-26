package fiji.plugin.trackmate.ctc.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import fiji.plugin.trackmate.gui.Fonts;

public class InfoPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public InfoPanel( final InfoParamSweepModel values )
	{
		setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblParamName = new JLabel( values.paramName );
		final GridBagConstraints gbcLblParamName = new GridBagConstraints();
		gbcLblParamName.insets = new Insets( 0, 0, 5, 0 );
		gbcLblParamName.gridx = 0;
		gbcLblParamName.gridy = 0;
		add( lblParamName, gbcLblParamName );

		final JLabel lblInfo = new JLabel( "<html>" + values.info + "</html>" );
		final GridBagConstraints gbcLblInfo = new GridBagConstraints();
		gbcLblInfo.fill = GridBagConstraints.BOTH;
		gbcLblInfo.insets = new Insets( 0, 0, 5, 0 );
		gbcLblInfo.gridx = 0;
		gbcLblInfo.gridy = 1;
		add( lblInfo, gbcLblInfo );

		final JLabel lblUrl = new JLabel( values.url );
		lblUrl.setForeground( Color.BLUE.darker() );
		lblUrl.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		lblUrl.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final MouseEvent e )
			{
				try
				{
					Desktop.getDesktop().browse( new URI( values.url ) );
				}
				catch ( final URISyntaxException | IOException urie )
				{
					urie.printStackTrace();
				}
			}

			@Override
			public void mouseEntered( final MouseEvent e )
			{
				lblUrl.setText( "<html><a href=" + values.url + ">" + values.url + "</a></html>" );
			}

			@Override
			public void mouseExited( final MouseEvent e )
			{
				lblUrl.setText( "<html>" + values.url + "</html>" );
			}
		} );

		final GridBagConstraints gbcLblUrl = new GridBagConstraints();
		gbcLblUrl.fill = GridBagConstraints.HORIZONTAL;
		gbcLblUrl.anchor = GridBagConstraints.NORTH;
		gbcLblUrl.gridx = 0;
		gbcLblUrl.gridy = 2;
		add( lblUrl, gbcLblUrl );

		// Fonts.
		GuiUtils.changeFont( this, Fonts.SMALL_FONT );
		lblParamName.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );
	}
}
