package fiji.plugin.trackmate.ctc.ui.components;

import java.util.Arrays;
import java.util.List;

public abstract class NumberParamSweepModel extends AbstractParamSweepModel< Number >
{

	public enum RangeType
	{
		LIN_RANGE( "linear range" ), LOG_RANGE( "log range" ), FIXED( "fixed value" ), MANUAL( "manual range" );

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

	protected String units = "";

	protected RangeType rangeType = RangeType.LIN_RANGE;

	protected Number min = 1;

	protected Number max = 10;

	protected int nSteps = 10;

	protected List< Number > manualRange = Arrays.asList( 1., 2., 3., 4., 5., 6., 7., 8., 9., 10. );

	@Override
	public abstract List< Number > getRange();

	public NumberParamSweepModel units( final String units )
	{
		if ( this.units.equals( units ) )
		{
			this.units = units;
			notifyListeners();
		}
		return this;
	}

	public NumberParamSweepModel rangeType( final RangeType rangeType )
	{
		if ( this.rangeType != rangeType )
		{
			this.rangeType = rangeType;
			notifyListeners();
		}
		return this;
	}

	public NumberParamSweepModel nSteps( final int nSteps )
	{
		if ( this.nSteps != nSteps )
		{
			this.nSteps = nSteps;
			notifyListeners();
		}
		return this;
	}

	@Override
	public String toString()
	{
		switch ( rangeType )
		{
		case FIXED:
			return String.format( "%s (%s):\n"
					+ " - type: %s\n"
					+ " - value: %s",
					paramName,
					units,
					rangeType,
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
					rangeType,
					min,
					max,
					nSteps,
					Arrays.toString( getRange().toArray() ) );
		case MANUAL:
			return String.format( "%s (%s):\n"
					+ " - type: %s\n"
					+ " - values: %s",
					paramName,
					units,
					rangeType,
					Arrays.toString( getRange().toArray() ) );
		default:
			throw new IllegalArgumentException( "Unknown range type: " + rangeType );
		}
	}
}
