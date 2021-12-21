package fiji.plugin.trackmate.ctc.ui;

public class BooleanParamSweepModel
{

	public enum RangeType
	{
		TEST_ALL( "test both" ), FIXED( "fixed value" );

		private final String name;

		RangeType( final String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	final String paramName;

	final RangeType type;

	final boolean fixedValue;

	public BooleanParamSweepModel(
			final String paramName,
			final RangeType type,
			final boolean fixedValue )
	{
		this.paramName = paramName;
		this.type = type;
		this.fixedValue = fixedValue;
	}

	public Boolean[] getRange()
	{
		switch ( type )
		{
		case FIXED:
			return new Boolean[] { fixedValue };
		case TEST_ALL:
			return new Boolean[] { false, true };
		default:
			throw new IllegalArgumentException( "Unknown range type: " + type );
		}
	}

	@Override
	public String toString()
	{
		switch ( type )
		{
		case FIXED:
			return String.format( "%s:\n"
					+ " - type: %s\n"
					+ " - value: %s",
					paramName,
					type,
					fixedValue );
		case TEST_ALL:
			return String.format( "%s:\n"
					+ " - type: %s\n",
					paramName,
					type );
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

		private RangeType rangeType = RangeType.FIXED;

		private boolean fixedValue = true;

		public Builder paramName( final String paramName )
		{
			this.paramName = paramName;
			return this;
		}

		public Builder rangeType( final RangeType rangeType )
		{
			this.rangeType = rangeType;
			return this;
		}

		public Builder fixedValue( final boolean fixedValue )
		{
			this.fixedValue = fixedValue;
			return this;
		}

		public BooleanParamSweepModel get()
		{
			return new BooleanParamSweepModel(
					paramName,
					rangeType,
					fixedValue );
		}
	}
}
