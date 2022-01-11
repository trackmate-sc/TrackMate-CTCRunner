/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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
