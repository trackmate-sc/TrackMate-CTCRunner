package fiji.plugin.trackmate.ctc.ui;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JFrame;

import org.scijava.Cancelable;
import org.scijava.Context;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.ctc.CTCMetricsRunner2;
import fiji.plugin.trackmate.ctc.CTCResultsCrawler;
import fiji.plugin.trackmate.ctc.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.ctc.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.ParameterSweepModelIO;
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

	private final CTCResultsCrawler crawler;

	private final ImagePlus imp;

	private final String gtPath;

	public ParameterSweepController( final ImagePlus imp, final String gtPath )
	{
		this.imp = imp;
		this.gtPath = gtPath;
		final File modelFile = ParameterSweepModelIO.makeSettingsFileForGTPath( gtPath );
		final File saveFolder = modelFile.getParentFile();
		model = ParameterSweepModelIO.readFrom( modelFile );
		crawler = new CTCResultsCrawler( Logger.DEFAULT_LOGGER );

		gui = new ParameterSweepPanel( imp, model, crawler, gtPath );
		gui.btnRun.addActionListener( e -> run() );
		gui.btnStop.addActionListener( e -> cancel( "User pressed the stop button." ) );
		gui.btnStop.setVisible( false );

		crawler.reset();
		try
		{
			crawler.crawl( saveFolder.getAbsolutePath() );
		}
		catch ( final IOException e )
		{
			gui.logger.error( "Error while crawling the folder " + saveFolder + " for CSV results file:\n" );
			gui.logger.error( e.getMessage() );
			e.printStackTrace();
		}

		// Save on model modification.
		model.listeners().add( () -> 
		{
			gui.refresh();
			ParameterSweepModelIO.saveTo( modelFile, model );
		} );

		frame = new JFrame( "TrackMate parameter sweep" );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.setSize( 600, 700 );
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
		gui.btnStop.setEnabled( true );
		gui.logger.setProgress( 0. );
		gui.tabbedPane.setSelectedIndex( 0 );
		final int count = model.count();
		final boolean saveEachTime = gui.chckbxSaveTrackMateFile.isSelected();
		new Thread( "TrackMate CTC runner thread" )
		{
			@Override
			public void run()
			{
				try
				{
					final File gtPathFile = new File( gtPath );
					final int targetChannel = gui.sliderChannel.getValue();
					final Context context = TMUtils.getContext();
					final CTCMetricsRunner2 runner = new CTCMetricsRunner2( imp, gtPath, context );
					runner.setBatchLogger( gui.logger );

					final Settings base = new Settings( imp );
					int progress = 0;
					for ( final DetectorSweepModel detectorModel : model.getActiveDetectors() )
					{
						final Iterator< Settings > dit = detectorModel.iterator( base, targetChannel );
						while ( dit.hasNext() )
						{
							final Settings ds = dit.next();
							if ( isCanceled() )
								return;

							gui.logger.log( "\n________________________________________\n" );
							gui.logger.log( TMUtils.getCurrentTimeString() + "\n" );
							gui.logger.setStatus( ds.detectorFactory.getName() );
							final ValuePair< TrackMate, Double > detectionResult = runner.execDetection( ds );
							final TrackMate trackmate = detectionResult.getA();
							final double detectionTiming = detectionResult.getB();

							for ( final TrackerSweepModel trackerModel : model.getActiveTracker() )
							{
								final Iterator< Settings > tit = trackerModel.iterator( ds, targetChannel );
								while ( tit.hasNext() )
								{
									final Settings dts = tit.next();
									if ( isCanceled() )
										return;

									gui.logger.setProgress( ( double ) ++progress / count );
									gui.logger.log( "________________________________________\n" );

									if ( crawler.isSettingsPresent( dts ) )
									{
										gui.logger.log( "Settings for detector " + dts.detectorFactory.getKey() + " with parameters:\n" );
										gui.logger.log( TMUtils.echoMap( dts.detectorSettings, 2 ) );
										gui.logger.log( "and tracker " + dts.trackerFactory.getKey() + " with parameters:\n" );
										gui.logger.log( TMUtils.echoMap( dts.trackerSettings, 2 ) );
										gui.logger.log( "were already tested. Skipping.\n" );
										continue;
									}

									final Settings settings = trackmate.getSettings();
									settings.trackerFactory = dts.trackerFactory;
									settings.trackerSettings = dts.trackerSettings;
									gui.logger.setStatus( settings.detectorFactory.getName() + " + " + settings.trackerFactory.getName() );

									// Exec tracking.
									final double trackingTiming = runner.execTracking( trackmate );

									// Perform and save CTC metrics measurements.
									runner.performCTCMetricsMeasurements( trackmate, detectionTiming, trackingTiming );

									// Update best results.
									crawler.reset();
									try
									{
										crawler.crawl( gtPathFile.getParent() );
									}
									catch ( final IOException e )
									{
										gui.logger.error( "Error while crawling the folder " + gtPathFile.getParent() + " for CSV results file:\n" );
										gui.logger.error( e.getMessage() );
										e.printStackTrace();
									}

									// Save TrackMate file if required.
									if ( saveEachTime )
									{
										final String nameGen = "TrackMate_%s_%s_%03d.xml";
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

										final TmXmlWriter writer = new TmXmlWriter( trackmateFile, Logger.VOID_LOGGER );
										writer.appendModel( trackmate.getModel() );
										writer.appendSettings( trackmate.getSettings() );
										writer.appendGUIState( "ConfigureViews" );
										try
										{
											writer.writeToFile();
											gui.logger.log( "Saved results to TrackMate file: " + trackmateFile + "\n" );
										}
										catch ( final IOException e )
										{
											gui.logger.error( e.getMessage() );
											e.printStackTrace();
										}
									}
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
		gui.btnStop.setEnabled( false );
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
}
