package fiji.plugin.trackmate.ctc.ui.components;

public class EnumParamSweepModel< T extends Enum< T > > extends ArrayParamSweepModel< T >
{

	@SuppressWarnings( "unused" )
	private final Class< T > enumClass;

	public EnumParamSweepModel( final Class< T > enumClass )
	{
		super( enumClass.getEnumConstants() );
		this.enumClass = enumClass;
	}

	@Override
	public EnumParamSweepModel< T > paramName( final String paramName )
	{
		return ( EnumParamSweepModel< T > ) super.paramName( paramName );
	}

	@Override
	public EnumParamSweepModel< T > rangeType( final RangeType rangeType )
	{
		if ( this.rangeType != rangeType )
		{
			this.rangeType = rangeType;
			notifyListeners();
		}
		return this;
	}

	@Override
	public EnumParamSweepModel< T > fixedValue( final T fixedValue )
	{
		if ( this.fixedValue != fixedValue )
		{
			this.fixedValue = fixedValue;
			notifyListeners();
		}
		return this;
	}

	@Override
	public EnumParamSweepModel< T > addValue( final T value )
	{
		if ( set.add( value ) )
			notifyListeners();

		return this;
	}

	@Override
	public EnumParamSweepModel< T > removeValue( final T value )
	{
		if ( set.remove( value ) )
			notifyListeners();

		return this;
	}
}
