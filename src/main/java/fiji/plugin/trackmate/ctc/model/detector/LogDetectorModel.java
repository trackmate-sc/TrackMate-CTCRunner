package fiji.plugin.trackmate.ctc.model.detector;

import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.LogDetectorFactory;

public class LogDetectorModel extends DetectorSweepModel
{

	public LogDetectorModel()
	{
		super( LogDetectorFactory.NAME, createModels(), new LogDetectorFactory<>() );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
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

		final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
		models.put( DetectorKeys.KEY_RADIUS, estimatedRadius );
		models.put( DetectorKeys.KEY_THRESHOLD, threshold );
		models.put( DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, subpixelLocalization );
		models.put( DetectorKeys.KEY_DO_MEDIAN_FILTERING, useMedian );
		return models;
	}
}