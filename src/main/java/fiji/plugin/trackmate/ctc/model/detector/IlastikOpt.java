package fiji.plugin.trackmate.ctc.model.detector;

import java.util.LinkedHashMap;
import java.util.Map;

import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepModel;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.ilastik.IlastikDetectorFactory;

public class IlastikOpt
{

	private IlastikOpt()
	{}

	public static Map< String, AbstractParamSweepModel< ? > > createModels()
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

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( IlastikDetectorFactory.KEY_CLASSIFIER_FILEPATH, classifierPath );
		models.put( IlastikDetectorFactory.KEY_CLASS_INDEX, classIndex );
		models.put( IlastikDetectorFactory.KEY_PROBA_THRESHOLD, probaThreshold );
		return models;
	}

	public static SpotDetectorFactoryBase< ? > createFactory()
	{
		return new IlastikDetectorFactory<>();
	}
}
