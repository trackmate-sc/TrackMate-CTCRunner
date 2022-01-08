package fiji.plugin.trackmate.ctc.model.tracker;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.ArrayParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.tracking.overlap.OverlapTracker.IoUCalculation;
import fiji.plugin.trackmate.tracking.overlap.OverlapTrackerFactory;

public class OverlapTrackerModel extends TrackerSweepModel
{

	public OverlapTrackerModel()
	{
		super( OverlapTrackerFactory.TRACKER_NAME, createModels(), new OverlapTrackerFactory() );
	}
	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final DoubleParamSweepModel scaleFactorParam = new DoubleParamSweepModel()
				.paramName( "Scale factor" )
				.min( 1. )
				.max( 1.4 )
				.nSteps( 3 )
				.rangeType( RangeType.LIN_RANGE );
		final DoubleParamSweepModel minIoUParam = new DoubleParamSweepModel()
				.paramName( "Minimal IoU" )
				.min( 0.3 )
				.max( 0.5 )
				.nSteps( 3 )
				.rangeType( RangeType.FIXED );
		final String[] iouCalVals = Arrays.stream( IoUCalculation.values() )
				.map( e -> e.name() )
				.collect( Collectors.toList() )
				.toArray( new String[] {} );
		final ArrayParamSweepModel< String > iouCalculationParam = new ArrayParamSweepModel<>( iouCalVals )
				.paramName( "IoU calculation" )
				.fixedValue( IoUCalculation.PRECISE.name() )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.ArrayParamSweepModel.RangeType.FIXED );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( OverlapTrackerFactory.KEY_SCALE_FACTOR, scaleFactorParam );
		models.put( OverlapTrackerFactory.KEY_MIN_IOU, minIoUParam );
		models.put( OverlapTrackerFactory.KEY_IOU_CALCULATION, iouCalculationParam );
		return models;
	}
}