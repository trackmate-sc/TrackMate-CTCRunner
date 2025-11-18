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
package fiji.plugin.trackmate.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class NestedIterator< T > implements Iterator< List< T > >
{
	private final List< Iterator< T > > iterators;

	private final List< T > currentCombination;

	private boolean hasNext;

	private final List< ? extends Iterable< T > > iterables;

	/**
	 * Constructs a NestedIterator from a list of iterables.
	 * 
	 * @param iterables
	 *            List of iterables to generate combinations from
	 */
	public NestedIterator( final List< ? extends Iterable< T > > iterables )
	{
		this.iterables = iterables;
		this.iterators = new ArrayList<>( iterables.size() );
		for ( final Iterable< T > iterable : iterables )
			this.iterators.add( iterable.iterator() );

		this.currentCombination = new ArrayList<>( iterables.size() );
		this.hasNext = true;

		// Initialize the first combination
		initializeFirstCombination();
	}

	/**
	 * Initializes the first combination by attempting to get the first element
	 * from each iterator.
	 */
	private void initializeFirstCombination()
	{
		for ( final Iterator< T > iterator : iterators )
		{
			if ( !iterator.hasNext() )
			{
				// If any iterator is empty, there are no combinations
				hasNext = false;
				return;
			}
			currentCombination.add( iterator.next() );
		}
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	@Override
	public List< T > next()
	{
		if ( !hasNext )
			throw new NoSuchElementException( "No more combinations" );

		// Create a copy of the current combination to return
		final List< T > result = new ArrayList<>( currentCombination );

		// Try to generate the next combination
		hasNext = generateNextCombination();

		return result;
	}

	/**
	 * Generates the next combination of elements.
	 * 
	 * @return true if a new combination is found, false otherwise
	 */
	private boolean generateNextCombination()
	{
		// Start from the last iterator
		for ( int i = iterators.size() - 1; i >= 0; i-- )
		{
			final Iterator< T > currentIterator = iterators.get( i );

			// If current iterator has more elements
			if ( currentIterator.hasNext() )
			{
				// Replace the current element with the next one
				currentCombination.set( i, currentIterator.next() );
				return true;
			}
			else
			{
				// Find the corresponding original iterable to reset the iterator
				final Iterable< T > originalIterable = iterables.get( i );

				// Reset iterator
				final Iterator< T > resetIterator = originalIterable.iterator();
				iterators.set( i, resetIterator );

				// Set the first element of the reset iterator
				currentCombination.set( i, resetIterator.next() );

				// If we've gone through all iterators, no more combinations
				if ( i == 0 )
					return false;
			}
		}
		return false;
	}

	/**
	 * Example usage method to demonstrate how to use the NestedIterator
	 */
	public static void main( final String[] args )
	{
		// Example usage with lists (which implement Iterable)
		final List< List< Integer > > iterables = Arrays.asList(
				Arrays.asList( 1, 2, 3 ),
				Arrays.asList( 4, 5 ),
				Arrays.asList( 6, 7, 8 ) );

		final NestedIterator< Integer > nestedIterator = new NestedIterator<>( iterables );

		while ( nestedIterator.hasNext() )
		{
			System.out.println( nestedIterator.next() );
		}
	}
}
