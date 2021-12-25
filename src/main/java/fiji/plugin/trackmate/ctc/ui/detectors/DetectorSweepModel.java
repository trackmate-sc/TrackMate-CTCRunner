package fiji.plugin.trackmate.ctc.ui.detectors;

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.AbstractSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;

public class DetectorSweepModel extends AbstractSweepModel< SpotDetectorFactoryBase< ? > >
{

	protected DetectorSweepModel( final String name, final Map< String, AbstractParamSweepModel< ? > > models, final SpotDetectorFactoryBase< ? > factory )
	{
		super( name, models, factory );
	}

	@Override
	public List< Settings > generateSettings( final Settings base, final int targetChannel )
	{
		final Settings s = base.copyOn( base.imp );
		final Map< String, Object > ds = factory.getDefaultSettings();
		ds.put( KEY_TARGET_CHANNEL, targetChannel );
		s.detectorFactory = factory.copy();
		s.detectorSettings = ds;

		List< Settings > list = Collections.singletonList( s );
		for ( final Entry< String, AbstractParamSweepModel< ? > > entry : models.entrySet() )
			list = expand( list, entry.getKey(), entry.getValue() );
		
		return list;
	}

	private List< Settings > expand( final List< Settings > in, final String key, final AbstractParamSweepModel< ? > model )
	{
		final List< Settings > out = new ArrayList<>( in.size() * model.getRange().size() );
		for ( final Settings s : in )
			for ( final Object val : model.getRange() )
			{
				final Settings copy = s.copyOn( s.imp );
				copy.detectorSettings.put( key, val );
				out.add( copy );
			}

		return out;
	}

	public static final Builder create()
	{
		return new Builder();
	}

	public static final class Builder extends AbstractSweepModel.Builder< Builder, DetectorSweepModel, SpotDetectorFactoryBase< ? > >
	{

		@Override
		public DetectorSweepModel get()
		{
			return new DetectorSweepModel( name, models, factory );
		}
	}
}
