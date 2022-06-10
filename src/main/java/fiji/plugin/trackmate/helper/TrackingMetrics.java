/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.helper;

import java.util.Arrays;

import fiji.plugin.trackmate.helper.TrackingMetricsType.MetricValue;

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

	public double get( final MetricValue key )
	{
		return arr[ type.id( key ) ];
	}

	public void set( final MetricValue key, final double val )
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
		for ( int i = 0; i < type.metrics().size(); i++ )
			str.append( String.format( " - %-14s: %.3f\n", type.metrics().get( i ), arr[ i ] ) );
		return str.toString();
	}

	/**
	 * Returns <code>true</code> if the metrics in this instance are better than
	 * the specified one, for the specified metrics value. The sense of 'better
	 * than' depends on the metric value. For instance a shorter execution time
	 * is better, but a higher score is better.
	 * 
	 * @param other
	 *            the metrics to compare.
	 * @param value
	 *            the metric value to base the comparison on.
	 * @return <code>true</code> if these metrics are better than the specified
	 *         one.
	 */
	public boolean isBetterThan( final TrackingMetrics other, final MetricValue value )
	{
		if ( other == null )
			return true;
		return value.optimumType.isBetterThan( get( value ), other.get( value ) );
	}
}
