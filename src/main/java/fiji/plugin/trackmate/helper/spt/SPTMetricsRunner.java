/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import com.opencsv.CSVWriter;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.spt.importer.SPTFormatImporter;
import fiji.plugin.trackmate.helper.spt.measure.TrackSegment;

public class SPTMetricsRunner extends MetricsRunner
{

	private final List< TrackSegment > referenceTracks;

	public SPTMetricsRunner( final String gtPath )
	{
		super( Paths.get( gtPath ).getParent(), "SPTMetrics" );
		this.referenceTracks = SPTFormatImporter.fromXML( new File( gtPath ) );
	}

	@Override
	public void performMetricsMeasurements( final TrackMate trackmate, final double detectionTiming, final double trackingTiming )
	{
		batchLogger.log( "Exporting as ISBI-SPT results.\n" );
		final Settings settings = trackmate.getSettings();
		final Model model = trackmate.getModel();
		final File csvFile = findSuitableCSVFile( settings );
		final String[] csvHeader1 = toCSVHeader( settings );

		try
		{
			final List< TrackSegment > candidateTrackss = SPTFormatImporter.fromTrackMate( model );

			// Perform SPT measurements.
			batchLogger.log( "Performing SPT metrics measurements.\n" );
			final double[] score = ISBIScoring.score( referenceTracks, candidateTrackss );
			final SPTMetrics m = SPTMetrics.fromArray( score );

			// Add timing measurements.
			final SPTMetrics metrics = m.copyEdit()
					.detectionTime( detectionTiming )
					.trackingTime( trackingTiming )
					.tim( detectionTiming + trackingTiming )
					.get();
			batchLogger.log( "SPT metrics:\n" );
			batchLogger.log( metrics.toString() + '\n' );

			// Write to CSV.
			final String[] line1 = toCSVLine( settings, csvHeader1 );
			final String[] line = metrics.concatWithCSVLine( line1 );

			try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile, true ),
					CSVWriter.DEFAULT_SEPARATOR,
					CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END ))
			{
				csvWriter.writeNext( line );
			}

		}
		catch ( final IOException | IllegalArgumentException e )
		{
			batchLogger.error( "Could not export tracking data to SPT files:\n" + e.getMessage() + '\n' );
			// Write default values to CSV.
			final String[] line1 = toCSVLine( settings, csvHeader1 );
			final SPTMetrics metrics = SPTMetrics.create()
					.alpha( Double.NaN )
					.beta( Double.NaN )
					.jsc( Double.NaN )
					.jscTheta( Double.NaN )
					.rmse( Double.NaN )
					.tim( Double.NaN )
					.detectionTime( Double.NaN )
					.trackingTime( Double.NaN )
					.get();
			final String[] line = metrics.concatWithCSVLine( line1 );
			try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile, true ),
					CSVWriter.DEFAULT_SEPARATOR,
					CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END ))
			{
				csvWriter.writeNext( line );
			}
			catch ( final IOException e1 )
			{
				batchLogger.error( "Could not write failed results to CSV file:\n" + e1.getMessage() + '\n' );
				e1.printStackTrace();
			}
		}
	}
}
