package fiji.plugin.trackmate.ctc.ui.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DoubleParamSweepModel extends NumberParamSweepModel
{

	@Override
	public List< Number > getRange()
	{
		switch ( rangeType )
		{
		case FIXED:
			return Collections.singletonList( min.doubleValue() );
		case LIN_RANGE:
			return linspace( min.doubleValue(), max.doubleValue(), nSteps );
		case LOG_RANGE:
			final List< Number > e = linspace( Math.log( 1 ), Math.log( 1. - min.doubleValue() + max.doubleValue() ), nSteps );
			final List< Number > out = new ArrayList<>( e.size() );
			for ( int i = 0; i < e.size(); i++ )
				out.add( Math.exp( e.get( i ).doubleValue() ) + min.doubleValue() - 1. );
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
		final List< Number > list = Arrays.asList( vals );
		if ( !this.manualRange.equals( list ) )
		{
			this.manualRange = list;
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

	static final List< Number > linspace( final double min, final double max, final int nSteps )
	{
		final List< Number > range = new ArrayList<>( nSteps );
		for ( int i = 0; i < nSteps; i++ )
			range.add( min + ( max - min ) * i / ( nSteps - 1 ) );

		return range;
	}

	public static final String str( final List< Number > range )
	{
		if ( range.isEmpty() )
			return "[]";

		final StringBuilder str = new StringBuilder();
		str.append( "[ " );
		str.append( String.format( "%s", range.get( 0 ) ) );
		for ( int i = 1; i < range.size(); i++ )
			str.append( String.format( ", %s", range.get( i ) ) );

		str.append( " ]" );
		return str.toString();
	}
}
