package fiji.plugin.trackmate.ctc.ui.detectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.detection.MaskDetectorFactory;

public class MaskDetectorSweepModel extends AbstractSettingsSweepModel
{

	private final BooleanParamSweepModel simplifyContourParam;

	public MaskDetectorSweepModel()
	{
		this( new BooleanParamSweepModel() );
	}

	public MaskDetectorSweepModel( final BooleanParamSweepModel simplifyContourParam )
	{
		this.simplifyContourParam = simplifyContourParam;
		simplifyContourParam.listeners().add( () -> notifyListeners() );
	}

	@Override
	public List< Settings > generateSettings( final Settings base, final int targetChannel )
	{
		final List< Settings > list = new ArrayList<>();
		for ( final Boolean simplifyContour : simplifyContourParam.getRange() )
		{
			final Settings s = base.copyOn( base.imp );
			s.detectorFactory = new MaskDetectorFactory<>();
			final Map< String, Object > ds = s.detectorFactory.getDefaultSettings();
			ds.put( MaskDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContour );
			s.detectorSettings = ds;
			list.add( s );
		}
		return list;
	}
}
