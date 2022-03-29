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
package fiji.plugin.trackmate.performance.spt;

import static fiji.plugin.trackmate.performance.spt.SPTMetricsDescription.ALPHA;
import static fiji.plugin.trackmate.performance.spt.SPTMetricsDescription.BETA;
import static fiji.plugin.trackmate.performance.spt.SPTMetricsDescription.DETECTION_TIME;
import static fiji.plugin.trackmate.performance.spt.SPTMetricsDescription.JSC;
import static fiji.plugin.trackmate.performance.spt.SPTMetricsDescription.JSCtheta;
import static fiji.plugin.trackmate.performance.spt.SPTMetricsDescription.RMSE;
import static fiji.plugin.trackmate.performance.spt.SPTMetricsDescription.TIM;
import static fiji.plugin.trackmate.performance.spt.SPTMetricsDescription.TRACKING_TIME;

import java.util.EnumMap;

public class SPTMetrics
{

	private final EnumMap< SPTMetricsDescription, Double > values = new EnumMap<>( SPTMetricsDescription.class );

	private SPTMetrics(
			final double alpha,
			final double beta,
			final double detectionsSimilarity,
			final double tracksSimilarity,
			final double rmse,
			final double tim,
			final double detectionTime,
			final double trackingTime )
	{
		values.put( ALPHA, Double.valueOf( alpha ) );
		values.put( BETA, Double.valueOf( beta ) );
		values.put( JSC, Double.valueOf( detectionsSimilarity ) );
		values.put( JSCtheta, Double.valueOf( tracksSimilarity ) );
		values.put( RMSE, Double.valueOf( rmse ) );
		values.put( TIM, Double.valueOf( tim ) );
		values.put( DETECTION_TIME, Double.valueOf( detectionTime ) );
		values.put( TRACKING_TIME, Double.valueOf( trackingTime ) );
	}

	public double get( final SPTMetricsDescription desc )
	{
		return values.get( desc );
	}

	public static final SPTMetrics fromCSVLine( final String[] line )
	{
		final SPTMetrics out = new SPTMetrics( Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
				Double.NaN, Double.NaN, Double.NaN );

		final SPTMetricsDescription[] vals = SPTMetricsDescription.values();
		// Order is important but is validated with header elsewhere.
		for ( int i = 0; i < vals.length; i++ )
			out.values.put( vals[ i ], Double.valueOf( line[ i ] ) );

		return out;
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		final double[] arr = toArray();
		final SPTMetricsDescription[] desc = SPTMetricsDescription.values();
		for ( int i = 0; i < 5; i++ )
			str.append( String.format( " - %-3s: %.3f\n", desc[ i ].ctcName(), arr[ i ] ) );
		str.append( String.format( " - %-3s: %.1f s\n", desc[ 7 ].ctcName(), arr[ 5 ] ) );
		str.append( String.format( " - %-14s: %.1f s\n", desc[ 8 ].ctcName(), arr[ 6 ] ) );
		str.append( String.format( " - %-14s: %.1f s", desc[ 9 ].ctcName(), arr[ 7 ] ) );
		return str.toString();
	}

	public double[] toArray()
	{
		return new double[] { values.get( ALPHA ),
				values.get( BETA ),
				values.get( JSC ),
				values.get( JSCtheta ),
				values.get( RMSE ),
				values.get( TIM ),
				values.get( DETECTION_TIME ),
				values.get( TRACKING_TIME ) };
	}

	public static SPTMetrics fromArray( final double[] score )
	{
		return SPTMetrics.create()
				.alpha( score[ 0 ] )
				.beta( score[ 1 ] )
				.jsc( score[ 2 ] )
				.jscTheta( score[ 3 ] )
				.rmse( score[ 4 ] )
				.get();
	}

	public Builder copyEdit()
	{
		final Builder builder = create();
		builder.alpha = values.get( ALPHA );
		builder.beta = values.get( BETA );
		builder.jsc = values.get( JSC );
		builder.jscTheta = values.get( JSCtheta );
		builder.rmse = values.get( RMSE );
		builder.tim = values.get( TIM );
		builder.detectionTime = values.get( DETECTION_TIME );
		builder.trackingTime = values.get( TRACKING_TIME );
		return builder;
	}

	/**
	 * Prepend the specified header with the CTC metrics header.
	 * 
	 * @param header
	 *            the header to preprint.
	 * @return a new String array.
	 */
	public static String[] concatWithCSVHeader( final String[] header )
	{
		final SPTMetricsDescription[] vals = SPTMetricsDescription.values();
		final String[] out = new String[ header.length + vals.length ];
		for ( int i = 0; i < vals.length; i++ )
			out[ i ] = vals[ i ].ctcName();

		for ( int i = 0; i < header.length; i++ )
			out[ vals.length + i ] = header[ i ];

		return out;
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

	public final static Builder create()
	{
		return new Builder();
	}

	public static final class Builder
	{

		private double alpha = Double.NaN;

		private double beta = Double.NaN;

		private double jsc = Double.NaN;

		private double jscTheta = Double.NaN;

		private double rmse = Double.NaN;

		private double tim = Double.NaN;

		private double detectionTime = Double.NaN;

		private double trackingTime = Double.NaN;

		public Builder alpha( final double alpha )
		{
			this.alpha = alpha;
			return this;
		}

		public Builder beta( final double beta )
		{
			this.beta = beta;
			return this;
		}

		public Builder jsc( final double jsc )
		{
			this.jsc = jsc;
			return this;
		}

		public Builder jscTheta( final double jscTheta )
		{
			this.jscTheta = jscTheta;
			return this;
		}

		public Builder rmse( final double rmse )
		{
			this.rmse = rmse;
			return this;
		}

		public Builder tim( final double tim )
		{
			this.tim = tim;
			return this;
		}

		public Builder detectionTime( final double detectionTime )
		{
			this.detectionTime = detectionTime;
			return this;
		}

		public Builder trackingTime( final double trackingTime )
		{
			this.trackingTime = trackingTime;
			return this;
		}

		public SPTMetrics get()
		{
			return new SPTMetrics( alpha, beta, jsc, jscTheta, rmse, tim, detectionTime, trackingTime );
		}
	}
}
