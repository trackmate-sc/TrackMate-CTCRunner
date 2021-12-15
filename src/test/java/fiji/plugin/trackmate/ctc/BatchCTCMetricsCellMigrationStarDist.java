package fiji.plugin.trackmate.ctc;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.scijava.Context;

import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.stardist.StarDistDetectorFactory;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.kalman.KalmanTrackerFactory;
import fiji.plugin.trackmate.tracking.kdtree.NearestNeighborTrackerFactory;
import fiji.plugin.trackmate.tracking.sparselap.SimpleSparseLAPTrackerFactory;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import net.imglib2.util.ValuePair;

/**
 * Performs batch tracking and CTC metrics measurements to find the optimal
 * settings of a tracking configuration.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class BatchCTCMetricsCellMigrationStarDist
{

	public static void main( final String[] args ) throws IOException
	{
		// Paths.
		final String rootFolder = "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\CellMigration";
		final String sourceImagePath = new File( rootFolder, "CellMigration.tif" ).getAbsolutePath();
		final String groundTruthPath = new File( rootFolder, "02_GT" ).getAbsolutePath();
		try (final Context context = new Context())
		{
			// Performer.
			final CTCMetricsRunner runner = new CTCMetricsRunner( sourceImagePath, groundTruthPath, context );

			// Configure detection.
			final StarDistDetectorFactory< ? > detectorFactory = new StarDistDetectorFactory<>();
			final Map< String, Object > detectorSettings = detectorFactory.getDefaultSettings();

			// Exec detection.
			final ValuePair< TrackMate, Double > detectionResult = runner.getOrExecDetection( detectorFactory, detectorSettings );

			final TrackMate trackmate = detectionResult.getA();
			final double detectionTiming = detectionResult.getB();

			/*
			 * Tracking part.
			 */

			// Parameter sweep.
			final double[] maxLinkingDistances = new double[] { 5., 10., 15., 20. };
			final int[] maxFrameGaps = new int[] { 0, 2, 3, 4 };

			final SpotTrackerFactory[] trackerFactories = new SpotTrackerFactory[] {
					new SimpleSparseLAPTrackerFactory(),
					new SparseLAPTrackerFactory(),
					new KalmanTrackerFactory(),
					new NearestNeighborTrackerFactory() };

			/*
			 * Main loop. We vary only the tracking part.
			 */

			for ( final SpotTrackerFactory trackerFactory : trackerFactories )
			{
				for ( final int frameGap : maxFrameGaps )
				{
					for ( final double mld : maxLinkingDistances )
					{
						// Configure tracker.
						final Map< String, Object > trackerSettings = trackerFactory.getDefaultSettings();
						switch ( trackerFactory.getKey() )
						{
						case SparseLAPTrackerFactory.THIS_TRACKER_KEY:
						case SimpleSparseLAPTrackerFactory.THIS2_TRACKER_KEY:
						{
							trackerSettings.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, mld );
							trackerSettings.put( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, frameGap );
							trackerSettings.put( TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, mld );
							trackerSettings.put( TrackerKeys.KEY_SPLITTING_MAX_DISTANCE, mld );
							// Allow track splitting for LAP tracker.
							if ( trackerFactory.getKey().equals( SparseLAPTrackerFactory.THIS_TRACKER_KEY ) )
								trackerSettings.put( TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, true );
							break;
						}
						case "KALMAN_TRACKER":
						{
							trackerSettings.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, mld );
							trackerSettings.put( KalmanTrackerFactory.KEY_KALMAN_SEARCH_RADIUS, mld );
							trackerSettings.put( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, frameGap );
							break;
						}
						case NearestNeighborTrackerFactory.TRACKER_KEY:
						{
							// Skip gap closing for NNT.
							if ( frameGap != 0 )
								continue;

							trackerSettings.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, mld );
							break;
						}
						default:
							throw new IllegalArgumentException( "Does not know how to configure tracker: " + trackerFactory.getKey() );
						}

						// Exec tracking.
						final double trackingTiming = runner.execTracking( trackmate, trackerFactory, trackerSettings );

						// Perform and save CTC metrics measurements.
						runner.performCTCMetricsMeasurements( trackmate, detectionTiming, trackingTiming );
					}
				}
			}
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}
}
