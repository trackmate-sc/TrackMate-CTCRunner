package fiji.plugin.trackmate.ctc.ui.components;

public class BooleanParamSweepModel extends AbstractParamSweepModel< Boolean >
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

	RangeType rangeType = RangeType.TEST_ALL;

	boolean fixedValue = true;

	@Override
	public Boolean[] getRange()
	{
		switch ( rangeType )
		{
		case FIXED:
			return new Boolean[] { fixedValue };
		case TEST_ALL:
			return new Boolean[] { false, true };
		default:
			throw new IllegalArgumentException( "Unknown range type: " + rangeType );
		}
	}

	@Override
	public String toString()
	{
		switch ( rangeType )
		{
		case FIXED:
			return String.format( "%s:\n"
					+ " - type: %s\n"
					+ " - value: %s",
					paramName,
					rangeType,
					fixedValue );
		case TEST_ALL:
			return String.format( "%s:\n"
					+ " - type: %s\n",
					paramName,
					rangeType );
		default:
			throw new IllegalArgumentException( "Unknown range type: " + rangeType );
		}
	}

	@Override
	public BooleanParamSweepModel paramName( final String paramName )
	{
		return ( BooleanParamSweepModel ) super.paramName( paramName );
	}

	public BooleanParamSweepModel rangeType( final RangeType rangeType )
	{
		if ( this.rangeType != rangeType )
		{
			this.rangeType = rangeType;
			notifyListeners();
		}
		return this;
	}

	public BooleanParamSweepModel fixedValue( final boolean fixedValue )
	{
		if ( this.fixedValue != fixedValue )
		{
			this.fixedValue = fixedValue;
			notifyListeners();
		}
		return this;
	}
}
