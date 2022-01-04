package fiji.plugin.trackmate.ctc.ui.detectors.optional;

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.detectors.DetectorSweepModel;
import fiji.plugin.trackmate.stardist.StarDistCustomDetectorFactory;
import fiji.plugin.trackmate.stardist.StarDistDetectorFactory;

public class StarDistDetector
{

	private static final class StardistDetectorModel extends DetectorSweepModel
	{

		protected StardistDetectorModel()
		{
			super( StarDistDetectorFactory.NAME, Collections.emptyMap(), new StarDistDetectorFactory<>() );
		}

		@Override
		public Iterator< Settings > iterator( final Settings base, final int targetChannel )
		{
			final Settings s = base.copyOn( base.imp );
			final Map< String, Object > ds = factory.getDefaultSettings();
			ds.put( KEY_TARGET_CHANNEL, targetChannel );
			s.detectorFactory = factory.copy();
			s.detectorSettings = ds;
			return Collections.singleton( s ).iterator();
		}
	}

	public static DetectorSweepModel stardistDetectorModel()
	{
		return new StardistDetectorModel();
	}

	public static DetectorSweepModel stardistCustomDetectorModel()
	{
		final StringRangeParamSweepModel stardistBunblePath = new StringRangeParamSweepModel()
				.paramName( "StarDist model bundle path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );
		final DoubleParamSweepModel probaThreshold = new DoubleParamSweepModel()
				.paramName( "Score threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 0.2 )
				.max( 0.8 )
				.nSteps( 3 );
		final DoubleParamSweepModel overlapThreshold = new DoubleParamSweepModel()
				.paramName( "Overlap threshold" )
				.rangeType( RangeType.FIXED )
				.min( 0.5 );

		return DetectorSweepModel.create()
				.name( StarDistCustomDetectorFactory.NAME )
				.factory( new StarDistCustomDetectorFactory<>() )
				.add( StarDistCustomDetectorFactory.KEY_MODEL_FILEPATH, stardistBunblePath )
				.add( StarDistCustomDetectorFactory.KEY_SCORE_THRESHOLD, probaThreshold )
				.add( StarDistCustomDetectorFactory.KEY_OVERLAP_THRESHOLD, overlapThreshold )
				.get();
	}
}
