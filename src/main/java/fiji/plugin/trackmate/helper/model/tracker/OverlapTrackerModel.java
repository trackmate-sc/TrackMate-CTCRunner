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
package fiji.plugin.trackmate.helper.model.tracker;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.tracking.overlap.OverlapTracker.IoUCalculation;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.ArrayParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.tracking.overlap.OverlapTrackerFactory;

@Plugin( type = TrackerSweepModel.class, priority = 1000000 - 4 )
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
				.addValue( IoUCalculation.PRECISE.name() )
				.rangeType( fiji.plugin.trackmate.helper.model.parameter.ArrayParamSweepModel.RangeType.FIXED );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( OverlapTrackerFactory.KEY_SCALE_FACTOR, scaleFactorParam );
		models.put( OverlapTrackerFactory.KEY_MIN_IOU, minIoUParam );
		models.put( OverlapTrackerFactory.KEY_IOU_CALCULATION, iouCalculationParam );
		return models;
	}
}
