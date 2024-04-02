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
import java.util.List;

public class StringRangeParamSweepModel extends AbstractParamSweepModel< String >
{

	protected final List< String > stringList = new ArrayList<>();

	private boolean isFile;

	@Override
	public StringRangeParamSweepModel paramName( final String paramName )
	{
		return ( StringRangeParamSweepModel ) super.paramName( paramName );
	}

	public StringRangeParamSweepModel removeAll()
	{
		if ( !stringList.isEmpty() )
		{
			stringList.clear();
			notifyListeners();
		}
		return this;
	}

	public StringRangeParamSweepModel addAll( final List< String > strs )
	{
		if ( !strs.isEmpty() )
		{
			stringList.addAll( strs );
			notifyListeners();
		}
		return this;
	}

	public StringRangeParamSweepModel setAll( final List< String > strs )
	{
		stringList.clear();
		stringList.addAll( strs );
		notifyListeners();
		return this;

	}

	public StringRangeParamSweepModel add( final String string )
	{
		stringList.add( string );
		notifyListeners();
		return this;
	}

	public StringRangeParamSweepModel remove( final int index )
	{
		if ( stringList.size() < 2 || index < 0 || index >= stringList.size() )
			return this;

		stringList.remove( index );
		notifyListeners();
		return this;
	}

	public StringRangeParamSweepModel set( final int id, final String text )
	{
		if ( !stringList.get( id ).equals( text ) )
		{
			stringList.set( id, text );
			notifyListeners();
		}
		return this;
	}

	public StringRangeParamSweepModel isFile( final boolean isFile )
	{
		if ( this.isFile != isFile )
		{
			this.isFile = isFile;
			notifyListeners();
		}
		return this;
	}

	@Override
	public List< String > getRange()
	{
		return new ArrayList<>( stringList );
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		str.append( String.format( "%s:\n", paramName ) );
		int index = 0;
		for ( final String string : stringList )
			str.append( String.format( " - %2d: %s\n", index++, string ) );
		return str.toString();
	}

	public boolean isFile()
	{
		return isFile;
	}
}
