package fiji.plugin.trackmate.ctc.ui.detectors;

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepModel;
import fiji.plugin.trackmate.weka.WekaDetectorFactory;

public class WekaDetectorSweepModel extends AbstractSettingsSweepModel
{

	final StringRangeParamSweepModel modelPathParam;

	final DoubleParamSweepModel probaThresholdParam;

	final IntParamSweepModel classIndexParam;

	public WekaDetectorSweepModel()
	{
		this( new StringRangeParamSweepModel(),
				new DoubleParamSweepModel(),
				new IntParamSweepModel() );
	}

	public WekaDetectorSweepModel(
			final StringRangeParamSweepModel modelPathParam,
			final DoubleParamSweepModel toleranceParam,
			final IntParamSweepModel classIndexParam )
	{
		super( WekaDetectorFactory.NAME );
		this.modelPathParam = modelPathParam;
		this.probaThresholdParam = toleranceParam;
		this.classIndexParam = classIndexParam;
		// Pass listeners.
		modelPathParam.listeners().add( () -> notifyListeners() );
		toleranceParam.listeners().add( () -> notifyListeners() );
		classIndexParam.listeners().add( () -> notifyListeners() );
	}

	@Override
	public List< Settings > generateSettings( final Settings base, final int targetChannel )
	{
		final List< Settings > list = new ArrayList<>();
		for ( final String modelPath : modelPathParam.getRange() )
			for ( final Number tolerance : probaThresholdParam.getRange() )
				for ( final Number classIndex : classIndexParam.getRange() )
					{
						final Settings s = base.copyOn( base.imp );
						s.detectorFactory = new WekaDetectorFactory<>();
						final Map< String, Object > ds = s.detectorFactory.getDefaultSettings();
						ds.put( KEY_TARGET_CHANNEL, targetChannel );
						ds.put( WekaDetectorFactory.KEY_CLASSIFIER_FILEPATH, modelPath );
						ds.put( WekaDetectorFactory.KEY_PROBA_THRESHOLD, tolerance.doubleValue() );
						ds.put( WekaDetectorFactory.KEY_CLASS_INDEX, classIndex.intValue() );
						s.detectorSettings = ds;
						list.add( s );
					}

		return list;
	}
}
