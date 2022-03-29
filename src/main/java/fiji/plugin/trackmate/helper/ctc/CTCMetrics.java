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
package fiji.plugin.trackmate.helper.ctc;

import static fiji.plugin.trackmate.helper.ctc.CTCMetricsDescription.BC;
import static fiji.plugin.trackmate.helper.ctc.CTCMetricsDescription.CCA;
import static fiji.plugin.trackmate.helper.ctc.CTCMetricsDescription.CT;
import static fiji.plugin.trackmate.helper.ctc.CTCMetricsDescription.DET;
import static fiji.plugin.trackmate.helper.ctc.CTCMetricsDescription.DETECTION_TIME;
import static fiji.plugin.trackmate.helper.ctc.CTCMetricsDescription.SEG;
import static fiji.plugin.trackmate.helper.ctc.CTCMetricsDescription.TF;
import static fiji.plugin.trackmate.helper.ctc.CTCMetricsDescription.TIM;
import static fiji.plugin.trackmate.helper.ctc.CTCMetricsDescription.TRA;
import static fiji.plugin.trackmate.helper.ctc.CTCMetricsDescription.TRACKING_TIME;

import java.util.EnumMap;

public class CTCMetrics
{

	private final EnumMap< CTCMetricsDescription, Double > values = new EnumMap<>( CTCMetricsDescription.class );

	private CTCMetrics(
			final double seg,
			final double tra,
			final double det,
			final double ct,
			final double tf,
			final double bci,
			final double cca,
			final double tim,
			final double detectionTime,
			final double trackingTime )
	{
		values.put( SEG, Double.valueOf( seg ) );
		values.put( TRA, Double.valueOf( tra ) );
		values.put( DET, Double.valueOf( det ) );
		values.put( CT, Double.valueOf( ct ) );
		values.put( TF, Double.valueOf( tf ) );
		values.put( BC, Double.valueOf( bci ) );
		values.put( CCA, Double.valueOf( cca ) );
		values.put( TIM, Double.valueOf( tim ) );
		values.put( DETECTION_TIME, Double.valueOf( detectionTime ) );
		values.put( TRACKING_TIME, Double.valueOf( trackingTime ) );
	}

	public double get( final CTCMetricsDescription desc )
	{
		return values.get( desc );
	}

	public static final CTCMetrics fromCSVLine( final String[] line )
	{
		final CTCMetrics out = new CTCMetrics( Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
				Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN );

		final CTCMetricsDescription[] vals = CTCMetricsDescription.values();
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
		final CTCMetricsDescription[] desc = CTCMetricsDescription.values();
		for ( int i = 0; i < 7; i++ )
			str.append( String.format( " - %-3s: %.3f\n", desc[ i ].ctcName(), arr[ i ] ) );
		str.append( String.format( " - %-3s: %.1f s\n", desc[ 7 ].ctcName(), arr[ 7 ] ) );
		str.append( String.format( " - %-14s: %.1f s\n", desc[ 8 ].ctcName(), arr[ 8 ] ) );
		str.append( String.format( " - %-14s: %.1f s", desc[ 9 ].ctcName(), arr[ 9 ] ) );
		return str.toString();
	}

	public double[] toArray()
	{
		return new double[] { values.get( SEG ),
				values.get( TRA ),
				values.get( DET ),
				values.get( CT ),
				values.get( TF ),
				values.get( CCA ),
				values.get( BC ),
				values.get( TIM ),
				values.get( DETECTION_TIME ),
				values.get( TRACKING_TIME ) };
	}

	public Builder copyEdit()
	{
		final Builder builder = create();
		builder.seg = values.get( SEG );
		builder.tra = values.get( TRA );
		builder.det = values.get( DET );
		builder.ct = values.get( CT );
		builder.tf = values.get( TF );
		builder.cca = values.get( CCA );
		builder.bci = values.get( BC );
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
		final CTCMetricsDescription[] vals = CTCMetricsDescription.values();
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

		private double seg = Double.NaN;

		private double tra = Double.NaN;

		private double det = Double.NaN;

		private double ct = Double.NaN;

		private double tf = Double.NaN;

		private double bci = Double.NaN;

		private double cca = Double.NaN;

		private double tim = Double.NaN;

		private double detectionTime = Double.NaN;

		private double trackingTime = Double.NaN;

		public Builder seg( final double seg )
		{
			this.seg = seg;
			return this;
		}

		public Builder tra( final double tra )
		{
			this.tra = tra;
			return this;
		}

		public Builder det( final double det )
		{
			this.det = det;
			return this;
		}

		public Builder ct( final double ct )
		{
			this.ct = ct;
			return this;
		}

		public Builder tf( final double tf )
		{
			this.tf = tf;
			return this;
		}

		public Builder bci( final double bc )
		{
			this.bci = bc;
			return this;
		}

		public Builder cca( final double cca )
		{
			this.cca = cca;
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

		public CTCMetrics get()
		{
			return new CTCMetrics( seg, tra, det, ct, tf, bci, cca, tim, detectionTime, trackingTime );
		}
	}

}
