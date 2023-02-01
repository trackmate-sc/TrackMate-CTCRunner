/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.DogDetectorFactory;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel.RangeType;

@Plugin( type = DetectorSweepModel.class, priority = 1000000 - 2 )
public class DogDetectorModel extends DetectorSweepModel
{

	public DogDetectorModel()
	{
		super( DogDetectorFactory.THIS_NAME, createModels(), new DogDetectorFactory<>() );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final DoubleParamSweepModel estimatedRadius = new DoubleParamSweepModel()
				.paramName( "Estimated radius" )
				.dimension( Dimension.LENGTH )
				.rangeType( RangeType.FIXED )
				.min( 5. );
		final DoubleParamSweepModel threshold = new DoubleParamSweepModel()
				.paramName( "Threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 50. )
				.max( 100. )
				.nSteps( 3 );
		final BooleanParamSweepModel subpixelLocalization = new BooleanParamSweepModel()
				.paramName( "Sub-pixel localization" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final BooleanParamSweepModel useMedian = new BooleanParamSweepModel()
				.paramName( "Median pre-processing" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( false );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( DetectorKeys.KEY_RADIUS, estimatedRadius );
		models.put( DetectorKeys.KEY_THRESHOLD, threshold );
		models.put( DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, subpixelLocalization );
		models.put( DetectorKeys.KEY_DO_MEDIAN_FILTERING, useMedian );
		return models;
	}
}
