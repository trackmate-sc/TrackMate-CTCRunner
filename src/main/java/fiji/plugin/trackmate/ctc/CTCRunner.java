package fiji.plugin.trackmate.ctc;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.scijava.Cancelable;
import org.scijava.Context;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.ctc.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.ctc.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.ctc.ui.ParameterSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.ParameterSweepModelIO;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.util.TMUtils;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.util.ValuePair;

public class CTCRunner implements Cancelable, Runnable
{

	private final ImagePlus imp;

	private final int targetChannel;

	private final String gtPath;

	private final ParameterSweepModel model;

	private final Logger logger;

	private final CTCResultsCrawler crawler;

	private String cancelReason;

	private final boolean saveEachTime;

	public CTCRunner( 
			final String sourceImagePath, 
			final int targetChannel,
			final String gtPath, 
			final Logger logger,
			final boolean saveEachTime )
	{
		this( sourceImagePath,
				targetChannel,
				gtPath,
				ParameterSweepModelIO.readFrom( ParameterSweepModelIO.makeSettingsFileForGTPath( gtPath ) ),
				logger,
				saveEachTime );
	}

	public CTCRunner( 
			final String sourceImagePath, 
			final int targetChannel, 
			final String gtPath, 
			final ParameterSweepModel model, 
			final Logger logger,
			final boolean saveEachTime )
	{
		this.targetChannel = targetChannel;
		this.gtPath = gtPath;
		this.model = model;
		this.logger = logger;
		this.saveEachTime = saveEachTime;
		this.imp = IJ.openImage( sourceImagePath );
		this.crawler = new CTCResultsCrawler( logger );
	}

	@Override
	public void run()
	{
		final Context context = TMUtils.getContext();
		final CTCMetricsRunner2 runner = new CTCMetricsRunner2( imp, gtPath, context );
		runner.setBatchLogger( logger );
		final int count = model.count();
		crawl();

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

				logger.log( "\n________________________________________\n" );
				logger.log( TMUtils.getCurrentTimeString() + "\n" );
				logger.setStatus( ds.detectorFactory.getName() );
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

						logger.setProgress( ( double ) ++progress / count );
						logger.log( "________________________________________\n" );

						if ( crawler.isSettingsPresent( dts ) )
						{
							logger.log( "Settings for detector " + dts.detectorFactory.getKey() + " with parameters:\n" );
							logger.log( TMUtils.echoMap( dts.detectorSettings, 2 ) );
							logger.log( "and tracker " + dts.trackerFactory.getKey() + " with parameters:\n" );
							logger.log( TMUtils.echoMap( dts.trackerSettings, 2 ) );
							logger.log( "were already tested. Skipping.\n" );
							continue;
						}

						final Settings settings = trackmate.getSettings();
						settings.trackerFactory = dts.trackerFactory;
						settings.trackerSettings = dts.trackerSettings;
						logger.setStatus( settings.detectorFactory.getName() + " + " + settings.trackerFactory.getName() );

						// Exec tracking.
						final double trackingTiming = runner.execTracking( trackmate );

						// Perform and save CTC metrics measurements.
						runner.performCTCMetricsMeasurements( trackmate, detectionTiming, trackingTiming );

						// Update results tables.
						crawl();

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
								logger.log( "Saved results to TrackMate file: " + trackmateFile + "\n" );
							}
							catch ( final IOException e )
							{
								logger.error( e.getMessage() );
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	private void crawl()
	{
		final String saveFolder = new File( gtPath ).getParent();
		crawler.reset();
		try
		{
			crawler.crawl( saveFolder );
		}
		catch ( final IOException e )
		{
			logger.error( "Error while crawling the folder " + saveFolder + " for CSV results file:\n" );
			logger.error( e.getMessage() );
			e.printStackTrace();
		}
	}

	public String getCurrentBestParams()
	{
		return crawler.printReport();
	}

	@Override
	public void cancel( final String cancelReason )
	{
		logger.log( TMUtils.getCurrentTimeString() + " - " + cancelReason + '\n' );
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
