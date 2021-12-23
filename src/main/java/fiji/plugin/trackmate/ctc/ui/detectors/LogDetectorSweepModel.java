package fiji.plugin.trackmate.ctc.ui.detectors;

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_DO_MEDIAN_FILTERING;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_RADIUS;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_THRESHOLD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.detection.LogDetectorFactory;

public class LogDetectorSweepModel extends AbstractSettingsSweepModel
{

	final DoubleParamSweepModel estimatedDiameterParam;

	final DoubleParamSweepModel thresholdParam;

	final BooleanParamSweepModel subpixelLocalizationParam;

	final BooleanParamSweepModel useMedianParam;

	public LogDetectorSweepModel()
	{
		this( new DoubleParamSweepModel(),
				new DoubleParamSweepModel(),
				new BooleanParamSweepModel(),
				new BooleanParamSweepModel() );
	}

	public LogDetectorSweepModel(
			final DoubleParamSweepModel estimatedDiameterParam,
			final DoubleParamSweepModel thresholdParam,
			final BooleanParamSweepModel subpixelLocalizationParam,
			final BooleanParamSweepModel useMedianParam )
	{
		this.estimatedDiameterParam = estimatedDiameterParam;
		this.thresholdParam = thresholdParam;
		this.subpixelLocalizationParam = subpixelLocalizationParam;
		this.useMedianParam = useMedianParam;
		// Pass listeners.
		estimatedDiameterParam.listeners().add( () -> notifyListeners() );
		thresholdParam.listeners().add( () -> notifyListeners() );
		subpixelLocalizationParam.listeners().add( () -> notifyListeners() );
		useMedianParam.listeners().add( () -> notifyListeners() );
	}

	@Override
	public List< Settings > generateSettings( final Settings base, final int targetChannel )
	{
		return generateSettings( base, new LogDetectorFactory<>(), targetChannel );
	}

	protected List< Settings > generateSettings( final Settings base, final LogDetectorFactory< ? > factory, final int targetChannel )
	{
		final List< Settings > list = new ArrayList<>();
		for ( final Number diameter : estimatedDiameterParam.getRange() )
			for ( final Number threshold : thresholdParam.getRange() )
				for ( final Boolean subpixel : subpixelLocalizationParam.getRange() )
					for ( final Boolean useMedian : useMedianParam.getRange() )
					{
						final Settings s = base.copyOn( base.imp );
						s.detectorFactory = factory.copy();
						final Map< String, Object > ds = factory.getDefaultSettings();
						ds.put( KEY_TARGET_CHANNEL, targetChannel );
						ds.put( KEY_RADIUS, diameter.doubleValue() / 2. );
						ds.put( KEY_THRESHOLD, threshold.doubleValue() );
						ds.put( KEY_DO_SUBPIXEL_LOCALIZATION, subpixel );
						ds.put( KEY_DO_MEDIAN_FILTERING, useMedian );
						s.detectorSettings = ds;
						list.add( s );
					}

		return list;
	}
}
