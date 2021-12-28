package fiji.plugin.trackmate.ctc.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

import org.scijava.Cancelable;
import org.scijava.Context;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.ctc.CTCMetricsRunner2;
import fiji.plugin.trackmate.ctc.ui.detectors.DetectorSweepModel;
import fiji.plugin.trackmate.ctc.ui.trackers.TrackerSweepModel;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.util.TMUtils;
import ij.ImagePlus;
import net.imglib2.util.ValuePair;

public class ParameterSweepController implements Cancelable
{

	private final ParameterSweepPanel gui;

	private final JFrame frame;

	private final ParameterSweepModel model;

	private String cancelReason;

	public ParameterSweepController( final ImagePlus imp )
	{
		model = new ParameterSweepModel( imp );
		gui = new ParameterSweepPanel( model );
		gui.btnRun.addActionListener( e -> run() );
		gui.btnStop.addActionListener( e -> cancel( "User pressed the stop button." ) );
		gui.btnStop.setVisible( false );

		frame = new JFrame( "TrackMate parameter sweep" );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.pack();
		frame.setLocationRelativeTo( null );
	}

	private void run()
	{
		cancelReason = null;
		// Refresh model :(
		gui.refresh();
		gui.enabler.disable();
		gui.btnRun.setVisible( false );
		gui.btnStop.setVisible( true );
		gui.logger.setProgress( 0. );
		final int count = model.count();
		final boolean saveEachTime = gui.chckbxSaveTrackMateFile.isSelected();
		new Thread( "TrackMate CTC runner thread" )
		{
			@Override
			public void run()
			{
				try
				{
					final String gtPath = gui.tfGroundTruth.getText();
					final int targetChannel = gui.sliderChannel.getValue();
					final Context context = TMUtils.getContext();
					final CTCMetricsRunner2 runner = new CTCMetricsRunner2( model.getImage(), gtPath, context );
					runner.setBatchLogger( gui.logger );

					final Settings base = new Settings( model.getImage() );
					int progress = 0;
					for ( final DetectorSweepModel detectorModel : model.getActiveDetectors() )
					{
						final List< Settings > detectorSettings = detectorModel.generateSettings( base, targetChannel );
						for ( final Settings ds : detectorSettings )
						{
							if ( isCanceled() )
								return;

							gui.logger.setStatus( ds.detectorFactory.getName() );
							final ValuePair< TrackMate, Double > detectionResult = runner.execDetection( ds );
							final TrackMate trackmate = detectionResult.getA();
							final double detectionTiming = detectionResult.getB();

							for ( final TrackerSweepModel trackerModel : model.getActiveTracker() )
							{
								final List< Settings > detectorAndTrackerSettings = trackerModel.generateSettings( ds, targetChannel );

								for ( final Settings dts : detectorAndTrackerSettings )
								{
									if ( isCanceled() )
										return;

									final Settings settings = trackmate.getSettings();
									settings.trackerFactory = dts.trackerFactory;
									settings.trackerSettings = dts.trackerSettings;
									gui.logger.setStatus( dts.trackerFactory.getName() );

									// Exec tracking.
									final double trackingTiming = runner.execTracking( trackmate );

									// Perform and save CTC metrics measurements.
									runner.performCTCMetricsMeasurements( trackmate, detectionTiming, trackingTiming );

									// Save TrackMate file if required.
									if ( saveEachTime )
									{
										final String nameGen = "TrackMate_%s_%s_%3d.xml";
										int i = 1;
										File trackmateFile;
										do
										{
											trackmateFile = new File( new File( gtPath ).getParent(),
													String.format( nameGen,
															settings.detectorFactory.getKey(),
															settings.trackerFactory.getKey(),
															i++ ) );
										}
										while ( trackmateFile.exists() );

										final TmXmlWriter writer = new TmXmlWriter( trackmateFile, gui.logger );
										writer.appendModel( trackmate.getModel() );
										writer.appendSettings( trackmate.getSettings() );
										writer.appendGUIState( "ConfigureViews" );
										try
										{
											writer.writeToFile();
										}
										catch ( final IOException e )
										{
											gui.logger.error( e.getMessage() );
											e.printStackTrace();
										}
									}

									gui.logger.setProgress( ( double ) ++progress / count );
								}
							}
						}
					}
				}
				finally
				{
					gui.btnRun.setVisible( true );
					gui.btnStop.setVisible( false );
					gui.enabler.reenable();
				}
			}
		}.start();
	}

	public void show()
	{
		frame.setVisible( true );
	}

	@Override
	public void cancel( final String cancelReason )
	{
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
}
