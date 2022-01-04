package fiji.plugin.trackmate.ctc.ui.detectors;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.DogDetectorFactory;
import fiji.plugin.trackmate.detection.LabeImageDetectorFactory;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.detection.MaskDetectorFactory;
import fiji.plugin.trackmate.detection.SpotDetectorFactory;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;

public class DetectorSweepModels
{

	public static DetectorSweepModel labelImgDetectorModel()
	{
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		return DetectorSweepModel.create()
				.name( LabeImageDetectorFactory.NAME )
				.factory( new LabeImageDetectorFactory<>() )
				.add( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours )
				.get();
	}

	public static DetectorSweepModel thresholdDetectorModel()
	{
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final DoubleParamSweepModel intensityThreshold = new DoubleParamSweepModel()
				.paramName( "Intensity threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 50. )
				.max( 100. )
				.nSteps( 3 );

		return DetectorSweepModel.create()
				.name( ThresholdDetectorFactory.NAME )
				.factory( new ThresholdDetectorFactory<>() )
				.add( ThresholdDetectorFactory.KEY_INTENSITY_THRESHOLD, intensityThreshold )
				.add( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours )
				.get();
	}

	public static DetectorSweepModel maskDetectorModel()
	{
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		return DetectorSweepModel.create()
				.name( MaskDetectorFactory.NAME )
				.factory( new MaskDetectorFactory<>() )
				.add( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours )
				.get();
	}

	public static DetectorSweepModel logDetectorModel()
	{
		return dlogDetectorModel( new LogDetectorFactory<>() );
	}

	public static DetectorSweepModel dogDetectorModel()
	{
		return dlogDetectorModel( new DogDetectorFactory<>() );
	}

	private static DetectorSweepModel dlogDetectorModel( final SpotDetectorFactory< ? > factory )
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

		return DetectorSweepModel.create()
				.name( factory.getName() )
				.factory( factory.copy() )
				.add( DetectorKeys.KEY_RADIUS, estimatedRadius )
				.add( DetectorKeys.KEY_THRESHOLD, threshold )
				.add( DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, subpixelLocalization )
				.add( DetectorKeys.KEY_DO_MEDIAN_FILTERING, useMedian )
				.get();
	}

	private DetectorSweepModels()
	{}
}
