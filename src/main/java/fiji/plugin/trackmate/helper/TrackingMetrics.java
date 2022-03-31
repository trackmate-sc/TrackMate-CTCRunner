package fiji.plugin.trackmate.helper;

import java.util.Arrays;

public class TrackingMetrics
{

	private final TrackingMetricsType type;

	private final double[] arr;

	public TrackingMetrics( final TrackingMetricsType type )
	{
		this.type = type;
		this.arr = new double[ type.metrics().size() ];
		Arrays.fill( arr, Double.NaN );
	}

	public boolean isNaN()
	{
		for ( final double d : arr )
		{
			if ( !Double.isNaN( d ) )
				return false;
		}
		return true;
	}

	public double get( final String key )
	{
		return arr[ type.id( key ) ];
	}

	public void set( final String key, final double val )
	{
		arr[ type.id( key ) ] = val;
	}

	public void set( final int id, final double val )
	{
		arr[ id ] = val;
	}

	public double[] toArray()
	{
		return arr;
	}

	public String[] concatWithCSVLine( final String[] content )
	{
		final double[] arr = toArray();
		final String[] out = new String[ content.length + arr.length ];
		for ( int i = 0; i < arr.length; i++ )
			out[ i ] = Double.toString( arr[ i ] );

		for ( int i = 0; i < content.length; i++ )
			out[ arr.length + i ] = content[ i ];

		return out;
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		final double[] arr = toArray();
		// We exploit the fact that the common keys are at the end.
		for ( int i = 0; i < type.metrics().size() - TrackingMetricsType.COMMON_KEYS.size(); i++ )
			str.append( String.format( " - %-3s: %.3f\n", type.metrics().get( i ), arr[ i ] ) );

		str.append( String.format( " - %-3s: %.1f s\n",
				TrackingMetricsType.COMMON_KEYS.get( 0 ), arr[ type.metrics().size() - 3 ] ) );
		str.append( String.format( " - %-14s: %.1f s\n",
				TrackingMetricsType.COMMON_KEYS.get( 1 ), arr[ type.metrics().size() - 2 ] ) );
		str.append( String.format( " - %-14s: %.1f s",
				TrackingMetricsType.COMMON_KEYS.get( 2 ), arr[ type.metrics().size() - 1 ] ) );
		return str.toString();
	}
}
