/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.ctc.model.parameter;

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
