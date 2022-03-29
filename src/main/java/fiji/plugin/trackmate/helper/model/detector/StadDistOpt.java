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
package fiji.plugin.trackmate.helper.model.detector;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.InfoParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.StringRangeParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.stardist.StarDistCustomDetectorFactory;
import fiji.plugin.trackmate.stardist.StarDistDetectorFactory;

public class StadDistOpt
{

	private StadDistOpt()
	{}

	public static SpotDetectorFactoryBase< ? > createFactoryBuiltin()
	{
		return new StarDistDetectorFactory<>();
	}

	public static Map< String, AbstractParamSweepModel< ? > > createModelsBuiltin()
	{
		final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
		models.put( "", new InfoParamSweepModel()
				.info( "The TrackMate-StarDist built-in detector has no parameter to tune." )
				.url( "" ) );
		return models;
	}

	public static SpotDetectorFactoryBase< ? > createFactoryCustom()
	{
		return new StarDistCustomDetectorFactory<>();
	}

	public static Map< String, AbstractParamSweepModel< ? > > createModelsCustom()
	{
		final StringRangeParamSweepModel stardistBunblePath = new StringRangeParamSweepModel()
				.paramName( "StarDist model bundle path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );
		final DoubleParamSweepModel probaThreshold = new DoubleParamSweepModel()
				.paramName( "Score threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 0.2 )
				.max( 0.8 )
				.nSteps( 3 );
		final DoubleParamSweepModel overlapThreshold = new DoubleParamSweepModel()
				.paramName( "Overlap threshold" )
				.rangeType( RangeType.FIXED )
				.min( 0.5 );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( StarDistCustomDetectorFactory.KEY_MODEL_FILEPATH, stardistBunblePath );
		models.put( StarDistCustomDetectorFactory.KEY_SCORE_THRESHOLD, probaThreshold );
		models.put( StarDistCustomDetectorFactory.KEY_OVERLAP_THRESHOLD, overlapThreshold );
		return models;
	}
}
