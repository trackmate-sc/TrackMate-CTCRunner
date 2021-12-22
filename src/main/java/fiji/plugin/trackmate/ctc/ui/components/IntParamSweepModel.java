package fiji.plugin.trackmate.ctc.ui.components;

import java.util.Arrays;
import java.util.LinkedHashSet;

public class IntParamSweepModel extends NumberParamSweepModel
{

	@Override
	public Number[] getRange()
	{
		switch ( rangeType )
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
			throw new IllegalArgumentException( "Unknown range type: " + rangeType );
		}
	}

	public IntParamSweepModel min( final int min )
	{
		if ( this.min.intValue() != min )
		{
			this.min = min;
			notifyListeners();
		}
		return this;
	}

	public IntParamSweepModel max( final int max )
	{
		if ( this.max.intValue() != max )
		{
			this.max = max;
			notifyListeners();
		}
		return this;
	}

	@Override
	public IntParamSweepModel rangeType( final RangeType rangeType )
	{
		return ( IntParamSweepModel ) super.rangeType( rangeType );
	}

	public IntParamSweepModel manualRange( final Integer... vals )
	{
		if ( !Arrays.equals( this.manualRange, vals ) )
		{
			this.manualRange = vals;
			notifyListeners();
		}
		return this;
	}

	@Override
	public IntParamSweepModel nSteps( final int nSteps )
	{
		return ( IntParamSweepModel ) super.nSteps( nSteps );
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
