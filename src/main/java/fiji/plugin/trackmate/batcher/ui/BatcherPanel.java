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

		final JLabel lblInput = new JLabel( "Input images" );
		lblInput.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );
		panelInput.add( lblInput, BorderLayout.NORTH );
		final FileListPanel fileListPanel = new FileListPanel( model.getFileListModel() );
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
