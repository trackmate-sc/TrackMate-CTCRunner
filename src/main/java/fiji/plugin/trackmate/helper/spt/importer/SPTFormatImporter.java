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
package fiji.plugin.trackmate.helper.spt.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.helper.spt.measure.Detection;
import fiji.plugin.trackmate.helper.spt.measure.TrackSegment;

/**
 * Convert a TrackMate instance to the ISBI SPT data structures.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class SPTFormatImporter
{

	public static List< TrackSegment > fromTrackMate( final Model model )
	{
		final Set< Integer > trackIDs = model.getTrackModel().unsortedTrackIDs( true );
		final List< TrackSegment > tracks = new ArrayList< TrackSegment >( trackIDs.size() );

		for ( final Integer trackID : trackIDs )
		{
			final List< Spot > spots = new ArrayList<>( model.getTrackModel().trackSpots( trackID ) );
			spots.sort( Spot.frameComparator );

			final TreeMap< Integer, Detection > detections = new TreeMap< Integer, Detection >();
			for ( final Spot spot : spots )
			{
				final int t = spot.getFeature( Spot.FRAME ).intValue();
				if ( t < 0 )
					throw new IllegalArgumentException( "invalid t value: " + t );
				if ( detections.containsKey( Integer.valueOf( t ) ) )
					throw new IllegalArgumentException( "duplicated detection for a single track at time " + t );

				final double x = spot.getDoublePosition( 0 );
				final double y = spot.getDoublePosition( 1 );
				final double z = spot.getDoublePosition( 2 );
				final Detection detection = new Detection( x, y, z, t );
				detection.setDetectionType( Detection.DETECTIONTYPE_REAL_DETECTION );
				detections.put( Integer.valueOf( t ), detection );
			}
			tracks.add( makeTrack( detections ) );
		}
		trimTrack( tracks );
		return tracks;
	}

	public static List< TrackSegment > fromXML( final File inputFile ) throws IllegalArgumentException
	{
		final List< TrackSegment > tracks = new ArrayList< TrackSegment >();
		final Document document = XMLUtil.loadDocument( inputFile );
		final Element root = XMLUtil.getRootElement( document );
		if ( root == null )
		{ throw new IllegalArgumentException( "can't find: <root> tag." ); }
		final Element trackingSet = XMLUtil.getElements( root, "TrackContestISBI2012" ).get( 0 );

		if ( trackingSet == null )
			throw new IllegalArgumentException( "can't find: <root><TrackContestISBI2012> tag." );

		final List< Element > particleElementArrayList = XMLUtil.getElements( trackingSet, "particle" );

		for ( final Element particleElement : particleElementArrayList )
		{
			final List< Element > detectionElementArrayList = XMLUtil.getElements( particleElement, "detection" );
			final TreeMap< Integer, Detection > detections = new TreeMap< Integer, Detection >();
			for ( final Element detectionElement : detectionElementArrayList )
			{
				final int t = XMLUtil.getAttributeIntValue( detectionElement, "t", -1 );
				if ( t < 0 )
					throw new IllegalArgumentException( "invalid t value: " + t );
				if ( detections.containsKey( Integer.valueOf( t ) ) )
					throw new IllegalArgumentException( "duplicated detection for a single track at time " + t );
				final double x = XMLUtil.getAttributeDoubleValue( detectionElement, "x", 0 );
				final double y = XMLUtil.getAttributeDoubleValue( detectionElement, "y", 0 );
				final double z = XMLUtil.getAttributeDoubleValue( detectionElement, "z", 0 );
				final Detection detection = new Detection( x, y, z, t );
				detection.setDetectionType( Detection.DETECTIONTYPE_REAL_DETECTION );
				detections.put( Integer.valueOf( t ), detection );
			}
			if ( !detections.isEmpty() )
				tracks.add( makeTrack( detections ) );
		}
		trimTrack( tracks );
		return tracks;
	}

	/**
	 * Export TrackSegment objects to a .xml file.
	 * 
	 * @param file
	 *            output .xml file containing track information in the format
	 *            used for the ISBI'2012 Particle tracking challenge
	 * @param tracks
	 *            a list of TrackSegment objects that corresponds to the tracks
	 *            to save
	 * 
	 */
	public static void toXML( final File file, final ArrayList< TrackSegment > tracks ) throws IllegalArgumentException
	{
		final Document document = XMLUtil.createDocument( true );
		final Element dataSetElement = document.createElement( "TrackContestISBI2012" );
		XMLUtil.getRootElement( document ).appendChild( dataSetElement );

		for ( final TrackSegment particle : tracks )
		{
			final Element particleElement = document.createElement( "particle" );
			dataSetElement.appendChild( particleElement );

			for ( final Detection detection : particle.getDetectionList() )
			{
				final Element detectionElement = document.createElement( "detection" );
				particleElement.appendChild( detectionElement );
				XMLUtil.setAttributeDoubleValue( detectionElement, "x", detection.getX() );
				XMLUtil.setAttributeDoubleValue( detectionElement, "y", detection.getY() );
				XMLUtil.setAttributeDoubleValue( detectionElement, "z", detection.getZ() );
				XMLUtil.setAttributeIntValue( detectionElement, "t", detection.getT() );
			}
		}

		XMLUtil.saveDocument( document, file );
	}

	/*
	 * Checks if detection contain NaN position, and trim track if they exists
	 * example ( considering only one coordinate )
	 * 
	 * NaN, 1 , 2 ,4 ,12, NaN, NaN => 1 , 2 , 4 , 12
	 * 
	 * NaN , 1 , Nan , 2 , 3 , NaN => Discarded
	 */
	private static final void trimTrack( final List< TrackSegment > trackArrayList )
	{
		for ( final TrackSegment track : new ArrayList< TrackSegment >( trackArrayList ) )
		{
			// trim NaN from the beginning of track.
			for ( final Detection detection : new ArrayList< Detection >( track.getDetectionList() ) )
			{
				if ( containsNaN( detection ) )
					track.removeDetection( detection );
				else
					break;
			}

			// trim NaN from the end of track.
			for ( int i = track.getDetectionList().size() - 1; i >= 0; i-- )
			{
				final Detection detection = track.getDetectionList().get( i );
				if ( containsNaN( detection ) )
					track.removeDetection( detection );
				else
					break;
			}

			if ( track.getDetectionList().size() == 0 )
			{
				trackArrayList.remove( track );
				continue;
			}

			// check if a NaN still exists in the remaining track
			for ( final Detection detection : new ArrayList< Detection >( track.getDetectionList() ) )
			{
				if ( containsNaN( detection ) )
				{
					trackArrayList.remove( track );
					break;
				}
			}
		}
	}

	/*
	 * Adds detections in chronological order and cap gaps with virtual
	 * detections.
	 */
	private static final TrackSegment makeTrack( final TreeMap< Integer, Detection > detections )
	{
		final TrackSegment track = new TrackSegment();
		Detection lastDetection = null;
		for ( final Entry< Integer, Detection > e : detections.entrySet() )
		{
			final Detection detection = e.getValue();
			if ( lastDetection != null )
			{
				// cap hole with virtual detections
				if ( detection.getT() > lastDetection.getT() + 1 )
				{
					final int lastT = lastDetection.getT();
					final int nextT = detection.getT();
					final double lastX = lastDetection.getX();
					final double lastY = lastDetection.getY();
					final double lastZ = lastDetection.getZ();
					final double nextX = detection.getX();
					final double nextY = detection.getY();
					final double nextZ = detection.getZ();
					final double gapT = 1 / ( ( double ) nextT - ( double ) lastT );
					for ( int t = lastT + 1; t < nextT; t++ )
					{
						// linear interpolation
						final Detection interpolatedDetection = new Detection(
								lastX + ( t - lastT ) * ( nextX - lastX ) * gapT,
								lastY + ( t - lastT ) * ( nextY - lastY ) * gapT,
								lastZ + ( t - lastT ) * ( nextZ - lastZ ) * gapT,
								t );
						interpolatedDetection.setDetectionType( Detection.DETECTIONTYPE_VIRTUAL_DETECTION );
						track.addDetection( interpolatedDetection );
					}
				}
			}
			track.addDetection( detection );
			lastDetection = detection;
		}
		return track;
	}

	private static boolean containsNaN( final Detection detection )
	{
		return ( Double.isNaN( detection.getX() )
				|| Double.isNaN( detection.getY() )
				|| Double.isNaN( detection.getZ() ) );
	}

}
