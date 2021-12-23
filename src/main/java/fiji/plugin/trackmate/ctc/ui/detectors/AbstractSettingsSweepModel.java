package fiji.plugin.trackmate.ctc.ui.detectors;

import java.util.List;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel.ModelListener;

public abstract class AbstractSettingsSweepModel
{

	protected final transient Listeners.List< ModelListener > modelListeners;

	private final String name;


	public AbstractSettingsSweepModel( final String name )
	{
		this.name = name;
		this.modelListeners = new Listeners.SynchronizedList<>();
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

	public abstract List< Settings > generateSettings( final Settings base, final int targetChannel );
}
