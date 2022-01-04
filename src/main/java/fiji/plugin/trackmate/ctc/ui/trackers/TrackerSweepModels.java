package fiji.plugin.trackmate.ctc.ui.trackers;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.kalman.KalmanTrackerFactory;
import fiji.plugin.trackmate.tracking.kdtree.NearestNeighborTrackerFactory;
import fiji.plugin.trackmate.tracking.overlap.OverlapTracker.IoUCalculation;
import fiji.plugin.trackmate.tracking.overlap.OverlapTrackerFactory;
import fiji.plugin.trackmate.tracking.sparselap.SimpleSparseLAPTrackerFactory;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;

public class TrackerSweepModels
{

	public static final TrackerSweepModel overlapTrackerModel()
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

		return TrackerSweepModel.create()
				.name( OverlapTrackerFactory.TRACKER_NAME )
				.factory( new OverlapTrackerFactory() )
				.add( OverlapTrackerFactory.KEY_SCALE_FACTOR, scaleFactorParam )
				.add( OverlapTrackerFactory.KEY_MIN_IOU, minIoUParam )
				.add( OverlapTrackerFactory.KEY_IOU_CALCULATION, iouCalculationParam )
				.get();
	}

	public static final TrackerSweepModel nearestNeighborTrackerModel()
	{
		final DoubleParamSweepModel initSearchParam = new DoubleParamSweepModel()
				.paramName( "Max linking distance" )
				.dimension( Dimension.LENGTH )
				.min( 10. )
				.max( 20. )
				.nSteps( 3 )
				.rangeType( RangeType.LIN_RANGE );

		return TrackerSweepModel.create()
				.name( NearestNeighborTrackerFactory.NAME )
				.factory( new NearestNeighborTrackerFactory() )
				.add( TrackerKeys.KEY_LINKING_MAX_DISTANCE, initSearchParam )
				.get();
	}

	public static final TrackerSweepModel kalmanTrackerModel()
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

		return TrackerSweepModel.create()
				.name( KalmanTrackerFactory.NAME )
				.factory( new KalmanTrackerFactory() )
				.add( TrackerKeys.KEY_LINKING_MAX_DISTANCE, initSearchParam )
				.add( KalmanTrackerFactory.KEY_KALMAN_SEARCH_RADIUS, searchRadiusParam )
				.add( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGapParam )
				.get();
	}

	public static final TrackerSweepModel simpleLAPTrackerModel()
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

		return TrackerSweepModel.create()
				.name( SimpleSparseLAPTrackerFactory.THIS2_NAME )
				.factory( new SimpleSparseLAPTrackerFactory() )
				.add( TrackerKeys.KEY_LINKING_MAX_DISTANCE, maxLinkingDistanceParam )
				.add( TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, gapClosingDistanceParam )
				.add( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGapParam )
				.get();
	}

	public static final TrackerSweepModel lapTrackerModel()
	{
		final DoubleParamSweepModel maxLinkingDistanceParam = new DoubleParamSweepModel()
				.paramName( "Max linking distance" )
				.dimension( Dimension.LENGTH )
				.min( 10. )
				.max( 20. )
				.nSteps( 3 )
				.rangeType( RangeType.LIN_RANGE );

		final BooleanParamSweepModel allowGapClosingParam = new BooleanParamSweepModel()
				.paramName( "Allow gap-closing" )
				.fixedValue( true )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED );
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

		final BooleanParamSweepModel allowTrackSplittingParam = new BooleanParamSweepModel()
				.paramName( "Allow track splitting" )
				.fixedValue( true )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED );
		final DoubleParamSweepModel splittingMaxDistanceParam = new DoubleParamSweepModel()
				.paramName( "Splitting max distance" )
				.dimension( Dimension.LENGTH )
				.min( 15. )
				.max( 25. )
				.nSteps( 3 )
				.rangeType( RangeType.FIXED );

		final BooleanParamSweepModel allowTrackMergingParam = new BooleanParamSweepModel()
				.paramName( "Allow track merging" )
				.fixedValue( true )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED );
		final DoubleParamSweepModel mergingMaxDistanceParam = new DoubleParamSweepModel()
				.paramName( "Merging max distance" )
				.dimension( Dimension.LENGTH )
				.min( 15. )
				.max( 25. )
				.nSteps( 3 )
				.rangeType( RangeType.FIXED );

		return TrackerSweepModel.create()
				.name( SparseLAPTrackerFactory.THIS_NAME )
				.factory( new SparseLAPTrackerFactory() )
				.add( TrackerKeys.KEY_LINKING_MAX_DISTANCE, maxLinkingDistanceParam )
				.add( TrackerKeys.KEY_ALLOW_GAP_CLOSING, allowGapClosingParam )
				.add( TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, gapClosingDistanceParam )
				.add( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGapParam )
				.add( TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, allowTrackSplittingParam )
				.add( TrackerKeys.KEY_SPLITTING_MAX_DISTANCE, splittingMaxDistanceParam )
				.add( TrackerKeys.KEY_ALLOW_TRACK_MERGING, allowTrackMergingParam )
				.add( TrackerKeys.KEY_MERGING_MAX_DISTANCE, mergingMaxDistanceParam )
				.get();
	}

	private TrackerSweepModels()
	{}
}
