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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.batcher.util.ExcelExporter;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;

@Plugin( type = BatchResultExporter.class, priority = Priority.LOW )
public class ExcelTableExporter implements BatchResultExporter
{

	private static final String NAME = "The 3 tables (XLSX)";

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

		final XSSFWorkbook wb = ExcelExporter.exportToWorkBook( trackmate.getModel() );
		final File file = new File( exportFolder.toFile(), baseName + "-table.xlsx" );
		try (FileOutputStream fileOut = new FileOutputStream( file ))
		{
			wb.write( fileOut );
			wb.close();
			logger.log( " - Excel tables saved to: " + file.toString() + '\n' );
		}
		catch ( final IOException e )
		{
			logger.error( " - Input/Output error:\n" + e.getMessage() + '\n' );
		}
	}

	@Override
	public String getInfoText()
	{
		return "<html>"
				+ "Export the numerical features of the visible tracks in a <br>"
				+ "Excel workbook, with one spreadsheet for each of the spot <br>"
				+ "features, edge features and track features in visible tracks."
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
		return "EXCEL_TABLES";
	}

	@Override
	public String getName()
	{
		return NAME;
	}
}
