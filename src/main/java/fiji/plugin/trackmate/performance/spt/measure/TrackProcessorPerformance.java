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

import java.util.ArrayList;
import java.util.List;

/**
 * Main class for the track processor that computes tracking quality with
 * respect to a reference set of tracks.
 * 
 * @version February 3, 2012
 * @author Nicolas Chenouard
 */

public class TrackProcessorPerformance
{

	private final ArrayList< TrackPair > trackPairs = new ArrayList< TrackPair >();

	private final ArrayList< TrackSegment > recoveredTracks = new ArrayList< TrackSegment >();

	private final ArrayList< TrackSegment > correctTracks = new ArrayList< TrackSegment >();

	private final ArrayList< TrackSegment > missedTracks = new ArrayList< TrackSegment >();

	private final ArrayList< TrackSegment > spuriousTracks = new ArrayList< TrackSegment >();

	public void setTrackGroups( final TrackGroup refTG, final TrackGroup candidateTG )
	{
		trackPairs.clear();
		recoveredTracks.clear();
		missedTracks.clear();
		spuriousTracks.clear();
		correctTracks.clear();
	}

	public PerformanceAnalyzer pairTracks(
			final List< TrackSegment > trackSegmentList1,
			final List< TrackSegment > trackSegmentList2,
			final double maxDist )
	{
		final boolean newMethod = true;
		final OneToOneMatcher matcher = new OneToOneMatcher( trackSegmentList1, trackSegmentList2 );
		final DistanceTypes distType = DistanceTypes.DISTANCE_EUCLIDIAN;
		if ( maxDist < 0 )
			return null;
		final ArrayList< TrackPair > pairs = new ArrayList< TrackPair >();
		try
		{
			pairs.addAll( matcher.pairTracks( maxDist, distType, newMethod ) );
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
			pairs.clear();
		}

		// debug1.displayMs();

		// remove spurious candidate tracks
		recoveredTracks.clear();
		correctTracks.clear();
		missedTracks.clear();
		spuriousTracks.clear();

		// Chronometer debug2 = new Chronometer("Second score loop (pairing)");

		for ( final TrackPair tp : pairs )
		{
			if ( tp.candidateTrack.getDetectionList().isEmpty() )
			{
				tp.candidateTrack = null;
				missedTracks.add( tp.referenceTrack );
			}
			else
			{
				recoveredTracks.add( tp.referenceTrack );
				correctTracks.add( tp.candidateTrack );
			}
		}
		for ( final TrackSegment ts : trackSegmentList2 )
		{
			if ( !correctTracks.contains( ts ) )
				spuriousTracks.add( ts );
		}

		// debug2.displayMs();

		trackPairs.clear();
		trackPairs.addAll( pairs );

		// Chronometer debug3 = new Chronometer("Perf Analyzer");
		final PerformanceAnalyzer analyzer = new PerformanceAnalyzer( trackSegmentList1, trackSegmentList2, trackPairs );
		// debug3.displayMs();

		return analyzer;
	}

	public static ArrayList< TrackPair > pairTracks( final ArrayList< TrackSegment > trackSegmentList1,
			final ArrayList< TrackSegment > trackSegmentList2, final DistanceTypes distType, final double maxDist ) throws Exception
	{
		if ( maxDist < 0 )
			throw new IllegalArgumentException( "Maximum distance needs to be a positive value" );
		final OneToOneMatcher matcher = new OneToOneMatcher( trackSegmentList1, trackSegmentList2 );
		final ArrayList< TrackPair > pairs = new ArrayList< TrackPair >();
		pairs.addAll( matcher.pairTracks( maxDist, distType ) );
		return pairs;
	}
}
