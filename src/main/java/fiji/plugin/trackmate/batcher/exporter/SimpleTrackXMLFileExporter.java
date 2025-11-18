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
package fiji.plugin.trackmate.batcher.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.ExportTracksToXML;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;

@Plugin( type = BatchResultExporter.class, priority = Priority.LOW )
public class SimpleTrackXMLFileExporter implements BatchResultExporter
{

	private static final String NAME = "Simple track file (XML)";

	@Override
	public String getInfoText()
	{
		return "<html>"
				+ "Saves tracks to a simple XML file.<p>"
				+ "The file will have one element per track, and each track <br>" +
				"contains several spot elements. These spots are <br>" +
				"sorted by frame number, and have 4 numerical attributes: <br>" +
				"the frame number this spot is in, and its X, Y, Z position in <br>" +
				"physical units as specified in the image properties. <br>" +
				"<p>" +
				"As such, this format <u>cannot</u> handle track merging and <br>" +
				"splitting properly, and is suited only for non-branching tracks."
				+ "</html>";
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getKey()
	{
		return "SAVE_TO_SIMPLE_TRACK_FILE";
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public List< String > getExportables()
	{
		return Collections.singletonList( NAME );
	}

	@Override
	public void export(
			final TrackMate trackmate,
			final DisplaySettings displaySettings,
			final List< String > keys,
			final List< ExporterParam > parameters,
			final Path exportFolder,
			final String baseName,
			final Logger logger )
	{
		if ( !keys.contains( NAME ) )
			return;

		final File file = new File( exportFolder.toFile(), baseName + "_Tracks.xml" );
		try
		{
			ExportTracksToXML.export( trackmate.getModel(), trackmate.getSettings(), file );
			logger.log( " - Simple track file saved to: " + file.toString() + '\n' );
		}
		catch ( final FileNotFoundException e )
		{
			logger.error( " - File not found:\n" + e.getMessage() + '\n' );
		}
		catch ( final IOException e )
		{
			logger.error( " - Input/Output error:\n" + e.getMessage() + '\n' );
		}
	}
}
