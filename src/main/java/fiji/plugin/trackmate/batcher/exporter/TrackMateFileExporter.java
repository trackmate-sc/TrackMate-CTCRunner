package fiji.plugin.trackmate.batcher.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.gui.wizard.descriptors.ConfigureViewsDescriptor;
import fiji.plugin.trackmate.io.TmXmlWriter;

public class TrackMateFileExporter implements BatchResultExporter
{

	private static final String NAME = "TrackMate file";

	@Override
	public String getInfoText()
	{
		return "<html>"
				+ "Saves the TrackMate content to a TrackMate XML file "
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
			final Map< String, Object > extraParameters,
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
