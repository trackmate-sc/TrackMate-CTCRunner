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
