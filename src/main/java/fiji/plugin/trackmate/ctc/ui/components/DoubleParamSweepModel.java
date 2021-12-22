package fiji.plugin.trackmate.ctc.ui.components;

import java.util.Arrays;

public class DoubleParamSweepModel extends NumberParamSweepModel
{

	@Override
	public Number[] getRange()
	{
		switch ( rangeType )
		{
		case FIXED:
			return new Number[] { min };
		case LIN_RANGE:
			return linspace( min.doubleValue(), max.doubleValue(), nSteps );
		case LOG_RANGE:
			final Double[] e = linspace( Math.log( 1 ), Math.log( 1. - min.doubleValue() + max.doubleValue() ), nSteps );
			final Double[] out = new Double[ e.length ];
			for ( int i = 0; i < out.length; i++ )
				out[ i ] = Math.exp( e[ i ] ) + min.doubleValue() - 1.;
			return out;
		case MANUAL:
			return manualRange;
		default:
			throw new IllegalArgumentException( "Unknown range type: " + rangeType );
		}
	}

	public DoubleParamSweepModel min( final double min )
	{
		if ( this.min.doubleValue() != min )
		{
			this.min = min;
			notifyListeners();
		}
		return this;
	}

	public DoubleParamSweepModel max( final double max )
	{
		if ( this.max.doubleValue() != max )
		{
			this.max = max;
			notifyListeners();
		}
		return this;
	}

	public DoubleParamSweepModel manualRange( final Double... vals )
	{
		if ( !Arrays.equals( this.manualRange, vals ) )
		{
			this.manualRange = vals;
			notifyListeners();
		}
		return this;
	}

	@Override
	public DoubleParamSweepModel paramName( final String paramName )
	{
		return ( DoubleParamSweepModel ) super.paramName( paramName );
	}

	@Override
	public DoubleParamSweepModel units( final String units )
	{
		return ( DoubleParamSweepModel ) super.units( units );
	}

	@Override
	public DoubleParamSweepModel nSteps( final int nSteps )
	{
		return ( DoubleParamSweepModel ) super.nSteps( nSteps );
	}

	@Override
	public DoubleParamSweepModel rangeType( final RangeType rangeType )
	{
		return ( DoubleParamSweepModel ) super.rangeType( rangeType );
	}

	static final Double[] linspace( final double min, final double max, final int nSteps )
	{
		final Double[] range = new Double[ nSteps ];
		for ( int i = 0; i < range.length; i++ )
			range[ i ] = min + ( max - min ) * i / ( nSteps - 1 );

		return range;
	}

	public static final String str( final Number[] range )
	{
		if ( range.length == 0 )
			return "[]";

		final StringBuilder str = new StringBuilder();
		str.append( "[ " );
		str.append( String.format( "%s", range[ 0 ] ) );
		for ( int i = 1; i < range.length; i++ )
			str.append( String.format( ", %s", range[ i ] ) );

		str.append( " ]" );
		return str.toString();
	}
}
