package fiji.plugin.trackmate.ctc.model.tracker;

import java.util.LinkedHashMap;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;

public class LAPTrackerModel extends TrackerSweepModel
{

	public LAPTrackerModel()
	{
		super( SparseLAPTrackerFactory.THIS_NAME, createModels(), new SparseLAPTrackerFactory() );
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

		final BooleanParamSweepModel allowGapClosingParam = new BooleanParamSweepModel()
				.paramName( "Allow gap-closing" )
				.fixedValue( true )
				.rangeType( fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel.RangeType.FIXED );
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
				.rangeType( fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel.RangeType.FIXED );
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
				.rangeType( fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel.RangeType.FIXED );
		final DoubleParamSweepModel mergingMaxDistanceParam = new DoubleParamSweepModel()
				.paramName( "Merging max distance" )
				.dimension( Dimension.LENGTH )
				.min( 15. )
				.max( 25. )
				.nSteps( 3 )
				.rangeType( RangeType.FIXED );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, maxLinkingDistanceParam );
		models.put( TrackerKeys.KEY_ALLOW_GAP_CLOSING, allowGapClosingParam );
		models.put( TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, gapClosingDistanceParam );
		models.put( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGapParam );
		models.put( TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, allowTrackSplittingParam );
		models.put( TrackerKeys.KEY_SPLITTING_MAX_DISTANCE, splittingMaxDistanceParam );
		models.put( TrackerKeys.KEY_ALLOW_TRACK_MERGING, allowTrackMergingParam );
		models.put( TrackerKeys.KEY_MERGING_MAX_DISTANCE, mergingMaxDistanceParam );
		return models;
	}
}