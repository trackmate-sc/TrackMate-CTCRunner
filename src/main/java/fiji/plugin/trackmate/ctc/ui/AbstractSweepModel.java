package fiji.plugin.trackmate.ctc.ui;

import java.util.Iterator;
import java.util.Map;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMateModule;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel.ModelListener;

public abstract class AbstractSweepModel< F extends TrackMateModule >
{

	protected transient Listeners.List< ModelListener > modelListeners;

	public abstract Iterator< Settings > iterator( final Settings base, final int targetChannel );

	protected final String name;

	protected final Map< String, AbstractParamSweepModel< ? > > models;

	protected final F factory;
	
	protected AbstractSweepModel( final String name, final Map< String, AbstractParamSweepModel< ? > > models, final F factory )
	{
		this.name = name;
		this.models = models;
		this.factory = factory;
	}

	public Listeners.List< ModelListener > listeners()
	{
		if ( modelListeners == null )
		{
			/*
			 * Work around the listeners field being null after deserialization.
			 * We also need to register again the sub-models.
			 */
			this.modelListeners = new Listeners.SynchronizedList<>();
			for ( final AbstractParamSweepModel< ? > model : models.values() )
				model.listeners().add( () -> notifyListeners() );
		}
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
}
