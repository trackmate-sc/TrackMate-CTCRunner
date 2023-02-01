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
package fiji.plugin.trackmate.helper.model.tracker;

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.IntParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.kalman.KalmanTrackerFactory;

@Plugin( type = TrackerSweepModel.class, priority = 1000000 - 3 )
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

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( TrackerKeys.KEY_LINKING_MAX_DISTANCE, initSearchParam );
		models.put( TrackerKeys.KEY_KALMAN_SEARCH_RADIUS, searchRadiusParam );
		models.put( TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGapParam );
		return models;
	}
}
