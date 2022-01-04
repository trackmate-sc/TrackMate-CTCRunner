package fiji.plugin.trackmate.ctc.ui.components;

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
