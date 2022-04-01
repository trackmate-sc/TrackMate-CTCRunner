package fiji.plugin.trackmate.helper.spt;

import java.util.Arrays;
import java.util.List;

import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.TrackingMetricsType;

public class SPTTrackingMetricsType extends TrackingMetricsType
{

	public static final MetricValue ALPHA = new MetricValue( "alpha", "Matching score against ground-truth", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue BETA = new MetricValue( "beta", "Matching score against ground-truth, penalizing spurious tracks", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue JSC = new MetricValue( "JSC", "Jaccard similarity coefficient for track points", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue JSCTHETA = new MetricValue( "JSCtheta", "Jaccard similarity coefficient for whole tracks", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue RMSE = new MetricValue( "RMSE", "Overall localization accuracy", MetricValueOptimum.LOWER_IS_BETTER, MetricValueBound.UNBOUNDED );

	private static final List< MetricValue > KEYS = Arrays.asList( new MetricValue[] { ALPHA, BETA, JSC, JSCTHETA, RMSE } );

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
		super( KEYS );
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
	public MetricValue defaultMetric()
	{
		return ALPHA;
	}
}
