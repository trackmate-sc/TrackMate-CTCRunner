/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2024 TrackMate developers.
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

public class ArrayParamSweepModel< T > extends AbstractParamSweepModel< T >
{

	protected final Set< T > set = new LinkedHashSet<>();

	protected RangeType rangeType = RangeType.FIXED;

	protected T fixedValue;

	private T[] allValues;

	private ArrayParamSweepModel()
	{
		super();
	}

	public ArrayParamSweepModel( final T[] allValues )
	{
		this();
		this.allValues = allValues;
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

	public RangeType getRangeType()
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

	@Override
	public ArrayParamSweepModel< T > paramName( final String paramName )
	{
		return ( ArrayParamSweepModel< T > ) super.paramName( paramName );
	}

	public ArrayParamSweepModel< T > rangeType( final RangeType rangeType )
	{
		if ( this.rangeType != rangeType )
		{
			this.rangeType = rangeType;
			notifyListeners();
		}
		return this;
	}

	public ArrayParamSweepModel< T > fixedValue( final T fixedValue )
	{
		if ( this.fixedValue != fixedValue )
		{
			this.fixedValue = fixedValue;
			notifyListeners();
		}
		return this;
	}

	public ArrayParamSweepModel< T > addValue( final T value )
	{
		if ( set.add( value ) )
			notifyListeners();
	
		return this;
	}

	public ArrayParamSweepModel< T > removeValue( final T value )
	{
		if ( set.remove( value ) )
			notifyListeners();
	
		return this;
	}

	public List< T > getAllValues()
	{
		return Arrays.asList( allValues );
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
}
