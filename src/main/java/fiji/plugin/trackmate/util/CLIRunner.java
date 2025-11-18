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
package fiji.plugin.trackmate.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.helper.HelperRunner;
import fiji.plugin.trackmate.helper.HelperRunner.Builder;
import fiji.plugin.trackmate.helper.TrackingMetricsType;
import fiji.plugin.trackmate.helper.ctc.CTCTrackingMetricsType;
import fiji.plugin.trackmate.helper.spt.SPTTrackingMetricsType;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.patcher.LegacyInjector;

public class CLIRunner
{

	public static void main( final String[] args ) throws FileNotFoundException
	{
		LegacyInjector.preinit();

		final String fileName = args[ 0 ];

		final JsonObject jsonObject = JsonParser.parseReader( new FileReader( fileName ) ).getAsJsonObject();

		// Pretty print the JSON object
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final String prettyJson = gson.toJson( jsonObject );
		System.out.println( "Content of the task definition file:\n" + prettyJson );

		final String metrics = jsonObject.get( "metrics" ).getAsString();
		final String groundTruthPath = jsonObject.get( "ground_truth_path" ).getAsString();
		final String sourceImagePath = jsonObject.get( "source_image_path" ).getAsString();
		final String saveFolder = jsonObject.get( "save_folder" ).getAsString();
		final String taskDefFile = jsonObject.get( "helper_task_definition_path" ).getAsString();
		final String logFile = jsonObject.get( "log_file" ).getAsString();
		final double maxDist = jsonObject.get( "spt_max_linking_distance" ).getAsDouble();

		final ImagePlus imp = IJ.openImage( sourceImagePath );
		final String units = "image units";

		final TrackingMetricsType type = metrics.trim().equals( "SPT" )
				? new SPTTrackingMetricsType( maxDist, units )
				: new CTCTrackingMetricsType();

		Logger logger;
		if ( !logFile.isEmpty() && new File( logFile ).canWrite() )
		{
			logger = new CircularLogFile( logFile, 100_000 );
			System.out.println( "Appending log to file: " + logFile );
		}
		else
		{
			logger = Logger.VOID_LOGGER;
			System.out.println( "No log file specified." );
		}

		final Builder builder = HelperRunner.create();
		final HelperRunner runner = builder
				.trackingMetricsType( type )
				.groundTruth( groundTruthPath )
				.savePath( saveFolder )
				.runSettings( taskDefFile )
				.image( imp )
				.batchLogger( logger )
				.sptMetricsMaxPairingDistance( maxDist )
				.get();

		if ( runner == null )
		{
			System.err.println( builder.getErrorMessage() );
			return;
		}
		System.out.println( "Starting parameter sweep." );
		runner.run();
		System.out.println( "Done." );
	}
}
