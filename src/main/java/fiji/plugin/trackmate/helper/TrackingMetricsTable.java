package fiji.plugin.trackmate.helper;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public TrackingMetricsTable(
			final TrackingMetricsType type,
			final List< TrackingMetrics > metrics,
			final List< String > detectors,
			final List< String > trackers,
			final List< Map< String, String > > detectorParams,
			final List< Map< String, String > > trackerParams )
	{
		this.type = type;
		this.metrics = metrics;
		this.detectors = detectors;
		this.trackers = trackers;
		this.detectorParams = detectorParams;
		this.trackerParams = trackerParams;
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
		str.append( "And tracker: " + trackers.get( i ) + " with settings:" );
		str.append( "\n" + TMUtils.echoMap( ( Map ) trackerParams.get( i ), 2 ) );
		str.append( "CTC metrics:\n" );
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
						+ trackerParams.get( 0 ).size() ];
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

			str.append( '\n' );
		}

		return str.toString();
	}
}
