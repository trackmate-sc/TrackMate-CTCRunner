package fiji.plugin.trackmate.ctc.model.detector;

import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;
import fiji.plugin.trackmate.morpholibj.Connectivity;
import fiji.plugin.trackmate.morpholibj.MorphoLibJDetectorFactory;

public class MorphoLibJOpt
{

	private MorphoLibJOpt()
	{}

	public static SpotDetectorFactoryBase< ? > createFactory()
	{
		return new MorphoLibJDetectorFactory<>();
	}

	public static Map< String, AbstractParamSweepModel< ? > > createModels()
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

		final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
		models.put( MorphoLibJDetectorFactory.KEY_TOLERANCE, toleranceParam );
		models.put( MorphoLibJDetectorFactory.KEY_CONNECTIVITY, connectivityParam );
		models.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContourParam );
		return models;
	}
}
