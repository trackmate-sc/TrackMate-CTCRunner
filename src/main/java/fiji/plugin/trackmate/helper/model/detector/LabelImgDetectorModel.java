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

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.detection.LabelImageDetectorFactory;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel;

@Plugin( type = DetectorSweepModel.class, priority = 1000000 - 6 )
public class LabelImgDetectorModel extends DetectorSweepModel
{

	public LabelImgDetectorModel()
	{
		super( LabelImageDetectorFactory.NAME, createModels(), new LabelImageDetectorFactory<>() );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours );
		return models;
	}
}
