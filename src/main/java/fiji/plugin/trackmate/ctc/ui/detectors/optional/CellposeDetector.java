package fiji.plugin.trackmate.ctc.ui.detectors.optional;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.cellpose.CellposeDetectorFactory;
import fiji.plugin.trackmate.cellpose.CellposeSettings.PretrainedModel;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.detectors.DetectorSweepModel;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;

public class CellposeDetector
{
	public static DetectorSweepModel cellposeDetectorModel()
	{
		final StringRangeParamSweepModel cellposePath = new StringRangeParamSweepModel()
				.paramName( "Cellpose Python path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );
		final EnumParamSweepModel< PretrainedModel > cellposeModel = new EnumParamSweepModel<>( PretrainedModel.class )
				.paramName( "Cellpose model" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel.RangeType.FIXED )
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
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		return DetectorSweepModel.create()
				.name( CellposeDetectorFactory.NAME )
				.factory( new CellposeDetectorFactory<>() )
				.add( CellposeDetectorFactory.KEY_CELLPOSE_PYTHON_FILEPATH, cellposePath )
				.add( CellposeDetectorFactory.KEY_CELLPOSE_MODEL, cellposeModel )
				.add( CellposeDetectorFactory.KEY_CELL_DIAMETER, cellDiameter )
				.add( DetectorKeys.KEY_TARGET_CHANNEL, channel1 )
				.add( CellposeDetectorFactory.KEY_OPTIONAL_CHANNEL_2, channel2 )
				.add( CellposeDetectorFactory.KEY_USE_GPU, useGPU )
				.add( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours )
				.get();
	}
}
