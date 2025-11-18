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

import java.util.List;
import java.util.Map;
import java.util.Set;

import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.helper.TrackingMetricsType.MetricValue;
import fiji.plugin.trackmate.util.TMUtils;

public class TrackingMetricsTable
{

	private final TrackingMetricsType type;

	private final List< TrackingMetrics > metrics;

	private final List< String > detectors;

	private final List< String > trackers;

	private final List< Map< String, String > > detectorParams;

	private final List< Map< String, String > > trackerParams;

	private final List< List< FeatureFilter > > spotFilters;

	private final List< List< FeatureFilter > > trackFilters;

	public TrackingMetricsTable(
			final TrackingMetricsType type,
			final List< TrackingMetrics > metrics,
			final List< String > detectors,
			final List< String > trackers,
			final List< Map< String, String > > detectorParams,
			final List< Map< String, String > > trackerParams,
			final List< List< FeatureFilter > > spotFilters,
			final List< List< FeatureFilter > > trackFilters )
	{
		this.type = type;
		this.metrics = metrics;
		this.detectors = detectors;
		this.trackers = trackers;
		this.detectorParams = detectorParams;
		this.trackerParams = trackerParams;
		this.spotFilters = spotFilters;
		this.trackFilters = trackFilters;
	}

	public int size()
	{
		return metrics.size();
	}

	public String getDetector( final int line )
	{
		return detectors.get( line );
	}

	public String getTracker( final int line )
	{
		return trackers.get( line );
	}

	public Map< String, String > getDetectorParams( final int line )
	{
		return detectorParams.get( line );
	}

	public Map< String, String > getTrackerParams( final int line )
	{
		return trackerParams.get( line );
	}

	public TrackingMetrics getMetrics( final int i )
	{
		return metrics.get( i );
	}

	public List< FeatureFilter > getSpotFilters( final int i )
	{
		return spotFilters.get( i );
	}

	public List< FeatureFilter > getTrackFilters( final int i )
	{
		return trackFilters.get( i );
	}

	public int bestFor( final String detector, final String tracker, final MetricValue key )
	{
		int bestLine = -1;
		TrackingMetrics best = null;
		for ( int i = 0; i < metrics.size(); i++ )
		{
			if ( ( null != detector && !detectors.get( i ).equals( detector ) )
					|| ( null != tracker && !trackers.get( i ).equals( tracker ) ) )
				continue;

			final TrackingMetrics c = metrics.get( i );
			if ( c.isBetterThan( best, key ) )
			{
				best = c;
				bestLine = i;
			}
		}
		return bestLine;
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public String printLine( final int i )
	{
		final StringBuilder str = new StringBuilder();
		str.append( "For detector: " + detectors.get( i ) + " with settings:" );
		str.append( "\n" + TMUtils.echoMap( ( Map ) detectorParams.get( i ), 2 ) );
		if ( !spotFilters.get( i ).isEmpty() )
		{
			str.append( "With spot filters:\n" );
			str.append( echoFilters( spotFilters.get( i ) ) );
		}
		str.append( "And tracker: " + trackers.get( i ) + " with settings:" );
		str.append( "\n" + TMUtils.echoMap( ( Map ) trackerParams.get( i ), 2 ) );
		if ( !trackFilters.get( i ).isEmpty() )
		{
			str.append( "With track filters:\n" );
			str.append( echoFilters( trackFilters.get( i ) ) );
		}
		str.append( type.name() + " metrics:\n" );
		str.append( metrics.get( i ).toString() );
		return str.toString();
	}

	@Override
	public String toString()
	{
		final int nspace = 2;
		int id = 0;

		// Cols.
		final List< MetricValue > descs = type.metrics();
		final int[] colWidths =
				new int[ descs.size()
						+ 2
						+ detectorParams.get( 0 ).size()
						+ trackerParams.get( 0 ).size()
						+ spotFilters.get( 0 ).size()
						+ trackFilters.get( 0 ).size() ];
		for ( int i = 0; i < descs.size(); i++ )
			colWidths[ id++ ] = Math.max( 5, descs.get( i ).key.length() );

		// Detector col.
		colWidths[ id++ ] = detectors.stream()
				.mapToInt( d -> d.length() )
				.max()
				.getAsInt();

		// Detector param cols.
		final Set< String > detectorKeys = detectorParams.get( 0 ).keySet();
		for ( final String dk : detectorKeys )
			colWidths[ id++ ] = dk.length();

		// Tracker col.
		colWidths[ id++ ] = trackers.stream()
				.mapToInt( d -> d.length() )
				.max()
				.getAsInt();

		// Tracker param cols.
		final Set< String > trackerKeys = trackerParams.get( 0 ).keySet();
		for ( final String dk : trackerKeys )
			colWidths[ id++ ] = dk.length();

		// Spot filter cols
		final List< FeatureFilter > spotFilterFirst = spotFilters.get( 0 );
		for ( final FeatureFilter ff : spotFilterFirst )
			colWidths[ id++ ] = ( "SPOT_FILTER_ON_" + ff.feature ).length();

		// Track filter cols
		final List< FeatureFilter > trackFilterFirst = trackFilters.get( 0 );
		for ( final FeatureFilter ff : trackFilterFirst )
			colWidths[ id++ ] = ( "TRACK_FILTER_ON_" + ff.feature ).length();

		// Add space.
		for ( int i = 0; i < colWidths.length; i++ )
			colWidths[ i ] += nspace;

		/*
		 * Header.
		 */

		final StringBuilder str = new StringBuilder( type.name() + ":\n" );
		id = 0;

		for ( final MetricValue desc : descs )
			str.append( String.format( "%" + colWidths[ id++ ] + "s", desc.key ) );

		str.append( String.format( "%" + colWidths[ id++ ] + "s", "DETECTOR" ) );

		for ( final String dk : detectorKeys )
			str.append( String.format( "%" + colWidths[ id++ ] + "s", dk ) );

		str.append( String.format( "%" + colWidths[ id++ ] + "s", "TRACKER" ) );

		for ( final String tk : trackerKeys )
			str.append( String.format( "%" + colWidths[ id++ ] + "s", tk ) );

		for ( final FeatureFilter ff : spotFilterFirst )
			str.append( String.format( "%" + colWidths[ id++ ] + "s", "SPOT_FILTER_ON_" + ff.feature ) );

		for ( final FeatureFilter ff : trackFilterFirst )
			str.append( String.format( "%" + colWidths[ id++ ] + "s", "TRACK_FILTER_" + ff.feature ) );

		str.append( '\n' );

		/*
		 * Content.
		 */

		for ( int i = 0; i < size(); i++ )
		{
			id = 0;
			final double[] cm = metrics.get( i ).toArray();
			for ( int j = 0; j < cm.length; j++ )
				str.append( String.format( "%" + colWidths[ id++ ] + ".3f", cm[ j ] ) );

			str.append( String.format( "%" + colWidths[ id++ ] + "s", detectors.get( i ) ) );

			final Map< String, String > dp = detectorParams.get( i );
			for ( final String dk : detectorKeys )
				str.append( String.format( "%" + colWidths[ id++ ] + "s", dp.get( dk ) ) );

			str.append( String.format( "%" + colWidths[ id++ ] + "s", trackers.get( i ) ) );

			final Map< String, String > tp = trackerParams.get( i );
			for ( final String tk : trackerKeys )
				str.append( String.format( "%" + colWidths[ id++ ] + "s", tp.get( tk ) ) );

			final List< FeatureFilter > sfs = spotFilters.get( i );
			for ( final FeatureFilter ff : sfs )
				str.append( String.format( "%" + colWidths[ id++ ] + "s", ff.toString().replace( ff.feature, "" ) ) );

			final List< FeatureFilter > tfs = trackFilters.get( i );
			for ( final FeatureFilter ff : tfs )
				str.append( String.format( "%" + colWidths[ id++ ] + "s", ff.toString().replace( ff.feature, "" ) ) );

			str.append( '\n' );
		}

		return str.toString();
	}

	public static final String echoFilters( final Iterable< FeatureFilter > filters )
	{
		final StringBuilder str = new StringBuilder();
		for ( final FeatureFilter ff : filters )
			str.append( " - " + ff.toString() + "\n" );

		return str.toString();
	}
}
