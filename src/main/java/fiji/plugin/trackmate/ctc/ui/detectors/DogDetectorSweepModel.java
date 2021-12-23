package fiji.plugin.trackmate.ctc.ui.detectors;

import java.util.List;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.detection.DogDetectorFactory;

public class DogDetectorSweepModel extends LogDetectorSweepModel
{

	public DogDetectorSweepModel()
	{
		this(
				new DoubleParamSweepModel(),
				new DoubleParamSweepModel(),
				new BooleanParamSweepModel(),
				new BooleanParamSweepModel() );
	}

	public DogDetectorSweepModel(
			final DoubleParamSweepModel estimatedDiameterParam,
			final DoubleParamSweepModel thresholdParam,
			final BooleanParamSweepModel subpixelLocalizationParam,
			final BooleanParamSweepModel useMedianParam )
	{
		super( "Difference of Gaussian detector",
				estimatedDiameterParam,
				thresholdParam,
				subpixelLocalizationParam,
				useMedianParam );
	}

	@Override
	public List< Settings > generateSettings( final Settings base, final int targetChannel )
	{
		return generateSettings( base, new DogDetectorFactory<>(), targetChannel );
	}
}
