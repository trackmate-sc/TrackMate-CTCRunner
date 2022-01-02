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
		final double segValue = seg.calculate( groundTruthPath, resultsFolder );
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
