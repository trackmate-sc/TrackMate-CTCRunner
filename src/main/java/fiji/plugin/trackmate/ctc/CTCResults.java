package fiji.plugin.trackmate.ctc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CTCResults
{

	private final List< EnumMap< CTCMetricsDescription, Double > > ctcMetrics;

	private final List< String > detectors;

	private final List< String > trackers;

	private final List< Map< String, String > > detectorParams;

	private final List< Map< String, String > > trackerParams;

	public CTCResults(
			final List< EnumMap< CTCMetricsDescription, Double > > ctcMetrics,
			final List< String > detectors,
			final List< String > trackers,
			final List< Map< String, String > > detectorParams,
			final List< Map< String, String > > trackerParams )
	{
		this.ctcMetrics = ctcMetrics;
		this.detectors = detectors;
		this.trackers = trackers;
		this.detectorParams = detectorParams;
		this.trackerParams = trackerParams;
	}

	public static final Builder create()
	{
		return new Builder();
	}

	public static class Builder
	{

		private String[] header;

		private final List< EnumMap< CTCMetricsDescription, Double > > ctcMetrics = new ArrayList<>();

		private final List< String > detectors = new ArrayList<>();

		private final List< String > trackers = new ArrayList<>();

		private final List< Map< String, String > > detectorParams = new ArrayList<>();

		private final List< Map< String, String > > trackerParams = new ArrayList<>();

		private int detectorCol = -1;

		private int trackerCol = -1;

		public Builder addHeader( final String[] header )
		{
			if ( !isCTCHeader( header ) )
				throw new IllegalArgumentException( "CSV header is not from a CTC results file." );

			this.detectorCol = Arrays.asList( header ).indexOf( "DETECTOR" );
			this.trackerCol = Arrays.asList( header ).indexOf( "TRACKER" );
			this.header = header;
			return this;
		}

		public Builder addFromCSV( final String[] line )
		{
			if ( header == null )
				throw new IllegalArgumentException( "CSV header is not set yet." );

			// Parse the 10 CTC metrics first.
			final EnumMap< CTCMetricsDescription, Double > ctcMap = new EnumMap<>( CTCMetricsDescription.class );
			final CTCMetricsDescription[] vals = CTCMetricsDescription.values();
			for ( int i = 0; i < vals.length; i++ )
				ctcMap.put( vals[ i ], Double.valueOf( line[ i ] ) );
			ctcMetrics.add( ctcMap );

			// Detector and Tracker.
			detectors.add( line[ detectorCol ] );
			trackers.add( line[ trackerCol ] );

			// Parameters.
			for ( int col = detectorCol + 1; col < trackerCol; col++ )
			{
				final Map< String, String > dp = new HashMap<>();
				dp.put( header[ col ], line[ col ] );
				detectorParams.add( dp );
			}
			for ( int col = trackerCol + 1; col < line.length; col++ )
			{
				final Map< String, String > tp = new HashMap<>();
				tp.put( header[ col ], line[ col ] );
				trackerParams.add( tp );
			}

			return this;
		}

		public CTCResults get()
		{
			return new CTCResults( ctcMetrics,
					detectors, trackers,
					detectorParams, trackerParams );
		}
	}

	public static final boolean isCTCHeader( final String[] header )
	{
		final String[] ctcHeader = CTCMetricsDescription.toHeader();
		for ( int i = 0; i < ctcHeader.length; i++ )
		{
			if ( !ctcHeader[ i ].equals( header[ i ] ) )
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
}
