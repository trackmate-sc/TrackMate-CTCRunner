package fiji.plugin.trackmate.ctc.ui;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.ctc.CTCResultsCrawler;

public class CrawlerResultsUITestDrive
{

	public static void main( final String[] args ) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
//			final String resultsFolder = "/Users/tinevez/Projects/JYTinevez/TrackMate-StarDist/CellMigration/";
		final String resultsFolder = "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\CellMigration";
		final CTCResultsCrawler crawler = new CTCResultsCrawler( Logger.DEFAULT_LOGGER );

		final CrawlerResultsPanel panel = new CrawlerResultsPanel( crawler );
		final JFrame frame = new JFrame( "TrackMate parameter sweep results" );
		frame.getContentPane().add( panel );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );

		crawler.crawl( resultsFolder );
	}
}
