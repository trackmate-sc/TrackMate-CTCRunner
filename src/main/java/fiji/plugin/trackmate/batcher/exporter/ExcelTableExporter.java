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
				+ "Export the numerical features of the visible tracks in a "
				+ "Excel workbook, with one spreadsheet for each of the spot features, "
				+ "edge features and track features in visible tracks."
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
