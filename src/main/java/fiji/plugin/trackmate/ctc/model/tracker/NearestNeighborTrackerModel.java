package fiji.plugin.trackmate.ctc.model.tracker;

import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.kdtree.NearestNeighborTrackerFactory;

public class NearestNeighborTrackerModel extends TrackerSweepModel
{

	public NearestNeighborTrackerModel()
	{
		super( NearestNeighborTrackerFactory.NAME, createModels(), new NearestNeighborTrackerFactory() );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final DoubleParamSweepModel linkingParam = new DoubleParamSweepModel()
				.paramName( "Max linking distance" )
				.dimension( Dimension.LENGTH )
				.min( 10. )
				.max( 20. )
				.nSteps( 3 )
				.rangeType( RangeType.LIN_RANGE );

		final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
		models.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, linkingParam );
		return models;
	}
}