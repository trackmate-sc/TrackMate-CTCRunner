package fiji.plugin.trackmate.ctc.model.tracker;

import java.util.LinkedHashMap;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.sparselap.SimpleSparseLAPTrackerFactory;

public class SimpleLAPTrackerModel extends TrackerSweepModel
{

	public SimpleLAPTrackerModel()
	{
		super( SimpleSparseLAPTrackerFactory.THIS2_NAME, createModels(), new SimpleSparseLAPTrackerFactory() );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final DoubleParamSweepModel maxLinkingDistanceParam = new DoubleParamSweepModel()
				.paramName( "Max linking distance" )
				.dimension( Dimension.LENGTH )
				.min( 10. )
				.max( 20. )
				.nSteps( 3 )
				.rangeType( RangeType.LIN_RANGE );
		final DoubleParamSweepModel gapClosingDistanceParam = new DoubleParamSweepModel()
				.paramName( "Gap-closing distance" )
				.dimension( Dimension.LENGTH )
				.min( 15. )
				.max( 25. )
				.nSteps( 3 )
				.rangeType( RangeType.FIXED );
		final IntParamSweepModel maxFrameGapParam = new IntParamSweepModel()
				.paramName( "Max frame gap" )
				.min( 2 )
				.rangeType( RangeType.FIXED );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, maxLinkingDistanceParam );
		models.put( TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, gapClosingDistanceParam );
		models.put( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGapParam );
		return models;
	}
}