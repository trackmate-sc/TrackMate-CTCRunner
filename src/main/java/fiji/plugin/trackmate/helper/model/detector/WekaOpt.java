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

import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.IntParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.StringRangeParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.weka.WekaDetectorFactory;

public class WekaOpt
{

	private WekaOpt()
	{}

	public static SpotDetectorFactoryBase< ? > createFactory()
	{
		return new WekaDetectorFactory<>();
	}

	public static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final StringRangeParamSweepModel classifierPath = new StringRangeParamSweepModel()
				.paramName( "Weka classifier path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );
		final DoubleParamSweepModel probaThreshold = new DoubleParamSweepModel()
				.paramName( "Probability threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 0.2 )
				.max( 0.8 )
				.nSteps( 3 );
		final IntParamSweepModel classIndex = new IntParamSweepModel()
				.paramName( "Class index" )
				.rangeType( RangeType.FIXED )
				.min( 1 );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( WekaDetectorFactory.KEY_CLASSIFIER_FILEPATH, classifierPath );
		models.put( WekaDetectorFactory.KEY_CLASS_INDEX, classIndex );
		models.put( WekaDetectorFactory.KEY_PROBA_THRESHOLD, probaThreshold );
		return models;
	}
}
