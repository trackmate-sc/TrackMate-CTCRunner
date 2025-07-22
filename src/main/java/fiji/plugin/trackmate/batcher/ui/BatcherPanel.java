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
package fiji.plugin.trackmate.batcher.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scijava.util.VersionUtils;

import com.itextpdf.text.Font;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.gui.components.LogPanel;

public class BatcherPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	final JButton btnRun;

	final JButton btnCancel;

	public final Logger logger;

	public BatcherPanel( final BatcherModel model )
	{
		setLayout( new BorderLayout( 5, 5 ) );
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		final JLabel lblTitle = new JLabel( "<html><center>TrackMate Batcher <small>v"
				+ VersionUtils.getVersion( BatcherPanel.class )
				+ "</small></center></html>" );
		lblTitle.setIcon( Icons.TRACKMATE_ICON_16x16 );
		lblTitle.setFont( Fonts.BIG_FONT );
		add( lblTitle, BorderLayout.NORTH );

		final JPanel mainPanel = new JPanel();
		add( mainPanel, BorderLayout.CENTER );
		mainPanel.setLayout( new GridLayout( 2, 2, 5, 5 ) );

		final JPanel panelInput = new JPanel();
		mainPanel.add( panelInput );
		panelInput.setLayout( new BorderLayout( 0, 0 ) );

		final JPanel fileListHeaderPanel = new JPanel();
		fileListHeaderPanel.setLayout( new BoxLayout( fileListHeaderPanel, BoxLayout.X_AXIS ) );
		final JLabel lblInput = new JLabel( "Input images" );
		lblInput.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );
		fileListHeaderPanel.add( lblInput );
		fileListHeaderPanel.add( Box.createHorizontalGlue() );
		final JButton clearAllFilesBtn = new JButton( Icons.BIN_ICON );
		fileListHeaderPanel.add( clearAllFilesBtn );
		final JLabel lblClearAll = new JLabel( "Clear all" );
		lblClearAll.setFont( Fonts.FONT );
		fileListHeaderPanel.add( lblClearAll );

		final FileListModel fileListModel = model.getFileListModel();
		clearAllFilesBtn.addActionListener( e -> fileListModel.removeAll() );

		panelInput.add( fileListHeaderPanel, BorderLayout.NORTH );
		final FileListPanel fileListPanel = new FileListPanel( fileListModel );
		fileListPanel.setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
		fileListPanel.setPreferredSize( new Dimension( 200, 200 ) );
		panelInput.add( fileListPanel );

		final JPanel panelRun = new JPanel();
		mainPanel.add( panelRun );
		panelRun.setLayout( new BorderLayout( 0, 0 ) );

		final JLabel lblSettings = new JLabel( "TrackMate settings" );
		lblSettings.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );
		panelRun.add( lblSettings, BorderLayout.NORTH );
		panelRun.add( new TrackMateReadConfigPanel( model.getTrackMateReadConfigModel() ) );

		final JPanel panelSettings = new JPanel();
		panelSettings.setLayout( new BorderLayout( 5, 5 ) );

		final JLabel labelRun = new JLabel( "Outputs" );
		labelRun.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );
		panelSettings.add( labelRun, BorderLayout.NORTH );
		panelSettings.add( new RunBatchPanel( model.getRunParamModel() ) );
		mainPanel.add( panelSettings );

		final JPanel log = new JPanel();
		log.setLayout( new BorderLayout( 5, 5 ) );

		final JLabel labelLog = new JLabel( "Execution log" );
		labelLog.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );
		log.add( labelLog, BorderLayout.NORTH );

		final LogPanel logPanel = new LogPanel();
		this.logger = logPanel.getLogger();
		logPanel.setPreferredSize( new Dimension( 300, 200 ) );
		log.add( logPanel );

		btnRun = new JButton( "Run", Icons.EXECUTE_ICON );
		btnCancel = new JButton( "Cancel", Icons.CANCEL_ICON );
		btnRun.setFont( Fonts.SMALL_FONT );
		btnCancel.setFont( Fonts.SMALL_FONT );
		btnCancel.setVisible( false );

		mainPanel.add( log );

		final JPanel panelButtons = new JPanel();
		panelButtons.setLayout( new BoxLayout( panelButtons, BoxLayout.X_AXIS ) );
		final Component horizontalGlue = Box.createHorizontalGlue();
		panelButtons.add( horizontalGlue );
		panelButtons.add( btnCancel );
		panelButtons.add( btnRun );
		add( panelButtons, BorderLayout.SOUTH );
	}
}
