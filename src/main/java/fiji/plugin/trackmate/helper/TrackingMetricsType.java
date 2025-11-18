/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

import gnu.trove.map.hash.TObjectIntHashMap;

public abstract class TrackingMetricsType
{

	public static enum MetricValueBound
	{
		ZERO_TO_ONE,
		UNBOUNDED;
	}

	public static enum MetricValueOptimum
	{
		HIGHER_IS_BETTER( ( v1, v2 ) -> {
			if ( Double.isNaN( v2 ) )
				return true;
			if ( Double.isNaN( v1 ) )
				return false;
			return v1 > v2;
		} ),
		LOWER_IS_BETTER( ( v1, v2 ) -> {
			if ( Double.isNaN( v2 ) )
				return true;
			if ( Double.isNaN( v1 ) )
				return false;
			return v1 < v2;
		} );

		private final BiPredicate< Double, Double > comparator;

		private MetricValueOptimum( final BiPredicate< Double, Double > comparator )
		{
			this.comparator = comparator;
		}

		/**
		 * Returns <code>true</code> if the first metric value is 'better than'
		 * the second one, in the sense of this optimum type.
		 *
		 * @param val1
		 *            the first metric value.
		 * @param val2
		 *            the second metric value.
		 * @return <code>true</code> if the first value is better than the
		 *         second one.
		 */
		public boolean isBetterThan( final double val1, final double val2 )
		{
			return comparator.test( val1, val2 );
		}
	}

	public static class MetricValue
	{
		public final String key;

		public final String description;

		public final MetricValueOptimum optimumType;

		public final MetricValueBound boundType;

		public MetricValue( final String key, final String description, final MetricValueOptimum optimumType, final MetricValueBound boundType )
		{
			this.key = key;
			this.description = description;
			this.optimumType = optimumType;
			this.boundType = boundType;
		}

		@Override
		public String toString()
		{
			return key;
		}
	}

	public static final MetricValue TIM = new MetricValue(
			"TIM",
			"Execution time",
			MetricValueOptimum.LOWER_IS_BETTER,
			MetricValueBound.UNBOUNDED );

	public static final MetricValue DETECTION_TIME = new MetricValue(
			"DETECTION_TIME",
			"Detection time",
			MetricValueOptimum.LOWER_IS_BETTER,
			MetricValueBound.UNBOUNDED );

	public static final MetricValue TRACKING_TIME = new MetricValue(
			"TRACKING_TIME",
			"Tracking time",
			MetricValueOptimum.LOWER_IS_BETTER,
			MetricValueBound.UNBOUNDED );

	private final List< MetricValue > metrics;

	private final TObjectIntHashMap< MetricValue > idMap;

	protected TrackingMetricsType( final List< MetricValue > metrics )
	{
		final List< MetricValue > ml = new ArrayList<>();
		ml.addAll( metrics );
		ml.add( TIM );
		ml.add( DETECTION_TIME );
		ml.add( TRACKING_TIME );
		this.metrics = Collections.unmodifiableList( ml );
		// id map.
		this.idMap = new TObjectIntHashMap<>( ml.size(), 0.5f, -1 );
		for ( int i = 0; i < ml.size(); i++ )
			idMap.put( ml.get( i ), i );
	}

	/**
	 * Returns the ordered list of metric keys this metric type provides.
	 *
	 * @return
	 */
	public List< MetricValue > metrics()
	{
		return metrics;
	}

	/**
	 * Returns the integer id of the specified key. This id is used to index the
	 * metric with the specified key e.g. in an array.
	 *
	 * @param key
	 *            the key of the metric.
	 * @return its id, or -1 if the specified key is unknown to this metric
	 *         type.
	 */
	public int id( final MetricValue key )
	{
		return idMap.get( key );
	}

	/**
	 * Returns the name of this metric type.
	 *
	 * @return the metric type name.
	 */
	public abstract String name();

	/**
	 * Returns the suffix to append to file names when saving results of this
	 * metric type.
	 *
	 * @return a short suffix.
	 */
	public abstract String csvSuffix();

	/**
	 * Returns the URL of the publication where this metric type is described.
	 *
	 * @return a URL as string.
	 */
	public abstract String url();

	/**
	 * Returns an information string about this metric type.
	 *
	 * @return a string.
	 */
	public abstract String info();

	/**
	 * Returns the key of the default metric in this type.
	 *
	 * @return the default key.
	 */
	public abstract MetricValue defaultMetric();

	/**
	 * Creates a new {@link MetricsRunner} that can perform performance metrics
	 * measurement for this type.
	 *
	 * @param gtPath
	 *            the path to the ground-truth folder or file compatible with
	 *            this metrics type.
	 * @param saveFolder
	 *            the path to the folder where results CSV files will be
	 *            written.
	 * @return a new {@link MetricsRunner}.
	 */
	public abstract MetricsRunner runner( String gtPath, String saveFolder );

	public TrackingMetricsTableBuilder tableBuilder()
	{
		return new TrackingMetricsTableBuilder( this );
	}

	/**
	 * Returns <code>true</code> if the header (specified as a String array) of
	 * a CSV file comes from a metric table file of this concrete type.
	 *
	 * @param header
	 *            the header read from the CSV file to inspect.
	 * @return <code>true</code> if the file is for the metrics of this type.
	 */
	public boolean isHeader( final String[] header )
	{
		// Order is important.
		for ( int i = 0; i < metrics.size(); i++ )
		{
			if ( !metrics.get( i ).key.equals( header[ i ] ) )
				return false;
		}

		final int detectorCol = Arrays.asList( header ).indexOf( "DETECTOR" );
		if ( detectorCol < 0 )
			return false;

		final int trackerCol = Arrays.asList( header ).indexOf( "TRACKER" );
		if ( trackerCol < 0 )
			return false;

		return true;
	}

	/**
	 * Prepend the specified header with this metrics header.
	 *
	 * @param header
	 *            the header to preprint.
	 * @return a new String array.
	 */
	public String[] concatWithHeader( final String[] header )
	{
		final String[] out = new String[ header.length + metrics.size() ];
		for ( int i = 0; i < metrics.size(); i++ )
			out[ i ] = metrics.get( i ).key;

		for ( int i = 0; i < header.length; i++ )
			out[ metrics.size() + i ] = header[ i ];

		return out;
	}

	protected TrackingMetrics fromCSVLine( final String[] line )
	{
		final TrackingMetrics out = new TrackingMetrics( this );
		// Order is important but is validated with header elsewhere.
		for ( int i = 0; i < metrics.size(); i++ )
			out.set( i, Double.valueOf( line[ i ] ) );

		return out;
	}
}
