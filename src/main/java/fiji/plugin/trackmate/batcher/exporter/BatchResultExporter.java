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

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.TrackMateModule;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;

/**
 * Interface for classes that can export the results of TrackMate into various
 * format, to be saved on disk.
 */
public interface BatchResultExporter extends TrackMateModule
{

	/**
	 * Returns the list of exportable keys in this exporter. Each key
	 * corresponds to one file to export, and one item in the GUI. One exporter
	 * can have several keys in case they share some common pre-processing
	 * steps.
	 * 
	 * @return a list of key.
	 */
	public List< String > getExportables();

	/**
	 * If this exporter requires extra parameters to be specified, this method
	 * returns default values and types for them.
	 * <p>
	 * By default this methods returns an empty map.
	 * 
	 * @return a list of extra parameters.
	 */
	public default List< ExporterParam > getExtraParameters()
	{
		return Collections.emptyList();
	}

	/**
	 * Performs the export for each of the specified exportable keys.
	 * 
	 * @param trackmate
	 *            the {@link TrackMate} to export.
	 * @param displaySettings
	 *            the display settings.
	 * @param keys
	 *            the list of exportable keys to generate. Will generate one
	 *            file per key in this list.
	 * @param parameters
	 *            the map of extra parameters that this exporter needs.
	 * @param exportFolder
	 *            the folder in which to save the files.
	 * @param baseName
	 *            the base name to use as a prefix for the exported files.
	 * @param logger
	 *            a logger to use to report process and errors.
	 */
	public void export(
			TrackMate trackmate,
			DisplaySettings displaySettings,
			List< String > keys,
			List< ExporterParam > parameters,
			Path exportFolder,
			String baseName,
			Logger logger );
}
