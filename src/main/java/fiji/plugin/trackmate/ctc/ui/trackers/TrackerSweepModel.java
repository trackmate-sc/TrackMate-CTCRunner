package fiji.plugin.trackmate.ctc.ui.trackers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.AbstractSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;

public class TrackerSweepModel extends AbstractSweepModel< SpotTrackerFactory >
{

	protected TrackerSweepModel( final String name, final Map< String, AbstractParamSweepModel< ? > > models, final SpotTrackerFactory factory )
	{
		super( name, models, factory );
	}

	@Override
	public List< Settings > generateSettings( final Settings base, final int targetChannel )
	{
		// Null factory signals not to create any settings.
		if ( factory == null )
			return Collections.emptyList();

		final Settings s = base.copyOn( base.imp );
		final Map< String, Object > ds = factory.getDefaultSettings();
		s.trackerFactory = factory.copy();
		s.trackerSettings = ds;

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

	public static final class Builder extends AbstractSweepModel.Builder< Builder, TrackerSweepModel, SpotTrackerFactory >
	{

		@Override
		public TrackerSweepModel get()
		{
			return new TrackerSweepModel( name, models, factory );
		}
	}
}
