package fiji.plugin.trackmate.ctc.model.tracker;

import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.tracking.overlap.OverlapTrackerFactory;
import fiji.plugin.trackmate.tracking.overlap.OverlapTracker.IoUCalculation;

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
		final EnumParamSweepModel< IoUCalculation > iouCalculationParam = new EnumParamSweepModel<>( IoUCalculation.class )
				.paramName( "IoU calculation" )
				.fixedValue( IoUCalculation.PRECISE )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel.RangeType.FIXED );

		final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
		models.put( OverlapTrackerFactory.KEY_SCALE_FACTOR, scaleFactorParam );
		models.put( OverlapTrackerFactory.KEY_MIN_IOU, minIoUParam );
		models.put( OverlapTrackerFactory.KEY_IOU_CALCULATION, iouCalculationParam );
		return models;
	}
}