package fiji.plugin.trackmate.ctc;

public enum CTCMetricsDescription
{
	
	SEG( "SEG", "Segmentation accuracy" ),
	TRA( "TRA", "Tracking accuracy" ),
	DET( "DET", "Detection quality" ),
	CT( "CT", "Complete tracks" ),
	TF( "TF", "Track fractions" ),
	CCA( "CCA", "Cell-cycle accuracy" ),
	BC( "BC", "Branching correctness" ),
	TIM( "TIM", "Execution time" ),
	DETECTION_TIME( "DETECTION_TIME", "Detection time" ),
	TRACKING_TIME( "TRACKING_TIME", "Tracking time" );

	private final String name;

	private final String description;

	CTCMetricsDescription( final String name, final String description )
	{
		this.name = name;
		this.description = description;
	}

	public String ctcName()
	{
		return name;
	}

	public String description()
	{
		return description;
	}

	public static final String[] toHeader()
	{
		final String[] out = new String[ values().length ];
		for ( int i = 0; i < values().length; i++ )
			out[ i ] = values()[ i ].name;

		return out;
	}
}
