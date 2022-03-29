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
package fiji.plugin.trackmate.performance.spt.measure;

import java.util.ArrayList;

import javax.sound.midi.Sequence;

public class TrackGroup
{

	private final Sequence sequence;

	/**
	 * DO NOT DIRECTLY ADD OR REMOVE tracks from this trackSegmentList. Always
	 * use getters and adder/setters
	 */
	private final ArrayList< TrackSegment > trackSegmentList;

	public TrackGroup( final Sequence sequence )
	{
		this.sequence = sequence;
		trackSegmentList = new ArrayList< TrackSegment >();
	}

	/**
	 * @return the sequence
	 */
	public Sequence getSequence()
	{
		return sequence;
	}

	public ArrayList< TrackSegment > getTrackSegmentList()
	{
		return trackSegmentList;
	}

	public void addTrackSegment( final TrackSegment ts )
	{
		if ( ts.getOwnerTrackGroup() != null )
		{
			System.err.println( "The trackSegment is already owned by another TrackGroup." );
			return;
		}

		// System.out.println("Track segment added to group. TS: "
		// +ts.toString() );

		ts.setOwnerTrackGroup( this );
		trackSegmentList.add( ts );
	}

	public TrackSegment getTrackSegmentWithDetection( final Detection detection )
	{

		final ArrayList< TrackSegment > trackSegmentList = getTrackSegmentList();

		for ( final TrackSegment ts : trackSegmentList )
		{
			if ( ts.containsDetection( detection ) )
				return ts;
		}

		return null;
	}

	public void clearAllTrackSegment()
	{
		final ArrayList< TrackSegment > trackSegmentListCopy = new ArrayList< TrackSegment >( trackSegmentList );

		for ( final TrackSegment ts : trackSegmentListCopy )
		{
			removeTrackSegment( ts );
		}
	}

	public void removeTrackSegment( final TrackSegment ts )
	{
		// should remove links
		ts.setOwnerTrackGroup( null );
		ts.removeAllLinks();
		trackSegmentList.remove( ts );
	}

	private String description;

	public String getDescription()
	{
		return description;
	}

	public void setDescription( final String description )
	{
		this.description = description;
	}

	@Override
	public String toString()
	{
		return description;
	}
}
