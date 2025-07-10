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

import java.util.LinkedHashMap;
import java.util.Map;

import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.ArrayParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.CondaEnvParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.StringRangeParamSweepModel;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;
import fiji.plugin.trackmate.tracking.trackastra.TrackastraCLI;
import fiji.plugin.trackmate.tracking.trackastra.TrackastraTrackerFactory;

public class TrackastraOpt
{

	private TrackastraOpt()
	{}

	static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final CondaEnvParamSweepModel condaEnv = new CondaEnvParamSweepModel()
				.paramName( "Trackastra conda environment" );

		final ArrayParamSweepModel< String > pretrainedModel = new ArrayParamSweepModel<>( new String[] {
				"general_2d",
				"ctc" } )
						.paramName( "Pretrained model" )
						.addValue( "general_2d" )
						.fixedValue( "general_2d" );

		final ArrayParamSweepModel< String > pretrainedOrCustom = new ArrayParamSweepModel<>( new String[] {
				"PRETRAINED_MODEL",
				"CUSTOM_MODEL_PATH" } )
						.paramName( "Use pretrained or custom model" )
						.addValue( "PRETRAINED_MODEL" )
						.fixedValue( "PRETRAINED_MODEL" );

		final StringRangeParamSweepModel customModelPath = new StringRangeParamSweepModel()
				.paramName( "Trackastra custom model path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );

		final ArrayParamSweepModel< String > trackingMode = new ArrayParamSweepModel<>( new String[] {
				"greedy_nodiv",
				"greedy",
				"ilp" } )
						.paramName( "Tracking mode" )
						.addValue( "greedy" )
						.fixedValue( "greedy" );

		final ArrayParamSweepModel< String > useGPU = new ArrayParamSweepModel<>( new String[] {
				"automatic",
				"mps",
				"cuda",
				"cpu" } )
						.paramName( "Use GPU" )
						.addValue( "automatic" )
						.fixedValue( "automatic" );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( TrackastraCLI.KEY_CONDA_ENV, condaEnv );
		models.put( TrackastraCLI.KEY_TRACKASTRA_MODEL, pretrainedModel );
		models.put( TrackastraCLI.KEY_TRACKASTRA_PRETRAINED_OR_CUSTOM, pretrainedOrCustom );
		models.put( TrackastraCLI.KEY_TRACKASTRA_CUSTOM_MODEL_FOLDER, customModelPath );
		models.put( TrackastraCLI.KEY_TRACKASTRA_TRACKING_MODE, trackingMode );
		models.put( TrackastraCLI.KEY_DEVICE, useGPU );
		return models;
	}

	public static SpotTrackerFactory createFactory()
	{
		return new TrackastraTrackerFactory();
	}
}
