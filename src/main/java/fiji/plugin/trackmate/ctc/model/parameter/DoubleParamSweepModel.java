package fiji.plugin.trackmate.ctc.model.parameter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fiji.plugin.trackmate.Dimension;

public class DoubleParamSweepModel extends NumberParamSweepModel
{

	public DoubleParamSweepModel()
	{
		manualRange.addAll( Arrays.asList( 1., 2., 3., 4., 5., 6., 7., 8., 9., 10. ) );
	}

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
			this.manualRange.clear();
			this.manualRange.addAll( list );
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
	public DoubleParamSweepModel dimension( final Dimension dimension )
	{
		return ( DoubleParamSweepModel ) super.dimension( dimension );
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
		str.append( String.format( "%s", round( range.get( 0 ), 6 ) ) );
		for ( int i = 1; i < range.size(); i++ )
			str.append( String.format( ", %s", round( range.get( i ), 6 ) ) );

		str.append( " ]" );
		return str.toString();
	}

	public static double round( final Number value, final int places )
	{
		BigDecimal bd = BigDecimal.valueOf( value.doubleValue() );
		bd = bd.setScale( places, RoundingMode.HALF_UP );
		return bd.doubleValue();
	}

}
