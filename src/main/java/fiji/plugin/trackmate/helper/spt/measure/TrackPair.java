/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2024 TrackMate developers.
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

/**
 * A pair of tracks. Includes a reference and a candidate track
 * 
 * @version February 3, 2012
 * 
 * @author Nicolas Chenouard
 *
 */
public class TrackPair
{
	
	TrackSegment referenceTrack;

	TrackSegment candidateTrack;

	double distance;

	int firstMatchingTime;

	int lastMatchingTime;

	int candidateIndex;

	int referenceIndex;

	/**
	 * Build the pair.
	 * 
	 * @param refTrack
	 *            the reference track
	 * @param cTrack
	 *            the candidate track
	 * @param dist
	 *            the distance between the two tracks
	 * @param firstMatchingTime
	 *            the first time point at which the two tracks match (distance
	 *            inferior to a gate)
	 * @param lastMatchingTime
	 *            the last time point at which the two tracks match (distance
	 *            inferior to a gate)
	 */
	public TrackPair( final TrackSegment refTrack, final TrackSegment cTrack, final double dist, final int firstMatchingTime, final int lastMatchingTime )
	{
		this.referenceTrack = refTrack;
		this.candidateTrack = cTrack;
		this.distance = dist;
		this.firstMatchingTime = firstMatchingTime;
		this.lastMatchingTime = lastMatchingTime;
	}
}
