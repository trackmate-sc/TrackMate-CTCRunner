package fiji.plugin.trackmate.ctc.model.detector;

import java.util.LinkedHashMap;
import java.util.Map;

import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.detection.MaskDetectorFactory;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;

public class MaskDetectorModel extends DetectorSweepModel
{

	public MaskDetectorModel()
	{
		super( MaskDetectorFactory.NAME, createModels(), new MaskDetectorFactory<>() );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.model.parameter.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours );
		return models;
	}
}