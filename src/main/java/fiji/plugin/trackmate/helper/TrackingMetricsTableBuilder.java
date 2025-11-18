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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.features.FeatureFilter;

public class TrackingMetricsTableBuilder
{

	private final TrackingMetricsType type;

	private String[] header;

	private final List< TrackingMetrics > metrics = new ArrayList<>();

	private final List< String > detectors = new ArrayList<>();

	private final List< String > trackers = new ArrayList<>();

	private final List< Map< String, String > > detectorParams = new ArrayList<>();

	private final List< Map< String, String > > trackerParams = new ArrayList<>();

	private final List< List< FeatureFilter > > spotFilters = new ArrayList<>();

	private final List< List< FeatureFilter > > trackFilters = new ArrayList<>();

	private int detectorCol = -1;

	private int trackerCol = -1;

	private int[] spotFilterCols;

	private int[] trackFilterCols;

	public TrackingMetricsTableBuilder( final TrackingMetricsType type )
	{
		this.type = type;
	}

	public TrackingMetricsTableBuilder addHeader( final String[] header )
	{
		if ( !type.isHeader( header ) )
			throw new IllegalArgumentException( "CSV header is not from a " + type.name() + " results file." );

		this.detectorCol = Arrays.asList( header ).indexOf( "DETECTOR" );
		this.trackerCol = Arrays.asList( header ).indexOf( "TRACKER" );
		this.spotFilterCols = findIndicesWithPrefix( header, "SPOT_FILTER_ON" );
		this.trackFilterCols = findIndicesWithPrefix( header, "TRACK_FILTER_ON" );

		this.header = header;
		return this;
	}

	public TrackingMetricsTableBuilder addFromCSV( final String[] line )
	{
		if ( header == null )
			throw new IllegalArgumentException( "CSV header is not set yet." );

		// Parse the metrics first.
		final TrackingMetrics m = type.fromCSVLine( line );
		metrics.add( m );

		// Detector and Tracker.
		detectors.add( line[ detectorCol ] );
		trackers.add( line[ trackerCol ] );

		// Parameters.
		final Map< String, String > dp = new HashMap<>();
		for ( int col = detectorCol + 1; col < trackerCol; col++ )
			dp.put( header[ col ], line[ col ] );

		detectorParams.add( dp );

		// Last track param col
		final int lastTrackerParamCol;
		if ( spotFilterCols.length > 0 )
			lastTrackerParamCol = spotFilterCols[ 0 ];
		else if ( trackFilterCols.length > 0 )
			lastTrackerParamCol = trackFilterCols[ 0 ];
		else
			lastTrackerParamCol = line.length;

		final Map< String, String > tp = new HashMap<>();
		for ( int col = trackerCol + 1; col < lastTrackerParamCol; col++ )
			tp.put( header[ col ], line[ col ] );

		trackerParams.add( tp );

		// Filters
		final List< FeatureFilter > sfs = new ArrayList<>( spotFilterCols.length );
		for ( int i = 0; i < spotFilterCols.length; i++ )
		{
			final int col = spotFilterCols[ i ];
			final String h = header[ col ];
			final String featureKey = h.replace( "SPOT_FILTER_ON_", "" );
			final FeatureFilter sf = fromString( line[ col ], featureKey );
			sfs.add( sf );
		}
		spotFilters.add( sfs );

		final List< FeatureFilter > tfs = new ArrayList<>( trackFilterCols.length );
		for ( int i = 0; i < trackFilterCols.length; i++ )
		{
			final int col = trackFilterCols[ i ];
			final String h = header[ col ];
			final String featureKey = h.replace( "TRACK_FILTER_ON_", "" );
			final FeatureFilter tf = fromString( line[ col ], featureKey );
			tfs.add( tf );
		}
		trackFilters.add( tfs );

		return this;
	}

	public TrackingMetricsTable get()
	{
		return new TrackingMetricsTable( type,
				metrics,
				detectors, trackers,
				detectorParams, trackerParams,
				spotFilters, trackFilters );
	}

	private static int[] findIndicesWithPrefix( final String[] array, final String prefix )
	{
		// List to store indices
		final List< Integer > indicesList = new ArrayList<>();

		// Iterate through the array
		for ( int i = 0; i < array.length; i++ )
			if ( array[ i ].startsWith( prefix ) )
				indicesList.add( i );

		// Convert List to array
		final int[] indices = new int[ indicesList.size() ];
		for ( int i = 0; i < indicesList.size(); i++ )
			indices[ i ] = indicesList.get( i );

		return indices;
	}

	private FeatureFilter fromString( final String str, final String featureKey )
	{
		// We expect something like ">2.202354"
		final boolean isAbove = str.trim().startsWith( ">" );
		final String valStr = str.substring( 1 );
		final double value = Double.parseDouble( valStr );
		return new FeatureFilter( featureKey, value, isAbove );
	}

}
