package fiji.plugin.trackmate.ctc;

public class CTCMetrics
{

	public final double seg;

	public final double tra;

	public final double det;

	public final double ct;

	public final double tf;

	public final double bci;

	public final double cca;

	public final double tim;

	public final double detectionTime;

	public final double trackingTime;

	private CTCMetrics(
			final double seg,
			final double tra,
			final double det,
			final double ct,
			final double tf,
			final double bci,
			final double cca,
			final double tim,
			final double detectionTime,
			final double trackingTime )
	{
		this.seg = seg;
		this.tra = tra;
		this.det = det;
		this.ct = ct;
		this.tf = tf;
		this.bci = bci;
		this.cca = cca;
		this.tim = tim;
		this.detectionTime = detectionTime;
		this.trackingTime = trackingTime;
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		final double[] arr = toArray();
		final CTCMetricsDescription[] desc = CTCMetricsDescription.values();
		for ( int i = 0; i < 7; i++ )
			str.append( String.format( " - %-3s: %.3f\n", desc[ i ].ctcName(), arr[ i ] ) );
		str.append( String.format( " - %-3s: %.1f s\n", desc[ 7 ].ctcName(), arr[ 7 ] ) );
		str.append( String.format( " - %-14s: %.1f s\n", desc[ 8 ].ctcName(), arr[ 8 ] ) );
		str.append( String.format( " - %-14s: %.1f s", desc[ 9 ].ctcName(), arr[ 9 ] ) );
		return str.toString();
	}

	public double[] toArray()
	{
		return new double[] { seg, tra, det, ct, tf, cca, bci, tim, detectionTime, trackingTime };
	}

	public Builder copyEdit()
	{
		final Builder builder = create();
		builder.seg = this.seg;
		builder.tra = this.tra;
		builder.det = this.det;
		builder.ct = this.ct;
		builder.tf = this.tf;
		builder.cca = this.cca;
		builder.bci = this.bci;
		builder.tim = this.tim;
		builder.detectionTime = this.detectionTime;
		builder.trackingTime = this.trackingTime;
		return builder;
	}

	/**
	 * Prepend the specified header with the CTC metrics header.
	 * 
	 * @param header
	 *            the header to preprint.
	 * @return a new String array.
	 */
	public static String[] concatWithCSVHeader( final String[] header )
	{
		final CTCMetricsDescription[] vals = CTCMetricsDescription.values();
		final String[] out = new String[ header.length + vals.length ];
		for ( int i = 0; i < vals.length; i++ )
			out[ i ] = vals[ i ].ctcName();

		for ( int i = 0; i < header.length; i++ )
			out[ vals.length + i ] = header[ i ];

		return out;
	}

	public String[] concatWithCSVLine( final String[] content )
	{
		final double[] arr = toArray();
		final String[] out = new String[ content.length + arr.length ];
		for ( int i = 0; i < arr.length; i++ )
			out[ i ] = Double.toString( arr[ i ] );

		for ( int i = 0; i < content.length; i++ )
			out[ arr.length + i ] = content[ i ];

		return out;
	}

	public final static Builder create()
	{
		return new Builder();
	}

	public static final class Builder
	{

		private double seg = Double.NaN;

		private double tra = Double.NaN;

		private double det = Double.NaN;

		private double ct = Double.NaN;

		private double tf = Double.NaN;

		private double bci = Double.NaN;

		private double cca = Double.NaN;

		private double tim = Double.NaN;

		private double detectionTime = Double.NaN;

		private double trackingTime = Double.NaN;

		public Builder seg( final double seg )
		{
			this.seg = seg;
			return this;
		}

		public Builder tra( final double tra )
		{
			this.tra = tra;
			return this;
		}

		public Builder det( final double det )
		{
			this.det = det;
			return this;
		}

		public Builder ct( final double ct )
		{
			this.ct = ct;
			return this;
		}

		public Builder tf( final double tf )
		{
			this.tf = tf;
			return this;
		}

		public Builder bci( final double bc )
		{
			this.bci = bc;
			return this;
		}

		public Builder cca( final double cca )
		{
			this.cca = cca;
			return this;
		}

		public Builder tim( final double tim )
		{
			this.tim = tim;
			return this;
		}

		public Builder detectionTime( final double detectionTime )
		{
			this.detectionTime = detectionTime;
			return this;
		}

		public Builder trackingTime( final double trackingTime )
		{
			this.trackingTime = trackingTime;
			return this;
		}

		public CTCMetrics get()
		{
			return new CTCMetrics( seg, tra, det, ct, tf, bci, cca, tim, detectionTime, trackingTime );
		}
	}
}