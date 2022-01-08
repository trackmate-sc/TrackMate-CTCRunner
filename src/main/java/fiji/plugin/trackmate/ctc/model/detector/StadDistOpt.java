package fiji.plugin.trackmate.ctc.model.detector;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.StringRangeParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.stardist.StarDistCustomDetectorFactory;
import fiji.plugin.trackmate.stardist.StarDistDetectorFactory;

public class StadDistOpt
{

	private StadDistOpt()
	{}

	public static SpotDetectorFactoryBase< ? > createFactoryBuiltin()
	{
		return new StarDistDetectorFactory<>();
	}

	public static Map< String, AbstractParamSweepModel< ? > > createModelsBuiltin()
	{
		return Collections.emptyMap();
	}

	public static SpotDetectorFactoryBase< ? > createFactoryCustom()
	{
		return new StarDistCustomDetectorFactory<>();
	}

	public static Map< String, AbstractParamSweepModel< ? > > createModelsCustom()
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

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( StarDistCustomDetectorFactory.KEY_MODEL_FILEPATH, stardistBunblePath );
		models.put( StarDistCustomDetectorFactory.KEY_SCORE_THRESHOLD, probaThreshold );
		models.put( StarDistCustomDetectorFactory.KEY_OVERLAP_THRESHOLD, overlapThreshold );
		return models;
	}
}
