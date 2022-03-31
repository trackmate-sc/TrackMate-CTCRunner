package fiji.plugin.trackmate.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gnu.trove.map.hash.TObjectIntHashMap;

public abstract class TrackingMetricsType
{

	public static final String TIM = "TIM";

	public static final String DETECTION_TIME = "DETECTION_TIME";

	public static final String TRACKING_TIME = "TRACKING_TIME";

	public static final List< String > COMMON_KEYS = Arrays.asList( new String[] {
			TIM, DETECTION_TIME, TRACKING_TIME } );

	private static final List< String > COMMON_DESCRIPTIONS = Arrays.asList( new String[] {
			"Execution time",
			"Detection time",
			"Tracking time" } );

	private final List< String > metrics;

	private final Map< String, String > descriptions;

	private final TObjectIntHashMap< String > idMap;

	protected TrackingMetricsType( final List< String > specificKeys, final List< String > specificDescriptions )
	{
		final List< String > ml = new ArrayList<>();
		ml.addAll( specificKeys );
		ml.addAll( COMMON_KEYS );
		this.metrics = Collections.unmodifiableList( ml );
		final Map< String, String > dm = new HashMap<>();
		for ( int i = 0; i < specificKeys.size(); i++ )
			dm.put( specificKeys.get( i ), specificDescriptions.get( i ) );
		for ( int i = 0; i < COMMON_KEYS.size(); i++ )
			dm.put( COMMON_KEYS.get( i ), COMMON_DESCRIPTIONS.get( i ) );
		this.descriptions = Collections.unmodifiableMap( dm );
		// id map.
		this.idMap = new TObjectIntHashMap<>( metrics.size(), 0.5f, -1 );
		for ( int i = 0; i < metrics.size(); i++ )
			idMap.put( metrics.get( i ), i );
	}

	/**
	 * Returns the ordered list of metric keys this metric type provides.
	 * 
	 * @return
	 */
	public List< String > metrics()
	{
		return metrics;
	}

	/**
	 * Returns the description of the metric with the specific key, or
	 * <code>null</code> if the key is unknown.
	 * 
	 * @param key
	 *            the metric key.
	 * @return the metric description.
	 */
	public String description( final String key)
	{
		return descriptions.get( key );
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
	public int id( final String key )
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
	public abstract String defaultMetric();

	/**
	 * Creates a new {@link MetricsRunner} that can perform performance metrics
	 * measurement for this type.
	 * 
	 * @param gtPath
	 *            the path to the ground-truth folder or file compatible with
	 *            this metrics type.
	 * @return a new {@link MetricsRunner}.
	 */
	public abstract MetricsRunner runner( String gtPath );

	public TrackingMetricsTableBuilder tableBuilder()
	{
		return new TrackingMetricsTableBuilder( this );
	}

	/**
	 * Returns <code>true</code> if the header (specified as a String array) of
	 * a CSV file comes from a metric table file of this concrete type.
	 * 
	 * @param readHeader
	 *            the header read from the CSV file to inspect.
	 * @return <code>true</code> if the file is for the metrics of this type.
	 */
	public boolean isHeader( final String[] header )
	{
		// Order is important.
		for ( int i = 0; i < metrics.size(); i++ )
		{
			if ( !metrics.get( i ).equals( header[ i ] ) )
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
			out[ i ] = metrics.get( i );

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
