package fiji.plugin.trackmate.batcher.ui;

import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.FocusAdapter;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;
import fiji.plugin.trackmate.util.TMUtils;
import net.imagej.ImageJ;

public class TrackMateReadConfigPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final JTextPane textPane;

	private final JTextField tfPath;

	private final TrackMateReadConfigModel model;

	public TrackMateReadConfigPanel( final TrackMateReadConfigModel model )
	{
		this.model = model;
		setLayout( new BorderLayout( 5, 5 ) );

		final JPanel panelBrowse = new JPanel();
		add( panelBrowse, BorderLayout.NORTH );
		panelBrowse.setLayout( new BoxLayout( panelBrowse, BoxLayout.X_AXIS ) );

		tfPath = new JTextField();
		tfPath.setColumns( 10 );
		tfPath.setFont( Fonts.SMALL_FONT );
		fiji.plugin.trackmate.gui.GuiUtils.selectAllOnFocus( tfPath );
		panelBrowse.add( tfPath );

		final JButton btnBrowse = new JButton( "Browse" );
		btnBrowse.setFont( Fonts.SMALL_FONT );
		panelBrowse.add( Box.createHorizontalStrut( 5 ) );
		panelBrowse.add( btnBrowse );

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setPreferredSize( new java.awt.Dimension( 200, 200 ) );
		this.add( scrollPane, BorderLayout.CENTER );

		textPane = new JTextPane();
		textPane.setEditable( false );
		textPane.setFont( SMALL_FONT );
		scrollPane.setViewportView( textPane );
		textPane.setBackground( this.getBackground() );

		// Listeners & co.
		final EverythingDisablerAndReenabler enabler = new EverythingDisablerAndReenabler( this, new Class[] { JLabel.class } );
		btnBrowse.addActionListener( e -> {

			enabler.disable();
			try
			{
				final FileFilter ff = new FileNameExtensionFilter( "XML files", "xml" );
				final File file = FileChooser.chooseFile(
						this,
						tfPath.getText(),
						ff,
						"Browse to a TrackMate file",
						DialogType.LOAD,
						SelectionMode.FILES_ONLY );
				if ( file != null )
				{
					tfPath.setText( file.getAbsolutePath() );
					updateInfoAndModel( tfPath.getText() );
				}
			}
			finally
			{
				enabler.reenable();
			}
		} );
		tfPath.addActionListener( e -> updateInfoAndModel( tfPath.getText() ) );
		final FocusAdapter fa = new FocusAdapter()
		{
			@Override
			public void focusLost( final java.awt.event.FocusEvent e )
			{
				updateInfoAndModel( tfPath.getText() );
			}
		};
		tfPath.addFocusListener( fa );

		// Set value.
		tfPath.setText( model.getProposedFile() );

		// Drop target
		setDropTarget( new AddFilesDropTarget() );
		textPane.setDropTarget( new AddFilesDropTarget() );
		tfPath.setDropTarget( new AddFilesDropTarget() );
		btnBrowse.setDropTarget( new AddFilesDropTarget() );

		// Update log
		fa.focusLost( null );
	}

	private void updateInfoAndModel( final String pathStr )
	{
		new Thread( "TrackMate-Helper reading TrackMate config thread" )
		{
			@Override
			public void run()
			{
				clear();
				model.setProposedFile( pathStr );
				final String trackmateFile = model.getTrackMateFile();
				if ( trackmateFile == null )
				{
					error( model.getErrorMessage() );
				}
				else
				{
					log( model.getSettings(), pathStr );
				}
			}

			private void log( final Settings settings, final String pathStr )
			{
				log( "TrackMate configuration found in\n" );
				log( pathStr + '\n', Logger.NORMAL_COLOR, false, true, false );

				log( "\nDetector:\n", Logger.BLUE_COLOR );
				if ( null == settings.detectorFactory )
				{
					log( "Detector is not set.\n" );
				}
				else
				{
					log( "Detector: " );
					log( settings.detectorFactory.getName() + ".\n", Logger.NORMAL_COLOR, true, false, false );
					if ( null == settings.detectorSettings )
					{
						log( "No detector settings found.\n" );
					}
					else
					{
						log( "Detector settings:\n" );
						log( TMUtils.echoMap( settings.detectorSettings, 2 ) );
					}
				}

				log( "\n" );
				log( "Object linking:\n", Logger.BLUE_COLOR );
				if ( null == settings.trackerFactory )
				{
					log( "Tracker is not set.\n" );
				}
				else
				{
					log( "Tracker: " );
					log( settings.trackerFactory.getName() + ".\n", Logger.NORMAL_COLOR, true, false, false );
					if ( null == settings.trackerSettings )
					{
						log( "No tracker settings found.\n" );
					}
					else
					{
						log( "Tracker settings:\n" );
						log( TMUtils.echoMap( settings.trackerSettings, 2 ) );
					}
				}

				log( "\n" );
				log( "Initial spot filter:\n", Logger.BLUE_COLOR );
				if ( null == settings.initialSpotFilterValue )
					log( "No initial quality filter.\n" );
				else
					log( "Initial quality filter value: " + settings.initialSpotFilterValue + ".\n" );

				log( "\n" );
				log( "Spot filters:\n", Logger.BLUE_COLOR );
				if ( settings.getSpotFilters() == null || settings.getSpotFilters().size() == 0 )
				{
					log( "No spot filters.\n" );
				}
				else
				{
					log( "Set with " + settings.getSpotFilters().size() + " spot filters:\n" );
					for ( final FeatureFilter featureFilter : settings.getSpotFilters() )
						log( " - " + featureFilter + "\n" );
				}

				log( "\n" );
				log( "Track filters:\n", Logger.BLUE_COLOR );
				if ( settings.getTrackFilters() == null || settings.getTrackFilters().size() == 0 )
				{
					log( "No track filters.\n" );
				}
				else
				{
					log( "Set with " + settings.getTrackFilters().size() + " track filters:\n" );
					for ( final FeatureFilter featureFilter : settings.getTrackFilters() )
						log( " - " + featureFilter + "\n" );
				}

				log( "\n" );
				log( "Feature analyzers:\n", Logger.BLUE_COLOR );
				log( settings.toStringFeatureAnalyzersInfo() );
			}

			public void error( final String message )
			{
				log( message, Logger.ERROR_COLOR );
			}

			public void log( final String message )
			{
				log( message, Logger.NORMAL_COLOR );
			}

			public void clear()
			{
				textPane.setText( "" );
			}

			public void log( final String message, final Color color )
			{
				log( message, color, false, false, false );
			}

			public void log( final String message, final Color color, final boolean bold, final boolean italic, final boolean underline )
			{
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							final StyledDocument doc = textPane.getStyledDocument();
							final StyleContext sc = StyleContext.getDefaultStyleContext();
							AttributeSet aset = sc.addAttribute( SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color );
							if ( bold )
								aset = sc.addAttribute( aset, StyleConstants.Bold, true );
							if ( italic )
								aset = sc.addAttribute( aset, StyleConstants.Italic, true );
							if ( underline )
								aset = sc.addAttribute( aset, StyleConstants.Underline, true );
							
							try
							{
								doc.insertString( doc.getLength(), message, aset );
								textPane.setCaretPosition( 0 );
							}
							catch ( final Exception e )
							{
								e.printStackTrace();
							}
						}
						catch ( final Exception e )
						{
							e.printStackTrace();
						}
					}
				} );
			}
		}.start();
	}

	/*
	 * PRIVATE CLASSES.
	 */

	private class AddFilesDropTarget extends DropTarget
	{

		private static final long serialVersionUID = 1L;

		@Override
		public synchronized void drop( final DropTargetDropEvent evt )
		{
			try
			{
				evt.acceptDrop( DnDConstants.ACTION_COPY );
				@SuppressWarnings( "unchecked" )
				final List< File > droppedFiles = ( List< File > ) evt.getTransferable().getTransferData( DataFlavor.javaFileListFlavor );
				if ( droppedFiles.isEmpty() )
					return;

				final String str = droppedFiles.get( 0 ).getAbsolutePath();
				tfPath.setText( str );
				updateInfoAndModel( str );
			}
			catch ( final Exception ex )
			{
				ex.printStackTrace();
			}
		}
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final ImageJ ij = new ImageJ();
		ij.launch( args );

		final TrackMateReadConfigModel model = new TrackMateReadConfigModel();
		model.setProposedFile( System.getProperty( "user.home" ) );
		model.listeners().add( () -> System.out.println( "New path to TrackMate file: " + model.getTrackMateFile() ) );

		final TrackMateReadConfigPanel panel = new TrackMateReadConfigPanel( model );
		final JFrame frame = new JFrame();
		frame.getContentPane().add( panel );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
