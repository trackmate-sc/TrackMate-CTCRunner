package fiji.plugin.trackmate.ctc.ui.components;

import java.util.List;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.ctc.ui.AbstractSweepModel.ModelListener;

public abstract class AbstractParamSweepModel< T >
{

	public abstract List< T > getRange();

	private final transient Listeners.List< ModelListener > modelListeners;

	protected String paramName = " ";

	public AbstractParamSweepModel()
	{
		this.modelListeners = new Listeners.SynchronizedList<>();
	}

	public AbstractParamSweepModel< T > paramName( final String paramName )
	{
		if ( !this.paramName.equals( paramName ) )
		{
			this.paramName = paramName;
			notifyListeners();
		}
		return this;
	}

	public String getParamName()
	{
		return paramName;
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
}
