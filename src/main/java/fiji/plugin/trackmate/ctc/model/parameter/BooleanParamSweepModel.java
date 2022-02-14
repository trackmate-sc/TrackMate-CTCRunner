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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

	private RangeType rangeType = RangeType.TEST_ALL;

	private boolean fixedValue = true;

	@Override
	public List< Boolean > getRange()
	{
		switch ( rangeType )
		{
		case FIXED:
			return Collections.singletonList( fixedValue );
		case TEST_ALL:
			return Arrays.asList( false, true );
		default:
			throw new IllegalArgumentException( "Unknown range type: " + rangeType );
		}
	}

	public boolean getFixedValue()
	{
		return fixedValue;
	}

	public RangeType getRangeType()
	{
		return rangeType;
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
