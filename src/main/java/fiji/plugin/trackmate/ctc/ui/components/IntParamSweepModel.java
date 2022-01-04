package fiji.plugin.trackmate.ctc.ui.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import fiji.plugin.trackmate.Dimension;

public class IntParamSweepModel extends NumberParamSweepModel
{

	public IntParamSweepModel()
	{
		manualRange.addAll( Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ) );
	}

	@Override
	public List< Number > getRange()
	{
		switch ( rangeType )
		{
		case FIXED:
			return Collections.singletonList( min.intValue() );
		case LIN_RANGE:
			return unique( linspace( min.intValue(), max.intValue(), nSteps ) );
		case LOG_RANGE:
			final List< Number > e = DoubleParamSweepModel
					.linspace(
					Math.log( 1 ),
					Math.log( 1. - min.intValue() + max.intValue() ), nSteps );
			final List< Integer > out = new ArrayList< >( e.size() );
			for ( int i = 0; i < e.size(); i++ )
				out.add( ( int ) Math.round( Math.exp( e.get( i ).doubleValue() )
						+ min.doubleValue() - 1. ) );
			return unique( out );
		case MANUAL:
			return manualRange;
		default:
			throw new IllegalArgumentException( "Unknown range type: " + rangeType );
		}
	}

	@Override
	public IntParamSweepModel paramName( final String paramName )
	{
		return ( IntParamSweepModel ) super.paramName( paramName );
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
	public IntParamSweepModel dimension( final Dimension dimension )
	{
		return ( IntParamSweepModel ) super.dimension( dimension );
	}

	@Override
	public IntParamSweepModel rangeType( final RangeType rangeType )
	{
		return ( IntParamSweepModel ) super.rangeType( rangeType );
	}

	public IntParamSweepModel manualRange( final Integer... vals )
	{
		final List< Number > list = Arrays.asList( vals );
		if ( !this.manualRange.equals( list ) )
		{
			this.manualRange.clear();
			this.manualRange.addAll( list );
			notifyListeners();
		}
		return this;
	}

	@Override
	public IntParamSweepModel nSteps( final int nSteps )
	{
		return ( IntParamSweepModel ) super.nSteps( nSteps );
	}

	private static final List< Integer > linspace( final int min, final int max, final int nSteps )
	{
		final List< Integer > range = new ArrayList<>( nSteps );
		for ( int i = 0; i < nSteps; i++ )
			range.add( min + ( max - min ) * i / ( nSteps - 1 ) );

		return range;
	}

	private static final List< Number > unique( final List< Integer > in )
	{
		final LinkedHashSet< Integer > set = new LinkedHashSet<>( in );
		return new ArrayList<>( set );
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
