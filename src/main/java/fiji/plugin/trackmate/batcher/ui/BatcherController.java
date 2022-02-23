package fiji.plugin.trackmate.batcher.ui;

import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.Cancelable;

import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.util.TMUtils;
import net.imagej.ImageJ;

public class BatcherController implements Cancelable
{

	private final BatcherModel model;

	private final BatcherPanel gui;

	private String cancelReason;

	public BatcherController()
	{
		this.model = new BatcherModel();
		this.gui = new BatcherPanel( model );
		gui.btnRun.addActionListener( e -> run() );
		gui.btnCancel.addActionListener( e -> cancel( "User pressed the cancel button." ) );
		gui.btnCancel.setVisible( false );
	}

	private void run()
	{
		cancelReason = null;
		gui.btnRun.setVisible( false );
		gui.btnCancel.setVisible( true );
		gui.btnCancel.setEnabled( true );
		new Thread( "TrackMate Batcher runner thread" )
		{
			@Override
			public void run()
			{
				try
				{
					for ( final String path : model.getFileListModel().getList() )
					{
						gui.logger.log( "\n_______________________________\n" );
						gui.logger.log( TMUtils.getCurrentTimeString() + "\n" );
						gui.logger.log( "Processing file " + path + '\n' );
					}

					gui.logger.log( "\n_______________________________\n" );
					gui.logger.log( TMUtils.getCurrentTimeString() + "\n" );
					gui.logger.log( "Finished!\n" );

				}
				finally
				{
					gui.btnRun.setVisible( true );
					gui.btnCancel.setVisible( false );
				}
			}
		}.start();
	}

	public void show()
	{
		// It still cannot stand the Metal L&F...
		fiji.plugin.trackmate.gui.GuiUtils.setSystemLookAndFeel();
		final JFrame frame = new JFrame( "TrackMate Helper" );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final java.awt.event.WindowEvent e )
			{
				cancel( "User closed the batcher window." );
			}
		} );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}

	@Override
	public void cancel( final String cancelReason )
	{
		gui.btnCancel.setEnabled( false );
		gui.logger.log( TMUtils.getCurrentTimeString() + " - " + cancelReason + '\n' );
		this.cancelReason = cancelReason;
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}

	@Override
	public boolean isCanceled()
	{
		return cancelReason != null;
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );

		final BatcherController controller = new BatcherController();
		controller.show();
	}
}
