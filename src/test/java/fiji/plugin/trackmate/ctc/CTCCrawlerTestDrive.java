package fiji.plugin.trackmate.ctc;

import java.io.IOException;

import fiji.plugin.trackmate.Logger;

public class CTCCrawlerTestDrive
{

	public static void main( final String[] args ) throws IOException
	{
//		final String resultsFolder = "/Users/tinevez/Projects/JYTinevez/TrackMate-StarDist/CellMigration/";
		final String resultsFolder = "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\CellMigration";
		final CTCResultsCrawler crawler = new CTCResultsCrawler( Logger.DEFAULT_LOGGER );
		crawler.crawl( resultsFolder );
		System.out.println( crawler.printReport() );
	}
}
