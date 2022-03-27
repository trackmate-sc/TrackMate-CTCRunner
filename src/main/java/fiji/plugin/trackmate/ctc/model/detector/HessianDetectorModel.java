package fiji.plugin.trackmate.ctc.model.detector;

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.HessianDetectorFactory;

@Plugin( type = DetectorSweepModel.class, priority = 1000000 - 3 )
public class HessianDetectorModel extends DetectorSweepModel
{

	public HessianDetectorModel(  )
	{
		super( HessianDetectorFactory.NAME, createModels(), new HessianDetectorFactory<>() );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final DoubleParamSweepModel estimatedXYRadius = new DoubleParamSweepModel()
				.paramName( "Estimated XY radius" )
				.dimension( Dimension.LENGTH )
				.rangeType( RangeType.FIXED )
				.min( 1. );
		final DoubleParamSweepModel estimatedZRadius = new DoubleParamSweepModel()
				.paramName( "Estimated Z radius" )
				.dimension( Dimension.LENGTH )
				.rangeType( RangeType.FIXED )
				.min( 2. );
		final DoubleParamSweepModel threshold = new DoubleParamSweepModel()
				.paramName( "Threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 0.5 )
				.max( 0.9 )
				.nSteps( 3 );
		final BooleanParamSweepModel normalizeQuality = new BooleanParamSweepModel()
				.paramName( "Normalize quality" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final BooleanParamSweepModel subpixelLocalization = new BooleanParamSweepModel()
				.paramName( "Sub-pixel localization" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( DetectorKeys.KEY_RADIUS, estimatedXYRadius );
		models.put( DetectorKeys.KEY_RADIUS_Z, estimatedZRadius );
		models.put( DetectorKeys.KEY_THRESHOLD, threshold );
		models.put( DetectorKeys.KEY_NORMALIZE, normalizeQuality );
		models.put( DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, subpixelLocalization );
		return models;
	}
}
