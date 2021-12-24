package fiji.plugin.trackmate.ctc.ui.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EnumParamSweepModel< T extends Enum< T > > extends AbstractParamSweepModel< T >
{

	public enum RangeType
	{
		TEST_ALL( "test all" ), FIXED( "single value" ), LIST( "list" );

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

	Set< T > set = new LinkedHashSet<>();

	RangeType rangeType = RangeType.FIXED;

	T fixedValue;

	private final Class< T > enumClass;

	public EnumParamSweepModel( final Class< T > enumClass )
	{
		this.enumClass = enumClass;
	}

	@Override
	public List< T > getRange()
	{
		switch ( rangeType )
		{
		case FIXED:
			return Collections.singletonList( fixedValue );
		case LIST:
			return new ArrayList<>( set );
		case TEST_ALL:
			return getAllValues();
		default:
			throw new IllegalArgumentException( "Unknown range type: " + rangeType );
		}
	}

	@Override
	public EnumParamSweepModel< T > paramName( final String paramName )
	{
		return ( EnumParamSweepModel< T > ) super.paramName( paramName );
	}

	public EnumParamSweepModel< T > rangeType( final RangeType rangeType )
	{
		if ( this.rangeType != rangeType )
		{
			this.rangeType = rangeType;
			notifyListeners();
		}
		return this;
	}

	public EnumParamSweepModel< T > fixedValue( final T fixedValue )
	{
		if ( this.fixedValue != fixedValue )
		{
			this.fixedValue = fixedValue;
			notifyListeners();
		}
		return this;
	}

	public EnumParamSweepModel< T > addValue( final T value )
	{
		if ( set.add( value ) )
			notifyListeners();

		return this;
	}

	public EnumParamSweepModel< T > removeValue( final T value )
	{
		if ( set.remove( value ) )
			notifyListeners();

		return this;
	}

	public List< T > getAllValues()
	{
		return Arrays.asList( enumClass.getEnumConstants() );
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
		case LIST:
		case TEST_ALL:
			return String.format( "%s:\n"
					+ " - type: %s\n"
					+ " - values: %s",
					paramName,
					rangeType,
					Arrays.toString( getRange().toArray() ) );
		default:
			throw new IllegalArgumentException( "Unknown range type: " + rangeType );
		}
	}
}
