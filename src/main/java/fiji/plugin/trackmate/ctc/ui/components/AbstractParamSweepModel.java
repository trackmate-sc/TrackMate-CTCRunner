package fiji.plugin.trackmate.ctc.ui.components;

import org.scijava.listeners.Listeners;

public abstract class AbstractParamSweepModel< T >
{

	public interface ModelListener
	{
		public void modelChanged();
	}

	public abstract T[] getRange();

	private final transient Listeners.List< ModelListener > modelListeners;

	protected String paramName = "";

	public AbstractParamSweepModel()
	{
		this.modelListeners = new Listeners.SynchronizedList<>();
	}

	public AbstractParamSweepModel< T > paramName( final String paramName )
	{
		if ( this.paramName.equals( paramName ) )
		{
			this.paramName = paramName;
			notifyListeners();
		}
		return this;
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
