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
package fiji.plugin.trackmate.ctc;

import java.io.IOException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.helper.ResultsCrawler;
import fiji.plugin.trackmate.helper.ctc.CTCTrackingMetricsType;

public class CTCCrawlerTestDrive
{

	public static void main( final String[] args ) throws IOException
	{
		final String resultsFolder = "/Users/tinevez/Projects/JYTinevez/TrackMateDLPaper/Data/CTCMetrics/CellMigration/";
//		final String resultsFolder = "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\CellMigration";
		final ResultsCrawler crawler = new ResultsCrawler( new CTCTrackingMetricsType(), Logger.DEFAULT_LOGGER );
		crawler.crawl( resultsFolder );
		System.out.println( crawler.printReport() );
	}
}
