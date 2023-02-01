/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
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
