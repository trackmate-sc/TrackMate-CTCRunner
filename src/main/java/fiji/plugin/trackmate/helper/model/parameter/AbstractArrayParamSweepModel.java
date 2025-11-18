/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
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
package fiji.plugin.trackmate.helper.model.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AbstractArrayParamSweepModel< T, F extends AbstractArrayParamSweepModel< T, F > > extends AbstractParamSweepModel< T >
{

	protected final Set< T > set = new LinkedHashSet<>();

	protected ArrayRangeType rangeType = ArrayRangeType.FIXED;

	protected T fixedValue;

	protected final List< T > allValues = new ArrayList<>();

	public AbstractArrayParamSweepModel( final T[] allValues )
	{
		this.allValues.addAll( Arrays.asList( allValues ) );
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

	public ArrayRangeType getRangeType()
	{
		return rangeType;
	}

	public T getFixedValue()
	{
		return fixedValue;
	}

	public Set< T > getSelection()
	{
		return set;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public F paramName( final String paramName )
	{
		return ( F ) super.paramName( paramName );
	}

	@SuppressWarnings( "unchecked" )
	public F rangeType( final ArrayRangeType rangeType )
	{
		if ( this.rangeType != rangeType )
		{
			this.rangeType = rangeType;
			notifyListeners();
		}
		return ( F ) this;
	}

	@SuppressWarnings( "unchecked" )
	public F fixedValue( final T fixedValue )
	{
		if ( this.fixedValue != fixedValue )
		{
			this.fixedValue = fixedValue;
			notifyListeners();
		}
		return ( F ) this;
	}

	@SuppressWarnings( "unchecked" )
	public F addValue( final T value )
	{
		if ( set.add( value ) )
			notifyListeners();

		return ( F ) this;
	}

	@SuppressWarnings( "unchecked" )
	public F removeValue( final T value )
	{
		if ( set.remove( value ) )
			notifyListeners();

		return ( F ) this;
	}

	public List< T > getAllValues()
	{
		return allValues;
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

	public enum ArrayRangeType
	{
		TEST_ALL( "test all" ), FIXED( "single value" ), LIST( "list" );

		private final String name;

		ArrayRangeType( final String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
