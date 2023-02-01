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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.LabelImgExporter;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.visualization.ViewUtils;
import ij.IJ;
import ij.ImagePlus;

@Plugin( type = BatchResultExporter.class, priority = Priority.NORMAL )
public class LabelImageExporter implements BatchResultExporter
{

	private static final String LABEL_IMAGE_KEY = "Label image (TIF)";

	@Override
	public List< String > getExportables()
	{
		return Arrays.asList( new String[] { LABEL_IMAGE_KEY } );
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

		// Generate label image.
		final Model model = trackmate.getModel();
		final ImagePlus imp = ( trackmate.getSettings().imp == null )
				? ViewUtils.makeEmpytImagePlus( model )
				: trackmate.getSettings().imp;

		final boolean exportSpotsAsDots = false;
		final boolean exportTracksOnly = false;
		final boolean useSpotIDsAsLabels = false;
		final ImagePlus labels = LabelImgExporter.createLabelImagePlus(
				model,
				imp,
				exportSpotsAsDots,
				exportTracksOnly,
				useSpotIDsAsLabels,
				logger );

		// Save label image.
		final File file = new File( exportFolder.toFile(), baseName + "-labels.tif" );
		final boolean ok = IJ.saveAsTiff( labels, file.getAbsolutePath() );
		if ( ok )
			logger.log( " - Label image saved to: " + file.toString() + '\n' );
		else
			logger.error( " - Problem writing label image to:" + file.toString() + '\n' );
	}

	@Override
	public String getInfoText()
	{
		return "<html>Export the tracking data as a label <br>"
				+ "image, saved as an ImageJ tif file."
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
		return "LABEL_IMAGE";
	}

	@Override
	public String getName()
	{
		return "Export label image";
	}
}
