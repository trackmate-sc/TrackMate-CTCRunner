/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2024 TrackMate developers.
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
package fiji.plugin.trackmate.helper.model.detector;

import static fiji.plugin.trackmate.cellpose.advanced.AdvancedCellposeCLI.KEY_CELL_PROB_THRESHOLD;
import static fiji.plugin.trackmate.cellpose.advanced.AdvancedCellposeCLI.KEY_FLOW_THRESHOLD;

import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.omnipose.advanced.AdvancedOmniposeDetectorFactory;

public class AdvancedOmniposeOpt
{

	private AdvancedOmniposeOpt()
	{}

	static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final Map< String, AbstractParamSweepModel< ? > > models = OmniposeOpt.createModels();

		final DoubleParamSweepModel flowThreshold = new DoubleParamSweepModel()
				.paramName( "Flow threshold" )
				.dimension( Dimension.NONE )
				.rangeType( RangeType.FIXED )
				.min( 0. )
				.max( 3. );
		final DoubleParamSweepModel maskThreshold = new DoubleParamSweepModel()
				.paramName( "Mask threshold" )
				.dimension( Dimension.NONE )
				.rangeType( RangeType.FIXED )
				.min( -6. )
				.max( 6. );

		models.put( KEY_FLOW_THRESHOLD, flowThreshold );
		models.put( KEY_CELL_PROB_THRESHOLD, maskThreshold );
		return models;
	}

	public static SpotDetectorFactoryBase< ? > createFactory()
	{
		return new AdvancedOmniposeDetectorFactory<>();
	}
}
