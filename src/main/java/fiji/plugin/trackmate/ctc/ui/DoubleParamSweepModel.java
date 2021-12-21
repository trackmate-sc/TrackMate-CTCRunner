package fiji.plugin.trackmate.ctc.ui;

import java.util.Arrays;

public class DoubleParamSweepModel
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

	final String paramName;

	final String units;

	final RangeType type;

	final double min;

	final double max;

	final int nSteps;

	final double[] manualRange;

	private DoubleParamSweepModel(
			final String paramName,
			final String units,
			final RangeType type,
			final double min,
			final double max,
			final int nSteps,
			final double[] manualRange )
	{
		this.paramName = paramName;
		this.units = units;
		this.type = type;
		this.min = min;
		this.nSteps = nSteps;
		this.max = max;
		this.manualRange = manualRange;
	}
	
	public double[] getRange()
	{
		switch ( type )
		{
		case FIXED:
			return new double[] { min };
		case LIN_RANGE:
			return linspace( min, max, nSteps );
		case LOG_RANGE:
			final double[] e = linspace( Math.log( 1 ), Math.log( 1 - min + max ), nSteps );
			final double[] out = new double[ e.length ];
			for ( int i = 0; i < out.length; i++ )
				out[ i ] = Math.exp( e[ i ] ) + min - 1;
			return out;
		case MANUAL:
			return manualRange;
		default:
			throw new IllegalArgumentException( "Unknown range type: " + type );
		}
	}

	@Override
	public String toString()
	{
		switch ( type )
		{
		case FIXED:
			return String.format( "%s (%s):\n"
					+ " - type: %s\n"
					+ " - value: %f",
					paramName,
					units,
					type,
					min );
		case LIN_RANGE:
		case LOG_RANGE:
			return String.format( "%s (%s):\n"
					+ " - type: %s\n"
					+ " - min: %f\n"
					+ " - max: %f\n"
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
	
	public static Builder create()
	{
		return new Builder();
	}
	
	public static class Builder
	{
		private String paramName = "no name set";

		private String units = "";

		private RangeType rangeType = RangeType.LIN_RANGE;

		private double min = 1.;

		private double max = 10.;

		private int nSteps = 10;
		
		private double[] manualRange = new double[] { 1., 2., 3., 4., 5., 6., 7., 8., 9., 10. };
		
		public Builder paramName( final String paramName )
		{
			this.paramName = paramName;
			return this;
		}

		public Builder units( final String units )
		{
			this.units = units;
			return this;
		}

		public Builder min( final double min )
		{
			this.min = min;
			return this;
		}

		public Builder max( final double max )
		{
			this.max = max;
			return this;
		}

		public Builder rangeType( final RangeType rangeType )
		{
			this.rangeType = rangeType;
			return this;
		}

		public Builder manualRange( final double... vals )
		{
			this.manualRange = vals;
			return this;
		}

		public Builder nSteps( final int nSteps )
		{
			this.nSteps = nSteps;
			return this;
		}

		public DoubleParamSweepModel get()
		{
			return new DoubleParamSweepModel(
					paramName,
					units,
					rangeType,
					Math.min( max, min ),
					Math.max( max, min ),
					Math.max( 2, nSteps ),
					manualRange );
		}
	}

	private static final double[] linspace( final double min, final double max, final int nSteps )
	{
		final double[] range = new double[ nSteps ];
		for ( int i = 0; i < range.length; i++ )
			range[ i ] = min + ( max - min ) * i / ( nSteps - 1 );

		return range;
	}

	public static final String str( final double[] range )
	{
		if ( range.length == 0 )
			return "[]";

		final StringBuilder str = new StringBuilder();
		str.append( "[ " );
		str.append( String.format( "%.2f", range[ 0 ] ) );
		for ( int i = 1; i < range.length; i++ )
			str.append( String.format( ", %.2f", range[ i ] ) );

		str.append( " ]" );
		return str.toString();
	}
}
