package fiji.plugin.trackmate.ctc.model.tracker;

import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.kalman.KalmanTrackerFactory;

public class KalmanTrackerModel extends TrackerSweepModel
{

	public KalmanTrackerModel()
	{
		super( KalmanTrackerFactory.NAME, createModels(), new KalmanTrackerFactory() );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final DoubleParamSweepModel initSearchParam = new DoubleParamSweepModel()
				.paramName( "Initial search radius" )
				.dimension( Dimension.LENGTH )
				.min( 10. )
				.max( 20. )
				.nSteps( 3 )
				.rangeType( RangeType.LIN_RANGE );
		final DoubleParamSweepModel searchRadiusParam = new DoubleParamSweepModel()
				.paramName( "Search radius" )
				.dimension( Dimension.LENGTH )
				.min( 15. )
				.max( 25. )
				.nSteps( 3 )
				.rangeType( RangeType.FIXED );
		final IntParamSweepModel maxFrameGapParam = new IntParamSweepModel()
				.paramName( "Max frame gap" )
				.min( 2 )
				.rangeType( RangeType.FIXED );

		final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
		models.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, initSearchParam );
		models.put( KalmanTrackerFactory.KEY_KALMAN_SEARCH_RADIUS, searchRadiusParam );
		models.put( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGapParam );
		return models;
	}
}