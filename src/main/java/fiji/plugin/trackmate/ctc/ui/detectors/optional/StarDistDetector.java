package fiji.plugin.trackmate.ctc.ui.detectors.optional;

import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.detectors.DetectorSweepModel;
import fiji.plugin.trackmate.stardist.StarDistCustomDetectorFactory;
import fiji.plugin.trackmate.stardist.StarDistDetectorFactory;

public class StarDistDetector
{

	public static DetectorSweepModel stardistDetectorModel()
	{
		return DetectorSweepModel.create()
				.name( StarDistDetectorFactory.NAME )
				.factory( new StarDistDetectorFactory<>() )
				.get();
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
