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
package fiji.plugin.trackmate.helper.spt;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.TrackingMetrics;
import fiji.plugin.trackmate.helper.spt.importer.SPTFormatImporter;
import fiji.plugin.trackmate.helper.spt.measure.DistanceTypes;
import fiji.plugin.trackmate.helper.spt.measure.TrackSegment;

public class SPTMetricsRunner extends MetricsRunner
{

	private static final double maxDist = 1.; // whatever units!

	private final List< TrackSegment > referenceTracks;

	public SPTMetricsRunner( final String gtPath, final String saveFolder )
	{
		super( Paths.get( saveFolder ), new SPTTrackingMetricsType() );
		this.referenceTracks = SPTFormatImporter.fromXML( new File( gtPath ) );
	}

	@Override
	public void performMetricsMeasurements( final TrackMate trackmate, final double detectionTiming, final double trackingTiming )
	{
		final Settings settings = trackmate.getSettings();
		final Model model = trackmate.getModel();
		final File csvFile = findSuitableCSVFile( settings );
		final String[] csvHeader1 = toCSVHeader( settings );

		final List< TrackSegment > candidateTracks = SPTFormatImporter.fromTrackMate( model );

		// Perform SPT measurements.
		batchLogger.log( "Performing SPT metrics measurements.\n" );
		final double[] score = ISBIScoring.score( referenceTracks, candidateTracks, maxDist, DistanceTypes.DISTANCE_EUCLIDIAN );

		final TrackingMetrics metrics = new TrackingMetrics( type );
		for ( int i = 0; i < score.length; i++ )
			metrics.set( i, score[ i ] );

		writeResults( csvFile, metrics, detectionTiming, trackingTiming, settings, csvHeader1 );
	}
}
