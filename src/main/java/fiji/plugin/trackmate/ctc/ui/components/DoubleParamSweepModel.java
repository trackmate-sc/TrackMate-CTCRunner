package fiji.plugin.trackmate.ctc.ui.components;

public class DoubleParamSweepModel extends NumberParamSweepModel
{

	private DoubleParamSweepModel(
			final String paramName,
			final String units,
			final RangeType type,
			final double min,
			final double max,
			final int nSteps,
			final Double[] manualRange )
	{
		super( paramName, units, type, min, max, nSteps, manualRange );
	}
	
	@Override
	public Number[] getRange()
	{
		switch ( type )
		{
		case FIXED:
			return new Number[] { min };
		case LIN_RANGE:
			return linspace( min.doubleValue(), max.doubleValue(), nSteps );
		case LOG_RANGE:
			final Double[] e = linspace( Math.log( 1 ), Math.log( 1. - min.doubleValue() + max.doubleValue() ), nSteps );
			final Double[] out = new Double[ e.length ];
			for ( int i = 0; i < out.length; i++ )
				out[ i ] = Math.exp( e[ i ] ) + min.doubleValue() - 1.;
			return out;
		case MANUAL:
			return manualRange;
		default:
			throw new IllegalArgumentException( "Unknown range type: " + type );
		}
	}

	public static Builder create()
	{
		return new Builder();
	}
	
	public static class Builder
	{
		private String paramName = "no name set";

		private String units = "";

		private RangeType rangeType = RangeType.LIN_RANGE;

		private double min = 1.;

		private double max = 10.;

		private int nSteps = 10;
		
		private Double[] manualRange = new Double[] { 1., 2., 3., 4., 5., 6., 7., 8., 9., 10. };
		
		public Builder paramName( final String paramName )
		{
			this.paramName = paramName;
			return this;
		}

		public Builder units( final String units )
		{
			this.units = units;
			return this;
		}

		public Builder min( final double min )
		{
			this.min = min;
			return this;
		}

		public Builder max( final double max )
		{
			this.max = max;
			return this;
		}

		public Builder rangeType( final RangeType rangeType )
		{
			this.rangeType = rangeType;
			return this;
		}

		public Builder manualRange( final Double... vals )
		{
			this.manualRange = vals;
			return this;
		}

		public Builder nSteps( final int nSteps )
		{
			this.nSteps = nSteps;
			return this;
		}

		public DoubleParamSweepModel get()
		{
			return new DoubleParamSweepModel(
					paramName,
					units,
					rangeType,
					Math.min( max, min ),
					Math.max( max, min ),
					Math.max( 2, nSteps ),
					manualRange );
		}
	}

	static final Double[] linspace( final double min, final double max, final int nSteps )
	{
		final Double[] range = new Double[ nSteps ];
		for ( int i = 0; i < range.length; i++ )
			range[ i ] = min + ( max - min ) * i / ( nSteps - 1 );

		return range;
	}

	public static final String str( final Number[] range )
	{
		if ( range.length == 0 )
			return "[]";

		final StringBuilder str = new StringBuilder();
		str.append( "[ " );
		str.append( String.format( "%s", range[ 0 ] ) );
		for ( int i = 1; i < range.length; i++ )
			str.append( String.format( ", %s", range[ i ] ) );

		str.append( " ]" );
		return str.toString();
	}
}
