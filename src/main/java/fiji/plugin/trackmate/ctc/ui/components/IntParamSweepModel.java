package fiji.plugin.trackmate.ctc.ui.components;

import java.util.LinkedHashSet;

public class IntParamSweepModel extends NumberParamSweepModel
{

	public IntParamSweepModel(
			final String paramName,
			final String units,
			final RangeType type,
			final int min,
			final int max,
			final int nSteps,
			final Integer[] manualRange )
	{
		super( paramName, units, type, min, max, nSteps, manualRange );
	}

	@Override
	public Number[] getRange()
	{
		switch ( type )
		{
		case FIXED:
			return new Number[] { min };
		case LIN_RANGE:
			return unique( linspace( min.intValue(), max.intValue(), nSteps ) );
		case LOG_RANGE:
			final Double[] e = DoubleParamSweepModel.linspace(
					Math.log( 1 ),
					Math.log( 1. - min.intValue() + max.intValue() ), nSteps );
			final Integer[] out = new Integer[ e.length ];
			for ( int i = 0; i < out.length; i++ )
				out[ i ] = ( int ) Math.round( Math.exp( e[ i ] ) + min.doubleValue() - 1. );
			return unique( out );
		case MANUAL:
			return manualRange;
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

		private int min = 1;

		private int max = 10;

		private int nSteps = 10;

		private Integer[] manualRange = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

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

		public Builder min( final int min )
		{
			this.min = min;
			return this;
		}

		public Builder max( final int max )
		{
			this.max = max;
			return this;
		}

		public Builder rangeType( final RangeType rangeType )
		{
			this.rangeType = rangeType;
			return this;
		}

		public Builder manualRange( final Integer... vals )
		{
			this.manualRange = vals;
			return this;
		}

		public Builder nSteps( final int nSteps )
		{
			this.nSteps = nSteps;
			return this;
		}

		public IntParamSweepModel get()
		{
			return new IntParamSweepModel(
					paramName,
					units,
					rangeType,
					Math.min( max, min ),
					Math.max( max, min ),
					Math.max( 2, nSteps ),
					manualRange );
		}
	}

	private static final Integer[] linspace( final int min, final int max, final int nSteps )
	{
		final Integer[] range = new Integer[ nSteps ];
		for ( int i = 0; i < nSteps; i++ )
			range[ i ] = min + ( max - min ) * i / ( nSteps - 1 );

		return range;
	}

	private static final Integer[] unique( final Integer[] in )
	{
		final LinkedHashSet< Integer > set = new LinkedHashSet<>();
		for ( int i = 0; i < in.length; i++ )
			set.add( in[ i ] );

		final Integer[] out = new Integer[ set.size() ];
		int index = 0;
		for ( final Integer i : set )
			out[ index++ ] = i;
		return out;
	}

	public static final String str( final int[] range )
	{
		if ( range.length == 0 )
			return "[]";

		final StringBuilder str = new StringBuilder();
		str.append( "[ " );
		str.append( String.format( "%d", range[ 0 ] ) );
		for ( int i = 1; i < range.length; i++ )
			str.append( String.format( ", %d", range[ i ] ) );

		str.append( " ]" );
		return str.toString();
	}
}
