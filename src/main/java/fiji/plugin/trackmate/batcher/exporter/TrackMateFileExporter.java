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
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.gui.wizard.descriptors.ConfigureViewsDescriptor;
import fiji.plugin.trackmate.io.TmXmlWriter;

@Plugin( type = BatchResultExporter.class, priority = Priority.HIGH )
public class TrackMateFileExporter implements BatchResultExporter
{

	private static final String NAME = "TrackMate file (XML)";

	@Override
	public String getInfoText()
	{
		return "<html>"
				+ "Saves the TrackMate content to a TrackMate XML file <br>"
				+ "that can be opened with TrackMate."
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
		return "SAVE_TO_TRACKMATE";
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
		final File file = new File( exportFolder.toFile(), baseName + ".xml" );
		final TmXmlWriter writer = new TmXmlWriter( file );
		writer.appendLog( trackmate.getModel().getLogger().toString() );
		writer.appendModel( trackmate.getModel() );
		writer.appendSettings( trackmate.getSettings() );
		writer.appendGUIState( ConfigureViewsDescriptor.KEY );
		writer.appendDisplaySettings( displaySettings );

		try
		{
			writer.writeToFile();
			logger.log( " - TrackMate file saved to: " + file.toString() + '\n' );
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
