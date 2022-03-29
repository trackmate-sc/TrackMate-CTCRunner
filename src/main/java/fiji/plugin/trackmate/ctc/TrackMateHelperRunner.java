package fiji.plugin.trackmate.ctc;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.scijava.Context;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.ctc.model.ParameterSweepModel;
import fiji.plugin.trackmate.ctc.model.ParameterSweepModelIO;
import fiji.plugin.trackmate.ctc.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.ctc.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.util.TMUtils;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.ImageJ;
import net.imglib2.util.ValuePair;

public class TrackMateHelperRunner
{

	private static final String DOC_STR = ""
			+ "TrackMate-Helper runner\n"
			+ "-----------------------\n"
			+ "Syntax:\n"
			+ "> java -cp path/to/TrackMate-CTCRunner-x.y.z.jar fiji.plugin.trackmate.ctc.TrackMateHelperRunner "
			+ "/path/to/ground-truth/folder /path/to/image/file target_channel\n";

	public static void main( final String[] args )
	{
		final ImageJ ij = new ImageJ();
		final Context context = ij.getContext();
		final Logger logger = Logger.DEFAULT_LOGGER;

		if ( args.length < 3 )
		{
			logger.error( DOC_STR );
			return;
		}
		boolean saveEachTime = false;
		if ( args.length > 3 )
			saveEachTime = Boolean.parseBoolean( args[ 3 ] );
		

		logger.log( "Opening image file " + args[ 0 ] + '\n' );
		final ImagePlus imp = IJ.openImage( args[ 0 ] );
		logger.log( "Done." + '\n' );
		
		final int targetChannel = Integer.parseInt( args[2] );
		logger.log( "Target channel selected: " + targetChannel + '\n' );

		final String gtPath = args[ 1 ];

		final File modelFile = ParameterSweepModelIO.makeSettingsFileForGTPath( gtPath );
		final File saveFolder = modelFile.getParentFile();
		final ParameterSweepModel model = ParameterSweepModelIO.readFrom( modelFile );

		final CTCResultsCrawler crawler = new CTCResultsCrawler( Logger.DEFAULT_LOGGER );
		crawler.reset();
		try
		{
			crawler.crawl( saveFolder.getAbsolutePath() );
		}
		catch ( final IOException e )
		{
			logger.error( "Error while crawling the folder " + saveFolder + " for CSV results file:\n" );
			logger.error( e.getMessage() );
			e.printStackTrace();
		}
		crawler.watch( saveFolder.getAbsolutePath() );

		final int count = model.count();
		String str = "Parameter sweep configuration  -  ";
		if ( count == 0 )
		{
			logger.error( "Please build a sweep model with at least one detector and one tracker.\n" );
			return;
		}
		else if ( count == 1 )
		{
			str += "One settings to test.";
		}
		else
		{
			str += String.format( "Will generate %d different settings to test.", count );
		}
		logger.log( str + '\n' );

		final CTCMetricsRunner runner = new CTCMetricsRunner( imp, gtPath, context );
		runner.setBatchLogger( Logger.DEFAULT_LOGGER );

		final Settings base = new Settings( imp );
		base.setSpotFilters( model.getSpotFilters() );
		base.setTrackFilters( model.getTrackFilters() );
		int progress = 0;
		for ( final DetectorSweepModel detectorModel : model.getActiveDetectors() )
		{
			final Iterator< Settings > dit = detectorModel.iterator( base, targetChannel );
			while ( dit.hasNext() )
			{
				final Settings ds = dit.next();

				logger.log( "\n________________________________________\n" );
				logger.log( TMUtils.getCurrentTimeString() + "\n" );
				logger.setStatus( ds.detectorFactory.getName() );

				final ValuePair< TrackMate, Double > detectionResult = runner.execDetection( ds );
				final TrackMate trackmate = detectionResult.getA();
				// Detection failed?
				if ( null == trackmate )
				{
					logger.error( "Error running TrackMate with these parameters.\nSkipping.\n" );
					progress += model.countTrackerSettings();
					logger.setProgress( ( double ) ++progress / count );
					continue;
				}
				// Got 0 spots to track?
				if ( trackmate.getModel().getSpots().getNSpots( true ) == 0 )
				{
					logger.log( "Settings result in having 0 spots to track.\nSkipping.\n" );
					progress += model.countTrackerSettings();
					logger.setProgress( ( double ) ++progress / count );
					continue;
				}
				final double detectionTiming = detectionResult.getB();

				for ( final TrackerSweepModel trackerModel : model.getActiveTracker() )
				{
					final Iterator< Settings > tit = trackerModel.iterator( ds, targetChannel );
					while ( tit.hasNext() )
					{
						final Settings dts = tit.next();

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
}
