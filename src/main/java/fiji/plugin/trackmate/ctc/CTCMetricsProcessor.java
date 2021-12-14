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
		return new CTCMetrics( segValue, traValue, detValue, ctValue, tfValue, bciValue, ccaValue );
	}

	public static class CTCMetrics
	{

		public final double seg;

		public final double tra;

		public final double det;

		public final double ct;

		public final double tf;

		public final double bci;

		public final double cca;

		private CTCMetrics(
				final double seg,
				final double tra,
				final double det,
				final double ct,
				final double tf,
				final double bci,
				final double cca )
		{
			this.seg = seg;
			this.tra = tra;
			this.det = det;
			this.ct = ct;
			this.tf = tf;
			this.bci = bci;
			this.cca = cca;
		}

		@Override
		public String toString()
		{
			return "SEG: " + seg
					+ "\nTRA: " + tra
					+ "\nDET: " + det
					+ "\nCT: " + ct
					+ "\nTF: " + tf
					+ "\nCCA: " + cca
					+ "\nBCi: " + bci;
		}

		/**
		 * Prepend the specified header with the CTC metrics header.
		 * 
		 * @param header
		 *            the header to preprint.
		 * @return a new String array.
		 */
		public static String[] concatWithCSVHeader( final String[] header )
		{
			final String[] out = new String[ header.length + 10 ];
			out[ 0 ] = "SEG";
			out[ 1 ] = "TRA";
			out[ 2 ] = "DET";
			out[ 3 ] = "CT";
			out[ 4 ] = "TF";
			out[ 5 ] = "CCA";
			out[ 6 ] = "BC";
			out[ 7 ] = "TIM";
			out[ 8 ] = "DETECTION_TIME";
			out[ 9 ] = "TRACKING_TIME";
			for ( int i = 0; i < header.length; i++ )
				out[ 10 + i ] = header[ i ];

			return out;
		}

		public String[] concatWithCSVLine( final String[] content, final double detectionTime, final double trackingTime )
		{
			final String[] out = new String[ content.length + 10 ];
			out[ 0 ] = "" + seg;
			out[ 1 ] = "" + tra;
			out[ 2 ] = "" + det;
			out[ 3 ] = "" + ct;
			out[ 4 ] = "" + tf;
			out[ 5 ] = "" + cca;
			out[ 6 ] = "" + bci;
			out[ 7 ] = "" + ( detectionTime + trackingTime );
			out[ 8 ] = "" + detectionTime;
			out[ 9 ] = "" + trackingTime;
			for ( int i = 0; i < content.length; i++ )
				out[ 10 + i ] = content[ i ];

			return out;
		}
	}
}
