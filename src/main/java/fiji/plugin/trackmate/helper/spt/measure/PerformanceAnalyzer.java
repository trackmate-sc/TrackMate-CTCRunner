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
package fiji.plugin.trackmate.helper.spt.measure;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities to compute several tracking performance criteria for a given
 * pairing between a reference and candidate set of tracks
 *
 * @version February 3, 2012
 * 
 * @author Nicolas Chenouard
 *
 */

public class PerformanceAnalyzer
{

	private final List< TrackSegment > referenceTracks;

	private final List< TrackSegment > candidateTracks;

	private final List< TrackPair > trackPairs;

	/**
	 * Build the analyzer
	 * 
	 * @param referenceTracks
	 *            the set of reference tracks
	 * @param candidateTracks
	 *            the set of candidate tracks
	 * @param trackPairs
	 *            the pairing between the set of tracks. Each track in the
	 *            reference set has to be represented.
	 */
	public PerformanceAnalyzer(
			final List< TrackSegment > referenceTracks,
			final List< TrackSegment > candidateTracks,
			final List< TrackPair > trackPairs )
	{
		this.referenceTracks = new ArrayList< TrackSegment >();
		this.referenceTracks.addAll( referenceTracks );
		this.candidateTracks = new ArrayList< TrackSegment >();
		this.candidateTracks.addAll( candidateTracks );
		this.trackPairs = new ArrayList< TrackPair >();
		this.trackPairs.addAll( trackPairs );
	}

	/**
	 * @return the number of reference tracks
	 */
	public int getNumRefTracks()
	{
		return referenceTracks.size();
	}

	/**
	 * @return the total number of detection for reference tracks
	 */
	public int getNumRefDetections()
	{
		int numDetections = 0;
		for ( final TrackSegment ts : referenceTracks )
			numDetections += ( ts.getLastDetection().getT() - ts.getFirstDetection().getT() + 1 );
		return numDetections;
	}

	/**
	 * @return the number of candidate tracks
	 */
	public int getNumCandidateTracks()
	{
		return candidateTracks.size();
	}

	/**
	 * @return the total number of detection for candidate tracks
	 */
	public int getNumCandidateDetections()
	{
		int numDetections = 0;
		for ( final TrackSegment ts : candidateTracks )
			numDetections += ( ts.getLastDetection().getT() - ts.getFirstDetection().getT() + 1 );

		return numDetections;
	}

	/**
	 * @return the distance between the pairs
	 */
	public double getPairedTracksDistance( final DistanceTypes distType, final double maxDist )
	{
		double distance = 0;
		for ( final TrackPair tp : trackPairs )
		{
			final TrackToTrackDistance d = new TrackToTrackDistance(
					tp.referenceTrack,
					tp.candidateTrack,
					distType,
					maxDist );
			distance += d.distance;
		}
		return distance;
	}

	/**
	 * @return the normalized distance between the pairs (alpha criterion)
	 */
	public double getPairedTracksNormalizedDistance( final DistanceTypes distType, final double maxDist )
	{
		double distance = 0;
		for ( final TrackPair tp : trackPairs )
		{
			final TrackToTrackDistance d = new TrackToTrackDistance( tp.referenceTrack, tp.candidateTrack, distType, maxDist );
			distance += d.distance;
		}
		/*
		 * divide now by the maximum distance that corresponds to reference
		 * tracks with no associated tracks
		 */
		double normalization = 0;
		for ( final TrackSegment ts : referenceTracks )
		{
			final TrackToTrackDistance d = new TrackToTrackDistance( ts, null, distType, maxDist );
			normalization += d.distance;
		}
		return 1d - distance / normalization;
	}

	/**
	 * @return the full distance between the pairs (beta criterion) that
	 *         accounts for non-associated candidate tracks
	 */
	public double getFullTrackingScore( final DistanceTypes distType, final double maxDist )
	{
		double distance = 0;
		for ( final TrackPair tp : trackPairs )
		{
			final TrackToTrackDistance d = new TrackToTrackDistance( tp.referenceTrack, tp.candidateTrack, distType, maxDist );
			distance += d.distance;
		}
		// compute the bound on the distance
		double bound = 0;
		for ( final TrackSegment ts : referenceTracks )
		{
			final TrackToTrackDistance d = new TrackToTrackDistance( ts, null, distType, maxDist );
			bound += d.distance;
		}
		// compute the penalty for wrong tracks
		double penalty = 0;
		for ( final TrackSegment ts : candidateTracks )
		{
			boolean found = false;
			for ( final TrackPair tp : trackPairs )
			{
				if ( tp.candidateTrack == ts )
				{
					found = true;
					break;
				}
			}
			if ( !found )
			{
				final TrackToTrackDistance d = new TrackToTrackDistance( ts, null, distType, maxDist );
				penalty += d.distance;
			}
		}
		return ( bound - distance ) / ( bound + penalty );
	}

	/**
	 * @return the number of non-associated candidate tracks
	 */
	public int getNumSpuriousTracks()
	{
		int numSpuriousTracks = 0;
		for ( final TrackSegment ts : candidateTracks )
		{
			boolean found = false;
			for ( final TrackPair tp : trackPairs )
			{
				if ( tp.candidateTrack == ts )
				{
					found = true;
					break;
				}
			}
			if ( !found )
				numSpuriousTracks++;
		}
		return numSpuriousTracks;
	}

	/**
	 * @return the number of non-associated reference tracks (or associated with
	 *         a dummy track)
	 */
	public int getNumMissedTracks()
	{
		int numMissedTrack = 0;
		for ( final TrackSegment ts : referenceTracks )
		{
			boolean found = false;
			for ( final TrackPair tp : trackPairs )
			{
				if ( tp.referenceTrack == ts )
				{
					if ( tp.candidateTrack != null && !tp.candidateTrack.getDetectionList().isEmpty() )
						found = true;
					break;
				}
			}
			if ( !found )
				numMissedTrack++;
		}
		return numMissedTrack;
	}

	/**
	 * @return the number of pairs between reference and candidate tracks
	 */
	public int getNumPairedTracks()
	{
		int numCorrectTracks = 0;
		for ( final TrackSegment ts : candidateTracks )
		{
			boolean found = false;
			for ( final TrackPair tp : trackPairs )
			{
				if ( tp.candidateTrack == ts )
				{
					found = true;
					break;
				}
			}
			if ( found )
				numCorrectTracks++;
		}
		return numCorrectTracks;
	}

	/**
	 * @return the total number of paired detections
	 */
	public int getNumPairedDetections( final double maxDist )
	{
		int numRecoveredDetections = 0;
		for ( final TrackPair tp : trackPairs )
		{
			final TrackToTrackDistance d = new TrackToTrackDistance( tp.referenceTrack, tp.candidateTrack, DistanceTypes.DISTANCE_MATCHING, maxDist );
			numRecoveredDetections += d.numMatchingDetections;
		}
		return numRecoveredDetections;
	}

	/**
	 * @return the number of detections for the reference tracks that are not
	 *         paired to a candidate detection
	 */
	public int getNumMissedDetections( final double maxDist )
	{
		int numMissedDetections = 0;
		for ( final TrackPair tp : trackPairs )
		{
			final TrackToTrackDistance d = new TrackToTrackDistance( tp.referenceTrack, tp.candidateTrack, DistanceTypes.DISTANCE_MATCHING, maxDist );
			numMissedDetections += d.numNonMatchedDetections;
		}
		return numMissedDetections;
	}

	/**
	 * @return the number of detections for the candidate tracks that are not
	 *         paired to a reference detection
	 */
	public int getNumWrongDetections( final double maxDist )
	{
		int numSpuriousDetections = 0;
		for ( final TrackSegment ts : candidateTracks )
		{
			boolean found = false;
			for ( final TrackPair tp : trackPairs )
			{
				if ( tp.candidateTrack == ts )
				{
					final TrackToTrackDistance d = new TrackToTrackDistance( tp.referenceTrack, tp.candidateTrack, DistanceTypes.DISTANCE_MATCHING, maxDist );
					numSpuriousDetections += d.numWrongDetections;
					found = true;
					break;
				}
			}
			if ( !found )
			{
				for ( final Detection d : ts.getDetectionList() )
					if ( d.getDetectionType() == Detection.DETECTIONTYPE_REAL_DETECTION )
						numSpuriousDetections++;
				// Virtual detections are not considered as spurious detections.
			}
		}
		return numSpuriousDetections;
	}

	public List< Double > getDistanceDetectionList( final double maxDist )
	{
		final List< Double > distanceList = new ArrayList< Double >();
		for ( final TrackPair tp : trackPairs )
			if ( tp.candidateTrack != null && !tp.candidateTrack.getDetectionList().isEmpty() )
				distanceList.addAll( getDetectionEuclidianDistances( tp.referenceTrack, tp.candidateTrack ) );

		return distanceList;
	}

	public double[] getDistanceDetectionData( final double maxDist )
	{
		double sumDistance = 0;
		double sumSquareDistance = 0;
		double minDistance = Double.MAX_VALUE;
		double maxDistance = 0;
		int numDetections = 0;
		for ( final TrackPair tp : trackPairs )
		{
			if ( tp.candidateTrack != null && !tp.candidateTrack.getDetectionList().isEmpty() )
			{
				final TrackToTrackDistance d = new TrackToTrackDistance( tp.referenceTrack, tp.candidateTrack, DistanceTypes.DISTANCE_MATCHING, maxDist );
				sumDistance += d.sumDetectionDistance;
				sumSquareDistance += d.sumSquareDetectionDistance;
				if ( d.minDetectionDistance < minDistance )
					minDistance = d.minDetectionDistance;
				if ( d.maxDetectionDistance > maxDistance )
					maxDistance = d.maxDetectionDistance;
				numDetections += d.numMatchingDetections;
			}
		}
		if ( numDetections == 0 )
			return new double[] { 0, 0, 0, 0 };
		else
		{
			final double rmse = Math.sqrt( sumSquareDistance / numDetections );
			final double stdDistance = Math.sqrt( sumSquareDistance / numDetections - Math.pow( sumDistance / numDetections, 2 ) );
			return new double[] { rmse, minDistance, maxDistance, stdDistance };
		}
	}

	public List< Double > getAllPairsDetectionEuclidianDistances()
	{
		final ArrayList< Double > distanceList = new ArrayList< Double >();
		for ( final TrackPair tp : trackPairs )
			distanceList.addAll( getDetectionEuclidianDistances( tp.referenceTrack, tp.candidateTrack ) );

		return distanceList;
	}

	protected List< Double > getDetectionEuclidianDistances( final TrackSegment ts1, final TrackSegment ts2 )
	{
		final ArrayList< Double > distanceList = new ArrayList< Double >();
		if ( ts2 == null || ts2.getDetectionList().isEmpty() )
			return new ArrayList< Double >();
		final int t0_1 = ts1.getFirstDetection().getT();
		final int tend_1 = ts1.getLastDetection().getT();
		final int t0_2 = ts2.getFirstDetection().getT();
		final int tend_2 = ts2.getLastDetection().getT();
		// test if there is an intersection between segments
		if ( ( t0_2 >= t0_1 && t0_2 <= tend_1 ) || ( tend_2 >= t0_1 && tend_2 <= tend_1 ) || ( t0_2 <= t0_1 && tend_2 >= tend_1 ) )
		{
			final int firstT = Math.max( t0_1, t0_2 );
			final int endT = Math.min( tend_1, tend_2 );
			for ( int t = firstT; t <= endT; t++ )
			{
				final Detection d1 = ts1.getDetectionAtTime( t );
				final Detection d2 = ts2.getDetectionAtTime( t );
				distanceList.add( Double.valueOf( Math.sqrt( ( d1.getX() - d2.getX() ) * ( d1.getX() - d2.getX() )
						+ ( d1.getY() - d2.getY() ) * ( d1.getY() - d2.getY() )
						+ ( d1.getZ() - d2.getZ() ) * ( d1.getZ() - d2.getZ() ) ) ) );
			}
		}
		return distanceList;
	}

	public List< Double > getReferenceTracksJumpLengthList()
	{
		final ArrayList< Double > lengthList = new ArrayList< Double >();
		for ( final TrackSegment ts : referenceTracks )
			lengthList.addAll( getJumpLengthList( ts ) );
		return lengthList;
	}

	public List< Double > getCandidateTracksJumpLengthList()
	{
		final ArrayList< Double > lengthList = new ArrayList< Double >();
		for ( final TrackSegment ts : candidateTracks )
			lengthList.addAll( getJumpLengthList( ts ) );
		return lengthList;
	}

	protected List< Double > getJumpLengthList( final TrackSegment ts1 )
	{
		final List< Double > lengthList = new ArrayList< Double >();
		final int firstT = ts1.getFirstDetection().getT();
		final int lastT = ts1.getLastDetection().getT();

		for ( int t = firstT; t < lastT; t++ )
		{
			final Detection d1 = ts1.getDetectionAtTime( t );
			if ( d1 != null )
			{
				final Detection d2 = ts1.getDetectionAtTime( t + 1 );
				if ( d2 != null )
					lengthList.add( Double.valueOf( Math.sqrt( ( d1.getX() - d2.getX() ) * ( d1.getX() - d2.getX() )
							+ ( d1.getY() - d2.getY() ) * ( d1.getY() - d2.getY() )
							+ ( d1.getZ() - d2.getZ() ) * ( d1.getZ() - d2.getZ() ) ) ) );
			}
		}
		return lengthList;
	}

	public double[] getCandidateTracksMSDs()
	{
		return getMSDs( candidateTracks );
	}

	public double[] getReferenceTracksMSDs()
	{
		return getMSDs( referenceTracks );
	}

	protected double[] getMSDs( final List< TrackSegment > tracks )
	{
		int maxTGap = 0;
		for ( final TrackSegment ts : tracks )
		{
			final int trackLength = ( ts.getLastDetection().getT() - ts.getFirstDetection().getT() ) + 1;
			if ( trackLength - 1 > maxTGap )
				maxTGap = trackLength - 1;
		}

		final double[] msds = new double[ maxTGap ];
		final int[] numJumps = new int[ maxTGap ];

		for ( final TrackSegment ts : tracks )
		{
			for ( int tGap = 1; tGap <= maxTGap; tGap++ )
			{
				final int firstT = ts.getFirstDetection().getT();
				final int lastT = ts.getLastDetection().getT();

				for ( int t = firstT; t <= lastT - tGap; t++ )
				{
					final Detection d1 = ts.getDetectionAtTime( t );
					if ( d1 != null )
					{
						final Detection d2 = ts.getDetectionAtTime( t + tGap );
						if ( d2 != null )
						{
							msds[ tGap - 1 ] += ( d1.getX() - d2.getX() ) * ( d1.getX() - d2.getX() ) + ( d1.getY() - d2.getY() ) * ( d1.getY() - d2.getY() ) + ( d1.getZ() - d2.getZ() ) * ( d1.getZ() - d2.getZ() );
							numJumps[ tGap - 1 ] += 1;
						}
					}
				}
			}
		}
		for ( int k = 0; k < msds.length; k++ )
		{
			if ( numJumps[ k ] > 0 )
				msds[ k ] /= numJumps[ k ];
		}
		return msds;
	}
}
