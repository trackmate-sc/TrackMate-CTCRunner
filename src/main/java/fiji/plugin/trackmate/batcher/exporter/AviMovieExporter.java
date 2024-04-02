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
package fiji.plugin.trackmate.batcher.exporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.ImageIcon;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.CaptureOverlayAction;
import fiji.plugin.trackmate.batcher.exporter.ExporterParam.IntExporterParam;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import ij.ImagePlus;
import ij.plugin.filter.AVI_Writer;

@Plugin( type = BatchResultExporter.class, priority = Priority.VERY_LOW )
public class AviMovieExporter implements BatchResultExporter
{

	private static final String NAME = "Movie (uncompressed AVI)";

	private static final String FPS_KEY = "fps";

	@Override
	public String getInfoText()
	{
		return "<html>"
				+ "Captures the TrackMate results as an AVI movie (uncompressed). <br>"
				+ "The source image will be shown before capture, and the capture <br>"
				+ "will have a 100% magnification or lower depending on what can fit <br>"
				+ "on your screen."
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
		return "AVI_MOVIE";
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
	public List< ExporterParam > getExtraParameters()
	{
		return Collections.singletonList( ExporterParam.intParam( FPS_KEY, 10, 1, 100 ) );
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

		final Optional< ExporterParam > opt = parameters.stream().filter( p -> p.name().equals( FPS_KEY ) ).findAny();
		int fps;
		if ( !opt.isPresent() )
		{
			logger.error( "Could not find the setting value for " + FPS_KEY + "\nSubstituting default.\n" );
			fps = 10;
		}
		else
		{
			final IntExporterParam param = ( IntExporterParam ) opt.get();
			fps = ( int ) param.value();
		}

		final Model model = trackmate.getModel();
		final SelectionModel selectionModel = new SelectionModel( model );
		final ImagePlus imp = trackmate.getSettings().imp;
		final HyperStackDisplayer displayer = new HyperStackDisplayer( model, selectionModel, imp, displaySettings );
		displayer.render();
		displayer.refresh();

		final int first = 1;
		final int last = trackmate.getSettings().imp.getNFrames();
		trackmate.getSettings().imp.show();
		final ImagePlus movie = CaptureOverlayAction.capture( trackmate, first, last, Logger.VOID_LOGGER );
		movie.getCalibration().fps = fps;
		try
		{
			final AVI_Writer aviWriter = new AVI_Writer();
			final File file = new File( exportFolder.toFile(), baseName + "-movie.avi" );
			final int compression = AVI_Writer.NO_COMPRESSION;
			final int jpegQuality = 100;
			aviWriter.writeImage( movie, file.getAbsolutePath(), compression, jpegQuality );
			logger.log( " - Movie saved to: " + file.toString() + '\n' );
		}
		catch ( final IOException e )
		{
			logger.error( " - Input/Output error:\n" + e.getMessage() + '\n' );
		}
	}

}
