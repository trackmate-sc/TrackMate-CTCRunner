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
		final int channel = jsonObject.get( "target_channel" ).getAsInt();

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
				.targetChannel( channel )
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
