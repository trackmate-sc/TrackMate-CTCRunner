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
 * Utility for computing the distance between two tracks
 * 
 * @version February 3, 2012
 * 
 * @author Nicolas Chenouard
 *
 */
public class TrackToTrackDistance
{
	
	double distance;

	boolean isMatching = false;

	int firstMatchingTime = -1;

	int lastMatchingTime = -1;

	int numMatchingDetections = 0;

	int numNonMatchedDetections = 0;

	int numWrongDetections = 0;

	double minDetectionDistance = Double.MAX_VALUE;

	double maxDetectionDistance = 0;

	double sumSquareDetectionDistance = 0;

	double sumDetectionDistance = 0;

	/**
	 * Compute the distance between two tracks
	 * 
	 * @param ts1
	 *            the first track
	 * @param ts2
	 *            the track with which to compare the first track
	 * @param distanceType
	 *            the type of distance between detections that is used for the
	 *            computation
	 * @param maxDist
	 *            the gate that is used for computing the distance between
	 *            detections
	 */
	public TrackToTrackDistance( final TrackSegment ts1, final TrackSegment ts2, final DistanceTypes distanceType, final double maxDist )
	{
		if ( ts2 == null || ts2.getDetectionList().isEmpty() )
		{
			isMatching = false;
			switch ( distanceType )
			{
			case DISTANCE_EUCLIDIAN:
				distance = maxDist * ( ts1.getLastDetection().getT() - ts1.getFirstDetection().getT() + 1 );
				break;
			case DISTANCE_MATCHING:
				distance = ( ts1.getLastDetection().getT() - ts1.getFirstDetection().getT() + 1 );
				break;
			}
			numMatchingDetections = 0;
			numNonMatchedDetections = ( ts1.getLastDetection().getT() - ts1.getFirstDetection().getT() + 1 );
			numWrongDetections = 0;
			return;
		}
		final int t0_1 = ts1.getFirstDetection().getT();
		final int tend_1 = ts1.getLastDetection().getT();
		final int t0_2 = ts2.getFirstDetection().getT();
		final int tend_2 = ts2.getLastDetection().getT();

		// test if there is an intersection between segments
		if ( ( t0_2 >= t0_1 && t0_2 <= tend_1 ) || ( tend_2 >= t0_1 && tend_2 <= tend_1 ) || ( t0_2 <= t0_1 && tend_2 >= tend_1 ) )
		{
			numWrongDetections += Math.max( 0, t0_1 - t0_2 );
			numWrongDetections += Math.max( 0, tend_2 - tend_1 );

			numNonMatchedDetections += Math.max( 0, t0_2 - t0_1 );
			numNonMatchedDetections += Math.max( 0, tend_1 - tend_2 );

			final int firstT = Math.max( t0_1, t0_2 );
			final int endT = Math.min( tend_1, tend_2 );
			switch ( distanceType )
			{
			case DISTANCE_EUCLIDIAN:
			{
				distance = maxDist * ( Math.abs( t0_2 - t0_1 ) + Math.abs( tend_2 - tend_1 ) );
				for ( int t = firstT; t <= endT; t++ )
				{
					final Detection d1 = ts1.getDetectionAtTime( t );
					final Detection d2 = ts2.getDetectionAtTime( t );
					final double ed = Math.sqrt( ( d1.getX() - d2.getX() ) * ( d1.getX() - d2.getX() ) + ( d1.getY() - d2.getY() ) * ( d1.getY() - d2.getY() ) + ( d1.getZ() - d2.getZ() ) * ( d1.getZ() - d2.getZ() ) );
					if ( d2.getDetectionType() == Detection.DETECTIONTYPE_REAL_DETECTION && ed < maxDist )
					{
						if ( !isMatching )
						{
							firstMatchingTime = t;
							isMatching = true;
						}
						lastMatchingTime = t;
						distance += ed;
						numMatchingDetections++;
						// distance between detections
						sumDetectionDistance += ed;
						sumSquareDetectionDistance += ( ed * ed );
						if ( ed < minDetectionDistance )
							minDetectionDistance = ed;
						else if ( ed > maxDetectionDistance )
							maxDetectionDistance = ed;
					}
					else
					{
						// virtual detections are not considered as spurious
						// detections
						if ( d2.getDetectionType() == Detection.DETECTIONTYPE_REAL_DETECTION )
							numWrongDetections++;
						numNonMatchedDetections++;
						distance += maxDist;
					}
				}
				break;
			}
			case DISTANCE_MATCHING:
			{
				boolean matching = false;
				distance = ( Math.abs( t0_2 - t0_1 ) + Math.abs( tend_2 - tend_1 ) );
				for ( int t = firstT; t <= endT; t++ )
				{
					final Detection d1 = ts1.getDetectionAtTime( t );
					final Detection d2 = ts2.getDetectionAtTime( t );
					final double ed = Math.sqrt( ( d1.getX() - d2.getX() ) * ( d1.getX() - d2.getX() ) + ( d1.getY() - d2.getY() ) * ( d1.getY() - d2.getY() ) + ( d1.getZ() - d2.getZ() ) * ( d1.getZ() - d2.getZ() ) );
					if ( d2.getDetectionType() == Detection.DETECTIONTYPE_REAL_DETECTION && ed < maxDist )
					{
						if ( !matching )
						{
							firstMatchingTime = t;
							matching = true;
						}
						lastMatchingTime = t;
						// not penalty if matching
						numMatchingDetections++;
						// distance between detections
						sumDetectionDistance += ed;
						sumSquareDetectionDistance += ( ed * ed );
						if ( ed < minDetectionDistance )
							minDetectionDistance = ed;
						else if ( ed > maxDetectionDistance )
							maxDetectionDistance = ed;
					}
					else
					{
						distance++;
						// virtual detections are not considered as spurious
						// detections
						if ( d2.getDetectionType() == Detection.DETECTIONTYPE_REAL_DETECTION )
							numWrongDetections++;
						numNonMatchedDetections++;
					}
				}
				break;
			}
			}
		}
		else
		{
			numMatchingDetections = 0;
			numWrongDetections += ( ts2.getLastDetection().getT() - ts2.getFirstDetection().getT() + 1 );
			numNonMatchedDetections += ( ts1.getLastDetection().getT() - ts1.getFirstDetection().getT() + 1 );
			isMatching = false;
			switch ( distanceType )
			{
			case DISTANCE_EUCLIDIAN:
				distance = maxDist * ( ts1.getLastDetection().getT() - ts1.getFirstDetection().getT() + 1 );
				break;
			case DISTANCE_MATCHING:
				distance = ( ts1.getLastDetection().getT() - ts1.getFirstDetection().getT() + 1 );
				break;
			}
		}
	}
}
