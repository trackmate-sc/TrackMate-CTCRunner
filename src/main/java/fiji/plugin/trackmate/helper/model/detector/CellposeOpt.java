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

import static fiji.plugin.trackmate.cellpose.CellposeCLI.KEY_CELLPOSE_MODEL;
import static fiji.plugin.trackmate.cellpose.CellposeCLI.KEY_CELLPOSE_PRETRAINED_OR_CUSTOM;
import static fiji.plugin.trackmate.cellpose.CellposeCLI.KEY_CELL_DIAMETER;
import static fiji.plugin.trackmate.cellpose.CellposeCLI.KEY_OPTIONAL_CHANNEL_2;
import static fiji.plugin.trackmate.cellpose.CellposeCLIBase.KEY_CELLPOSE_CUSTOM_MODEL_FILEPATH;
import static fiji.plugin.trackmate.cellpose.CellposeCLIBase.KEY_USE_GPU;

import java.util.LinkedHashMap;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.cellpose.CellposeCLIBase;
import fiji.plugin.trackmate.cellpose.CellposeDetectorFactory;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;
import fiji.plugin.trackmate.helper.model.parameter.AbstractArrayParamSweepModel.ArrayRangeType;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.ArrayParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel.BooleanRangeType;
import fiji.plugin.trackmate.helper.model.parameter.CondaEnvParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.helper.model.parameter.StringRangeParamSweepModel;

public class CellposeOpt
{

	private CellposeOpt()
	{}

	static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final CondaEnvParamSweepModel condaEnv = new CondaEnvParamSweepModel()
				.paramName( "Cellpose conda environment" );

		final ArrayParamSweepModel< String > pretrainedOrCustom = new ArrayParamSweepModel<>( new String[] {
				KEY_CELLPOSE_MODEL,
				KEY_CELLPOSE_CUSTOM_MODEL_FILEPATH } )
						.paramName( "Use pretrained or custom model" )
						.addValue(KEY_CELLPOSE_MODEL )
						.fixedValue( KEY_CELLPOSE_MODEL );

		final ArrayParamSweepModel< String > pretrainedModel = new ArrayParamSweepModel<>( new String[] {
				"cyto3",
				"nucleitorch_0",
				"tissuenet_cp3" ,
				"livecell_cp3" ,
				"yeast_PhC_cp3" ,
				"yeast_BF_cp3" ,
				"bact_phase_cp3" ,
				"bact_fluor_cp3" ,
				"deepbacs_cp3" ,
				"cyto2torch_0",
				"cytotorch_0" } )
						.paramName( "Pretrained model" )
						.addValue( "cyto3" )
						.fixedValue( "cyto3" );

		final StringRangeParamSweepModel cellposeCustomModelPath = new StringRangeParamSweepModel()
				.paramName( "Cellpose custom model path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );

		final String[] chans1 = new String[] { "0", "1", "2", "3", "4" };
		final ArrayParamSweepModel< String > channel1 = new ArrayParamSweepModel<>( chans1 )
				.rangeType( ArrayRangeType.FIXED )
				.paramName( "Channel to segment" )
				.fixedValue( "0" )
				.addValue( "0" );

		final String[] chans2 = new String[] { "0", "1", "2", "3", "4" };
		final ArrayParamSweepModel< String > channel2 = new ArrayParamSweepModel<>( chans2 )
				.rangeType( ArrayRangeType.FIXED )
				.paramName( "Optional second channel" )
				.fixedValue( "0" )
				.addValue( "0" );

		final DoubleParamSweepModel cellDiameter = new DoubleParamSweepModel()
				.paramName( "Cell diameter" )
				.dimension( Dimension.LENGTH )
				.rangeType( RangeType.FIXED )
				.min( 0. )
				.max( 50. );

		final BooleanParamSweepModel useGPU = new BooleanParamSweepModel()
				.paramName( "Use GPU" )
				.rangeType( BooleanRangeType.FIXED )
				.fixedValue( true );

		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( BooleanRangeType.FIXED )
				.fixedValue( true );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( CellposeCLIBase.KEY_CONDA_ENV, condaEnv );
		models.put( KEY_CELLPOSE_PRETRAINED_OR_CUSTOM, pretrainedOrCustom );
		models.put( KEY_CELLPOSE_MODEL, pretrainedModel );
		models.put( KEY_CELLPOSE_CUSTOM_MODEL_FILEPATH, cellposeCustomModelPath );
		models.put( KEY_CELL_DIAMETER, cellDiameter );
		models.put( DetectorKeys.KEY_TARGET_CHANNEL, channel1 );
		models.put( KEY_OPTIONAL_CHANNEL_2, channel2 );
		models.put( KEY_USE_GPU, useGPU );
		models.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours );
		return models;
	}

	public static SpotDetectorFactoryBase< ? > createFactory()
	{
		return new CellposeDetectorFactory<>();
	}
}
