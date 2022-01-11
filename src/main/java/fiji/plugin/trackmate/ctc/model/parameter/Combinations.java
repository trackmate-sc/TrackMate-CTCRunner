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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Combinations implements Iterator< Map< String, Object > >
{

	private final List< List< Object > > elements;

	private final int[] indices;

	private final List< String > keys;

	public Combinations( final Map< String, List< Object > > values )
	{
		this.elements = new ArrayList<>( values.size() );
		this.keys = new ArrayList<>( values.size() );
		for ( final String key : values.keySet() )
		{
			keys.add( key );
			elements.add( values.get( key ) );
		}
		this.indices = new int[ values.size() ];
	}

	@Override
	public boolean hasNext()
	{
		if ( elements.isEmpty() )
			return false;
		// has first index not yet reached max position?
		return indices[ 0 ] < elements.get( 0 ).size();
	}

	@Override
	public Map< String, Object > next()
	{
		// get next
		final Map< String, Object > result = new LinkedHashMap<>( indices.length );
		for ( int i = 0; i < indices.length; i++ )
			result.put( keys.get( i ), elements.get( i ).get( indices[ i ] ) );

		// increase indices
		for ( int i = indices.length - 1; i >= 0; i-- )
		{
			indices[ i ]++;
			if ( indices[ i ] >= elements.get( i ).size() && i > 0 )
				indices[ i ] %= elements.get( i ).size();
			else
				break;
		}
		return result;
	}
}
