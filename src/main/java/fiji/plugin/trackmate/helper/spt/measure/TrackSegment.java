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
/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package fiji.plugin.trackmate.helper.spt.measure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TrackSegment is a pool of consecutive detection.
 * 
 * @author Fabrice de Chaumont
 * @author Stephane
 */

public class TrackSegment implements Cloneable
{

	protected static Map< TrackSegment, Integer > idHashMapList = new HashMap< TrackSegment, Integer >(); // 1-1

	protected static Map< Integer, TrackSegment > idKeyHashMapList = new HashMap< Integer, TrackSegment >(); // 1-1
																												// hashmap.
	private List< Detection > detectionList = new ArrayList< Detection >();

	private List< TrackSegment > previousList = new ArrayList< TrackSegment >();

	private List< TrackSegment > nextList = new ArrayList< TrackSegment >();

	private TrackGroup ownerTrackGroup = null;

	private int id = 0;

	private static final Random RANDOM = new Random();

	public static TrackSegment getTrackSegmentById( final int id )
	{
		synchronized ( idKeyHashMapList )
		{
			return idKeyHashMapList.get( Integer.valueOf( id ) );
		}
	}

	public TrackSegment()
	{
		super();

		generateId();
	}

	/** Constructor with a list of detection */
	public TrackSegment( final ArrayList< Detection > detectionList )
	{
		super();

		this.detectionList = detectionList;
		generateId();

		// FIXME: add a duplicate owner test.
		// for (Detection detection : detectionList)
		// {
		// if (detection.getOwnerTrackSegment() != null)
		// {
		// System.err.println("TrackSegment : The detection is already owned by
		// an other trackSegment.");
		// return;
		// }
		// detection.setOwnerTrackSegment(this);
		// }
	}

	@Override
	protected Object clone() throws CloneNotSupportedException
	{
		final TrackSegment cloneSegment = ( TrackSegment ) super.clone();

		for ( final Detection detection : detectionList )
		{
			final Detection cloneDetection = ( Detection ) detection.clone();
			// cloneDetection.setOwnerTrackSegment(cloneSegment);
			cloneSegment.detectionList.add( cloneDetection );
		}

		previousList = new ArrayList< TrackSegment >( previousList );
		nextList = new ArrayList< TrackSegment >( nextList );

		generateId();

		return cloneSegment;

	}

	public void removeId()
	{
		synchronized ( idKeyHashMapList )
		{
			synchronized ( idHashMapList )
			{
				idKeyHashMapList.remove( Integer.valueOf( id ) );
				idHashMapList.remove( this );
			}
		}
	}

	public int getId()
	{
		return id;
	}

	public void generateId()
	{
		// just for safety
		removeId();

		synchronized ( idKeyHashMapList )
		{
			synchronized ( idHashMapList )
			{
				while ( true )
				{
					final Integer key = Integer.valueOf( RANDOM.nextInt() );

					// available ?
					if ( idKeyHashMapList.get( key ) == null )
					{
						idHashMapList.put( this, key );
						idKeyHashMapList.put( key, this );

						this.id = key.intValue();

						return;
					}
				}
			}
		}
	}

	public void setId( final int id )
	{
		final Integer key = Integer.valueOf( id );

		synchronized ( idKeyHashMapList )
		{
			synchronized ( idHashMapList )
			{
				// available ?
				if ( idKeyHashMapList.get( key ) == null )
				{
					idHashMapList.put( this, key );
					idKeyHashMapList.put( key, this );

					this.id = key.intValue();
				}
//				else
//					System.out.println( "track id already loaded" );
			}
		}
	}

	/** */
	public boolean containsDetection( final Detection detection )
	{
		return detectionList.contains( detection );
	}

	/** set all dependent selection */
	public void setAllDetectionEnabled( final boolean selected )
	{
		for ( final Detection d : detectionList )
			d.setEnabled( selected );
	}

	/** is all dependent selection are enabled */
	public boolean isAllDetectionEnabled()
	{
		if ( detectionList.isEmpty() )
			return false;

		boolean result = true;
		for ( final Detection d : detectionList )
			if ( !d.isEnabled() )
				result = false;

		return result;
	}

	/** set all dependent selection */
	public void setAllDetectionSelected( final boolean selected )
	{
		for ( final Detection d : detectionList )
			d.setSelected( selected );
	}

	/** check if all dependent selection are selected */
	public boolean isAllDetectionSelected()
	{
		if ( detectionList.isEmpty() )
			return false;

		boolean result = true;
		for ( final Detection d : detectionList )
			if ( !d.isSelected() )
				result = false;

		return result;
	}

	/** Add a detection in the segmentTrack */
	public void addDetection( final Detection detection )
	{
		if ( detectionList.size() > 0 )
		{
			final Detection detectionPrevious = getLastDetection();
			if ( detection.getT() != detectionPrevious.getT() + 1 )
			{
				System.err.println(
						"TrackSegment : The detection must be added with consecutive T value. Detection was not added" );
				// throw new IllegalArgumentException();
				return;
			}

			// FIXME: check if the detection is already used by another plugin
			// if (detection.getOwnerTrackSegment() != null)
			// {
			// System.err.println("TrackSegment : The detection is already owned
			// by an other trackSegment.");
			// return;
			// }
			// detection.setOwnerTrackSegment(this);
		}
		detectionList.add( detection );
	}

	public void removeDetection( final Detection detection )
	{
		// detection.setOwnerTrackSegment(null);
		detectionList.remove( detection );
	}

	/** Remove a detection in the segmentTrack */
	public void removeLastDetection()
	{
		// getLastDetection().ownerTrackSegment = null;
		detectionList.remove( getLastDetection() );
	}

	/** Add a TrackSegment before this trackSegment */
	public void addPrevious( final TrackSegment trackSegment )
	{
		previousList.add( trackSegment );
		trackSegment.nextList.add( this );
	}

	/** Remove a TrackSegment before this trackSegment */
	public void removePrevious( final TrackSegment trackSegment )
	{
		previousList.remove( trackSegment );
		trackSegment.nextList.remove( this );
	}

	/** Add a TrackSegment after this trackSegment */
	public void addNext( final TrackSegment trackSegment )
	{
		nextList.add( trackSegment );
		trackSegment.previousList.add( this );
	}

	/** Remove a TrackSegment after this trackSegment */
	public void removeNext( final TrackSegment trackSegment )
	{
		nextList.remove( trackSegment );
		trackSegment.previousList.remove( this );
	}

	/** return first detection ( should be first in time too ) */
	public Detection getFirstDetection()
	{
		if ( detectionList.size() == 0 )
			return null;

		return detectionList.get( 0 );
	}

	/** return detection at index i */
	public Detection getDetectionAt( final int i )
	{
		return detectionList.get( i );
	}

	/** return detection at time t */
	public Detection getDetectionAtTime( final int t )
	{
		for ( final Detection detection : detectionList )
		{
			if ( detection.getT() == t )
				return detection;
		}
		return null;
	}

	/**
	 * return detection list WARNING: User should use addDetection and
	 * removeDetection instead of doing it himself using direct access to the
	 * ArrayList. Using addDetection or removeDetection ensure the property
	 * ownerTrackSegment of the Detection to be correctly updated.
	 */
	public ArrayList< Detection > getDetectionList()
	{
		return ( ArrayList< Detection > ) detectionList;
	}

	/** return last detection ( should be last in time too ) */
	public Detection getLastDetection()
	{
		if ( detectionList.size() == 0 )
			return null;

		return detectionList.get( detectionList.size() - 1 );
	}

	/** return detection index */
	public int getDetectionIndex( final Detection detection )
	{
		return detectionList.indexOf( detection );
	}

	public void setOwnerTrackGroup( final TrackGroup tg )
	{
		ownerTrackGroup = tg;
	}

	public TrackGroup getOwnerTrackGroup()
	{
		return ownerTrackGroup;
	}

	public void removeAllLinks()
	{
		final List< TrackSegment > previousListCopy = new ArrayList< TrackSegment >( previousList );
		for ( final TrackSegment previousTrackSegment : previousListCopy )
			removePrevious( previousTrackSegment );

		final List< TrackSegment > nextListCopy = new ArrayList< TrackSegment >( nextList );
		for ( final TrackSegment nextTrackSegment : nextListCopy )
			removeNext( nextTrackSegment );
	}
}
