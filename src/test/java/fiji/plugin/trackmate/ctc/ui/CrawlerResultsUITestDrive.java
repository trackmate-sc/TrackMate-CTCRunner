package fiji.plugin.trackmate.ctc.ui;

import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.ctc.TrackMateParameterSweepResultsPlugin;

public class CrawlerResultsUITestDrive
{

	public static void main( final String[] args ) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
//			final String resultsFolder = "/Users/tinevez/Projects/JYTinevez/TrackMate-StarDist/CellMigration/";
		final String resultsFolder = "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\CellMigration";

		new TrackMateParameterSweepResultsPlugin().run( resultsFolder );
	}
}
