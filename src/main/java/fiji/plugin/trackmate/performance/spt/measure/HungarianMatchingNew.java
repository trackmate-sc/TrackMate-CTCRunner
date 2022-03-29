/*-
 * #%L
 * Image Analysis Hub support for Life Scientists.
 * %%
 * Copyright (C) 2021 IAH developers.
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the IAH / C2RT / Institut Pasteur nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
/**
 * 
 */
package fiji.plugin.trackmate.performance.spt.measure;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * @author Stephane Dallongeville
 */
public class HungarianMatchingNew
{

	private final int numRow;

	private final int numCol;

	private final int k;

	private final double[][] costs;

	private final int[] rowsStar;

	private final int[] colsStar;

	private final int[] rowsPrime;

	private final boolean[] rowsCovered;

	private final boolean[] colsCovered;

	private final int[] colsUnStar;

	private final int[] rowsDoStar;

	private int step;

	private boolean done;

	/**
	 * Create the optimizer.
	 * 
	 * @param values
	 *            table of assignment costs. The number of lines has to be less
	 *            are equal to the number of columns (i.e., costs.length <=
	 *            costs[0]. length == true).
	 */
	public HungarianMatchingNew( final double[][] values )
	{
		int r, c;

		numRow = values.length;
		numCol = Math.max( values[ 0 ].length, numRow );
		k = Math.min( numRow, numCol );
		costs = new double[ numRow ][ numCol ];

		// find maximum value
		double max = values[ 0 ][ 0 ];
		for ( r = 0; r < values.length; r++ )
		{
			final double v = DoubleStream.of( values[ r ] ).max().getAsDouble();
			if ( v > max )
				max = v;
		}

		for ( r = 0; r < values.length; r++ )
		{
			final double[] rowValues = values[ r ];
			final double[] rowCosts = costs[ r ];

			for ( c = 0; c < rowValues.length; c++ )
				rowCosts[ c ] = rowValues[ c ];
			for ( ; c < numCol; c++ )
				rowCosts[ c ] = max;
		}

		rowsStar = new int[ numRow ];
		colsStar = new int[ numCol ];
		rowsPrime = new int[ numRow ];
		rowsCovered = new boolean[ numRow ];
		colsCovered = new boolean[ numCol ];

		colsUnStar = new int[ numCol ];
		rowsDoStar = new int[ numRow ];

		Arrays.fill( rowsPrime, -1 );
	}

	public boolean[][] compute()
	{
		initialReduce();

		done = false;
		step = 2;
		while ( !done )
		{
			switch ( step )
			{
			case 2:
				updateStar();
				break;

			case 3:
				doColCover();
				break;

			case 4:
				doPrime();
				break;

			case 5:
				//
				break;

			case 6:
				reduce();
				break;
			}
		}

		// default value is false in boolean array
		final boolean result[][] = new boolean[ numRow ][ numCol ];

		for ( int r = 0; r < numRow; r++ )
		{
			final int c = rowsStar[ r ];
			if ( c != -1 )
				result[ r ][ c ] = true;
		}

		return result;
	}

	// For each row we find the row minimum and subtract it from all entries on
	// that row.
	// For each column we find the column minimum and subtract it from all
	// entries on that column.
	public void initialReduce()
	{
		for ( int r = 0; r < numRow; r++ )
		{
			final double[] rowCosts = costs[ r ];

			// get row minimum cost
			final double min = DoubleStream.of( rowCosts ).min().getAsDouble();

			// subtract it to all entries
			for ( int c = 0; c < numCol; c++ )
				rowCosts[ c ] -= min;
		}

		// for (int c = 0; c < dim; c++)
		// {
		// // get column minimum cost
		// double min = costs[0][c];
		// for (int r = 1; r < dim; r++)
		// {
		// final double v = costs[r][c];
		//
		// if (v < min)
		// min = v;
		// }
		//
		// // subtract it to all entries
		// for (int r = 0; r < dim; r++)
		// costs[r][c] -= min;
		// }
	}

	// update starring
	private void updateStar()
	{
		Arrays.fill( rowsStar, -1 );
		Arrays.fill( colsStar, -1 );

		for ( int r = 0; r < numRow; r++ )
			updateRowStar( r );

		step = 3;
	}

	private void updateRowStar( final int r )
	{
		final double[] rowCosts = costs[ r ];

		for ( int c = 0; c < numCol; c++ )
		{
			if ( colsStar[ c ] == -1 )
			{
				if ( rowCosts[ c ] == 0 )
				{
					rowsStar[ r ] = c;
					colsStar[ c ] = r;
					return;
				}
			}
		}
	}

	// cover column with contains star
	private void doColCover()
	{
		Arrays.fill( colsCovered, false );

		int numColCovered = 0;
		for ( int c = 0; c < numCol; c++ )
		{
			if ( colsStar[ c ] != -1 )
			{
				colsCovered[ c ] = true;
				numColCovered++;
			}
		}

		if ( numColCovered == k )
			done = true;
		else
			step = 4;
	}

	int prim = 0;

	// prime uncovered zero
	private void doPrime()
	{
//        System.out.println("prim " + prim++);

		for ( int c = 0; c < numCol; c++ )
			if ( !colsCovered[ c ] )
				if ( doPrimCol( c ) )
					return;

		step = 6;
	}

	private boolean doPrimCol( final int c )
	{
		for ( int r = 0; r < numRow; r++ )
		{
			if ( !rowsCovered[ r ] )
			{
				// no covered zero ?
				if ( costs[ r ][ c ] == 0 )
				{
					// prime it
					rowsPrime[ r ] = c;

					// get star column for this row ?
					final int starCol = rowsStar[ r ];

					// no star on this row
					if ( starCol == -1 )
					{
						convertPrimeToStar( r, c );
						return true;
					}

					rowsCovered[ r ] = true;
					colsCovered[ starCol ] = false;

					// so we don't forget newly uncovered zeros
					if ( starCol < c )
						if ( doPrimCol( starCol ) )
							return true;
				}
			}
		}

		return false;
	}

	private void convertPrimeToStar( final int r, final int c )
	{
		int nb = 0;

		int primeCol = c;
		int starRow = colsStar[ primeCol ];

		while ( starRow != -1 )
		{
			colsUnStar[ nb ] = primeCol;
			rowsDoStar[ nb ] = starRow;
			nb++;

			primeCol = rowsPrime[ starRow ];
			starRow = colsStar[ primeCol ];
		}

		for ( int i = 0; i < nb; i++ )
		{
			final int startCol = colsUnStar[ i ];

			// unstar
			rowsStar[ colsStar[ startCol ] ] = -1;
			colsStar[ startCol ] = -1;
		}

		for ( int i = 0; i < nb; i++ )
		{
			final int primeRow = rowsDoStar[ i ];
			final int pc = rowsPrime[ primeRow ];

			// star
			colsStar[ pc ] = primeRow;
			rowsStar[ primeRow ] = pc;
		}
		// star
		colsStar[ c ] = r;
		rowsStar[ r ] = c;

		Arrays.fill( rowsPrime, -1 );
		Arrays.fill( rowsCovered, false );
		Arrays.fill( colsCovered, false );

		step = 3;
	}

	private void markRow( final int r )
	{
		if ( !rowsCovered[ r ] )
		{
			rowsCovered[ r ] = true;

			final double[] rowCosts = costs[ r ];

			for ( int c = 0; c < numCol; c++ )
				if ( rowCosts[ c ] == 0 )
					markCol( c );
		}
	}

	private void markCol( final int c )
	{
		if ( !colsCovered[ c ] )
		{
			colsCovered[ c ] = true;

			final int r = colsStar[ c ];

			if ( r != -1 )
				markRow( r );
		}
	}

	// reduce costs
	public void reduce()
	{
//        System.out.println("reduce " + red++);

		double min = Double.MAX_VALUE;

		// find minimum of uncovered elements
		for ( int r = 0; r < numRow; r++ )
		{
			if ( !rowsCovered[ r ] )
			{
				final double[] rowCosts = costs[ r ];

				for ( int c = 0; c < numCol; c++ )
				{
					if ( !colsCovered[ c ] )
					{
						final double v = rowCosts[ c ];

						if ( v < min )
							min = v;
					}
				}
			}
		}

		// subtract minimum from uncovered elements
		// and add it to elements covered 2 times
		for ( int r = 0; r < numRow; r++ )
		{
			final double[] rowCosts = costs[ r ];

			if ( rowsCovered[ r ] )
			{
				for ( int c = 0; c < numCol; c++ )
					if ( colsCovered[ c ] )
						rowCosts[ c ] += min;
			}
			else
			{
				for ( int c = 0; c < numCol; c++ )
					if ( !colsCovered[ c ] )
						rowCosts[ c ] -= min;
			}
		}

		step = 4;
	}

	int red = 0;

	// reduce costs
	public void reduce2()
	{
//        System.out.println("reduce " + red++);

		double min = Double.MAX_VALUE;

		// find minimum of uncovered elements
		for ( int r = 0; r < numRow; r++ )
		{
			if ( !rowsCovered[ r ] )
			{
				final double[] rowCosts = costs[ r ];

				for ( int c = 0; c < numCol; c++ )
				{
					if ( !colsCovered[ c ] )
					{
						final double v = rowCosts[ c ];

						if ( v < min )
							min = v;
					}
				}
			}
		}

		// subtract minimum from uncovered elements
		// and add it to elements covered 2 times
		for ( int r = 0; r < numRow; r++ )
		{
			if ( !rowsCovered[ r ] )
			{
				final double[] rowCosts = costs[ r ];

				for ( int c = 0; c < numCol; c++ )
					if ( !colsCovered[ c ] )
						rowCosts[ c ] -= min;
			}
			else
			{
				final double[] rowCosts = costs[ r ];

				for ( int c = 0; c < numCol; c++ )
					if ( colsCovered[ c ] )
						rowCosts[ c ] += min;
			}
		}
	}
}
