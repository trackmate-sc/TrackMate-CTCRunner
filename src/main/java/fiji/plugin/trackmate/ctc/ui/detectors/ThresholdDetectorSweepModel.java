package fiji.plugin.trackmate.ctc.ui.detectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;

public class ThresholdDetectorSweepModel extends AbstractSweepModel
{

	private final BooleanParamSweepModel simplifyContourParam;

	private final DoubleParamSweepModel intensityThresholdParam;

	public ThresholdDetectorSweepModel()
	{
		this( new DoubleParamSweepModel(), new BooleanParamSweepModel() );
	}

	public ThresholdDetectorSweepModel(
			final DoubleParamSweepModel intensityThresholdParam,
			final BooleanParamSweepModel simplifyContourParam )
	{
		this.intensityThresholdParam = intensityThresholdParam;
		this.simplifyContourParam = simplifyContourParam;
		simplifyContourParam.listeners().add( () -> notifyListeners() );
		intensityThresholdParam.listeners().add( () -> notifyListeners() );
	}

	@Override
	public List< Settings > generateSettings( final Settings base, final int targetChannel )
	{
		final List< Settings > list = new ArrayList<>();
		for ( final Number intensityThreshold : intensityThresholdParam.getRange() )
			for ( final Boolean simplifyContour : simplifyContourParam.getRange() )
			{
				final Settings s = base.copyOn( base.imp );
				s.detectorFactory = new ThresholdDetectorFactory<>();
				final Map< String, Object > ds = s.detectorFactory.getDefaultSettings();
				ds.put( ThresholdDetectorFactory.KEY_INTENSITY_THRESHOLD, intensityThreshold.doubleValue() );
				ds.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContour );
				s.detectorSettings = ds;
				list.add( s );
			}

		return list;
	}
}
