package fiji.plugin.trackmate.helper.spt;

import java.util.Arrays;
import java.util.List;

import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.TrackingMetricsType;

public class SPTTrackingMetricsType extends TrackingMetricsType
{

	public static final String ALPHA = "alpha";

	public static final String BETA = "beta";

	public static final String JSC = "JSC";

	public static final String JSCTHETA = "JSCtheta";

	public static final String RMSE = "RMSE";

	private static final List< String > KEYS = Arrays.asList( new String[] {
			ALPHA, BETA, JSC, JSCTHETA, RMSE } );

	private static final List< String > DESCRIPTIONS = Arrays.asList( new String[] {
			"Matching score against ground-truth",
			"Matching score against ground-truth, penalizing spurious tracks",
			"Jaccard similarity coefficient for track points",
			"Jaccard similarity coefficient for whole tracks",
			"Overall localization accuracy" } );

	public static final String NAME = "Single-Particle Tracking (SPT) Challenge metrics";

	public static final String CSV_SUFFIX = "SPTMetrics";

	public static final String URL = "https://doi.org/10.1038/nmeth.2808";

	public static final String INFO = "<html>"
			+ "The Single-Particle Tracking challenge (SPT) metrics focus on measuring the "
			+ "performance of tracking pipelines applied to small objects that have no shape "
			+ "and a possibly complex motion, possibly very dense with overlapping trajectories. "
			+ "They typically apply to tracking sub-resolved "
			+ "particles or cell organelles in Life-Sciences. "
			+ "<p>"
			+ "The SPT metrics are derived from an ISBI Grand Challenge published here:"
			+ "</html>";

	public SPTTrackingMetricsType()
	{
		super( KEYS, DESCRIPTIONS );
	}

	@Override
	public MetricsRunner runner( final String gtPath )
	{
		return new SPTMetricsRunner( gtPath );
	}

	@Override
	public String name()
	{
		return NAME;
	}

	@Override
	public String csvSuffix()
	{
		return CSV_SUFFIX;
	}

	@Override
	public String url()
	{
		return URL;
	}

	@Override
	public String info()
	{
		return INFO;
	}

	@Override
	public String defaultMetric()
	{
		return ALPHA;
	}
}
