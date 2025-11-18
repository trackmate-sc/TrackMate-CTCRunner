/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
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
import fiji.plugin.trackmate.helper.model.parameter.CondaEnvParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.StringRangeParamSweepModel;
import fiji.plugin.trackmate.yolo.YOLOCLI;
import fiji.plugin.trackmate.yolo.YOLODetectorFactory;

public class YOLOOpt
{

	private YOLOOpt()
	{}

	static Map< String, AbstractParamSweepModel< ? > > createModels()
	{

		final YOLOCLI cli = new YOLOCLI();

		final CondaEnvParamSweepModel condaEnv = new CondaEnvParamSweepModel()
				.paramName( "YOLO conda environment" );

		final StringRangeParamSweepModel modelPath = new StringRangeParamSweepModel()
				.paramName( cli.modelPath().getName() )
				.isFile( true )
				.add( cli.modelPath().getDefaultValue() );

		final DoubleParamSweepModel ioUParam = new DoubleParamSweepModel()
				.paramName( cli.iouThreshold().getName() )
				.min( 0.5 )
				.max( 0.9 )
				.nSteps( 3 );

		final DoubleParamSweepModel confidenceParam = new DoubleParamSweepModel()
				.paramName( cli.confidenceThreshold().getName() )
				.min( 0.1 )
				.max( 0.5 )
				.nSteps( 5 );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( YOLOCLI.KEY_CONDA_ENV, condaEnv );
		models.put( YOLODetectorFactory.KEY_YOLO_MODEL_FILEPATH, modelPath );
		models.put( YOLODetectorFactory.KEY_YOLO_CONF, confidenceParam );
		models.put( YOLODetectorFactory.KEY_YOLO_IOU, ioUParam );
		return models;
	}

	public static SpotDetectorFactoryBase< ? > createFactory()
	{
		return new YOLODetectorFactory<>();
	}
}
