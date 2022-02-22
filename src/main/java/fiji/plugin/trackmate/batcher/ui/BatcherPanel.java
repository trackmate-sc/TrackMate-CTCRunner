package fiji.plugin.trackmate.batcher.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.gui.components.LogPanel;
import net.imagej.ImageJ;

public class BatcherPanel extends JPanel
{

	/**
	 * Create the panel.
	 */
	public BatcherPanel()
	{
		final FileListModel inputModel = new FileListModel();
		final TrackMateReadConfigModel settingsModel = new TrackMateReadConfigModel();

		setLayout( new BorderLayout( 5, 5 ) );
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		final JLabel lblTitle = new JLabel( "TrackMate Batcher v" );
		add( lblTitle, BorderLayout.NORTH );

		final JPanel mainPanel = new JPanel();
		add( mainPanel, BorderLayout.CENTER );
		mainPanel.setLayout( new GridLayout( 2, 2, 5, 5 ) );

		final JPanel panelInput = new JPanel();
		mainPanel.add( panelInput );
		panelInput.setLayout( new BorderLayout( 0, 0 ) );

		final JLabel lblInput = new JLabel( "Input images" );
		panelInput.add( lblInput, BorderLayout.NORTH );
		final FileListPanel fileListPanel = new FileListPanel( inputModel );
		fileListPanel.setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
		fileListPanel.setPreferredSize( new Dimension( 200, 200 ) );
		panelInput.add( fileListPanel );

		final JPanel panelRun = new JPanel();
		mainPanel.add( panelRun );
		panelRun.setLayout( new BorderLayout( 0, 0 ) );

		final JLabel lblSettings = new JLabel( "TrackMate settings" );
		panelRun.add( lblSettings, BorderLayout.NORTH );
		panelRun.add( new TrackMateReadConfigPanel( settingsModel ) );

		final JPanel panelSettings = new JPanel();
		mainPanel.add( panelSettings );

		final LogPanel logPanel = new LogPanel();
		logPanel.setPreferredSize( new Dimension( 300, 200 ) );
		mainPanel.add( logPanel );

	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final ImageJ ij = new ImageJ();
		ij.launch( args );

		final TrackMateReadConfigModel model = new TrackMateReadConfigModel();
		model.setProposedFile( System.getProperty( "user.home" ) );
		model.listeners().add( () -> System.out.println( "New path to TrackMate file: " + model.getTrackMateFile() ) );

		final BatcherPanel panel = new BatcherPanel();
		final JFrame frame = new JFrame();
		frame.getContentPane().add( panel );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}

}
