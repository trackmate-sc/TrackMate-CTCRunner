package fiji.plugin.trackmate.helper.ctc;

import java.util.Arrays;
import java.util.List;

import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.TrackingMetricsType;
import fiji.plugin.trackmate.util.TMUtils;

public class CTCTrackingMetricsType extends TrackingMetricsType
{

	public static final String SEG = "SEG";

	public static final String TRA = "TRA";

	public static final String DET = "DET";

	public static final String CT = "CT";

	public static final String TF = "TF";

	public static final String CCA = "CCA";

	public static final String BC = "BC";

	private static final List< String > KEYS = Arrays.asList( new String[] {
			SEG, TRA, DET, CT, TF, CCA, BC } );

	private static final List< String > DESCRIPTIONS = Arrays.asList( new String[] {
			"Segmentation accuracy",
			"Tracking accuracy",
			"Detection quality",
			"Complete tracks",
			"Track fractions",
			"Cell-cycle accuracy",
			"Branching correctness" } );

	public static final String NAME = "Cell-Tracking challenge (CTC) metrics";

	public static final String CSV_SUFFIX = "CTCMetrics";

	public static final String URL = "https://doi.org/10.1038/nmeth.4473";

	public static final String INFO = "<html>"
			+ "The Cell-Tracking challenge (CTC) metrics focus on measuring the performance "
			+ "of tracking pipelines applied to objects that have a shape, move and possibly "
			+ "divide, typically cells in Life-Sciences. They include metrics for tracking "
			+ "accuracy and segmentation accuracy. "
			+ "The CTC framework supports tracking algorithms that detect object division and "
			+ "offers metrics to quantify the detection accuracy of such events. "
			+ "<p>"
			+ "The CTC metrics are derived from an ISBI Grand Challenge published here:"
			+ "</html>";

	public CTCTrackingMetricsType()
	{
		super( KEYS, DESCRIPTIONS );
	}

	@Override
	public MetricsRunner runner( final String gtPath )
	{
		return new CTCMetricsRunner( gtPath, TMUtils.getContext() );
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
		return DET;
	}
}
