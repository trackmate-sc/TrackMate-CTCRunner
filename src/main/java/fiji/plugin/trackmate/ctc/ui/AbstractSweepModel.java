package fiji.plugin.trackmate.ctc.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMateModule;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel.ModelListener;

public abstract class AbstractSweepModel< F extends TrackMateModule >
{

	protected final transient Listeners.List< ModelListener > modelListeners;

	public abstract List< Settings > generateSettings( final Settings base, final int targetChannel );

	protected final String name;

	protected final Map< String, AbstractParamSweepModel< ? > > models;

	protected final F factory;

	protected AbstractSweepModel( final String name, final Map< String, AbstractParamSweepModel< ? > > models, final F factory )
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

	protected void notifyListeners()
	{
		for ( final ModelListener l : modelListeners.list )
			l.modelChanged();
	}

	public String getName()
	{
		return name;
	}

	public static abstract class Builder< T extends Builder< T, M, F >, M extends AbstractSweepModel< F >, F extends TrackMateModule >
	{
	
		protected String name;

		protected F factory;
	
		protected final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
	
		@SuppressWarnings( "unchecked" )
		public T name( final String name )
		{
			this.name = name;
			return ( T ) this;
		}
	
		@SuppressWarnings( "unchecked" )
		public T factory( final F factory )
		{
			this.factory = factory;
			return ( T ) this;
		}

		@SuppressWarnings( "unchecked" )
		public < R > T add( final String parameterKey, final AbstractParamSweepModel< R > model )
		{
			models.put( parameterKey, model );
			return ( T ) this;
		}
	
		public abstract M get();
	}
}
