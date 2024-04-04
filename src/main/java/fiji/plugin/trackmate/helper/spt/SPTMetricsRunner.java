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
package fiji.plugin.trackmate.helper.spt;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.TrackingMetrics;
import fiji.plugin.trackmate.helper.spt.importer.SPTFormatImporter;
import fiji.plugin.trackmate.helper.spt.importer.XMLUtil;
import fiji.plugin.trackmate.helper.spt.measure.DistanceTypes;
import fiji.plugin.trackmate.helper.spt.measure.TrackSegment;
import fiji.plugin.trackmate.io.TmXmlReader;

public class SPTMetricsRunner extends MetricsRunner
{

	private final List< TrackSegment > referenceTracks;

	private final double maxDist;

	private final String units;

	public SPTMetricsRunner( final String gtPath, final String saveFolder, final double maxDist, final String units )
	{
		super( Paths.get( saveFolder ), new SPTTrackingMetricsType( maxDist, units ) );
		this.maxDist = maxDist;
		this.units = units;

		// Is the GT a TrackMate or a ISBI challenge file?
		final File gtFile = new File( gtPath );
		final Document document = XMLUtil.loadDocument( gtFile );
		final Element root = XMLUtil.getRootElement( document );
		if ( root == null )
			throw new IllegalArgumentException( "can't find: <root> tag." );

		final ArrayList< Element > rootEls = XMLUtil.getElements( root, "TrackContestISBI2012" );
		if ( rootEls.size() == 0 )
		{
			// Not an ISBI challenge file.
			final TmXmlReader reader = new TmXmlReader( gtFile );
			if ( !reader.isReadingOk() )
				throw new IllegalArgumentException( "Ground-truth file is neither a TrackMate file nor a ISBI SPT challenge file." );

			final Model model = reader.getModel();
			try
			{
				this.referenceTracks = SPTFormatImporter.fromTrackMate( model );
			}
			catch ( final Exception iae )
			{
				throw new IllegalArgumentException( "TrackMate ground-truth file cannot be used with SPT metrics:\n" + iae.getMessage() );
			}
		}
		else
		{
			// ISBI challenge file.
			this.referenceTracks = SPTFormatImporter.fromXML( gtFile );
		}
	}

	@Override
	public TrackingMetrics performMetricsMeasurements( final TrackMate trackmate )
	{
		final Model model = trackmate.getModel();
		final List< TrackSegment > candidateTracks = SPTFormatImporter.fromTrackMate( model );
		// Perform SPT measurements.
		batchLogger.log( String.format( "Performing SPT metrics measurements with max pairing dist = %.2f %s\n",
				maxDist, units ) );
		final double[] score = ISBIScoring.score( referenceTracks, candidateTracks, maxDist, DistanceTypes.DISTANCE_EUCLIDIAN );

		final TrackingMetrics metrics = new TrackingMetrics( type );
		for ( int i = 0; i < score.length; i++ )
			metrics.set( i, score[ i ] );

		return metrics;
	}
}
