package fiji.plugin.trackmate.ctc.ui.components;

import java.util.Arrays;

public abstract class NumberParamSweepModel
{

	public enum RangeType
	{
		LIN_RANGE( "linear range" ), LOG_RANGE( "log range" ), FIXED( "fixedd value" ), MANUAL( "manual range" );

		private final String name;

		RangeType( final String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	protected final String paramName;

	protected final String units;

	protected final RangeType type;

	protected final Number min;

	protected final Number max;

	protected final int nSteps;

	protected final Number[] manualRange;

	public NumberParamSweepModel(
			final String paramName,
			final String units,
			final RangeType type,
			final Number min,
			final Number max,
			final int nSteps,
			final Number[] manualRange )

	{
		this.paramName = paramName;
		this.units = units;
		this.type = type;
		this.min = min;
		this.max = max;
		this.nSteps = nSteps;
		this.manualRange = manualRange;
	}

	public abstract Number[] getRange();

	@Override
	public String toString()
	{
		switch ( type )
		{
		case FIXED:
			return String.format( "%s (%s):\n"
					+ " - type: %s\n"
					+ " - value: %s",
					paramName,
					units,
					type,
					min );
		case LIN_RANGE:
		case LOG_RANGE:
			return String.format( "%s (%s):\n"
					+ " - type: %s\n"
					+ " - min: %s\n"
					+ " - max: %s\n"
					+ " - nSteps: %d\n"
					+ " - values: %s",
					paramName,
					units,
					type,
					min,
					max,
					nSteps,
					Arrays.toString( getRange() ) );
		case MANUAL:
			return String.format( "%s (%s):\n"
					+ " - type: %s\n"
					+ " - values: %s",
					paramName,
					units,
					type,
					Arrays.toString( getRange() ) );
		default:
			throw new IllegalArgumentException( "Unknown range type: " + type );
		}
	}
}
