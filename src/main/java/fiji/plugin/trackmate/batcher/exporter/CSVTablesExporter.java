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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.ExportStatsTablesAction;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.visualization.table.TablePanel;
import fiji.plugin.trackmate.visualization.table.TrackTableView;

@Plugin( type = BatchResultExporter.class, priority = Priority.NORMAL )
public class CSVTablesExporter implements BatchResultExporter
{

	private static final String SPOT_KEY = "Spot table (CSV)";

	private static final String EDGE_KEY = "Edge table (CSV)";

	private static final String TRACK_KEY = "Track table (CSV)";

	@Override
	public List< String > getExportables()
	{
		return Arrays.asList( new String[] { SPOT_KEY, EDGE_KEY, TRACK_KEY } );
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
		final Set< String > commonKeys = getExportables().stream()
				.filter( keys::contains )
				.collect( Collectors.toSet() );
		if ( commonKeys.isEmpty() )
			return;

		final TrackTableView tables = ExportStatsTablesAction.createTrackTables(
				trackmate.getModel(),
				new SelectionModel( trackmate.getModel() ),
				displaySettings );
		if ( commonKeys.contains( SPOT_KEY ) )
			exportTable( tables.getSpotTable(), exportFolder, baseName, "spots", logger );
		if ( commonKeys.contains( EDGE_KEY ) )
			exportTable( tables.getEdgeTable(), exportFolder, baseName, "edges", logger );
		if ( commonKeys.contains( TRACK_KEY ) )
			exportTable( tables.getTrackTable(), exportFolder, baseName, "tracks", logger );
	}

	private static void exportTable(
			final TablePanel< ? > table,
			final Path exportFolder,
			final String baseName,
			final String suffix,
			final Logger logger )
	{
		final File file = new File( exportFolder.toFile(), baseName + '-' + suffix + ".csv" );
		try
		{
			table.exportToCsv( file );
			logger.log( " - Table for " + suffix + " saved to: " + file.toString() + '\n' );
		}
		catch ( final IOException e )
		{
			logger.error( " - Input/Output error:\n" + e.getMessage() + '\n' );
		}
	}

	@Override
	public String getInfoText()
	{
		return "<html>Export the TrackMate content as 3 CSV files containing the "
				+ "numerical feature values of:"
				+ "<ul>"
				+ "<li>The spots in visible tracks.</li>"
				+ "<li>The edges of these tracks.</li>"
				+ "<li>The tracks.</li>"
				+ "</ul>"
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
		return "CSV_TABLES";
	}

	@Override
	public String getName()
	{
		return "Export to CSV tables";
	}
}
