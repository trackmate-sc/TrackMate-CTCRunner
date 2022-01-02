package fiji.plugin.trackmate.ctc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CTCResults
{

	private final List< CTCMetrics > ctcMetrics;

	private final List< String > detectors;

	private final List< String > trackers;

	private final List< Map< String, String > > detectorParams;

	private final List< Map< String, String > > trackerParams;

	public CTCResults(
			final List< CTCMetrics > ctcMetrics,
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

	public int size()
	{
		return ctcMetrics.size();
	}

	@Override
	public String toString()
	{
		final int nspace = 2;

		int id = 0;

		// CTC cols.
		final CTCMetricsDescription[] descs = CTCMetricsDescription.values();
		final int[] colWidths =
				new int[ descs.length
				+ 2
				+ detectorParams.get( 0 ).size()
				+ trackerParams.get( 0 ).size() ];
		for ( int i = 0; i < descs.length; i++ )
			colWidths[ id++ ] = Math.max( 5, descs[ i ].ctcName().length() );

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

		final StringBuilder str = new StringBuilder();
		id = 0;

		for ( final CTCMetricsDescription desc : descs )
			str.append( String.format( "%" + colWidths[ id++ ] + "s", desc.ctcName() ) );

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
			final double[] cm = ctcMetrics.get( i ).toArray();
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

	public static final Builder create()
	{
		return new Builder();
	}

	public static class Builder
	{

		private String[] header;

		private final List< CTCMetrics > ctcMetrics = new ArrayList<>();

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
			final CTCMetrics metrics = CTCMetrics.fromCSVLine( line );
			ctcMetrics.add( metrics );

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

		public CTCResults get()
		{
			return new CTCResults( ctcMetrics,
					detectors, trackers,
					detectorParams, trackerParams );
		}
	}

	public static final boolean isCTCHeader( final String[] header )
	{
		// Order is important.
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
