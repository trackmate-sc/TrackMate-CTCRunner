package fiji.plugin.trackmate.ctc.ui.detectors.optional;

import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.detectors.DetectorSweepModel;
import fiji.plugin.trackmate.weka.WekaDetectorFactory;

public class WekaDetector
{
	public static DetectorSweepModel wekaDetectorModel()
	{
		final StringRangeParamSweepModel classifierPath = new StringRangeParamSweepModel()
				.paramName( "Weka classifier path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );
		final DoubleParamSweepModel probaThreshold = new DoubleParamSweepModel()
				.paramName( "Probability threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 0.2 )
				.max( 0.8 )
				.nSteps( 3 );
		final IntParamSweepModel classIndex = new IntParamSweepModel()
				.paramName( "Class index" )
				.rangeType( RangeType.FIXED )
				.min( 1 );

		return DetectorSweepModel.create()
				.name( WekaDetectorFactory.NAME )
				.factory( new WekaDetectorFactory<>() )
				.add( WekaDetectorFactory.KEY_CLASSIFIER_FILEPATH, classifierPath )
				.add( WekaDetectorFactory.KEY_CLASS_INDEX, classIndex )
				.add( WekaDetectorFactory.KEY_PROBA_THRESHOLD, probaThreshold )
				.get();
	}
}
