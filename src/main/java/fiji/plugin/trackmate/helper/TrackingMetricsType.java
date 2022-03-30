package fiji.plugin.trackmate.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TrackingMetricsType
{

	public static final List< String > COMMON_KEYS = Arrays.asList( new String[] {
			"TIM",
			"DETECTION_TIME",
			"TRACKING_TIME" } );

	private static final List< String > COMMON_DESCRIPTIONS = Arrays.asList( new String[] {
			"Execution time",
			"Detection time",
			"Tracking time" } );

	private final List< String > metrics;

	private final Map< String, String > descriptions;

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
	 * Returns the name of this metric type.
	 * 
	 * @return the metric type name.
	 */
	public abstract String name();

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

	protected abstract TrackingMetrics fromCSVLine( String[] line );
}
