/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
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

import static fiji.plugin.trackmate.batcher.exporter.CSVTablesExporter.exportTable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.visualization.table.AllSpotsTableView;
import fiji.plugin.trackmate.visualization.table.TablePanel;

@Plugin( type = BatchResultExporter.class, priority = Priority.NORMAL )
public class AllSpotsTableExporter implements BatchResultExporter
{

	private static final String KEY = "All spots table (CSV)";

	@Override
	public List< String > getExportables()
	{
		return Collections.singletonList( KEY );
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
		if ( !keys.contains( KEY ) )
			return;

		TablePanel< Spot > table = AllSpotsTableView.createSpotTable( trackmate.getModel(), displaySettings );
		exportTable( table, exportFolder, baseName, "all-spots", logger );
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getKey()
	{
		return "ALL_SPOTS_TABLE";
	}

	@Override
	public String getName()
	{
		return "Export all spots to CSV";
	}

	@Override
	public String getInfoText()
	{
		return "<html>Export all the spots data in a CSV file, regardless of "
				+ "whether the spots are in a track or not. Only visible "
				+ "spots are exported."
				+ "</html>";
	}
}
