package fiji.plugin.trackmate.ctc;

import java.io.File;

import fiji.plugin.trackmate.Logger;
import net.imagej.ImageJ;

public class CTCRunnerTestDrive
{
	public static void main( final String[] args )
	{
		new ImageJ();
		final String rootFolder = "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\CellMigration";
		final String sourceImagePath = new File( rootFolder, "CellMigration.tif" ).getAbsolutePath();
		final String groundTruthPath = new File( rootFolder, "02_GT" ).getAbsolutePath();
		final CTCRunner runner = new CTCRunner(
				sourceImagePath,
				1,
				groundTruthPath,
				Logger.DEFAULT_LOGGER,
				false );
		runner.run();
		System.out.println( runner.getCurrentBestParams() );
	}
}
