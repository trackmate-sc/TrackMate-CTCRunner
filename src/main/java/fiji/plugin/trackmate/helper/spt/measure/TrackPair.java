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
