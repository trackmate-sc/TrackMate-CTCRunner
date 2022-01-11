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
package fiji.plugin.trackmate.ctc;

import java.io.IOException;

import org.scijava.Context;
import org.scijava.log.LogService;

import io.scif.img.ImgIOException;
import net.celltrackingchallenge.measures.BCi;
import net.celltrackingchallenge.measures.CCA;
import net.celltrackingchallenge.measures.CT;
import net.celltrackingchallenge.measures.DET;
import net.celltrackingchallenge.measures.SEG;
import net.celltrackingchallenge.measures.TF;
import net.celltrackingchallenge.measures.TRA;
import net.celltrackingchallenge.measures.TrackDataCache;

/**
 * Performs all the CTC metrics measurements.
 * 
 * @author Jean-Yves Tinevez
 */
public class CTCMetricsProcessor
{

	private final SEG seg;

	private final TRA tra;

	private final DET det;

	private final CT ct;

	private final TF tf;

	private final BCi bci;

	private final CCA cca;

	public CTCMetricsProcessor( final Context context, final int logLevel )
	{
		// LogService
		final LogService logService = context.getService( LogService.class );
		logService.setLevel( logLevel );

		// Segmentation accuracy.
		this.seg = new SEG( logService );
		// Tracking accuracy.
		this.tra = new TRA( logService );
		tra.doLogReports = true;
		tra.doMatchingReports = true;
		// Detection quality.
		this.det = new DET( logService );
		// Complete tracks.
		this.ct = new CT( logService );
		// Track fractions.
		this.tf = new TF( logService );
		// Branching correctness.
		this.bci = new BCi( logService );
		bci.setI( 2 );
		// Cell cycle accuracy.
		this.cca = new CCA( logService );
	}

	public CTCMetrics process( final String groundTruthPath, final String resultsFolder ) throws ImgIOException, IOException
	{
		double segValue = Double.NaN;
		try
		{
			segValue = seg.calculate( groundTruthPath, resultsFolder );
		}
		catch ( final IllegalArgumentException e )
		{
			/*
			 * Could not find the source to compute SEG metrics. Never-mind,
			 * return NaN.
			 */
		}

		final double traValue = tra.calculate( groundTruthPath, resultsFolder );
		final TrackDataCache sharedCache = tra.getCache();
		final double detValue = det.calculate( groundTruthPath, resultsFolder, sharedCache );
		final double ctValue = ct.calculate( groundTruthPath, resultsFolder, sharedCache );
		final double tfValue = tf.calculate( groundTruthPath, resultsFolder, sharedCache );
		double ccaValue;
		try
		{
			ccaValue = cca.calculate( groundTruthPath, resultsFolder );
		}
		catch ( final IllegalArgumentException e )
		{
			ccaValue = Double.NaN;
		}
		final double bciValue = bci.calculate( groundTruthPath, resultsFolder, sharedCache );
		return CTCMetrics.create()
				.seg( segValue )
				.tra( traValue )
				.det( detValue )
				.ct( ctValue )
				.tf( tfValue )
				.bci( bciValue )
				.cca( ccaValue )
				.get();
	}
}
