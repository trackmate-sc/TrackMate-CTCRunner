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

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;
import fiji.plugin.trackmate.helper.model.detector.DetectorSweepModel.ModelsIterator;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.EnumParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.IntParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.morpholibj.Connectivity;
import fiji.plugin.trackmate.morpholibj.MorphoLibJDetectorFactory;

public class MorphoLibJOpt
{

	private MorphoLibJOpt()
	{}

	public static SpotDetectorFactoryBase< ? > createFactory()
	{
		return new MorphoLibJDetectorFactory<>();
	}

	public static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final DoubleParamSweepModel toleranceParam = new DoubleParamSweepModel()
				.paramName( "Tolerance" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 40. )
				.max( 60. )
				.nSteps( 3 );
		final EnumParamSweepModel< Connectivity > connectivityParam = new EnumParamSweepModel<>( Connectivity.class )
				.paramName( "Connectivity" )
				.rangeType( fiji.plugin.trackmate.helper.model.parameter.ArrayParamSweepModel.RangeType.FIXED )
				.addValue( Connectivity.DIAGONAL )
				.fixedValue( Connectivity.DIAGONAL );
		final BooleanParamSweepModel simplifyContourParam = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( MorphoLibJDetectorFactory.KEY_TOLERANCE, toleranceParam );
		models.put( MorphoLibJDetectorFactory.KEY_CONNECTIVITY, connectivityParam );
		models.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContourParam );
		return models;
	}

	public static Iterator< Settings > iterator( final Map< String, AbstractParamSweepModel< ? > > models, final Settings base, final int targetChannel )
	{
		final Settings s = base.copyOn( base.imp );
		final Map< String, Object > ds = createFactory().getDefaultSettings();
		ds.put( KEY_TARGET_CHANNEL, targetChannel );
		s.detectorFactory = createFactory();
		s.detectorSettings = ds;

		// Substitute the Connectivity model, that must return an integer...
		@SuppressWarnings( "unchecked" )
		final EnumParamSweepModel< Connectivity > connectivityModel = (fiji.plugin.trackmate.helper.model.parameter.EnumParamSweepModel< Connectivity > ) models.get( MorphoLibJDetectorFactory.KEY_CONNECTIVITY );
		final Integer[] vals = connectivityModel.getRange().stream()
				.map( Connectivity::getConnectivity )
				.collect( Collectors.toList() )
				.toArray( new Integer[] {} );
		final IntParamSweepModel connIntModel = new IntParamSweepModel()
				.paramName( connectivityModel.getParamName() )
				.rangeType( RangeType.MANUAL )
				.manualRange( vals );
		final Map< String, AbstractParamSweepModel< ? > > mappedModels = new HashMap<>( models );
		mappedModels.put( MorphoLibJDetectorFactory.KEY_CONNECTIVITY, connIntModel );
		return new ModelsIterator( s, mappedModels );
	}
}
