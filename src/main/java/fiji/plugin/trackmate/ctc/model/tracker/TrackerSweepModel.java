package fiji.plugin.trackmate.ctc.model.tracker;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.AbstractSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.Combinations;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;

public class TrackerSweepModel extends AbstractSweepModel< SpotTrackerFactory >
{

	protected TrackerSweepModel( final String name, final Map< String, AbstractParamSweepModel< ? > > models, final SpotTrackerFactory factory )
	{
		super( name, models, factory );
	}

	@Override
	public Iterator< Settings > iterator( final Settings base, final int targetChannel )
	{
		// Null factory signals not to create any settings.
		if ( factory == null )
			return Collections.emptyIterator();

		final Settings s = base.copyOn( base.imp );
		final Map< String, Object > ds = factory.getDefaultSettings();
		s.trackerFactory = factory.copy();
		s.trackerSettings = ds;

		return new MyIterator( s, models );
	}

	private static class MyIterator implements Iterator< Settings >
	{

		private final Settings base;

		private final Combinations combinations;

		public MyIterator( final Settings base, final Map< String, AbstractParamSweepModel< ? > > models )
		{
			this.base = base;
			final Map< String, List< Object > > values = new LinkedHashMap<>( models.size() );
			for ( final String key : models.keySet() )
			{
				@SuppressWarnings( "unchecked" )
				final List< Object > range = ( List< Object > ) models.get( key ).getRange();
				values.put( key, range );
			}
			this.combinations = new Combinations( values );
		}

		@Override
		public boolean hasNext()
		{
			return combinations.hasNext();
		}

		@Override
		public Settings next()
		{
			final Settings copy = base.copyOn( base.imp );
			final Map< String, Object > params = combinations.next();
			for ( final String key : params.keySet() )
			{
				final Object val = params.get( key );
				copy.trackerSettings.put( key, val );
			}
			return copy;
		}
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
