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
