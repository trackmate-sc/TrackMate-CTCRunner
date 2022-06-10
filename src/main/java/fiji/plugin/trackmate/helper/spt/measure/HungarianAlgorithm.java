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
package fiji.plugin.trackmate.helper.spt.measure;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class HungarianAlgorithm
{
	public int[] starsByCol2 = new int[ 20 ];

//	private final int n = 0;
//
//	private final int m = 0;

	public int[][] computeAssignments( final double[][] matrix )
	{
		// subtract minumum value from rows and columns to create lots of zeroes
		reduceMatrix( matrix );

		// non negative values are the index of the starred or primed zero in
		// the row or column
		final int[] starsByRow = new int[ matrix.length ];
		Arrays.fill( starsByRow, -1 );
		final int[] starsByCol = new int[ matrix[ 0 ].length ];
		Arrays.fill( starsByCol, -1 );
		final int[] primesByRow = new int[ matrix.length ];
		Arrays.fill( primesByRow, -1 );

		// 1s mean covered, 0s mean not covered
		final int[] coveredRows = new int[ matrix.length ];
		final int[] coveredCols = new int[ matrix[ 0 ].length ];

		// star any zero that has no other starred zero in the same row or
		// column
		initStars( matrix, starsByRow, starsByCol );
		coverColumnsOfStarredZeroes( starsByCol, coveredCols );

		while ( !allAreCovered( coveredCols ) )
		{
//			System.out.println( n++ );

			int[] primedZero = primeSomeUncoveredZero( matrix, primesByRow, coveredRows, coveredCols );

			while ( primedZero == null )
			{
				// keep making more zeroes until we find something that we can
				// prime (i.e. a zero
				// that is uncovered)
				makeMoreZeroes( matrix, coveredRows, coveredCols );
				primedZero = primeSomeUncoveredZero( matrix, primesByRow, coveredRows, coveredCols );
			}

			// check if there is a starred zero in the primed zero's row
			final int columnIndex = starsByRow[ primedZero[ 0 ] ];
			if ( -1 == columnIndex )
			{

				// if not, then we need to increment the zeroes and start over
				incrementSetOfStarredZeroes( primedZero, starsByRow, starsByCol, primesByRow );
				Arrays.fill( primesByRow, -1 );
				Arrays.fill( coveredRows, 0 );
				Arrays.fill( coveredCols, 0 );
				coverColumnsOfStarredZeroes( starsByCol, coveredCols );
			}
			else
			{
				// cover the row of the primed zero and uncover the column of
				// the starred zero in
				// the same row
				coveredRows[ primedZero[ 0 ] ] = 1;
				coveredCols[ columnIndex ] = 0;
			}
		}

		// ok now we should have assigned everything
		// take the starred zeroes in each column as the correct assignments

		final int[][] retval = new int[ matrix.length ][];
		for ( int i = 0; i < starsByCol.length; i++ )
		{
			retval[ i ] = new int[] { starsByCol[ i ], i };
		}

		return retval;
	}

	private boolean allAreCovered( final int[] coveredCols )
	{
		for ( final int covered : coveredCols )
		{
			if ( 0 == covered )
				return false;
		}
		return true;
	}

	/**
	 * the first step of the hungarian algorithm is to find the smallest element
	 * in each row and subtract it's values from all elements in that row
	 * 
	 * @return the next step to perform
	 */

	private void reduceMatrix( final double[][] matrix )
	{
//		System.out.println( "reduceMatrix" );

		for ( int i = 0; i < matrix.length; i++ )
		{

			// find the min value in the row
			double minValInRow = Double.MAX_VALUE;
			for ( int j = 0; j < matrix[ i ].length; j++ )
			{
				if ( minValInRow > matrix[ i ][ j ] )
				{
					minValInRow = matrix[ i ][ j ];
				}
			}

			// subtract it from all values in the row
			for ( int j = 0; j < matrix[ i ].length; j++ )
			{
				matrix[ i ][ j ] -= minValInRow;
			}
		}

		for ( int i = 0; i < matrix[ 0 ].length; i++ )
		{
			double minValInCol = Double.MAX_VALUE;
			for ( int j = 0; j < matrix.length; j++ )
			{
				if ( minValInCol > matrix[ j ][ i ] )
				{
					minValInCol = matrix[ j ][ i ];
				}
			}

			for ( int j = 0; j < matrix.length; j++ )
			{
				matrix[ j ][ i ] -= minValInCol;
			}

		}

	}

	/**
	 * init starred zeroes for each column find the first zero if there is no
	 * other starred zero in that row then star the zero, cover the column and
	 * row and go onto the next column
	 * 
	 * @param costMatrix
	 * @param starredZeroes
	 * @param coveredRows
	 * @param coveredCols
	 * @return the next step to perform
	 */

	private void initStars( final double costMatrix[][], final int[] starsByRow, final int[] starsByCol )
	{
		final int[] rowHasStarredZero = new int[ costMatrix.length ];
		final int[] colHasStarredZero = new int[ costMatrix[ 0 ].length ];

		for ( int i = 0; i < costMatrix.length; i++ )
		{
			for ( int j = 0; j < costMatrix[ i ].length; j++ )
			{
				if ( 0 == costMatrix[ i ][ j ] && 0 == rowHasStarredZero[ i ] && 0 == colHasStarredZero[ j ] )
				{
					starsByRow[ i ] = j;
					starsByCol[ j ] = i;
					rowHasStarredZero[ i ] = 1;
					colHasStarredZero[ j ] = 1;
					break; // move onto the next row
				}
			}
		}
	}

	/**
	 * just marke the columns covered for any coluimn containing a starred zero
	 * 
	 * @param starsByCol
	 * @param coveredCols
	 */

	private void coverColumnsOfStarredZeroes( final int[] starsByCol, final int[] coveredCols )
	{
		for ( int i = 0; i < starsByCol.length; i++ )
		{
			coveredCols[ i ] = -1 == starsByCol[ i ] ? 0 : 1;
		}
	}

	/**
	 * finds some uncovered zero and primes it
	 * 
	 * @param matrix
	 * @param primesByRow
	 * @param coveredRows
	 * @param coveredCols
	 * @return
	 */

	private int[] primeSomeUncoveredZero( final double matrix[][], final int[] primesByRow, final int[] coveredRows, final int[] coveredCols )
	{
//		System.out.println( m++ );

		// System.out.println("primeSomeUncoveredZero");

		// find an uncovered zero and prime it
		for ( int i = 0; i < matrix.length; i++ )
		{
			if ( 1 == coveredRows[ i ] )
				continue;
			for ( int j = 0; j < matrix[ i ].length; j++ )
			{
				// if it's a zero and the column is not covered
				if ( 0 == matrix[ i ][ j ] && 0 == coveredCols[ j ] )
				{

					// ok this is an unstarred zero
					// prime it
					primesByRow[ i ] = j;
					return new int[] { i, j };
				}
			}
		}
		return null;

	}

	/**
	 * @param unpairedZeroPrime
	 * @param starsByRow
	 * @param starsByCol
	 * @param primesByRow
	 */

	private void incrementSetOfStarredZeroes( final int[] unpairedZeroPrime, final int[] starsByRow, final int[] starsByCol,
			final int[] primesByRow )
	{
		// build the alternating zero sequence (prime, star, prime, star, etc)
		int i, j = unpairedZeroPrime[ 1 ];

		final Set< int[] > zeroSequence = new LinkedHashSet< int[] >();
		zeroSequence.add( unpairedZeroPrime );
		boolean paired = false;
		do
		{
			i = starsByCol[ j ];
			paired = -1 != i && zeroSequence.add( new int[] { i, j } );
			if ( !paired )
				break;

			j = primesByRow[ i ];
			paired = -1 != j && zeroSequence.add( new int[] { i, j } );

		}
		while ( paired );

		// unstar each starred zero of the sequence
		// and star each primed zero of the sequence
		for ( final int[] zero : zeroSequence )
		{
			if ( starsByCol[ zero[ 1 ] ] == zero[ 0 ] )
			{
				starsByCol[ zero[ 1 ] ] = -1;
				starsByRow[ zero[ 0 ] ] = -1;
			}
			if ( primesByRow[ zero[ 0 ] ] == zero[ 1 ] )
			{
				starsByRow[ zero[ 0 ] ] = zero[ 1 ];
				starsByCol[ zero[ 1 ] ] = zero[ 0 ];
			}
		}

	}

	private void makeMoreZeroes( final double[][] matrix, final int[] coveredRows, final int[] coveredCols )
	{
		// find the minimum uncovered value
		double minUncoveredValue = Double.MAX_VALUE;
		for ( int i = 0; i < matrix.length; i++ )
		{
			if ( 0 == coveredRows[ i ] )
			{
				for ( int j = 0; j < matrix[ i ].length; j++ )
				{
					if ( 0 == coveredCols[ j ] && matrix[ i ][ j ] < minUncoveredValue )
					{
						minUncoveredValue = matrix[ i ][ j ];
					}
				}
			}
		}

		// add the min value to all covered rows
		for ( int i = 0; i < coveredRows.length; i++ )
		{
			if ( 1 == coveredRows[ i ] )
			{
				for ( int j = 0; j < matrix[ i ].length; j++ )
				{
					matrix[ i ][ j ] += minUncoveredValue;
				}
			}
		}

		// subtract the min value from all uncovered columns
		for ( int i = 0; i < coveredCols.length; i++ )
		{
			if ( 0 == coveredCols[ i ] )
			{
				for ( int j = 0; j < matrix.length; j++ )
				{
					matrix[ j ][ i ] -= minUncoveredValue;
				}
			}
		}
	}

}
