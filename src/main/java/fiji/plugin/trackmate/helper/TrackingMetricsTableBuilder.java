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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackingMetricsTableBuilder
{

	private final TrackingMetricsType type;

	private String[] header;

	private final List< TrackingMetrics > metrics = new ArrayList<>();

	private final List< String > detectors = new ArrayList<>();

	private final List< String > trackers = new ArrayList<>();

	private final List< Map< String, String > > detectorParams = new ArrayList<>();

	private final List< Map< String, String > > trackerParams = new ArrayList<>();

	private int detectorCol = -1;

	private int trackerCol = -1;

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

		final Map< String, String > tp = new HashMap<>();
		for ( int col = trackerCol + 1; col < line.length; col++ )
			tp.put( header[ col ], line[ col ] );

		trackerParams.add( tp );

		return this;
	}

	public TrackingMetricsTable get()
	{
		return new TrackingMetricsTable( type,
				metrics,
				detectors, trackers,
				detectorParams, trackerParams );
	}
}
