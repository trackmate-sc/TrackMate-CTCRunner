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
package fiji.plugin.trackmate.helper.model.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fiji.plugin.trackmate.Dimension;

public abstract class NumberParamSweepModel extends AbstractParamSweepModel< Number >
{

	public enum RangeType
	{
		LIN_RANGE( "linear range" ), LOG_RANGE( "log range" ), FIXED( "fixed value" ), MANUAL( "manual range" );

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

	protected Dimension dimension = Dimension.NONE;

	protected RangeType rangeType = RangeType.LIN_RANGE;

	protected Number min = 1;

	protected Number max = 10;

	protected int nSteps = 10;

	protected final List< Number > manualRange = new ArrayList<>();

	@Override
	public abstract List< Number > getRange();

	public NumberParamSweepModel dimension( final Dimension dimension )
	{
		if ( !this.dimension.equals( dimension ) )
		{
			this.dimension = dimension;
			notifyListeners();
		}
		return this;
	}

	public NumberParamSweepModel rangeType( final RangeType rangeType )
	{
		if ( this.rangeType != rangeType )
		{
			this.rangeType = rangeType;
			notifyListeners();
		}
		return this;
	}

	public NumberParamSweepModel nSteps( final int nSteps )
	{
		if ( this.nSteps != nSteps )
		{
			this.nSteps = nSteps;
			notifyListeners();
		}
		return this;
	}

	public Dimension getDimension()
	{
		return dimension;
	}

	public RangeType getRangeType()
	{
		return rangeType;
	}

	public int getNSteps()
	{
		return nSteps;
	}
	
	public Number getMin()
	{
		return min;
	}
	
	public Number getMax()
	{
		return max;
	}

	public List< Number > getManualRange()
	{
		return manualRange;
	}

	@Override
	public String toString()
	{
		switch ( rangeType )
		{
		case FIXED:
			return String.format( "%s (%s):\n"
					+ " - type: %s\n"
					+ " - value: %s",
					paramName,
					dimension.toString(),
					rangeType,
					min );
		case LIN_RANGE:
		case LOG_RANGE:
			return String.format( "%s (%s):\n"
					+ " - type: %s\n"
					+ " - min: %s\n"
					+ " - max: %s\n"
					+ " - nSteps: %d\n"
					+ " - values: %s",
					paramName,
					dimension.toString(),
					rangeType,
					min,
					max,
					nSteps,
					Arrays.toString( getRange().toArray() ) );
		case MANUAL:
			return String.format( "%s (%s):\n"
					+ " - type: %s\n"
					+ " - values: %s",
					paramName,
					dimension.toString(),
					rangeType,
					Arrays.toString( getRange().toArray() ) );
		default:
			throw new IllegalArgumentException( "Unknown range type: " + rangeType );
		}
	}
}
