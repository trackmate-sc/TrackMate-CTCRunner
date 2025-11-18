/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
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
