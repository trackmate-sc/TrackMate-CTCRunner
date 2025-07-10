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
package fiji.plugin.trackmate.helper.ctc;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.scijava.Context;

import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.CTCExporter;
import fiji.plugin.trackmate.action.CTCExporter.ExportType;
import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.TrackingMetrics;

/**
 * Performs tracking and all the CTC metrics measurements with a TrackMate
 * instance.
 *
 * @author Jean-Yves Tinevez
 */
public class CTCMetricsRunner extends MetricsRunner
{

	/**
	 * CTC processor instance.
	 */
	private final CTCMetricsProcessor ctc;

	/**
	 * Path to ground truth folder.
	 */
	private final String gtPath;

	public CTCMetricsRunner( final String gtPath, final String saveFolder, final Context context )
	{
		super( Paths.get( saveFolder ), new CTCTrackingMetricsType() );
		this.gtPath = gtPath;
		final int logLevel = 0; // silence CTC logging.
		this.ctc = new CTCMetricsProcessor( context, logLevel );
	}

	@Override
	public TrackingMetrics performMetricsMeasurements( final TrackMate trackmate ) throws MetricsComputationErrorException
	{
		batchLogger.log( "Exporting test results to CTC format.\n" );
		final int id = CTCExporter.getAvailableDatasetID( resultsRootPath.toString() );
		final String resultsFolder = CTCExporter.getExportTrackingDataPath( resultsRootPath.toString(), id, ExportType.RESULTS, trackmate );
		try
		{
			// Export to CTC files.
			CTCExporter.exportTrackingData( resultsRootPath.toString(), id, ExportType.RESULTS, trackmate, trackmateLogger );

			// Perform CTC measurements.
			batchLogger.log( "Performing CTC metrics measurements.\n" );
			final TrackingMetrics metrics = ctc.process( gtPath, resultsFolder );
			return metrics;
		}
		catch ( final IOException | IllegalArgumentException e )
		{
			batchLogger.error( "Could not export tracking data to CTC files:\n" + e.getMessage() + '\n' );
			throw new MetricsComputationErrorException();
		}
		finally
		{
			try
			{
				// Delete CTC export folder.
				deleteFolder( resultsFolder );
			}
			catch ( final RuntimeException e )
			{
				batchLogger.error( "Failed to delete CTC export folder: " + resultsFolder + "\n"
						+ "Please delete it manually later.\n" );
			}
		}
	}

	private static final void deleteFolder( final String folder )
	{
		final Path path = Paths.get( folder );
		try
		{
			Files.walkFileTree( path, new SimpleFileVisitor< Path >()
			{
				@Override
				public FileVisitResult visitFile( final Path file, final BasicFileAttributes attrs ) throws IOException
				{
					Files.delete( file );
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory( final Path dir, final IOException e ) throws IOException
				{
					if ( e == null )
					{
						Files.delete( dir );
						return FileVisitResult.CONTINUE;
					}
					throw e;
				}
			} );
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( "Failed to delete " + path, e );
		}
	}
}
