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
package fiji.plugin.trackmate.helper;

import java.awt.event.WindowAdapter;
import java.io.IOException;

import javax.swing.JFrame;

import org.scijava.prefs.PrefService;
import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.ctc.CTCTrackingMetricsType;
import fiji.plugin.trackmate.helper.spt.SPTTrackingMetricsType;
import fiji.plugin.trackmate.helper.ui.CrawlerResultsPanel;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.plugin.PlugIn;

public class TrackMateParameterSweepResultsPlugin implements PlugIn
{

	private static final String RESULT_FOLDER_KEY = "RESULT_FOLDER";

	private static final String METRICS_TYPE_KEY = "METRICS_TYPE";

	@Override
	public void run( final String arg )
	{
		final GenericDialogPlus dialog = new GenericDialogPlus( "TrackMate-Helper results inspector" );
		dialog.setIconImage( Icons.TRACKMATE_ICON.getImage() );

		dialog.addImage( Icons.TRACKMATE_ICON );
		dialog.addToSameRow();
		dialog.addMessage( "TrackMate-Helper results inspector", Fonts.BIG_FONT );
		dialog.addMessage( "v" + VersionUtils.getVersion( TrackMateParameterSweepPlugin.class ), Fonts.SMALL_FONT );

		dialog.addMessage( "__________________________________________________________" );

		dialog.addMessage( "Please select a folder containing the CSV files\n"
				+ "generated by the TrackMate-Helper." );
		dialog.addMessage( "Path to parameter sweep results folder" );

		final PrefService prefService = TMUtils.getContext().getService( PrefService.class );
		final String lastUsedGtFolder = prefService.get( TrackMateParameterSweepResultsPlugin.class,
				RESULT_FOLDER_KEY, System.getProperty( "user.home" ) );
		dialog.addDirectoryField( "", lastUsedGtFolder, 50 );

		final String lastUsedMetrics = prefService.get( TrackMateParameterSweepResultsPlugin.class,
				METRICS_TYPE_KEY, "CTC" );
		dialog.addChoice( "What metrics type was used?", new String[] { "CTC", "SPT" }, lastUsedMetrics );

		dialog.showDialog();
		if ( dialog.wasCanceled() )
			return;

		final String resultsFolder = dialog.getNextString();
		prefService.put( TrackMateParameterSweepResultsPlugin.class, RESULT_FOLDER_KEY, resultsFolder );

		final String typeStr = dialog.getNextChoice();
		TrackingMetricsType type;
		if ( typeStr.equals( "CTC" ) )
		{
			type = new CTCTrackingMetricsType();
		}
		else if ( typeStr.equals( "SPT" ) )
		{
			// SPT max distance does not matter for inspection.
			type = new SPTTrackingMetricsType( 1., "image units" );
		}
		else
		{
			System.err.println( "Unknown metrics type:  " + typeStr + ". Please specify CTC or SPT." );
			return;
		}
		prefService.put( TrackMateParameterSweepResultsPlugin.class, METRICS_TYPE_KEY, typeStr );

		final ResultsCrawler crawler = new ResultsCrawler( type, Logger.IJ_LOGGER );
		try
		{
			crawler.crawl( resultsFolder );
		}
		catch ( final IOException e1 )
		{
			IJ.error( "Problem while inspecting folder " + resultsFolder
					+ " for CTC results files:\n" + e1.getMessage() );
			e1.printStackTrace();
		}

		final CrawlerResultsPanel panel = new CrawlerResultsPanel( crawler, null );
		crawler.watch( resultsFolder );

		final JFrame frame = new JFrame( "TrackMate parameter sweep results" );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final java.awt.event.WindowEvent e )
			{
				crawler.stopWatching();
			}
		} );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( panel );
		frame.setSize( 800, 400 );
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
