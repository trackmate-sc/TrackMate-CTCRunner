package fiji.plugin.trackmate.batcher.exporter;

import fiji.plugin.trackmate.gui.displaysettings.StyleElements;
import fiji.plugin.trackmate.gui.displaysettings.StyleElements.BoundedDoubleElement;
import fiji.plugin.trackmate.gui.displaysettings.StyleElements.IntElement;
import fiji.plugin.trackmate.gui.displaysettings.StyleElements.StyleElement;

public abstract class ExporterParam
{

	protected final String paramName;

	protected ExporterParam( final String paramName )
	{
		this.paramName = paramName;
	}

	public String name()
	{
		return paramName;
	}

	public abstract Object value();

	/**
	 * Returns a new {@link StyleElement} that will modify the value of this
	 * parameter.
	 * 
	 * @param notify
	 *            what to run when the value of this element is changed.
	 * @return a new Element instance.
	 */
	public abstract StyleElement element( Runnable notify );

	public static IntExporterParam intParam( final String name, final int value, final int min, final int max )
	{
		return new IntExporterParam( name, value, min, max );
	}

	public static DoubleExporterParam doubleParam( final String name, final double value, final double min, final double max )
	{
		return new DoubleExporterParam( name, value, min, max );
	}

	public static class IntExporterParam extends ExporterParam
	{

		private int value;

		private final int min;

		private final int max;

		public IntExporterParam( final String name, final int value, final int min, final int max )
		{
			super( name );
			this.value = value;
			this.min = min;
			this.max = max;
		}

		@Override
		public StyleElement element( final Runnable notify )
		{
			final IntElement el = StyleElements.intElement( paramName, min, max, () -> value, v -> {
				value = v;
				notify.run();
			} );
			el.set( value );
			return el;
		}

		@Override
		public Object value()
		{
			return Integer.valueOf( value );
		}
	}

	public static class DoubleExporterParam extends ExporterParam
	{

		private double value;

		private final double min;

		private final double max;

		public DoubleExporterParam( final String name, final double value, final double min, final double max )
		{
			super( name );
			this.value = value;
			this.min = min;
			this.max = max;
		}

		@Override
		public StyleElement element( final Runnable notify )
		{
			final BoundedDoubleElement el = StyleElements.boundedDoubleElement( paramName, min, max, () -> value,
					v -> {
						value = v;
						notify.run();
					} );
			el.set( value );
			return el;
		}

		@Override
		public Object value()
		{
			return Double.valueOf( value );
		}
	}

}
