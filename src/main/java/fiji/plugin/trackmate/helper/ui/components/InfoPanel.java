/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
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
import fiji.plugin.trackmate.helper.model.parameter.InfoParamSweepModel;

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

		final JLabel lblParamName = new JLabel( values.getParamName() );
		final GridBagConstraints gbcLblParamName = new GridBagConstraints();
		gbcLblParamName.insets = new Insets( 0, 0, 5, 0 );
		gbcLblParamName.gridx = 0;
		gbcLblParamName.gridy = 0;
		add( lblParamName, gbcLblParamName );

		final JLabel lblInfo = new JLabel( "<html>" + values.getInfo() + "</html>" );
		final GridBagConstraints gbcLblInfo = new GridBagConstraints();
		gbcLblInfo.fill = GridBagConstraints.BOTH;
		gbcLblInfo.insets = new Insets( 0, 0, 5, 0 );
		gbcLblInfo.gridx = 0;
		gbcLblInfo.gridy = 1;
		add( lblInfo, gbcLblInfo );

		final JLabel lblUrl = new JLabel( values.getUrl() );
		lblUrl.setForeground( Color.BLUE.darker() );
		lblUrl.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		lblUrl.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final MouseEvent e )
			{
				try
				{
					Desktop.getDesktop().browse( new URI( values.getUrl() ) );
				}
				catch ( final URISyntaxException | IOException urie )
				{
					urie.printStackTrace();
				}
			}

			@Override
			public void mouseEntered( final MouseEvent e )
			{
				lblUrl.setText( "<html><a href=" + values.getUrl() + ">" + values.getUrl() + "</a></html>" );
			}

			@Override
			public void mouseExited( final MouseEvent e )
			{
				lblUrl.setText( "<html>" + values.getUrl() + "</html>" );
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
