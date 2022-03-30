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
package fiji.plugin.trackmate.helper;

import java.awt.event.WindowAdapter;
import java.io.IOException;

import javax.swing.JFrame;

import org.scijava.prefs.PrefService;
import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.ctc.CTCResultsCrawler;
import fiji.plugin.trackmate.helper.ui.CrawlerResultsPanel;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.plugin.PlugIn;

public class TrackMateParameterSweepResultsPlugin implements PlugIn
{

	private static final String RESULT_FOLDER_KEY = "RESULT_FOLDER";

	@Override
	public void run( final String arg )
	{
		GuiUtils.setSystemLookAndFeel();
		final CTCResultsCrawler crawler = new CTCResultsCrawler( Logger.IJ_LOGGER );
		final String resultsFolder;
		if ( arg != null && !arg.isEmpty() )
		{
			resultsFolder = arg;
		}
		else
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

			dialog.showDialog();
			if ( dialog.wasCanceled() )
				return;

			resultsFolder = dialog.getNextString();
			prefService.put( TrackMateParameterSweepResultsPlugin.class, RESULT_FOLDER_KEY, resultsFolder );
		}
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