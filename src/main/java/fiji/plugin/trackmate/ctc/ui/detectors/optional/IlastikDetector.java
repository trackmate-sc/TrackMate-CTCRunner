package fiji.plugin.trackmate.ctc.ui.detectors.optional;

import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.detectors.DetectorSweepModel;
import fiji.plugin.trackmate.ilastik.IlastikDetectorFactory;

public class IlastikDetector
{
	public static DetectorSweepModel ilastikDetectorModel()
	{
		final StringRangeParamSweepModel classifierPath = new StringRangeParamSweepModel()
				.paramName( "Ilastik project path" )
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
				.name( IlastikDetectorFactory.NAME )
				.factory( new IlastikDetectorFactory<>() )
				.add( IlastikDetectorFactory.KEY_CLASSIFIER_FILEPATH, classifierPath )
				.add( IlastikDetectorFactory.KEY_CLASS_INDEX, classIndex )
				.add( IlastikDetectorFactory.KEY_PROBA_THRESHOLD, probaThreshold )
				.get();
	}
}
