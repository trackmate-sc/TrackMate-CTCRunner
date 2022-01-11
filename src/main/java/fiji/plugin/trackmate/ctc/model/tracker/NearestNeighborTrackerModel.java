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

import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.NumberParamSweepModel.RangeType;
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
