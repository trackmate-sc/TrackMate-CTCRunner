package fiji.plugin.trackmate.ctc.ui.trackers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel.ModelListener;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;

public class TrackerSweepModel
{

	private final transient Listeners.List< ModelListener > modelListeners;

	private final String name;

	protected final Map< String, AbstractParamSweepModel< ? > > models;

	protected final SpotTrackerFactory factory;

	protected TrackerSweepModel( final String name, final Map< String, AbstractParamSweepModel< ? > > models, final SpotTrackerFactory factory )
	{
		this.name = name;
		this.models = models;
		this.factory = factory;
		this.modelListeners = new Listeners.SynchronizedList<>();

		for ( final AbstractParamSweepModel< ? > model : models.values() )
			model.listeners().add( () -> notifyListeners() );
	}

	public Listeners.List< ModelListener > listeners()
	{
		return modelListeners;
	}

	private void notifyListeners()
	{
		for ( final ModelListener l : modelListeners.list )
			l.modelChanged();
	}

	public String getName()
	{
		return name;
	}

	public List< Settings > generateSettings( final Settings base, final int targetChannel )
	{
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
				copy.trackerSettings.put( key, val );
				out.add( copy );
			}

		return out;
	}

	public static Builder create()
	{
		return new Builder();
	}

	public static class Builder
	{

		private String name;

		private SpotTrackerFactory factory;

		private final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();

		public Builder name( final String name )
		{
			this.name = name;
			return this;
		}

		public Builder factory( final SpotTrackerFactory factory )
		{
			this.factory = factory;
			return this;
		}

		public < T > Builder add( final String parameterKey, final AbstractParamSweepModel< T > model )
		{
			models.put( parameterKey, model );
			return this;
		}

		public TrackerSweepModel get()
		{
			if ( name == null )
				throw new IllegalArgumentException( "Name is missing." );
			if ( factory == null )
				throw new IllegalArgumentException( "Factory is missing" );
			return new TrackerSweepModel( name, models, factory );
		}
	}
}
