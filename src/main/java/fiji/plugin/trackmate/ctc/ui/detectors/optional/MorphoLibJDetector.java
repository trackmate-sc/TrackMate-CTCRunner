package fiji.plugin.trackmate.ctc.ui.detectors.optional;

import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.ctc.ui.detectors.DetectorSweepModel;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;
import fiji.plugin.trackmate.morpholibj.Connectivity;
import fiji.plugin.trackmate.morpholibj.MorphoLibJDetectorFactory;

public class MorphoLibJDetector
{
	public static final DetectorSweepModel morphoLibJDetectorModel()
	{
		final DoubleParamSweepModel toleranceParam = new DoubleParamSweepModel()
				.paramName( "Tolerance" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 40. )
				.max( 60. )
				.nSteps( 3 );
		final EnumParamSweepModel< Connectivity > connectivityParam = new EnumParamSweepModel<>( Connectivity.class )
				.paramName( "Connectivity" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel.RangeType.FIXED )
				.fixedValue( Connectivity.DIAGONAL );
		final BooleanParamSweepModel simplifyContourParam = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		return DetectorSweepModel.create()
				.name( MorphoLibJDetectorFactory.NAME )
				.factory( new MorphoLibJDetectorFactory<>() )
				.add( MorphoLibJDetectorFactory.KEY_TOLERANCE, toleranceParam )
				.add( MorphoLibJDetectorFactory.KEY_CONNECTIVITY, connectivityParam )
				.add( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContourParam )
				.get();
	}
}
