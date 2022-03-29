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
package fiji.plugin.trackmate.performance.spt.measure;

/**
 * Creation of the best one-to-one matching between a set of reference tracks and a set of candidate
 * tracks.
 * Dummy tracks representing no candidate tracks associated to a reference track are allowed.
 * 
 * @version February 4, 2012
 * @author Nicolas Chenouard
 * 
 * Minor modifications by Jean-Yves Tinevez, 2022.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OneToOneMatcher
{

	private final List< TrackSegment > refTracks;

	private final List< TrackSegment > candidateTracks;

	private final List< List< TrackPair > > feasiblePairs;

	/**
	 * @param refTracks
	 *            the set of reference tracks
	 * @param candidateTracks
	 *            the set of candidate tracks
	 */
	public OneToOneMatcher( final List< TrackSegment > refTracks, final List< TrackSegment > candidateTracks )
	{
		this.refTracks = new ArrayList< TrackSegment >();
		this.refTracks.addAll( refTracks );

		this.candidateTracks = new ArrayList< TrackSegment >();
		this.candidateTracks.addAll( candidateTracks );

		this.feasiblePairs = new ArrayList< List< TrackPair > >();
	}

	/**
	 * Compute the best pairing
	 * 
	 * @param maxDist
	 *            maximum Euclidian distance between two detections (gate)
	 * @param distType
	 *            type of distance that is used for computing the costs of
	 *            association
	 * @param useNewMethod
	 *            use new method (should be faster).
	 * @return the best pairing between tracks. All the reference tracks are
	 *         paired (potentially to a dummy track), while some candidate
	 *         tracks may not be in the list
	 * @throws Exception
	 */
	public List< TrackPair > pairTracks( final double maxDist, final DistanceTypes distType, final boolean useNewMethod ) throws Exception
	{
		// build the potential track pairs
		this.feasiblePairs.clear();
		for ( final TrackSegment ts : refTracks )
			this.feasiblePairs.add( getFeasiblePairs( ts, candidateTracks, distType, maxDist ) );

		// cluster track pairs
		final List< TrackPairsCluster > clusters = new ArrayList< OneToOneMatcher.TrackPairsCluster >();
		try
		{
			clusters.addAll( getTrackPairClusters() );
		}
		catch ( final Exception e )
		{
			throw e;
		}

		final List< TrackPair > assignment = new ArrayList< TrackPair >();
		for ( final TrackPairsCluster cluster : clusters )
		{
			cluster.buildCostMatrix();
			// use Munkres algorithm to find the best pairing
			try
			{
				boolean[][] matching;
				if ( useNewMethod )
				{
					final HungarianMatchingNew matcher = new HungarianMatchingNew( cluster.costs );
					matching = matcher.compute();
				}
				else
				{
					final HungarianMatching matcher = new HungarianMatching( cluster.costs );
					matching = matcher.optimize();
				}
				assignment.addAll( cluster.getAssignements( matching ) );
			}
			catch ( final Exception e )
			{
				throw e;
			}
		}
		return assignment;
	}

	public List< TrackPair > pairTracks( final double maxDist, final DistanceTypes distType ) throws Exception
	{
		return pairTracks( maxDist, distType, false );
	}

	/**
	 * Build the clusters of TrackPair objects
	 * 
	 * @return list of TrackPairsCluster for the current set of TrackPairs
	 */
	private List< TrackPairsCluster > getTrackPairClusters() throws Exception
	{
		List< TrackPairsCluster > clusters = new ArrayList< TrackPairsCluster >();
		for ( final List< TrackPair > trackPairsList : feasiblePairs )
		{
			if ( !trackPairsList.isEmpty() )
			{
				/*
				 * create a cluster with tracks corresponding to the current
				 * reference track
				 */
				final TrackPairsCluster currentCluster = new TrackPairsCluster();
				final TrackSegment refTrack = trackPairsList.get( 0 ).referenceTrack;
				currentCluster.usedReferenceTracks.add( refTrack );
				for ( final TrackPair tp : trackPairsList )
					currentCluster.usedCandidateTracks.add( tp.candidateTrack );
				currentCluster.trackPairs.addAll( trackPairsList );
				// now try to merge this cluster with others
				final ArrayList< TrackPairsCluster > clustersCopy = new ArrayList< TrackPairsCluster >();
				clustersCopy.add( currentCluster );
				for ( final TrackPairsCluster cluster : clusters )
				{
					/*
					 * check if cluster contains tracks that are used by the
					 * current cluster we do not check for the reference track
					 * as it should not be used elsewhere
					 */
					boolean found = false;
					for ( final TrackPair tp : trackPairsList )
					{
						found = cluster.usedCandidateTracks.contains( tp.candidateTrack );
						if ( found )
							break;
					}
					if ( found )
					{
						// merge clusters
						currentCluster.mergeCluster( cluster );
					}
					else
					{
						// keep the investigate cluster intact
						clustersCopy.add( cluster );
					}
				}
				clusters = clustersCopy;
			}
			else
			{
				throw new Exception( "There is a track cluster empty" );
			}
		}
		return clusters;
	}

	/**
	 * Compute the set of feasible pairs between a reference track and candidate
	 * and dummy tracks. A pair is not feasible if it does not bring improvement
	 * over the association of the reference track with a dummy track.
	 * 
	 * @param ts
	 *            the reference TrackSegment object
	 * @param tracks2
	 *            the set of candidate tracks
	 * @param distType
	 *            type of distance that is used for computing the costs of
	 *            association
	 * @param maxDist
	 *            the gate (maximum Euclidian distance) for distance computation
	 */
	private List< TrackPair > getFeasiblePairs(
			final TrackSegment ts,
			final List< TrackSegment > tracks2,
			final DistanceTypes distType,
			final double maxDist )
	{
		final ArrayList< TrackPair > feasiblePairs = new ArrayList< TrackPair >();
		for ( final TrackSegment ts2 : tracks2 )
		{
			final TrackToTrackDistance distance = new TrackToTrackDistance( ts, ts2, distType, maxDist );
			if ( distance.isMatching )
			{
				final TrackPair pair = new TrackPair(
						ts,
						ts2,
						distance.distance,
						distance.firstMatchingTime,
						distance.lastMatchingTime );
				feasiblePairs.add( pair );
			}
		}
		// add a dummy track for representing no association
		final TrackSegment dummyTrack = new TrackSegment();
		final TrackToTrackDistance distance = new TrackToTrackDistance( ts, dummyTrack, distType, maxDist );
		final TrackPair pair = new TrackPair(
				ts,
				new TrackSegment(),
				distance.distance,
				distance.firstMatchingTime,
				distance.lastMatchingTime );
		feasiblePairs.add( pair );
		return feasiblePairs;
	}

	/**
	 * cluster of TrackPair objects that share common tracks
	 */
	private class TrackPairsCluster
	{
		private final Set< TrackSegment > usedReferenceTracks = new HashSet< TrackSegment >();

		private final Set< TrackSegment > usedCandidateTracks = new HashSet< TrackSegment >();

		private final List< TrackPair > trackPairs = new ArrayList< TrackPair >();

		private double[][] costs;

		private List< TrackSegment > candidateTrackList;

		private List< TrackSegment > referenceTrackList;

		public void mergeCluster( final TrackPairsCluster toMerge )
		{
			this.usedCandidateTracks.addAll( toMerge.usedCandidateTracks );
			this.usedReferenceTracks.addAll( toMerge.usedReferenceTracks );
			this.trackPairs.addAll( toMerge.trackPairs );
		}

		/**
		 * Compute the cost matrix for the assignment between reference and
		 * candidate tracks of a TrackPairsCluster object
		 */
		private void buildCostMatrix()
		{
			double maxDist = 0;
			for ( final TrackPair tp : this.trackPairs )
				maxDist = Math.max( maxDist, tp.distance );

			candidateTrackList = new ArrayList< TrackSegment >();
			candidateTrackList.addAll( usedCandidateTracks );
			referenceTrackList = new ArrayList< TrackSegment >();
			referenceTrackList.addAll( usedReferenceTracks );
			costs = new double[ referenceTrackList.size() ][ candidateTrackList.size() ];

			// fill costs
			for ( int i = 0; i < costs.length; i++ )
				for ( int j = 0; j < costs[ i ].length; j++ )
					costs[ i ][ j ] = maxDist + 1;

			for ( final TrackPair tp : this.trackPairs )
			{
				tp.referenceIndex = referenceTrackList.indexOf( tp.referenceTrack );
				tp.candidateIndex = candidateTrackList.indexOf( tp.candidateTrack );
				costs[ tp.referenceIndex ][ tp.candidateIndex ] = tp.distance;
			}
		}

		/**
		 * Build the list of track pairs that corresponds to a given matching
		 * matrix
		 * 
		 * @param matching
		 *            a boolean matrix indicating the association between
		 *            reference tracks (rows) and candidate tracks (columns)
		 * @return the list of TrackPair objects that correspond to the matching
		 */
		private List< TrackPair > getAssignements( final boolean[][] matching ) throws Exception
		{
			final List< TrackPair > assignment = new ArrayList< TrackPair >();
			for ( int referenceIndex = 0; referenceIndex < referenceTrackList.size(); referenceIndex++ )
			{
				boolean found = false;
				int candidateIndex = -1;
				for ( int j = 0; j < matching[ referenceIndex ].length; j++ )
				{
					if ( matching[ referenceIndex ][ j ] )
					{
						found = true;
						candidateIndex = j;
						break;
					}
				}
				if ( !found )
					throw new Exception( "No match found when building assignment" );
				found = false;
				for ( final TrackPair tp : trackPairs )
				{
					if ( tp.candidateIndex == candidateIndex && tp.referenceIndex == referenceIndex )
					{
						assignment.add( tp );
						found = true;
						break;
					}
				}
				if ( !found )
					throw new Exception( "Track pair not found when building assignment" );
			}
			return assignment;
		}
	}
}
