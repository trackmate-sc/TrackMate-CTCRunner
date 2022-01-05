package fiji.plugin.trackmate.ctc.model.detector;

import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.detection.LabeImageDetectorFactory;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;

public class LabelImgDetectorModel extends DetectorSweepModel
{

	public LabelImgDetectorModel()
	{
		super( LabeImageDetectorFactory.NAME, createModels(), new LabeImageDetectorFactory<>() );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
		models.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours );
		return models;
	}
}