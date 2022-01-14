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
package fiji.plugin.trackmate.ctc.model.detector;

import java.util.LinkedHashMap;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.cellpose.CellposeDetectorFactory;
import fiji.plugin.trackmate.cellpose.CellposeSettings.PretrainedModel;
import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.EnumParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.ctc.model.parameter.StringRangeParamSweepModel;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;

public class CellposeOpt
{

	private CellposeOpt()
	{}

	static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final StringRangeParamSweepModel cellposePath = new StringRangeParamSweepModel()
				.paramName( "Cellpose Python path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );
		final EnumParamSweepModel< PretrainedModel > cellposeModel = new EnumParamSweepModel<>( PretrainedModel.class )
				.paramName( "Cellpose model" )
				.rangeType( fiji.plugin.trackmate.ctc.model.parameter.ArrayParamSweepModel.RangeType.FIXED )
				.fixedValue( PretrainedModel.CYTO );
		final IntParamSweepModel channel1 = new IntParamSweepModel()
				.paramName( "Channel to segment" )
				.rangeType( RangeType.FIXED )
				.min( 0 )
				.max( 4 );
		final IntParamSweepModel channel2 = new IntParamSweepModel()
				.paramName( "Optional second channel" )
				.rangeType( RangeType.FIXED )
				.min( 0 )
				.max( 4 );
		final DoubleParamSweepModel cellDiameter = new DoubleParamSweepModel()
				.paramName( "Cell diameter" )
				.dimension( Dimension.LENGTH )
				.rangeType( RangeType.FIXED )
				.min( 0. )
				.max( 50. );
		final BooleanParamSweepModel useGPU = new BooleanParamSweepModel()
				.paramName( "Use GPU" )
				.rangeType( fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( CellposeDetectorFactory.KEY_CELLPOSE_PYTHON_FILEPATH, cellposePath );
		models.put( CellposeDetectorFactory.KEY_CELLPOSE_MODEL, cellposeModel );
		models.put( CellposeDetectorFactory.KEY_CELL_DIAMETER, cellDiameter );
		models.put( DetectorKeys.KEY_TARGET_CHANNEL, channel1 );
		models.put( CellposeDetectorFactory.KEY_OPTIONAL_CHANNEL_2, channel2 );
		models.put( CellposeDetectorFactory.KEY_USE_GPU, useGPU );
		models.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours );
		return models;
	}

	public static SpotDetectorFactoryBase< ? > createFactory()
	{
		return new CellposeDetectorFactory<>();
	}

	public static Object castPretrainedModel( final String str )
	{
		for ( final PretrainedModel e : PretrainedModel.values() )
			if ( e.toString().equals( str ) )
				return e;

		return null;
	}
}
