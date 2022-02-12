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
package fiji.plugin.trackmate.ctc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.prefs.PrefService;
import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.ctc.ui.ParameterSweepController;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class TrackMateParameterSweepPlugin implements PlugIn
{

	private static final String GT_FOLDER_KEY = "GT_FOLDER";

	private static final String IMAGE_PATH_KEY = "IMAGE_PATH";

	private ParameterSweepController controller;

	@Override
	public void run( final String arg )
	{
		GuiUtils.setSystemLookAndFeel();
		if ( arg != null && !arg.isEmpty() )
		{
			final List< String > args = Arrays.asList( arg.split( "," ) )
					.stream()
					.map( String::trim )
					.collect( Collectors.toList() );
			if ( args.size() > 1 )
			{
				final ImagePlus imp = IJ.openImage( args.get( 0 ) );
				imp.show();
				final String gtPath = args.get( 1 );
				controller = new ParameterSweepController( imp, gtPath );
				controller.show();
				return;
			}
		}

		final PrefService prefService = TMUtils.getContext().getService( PrefService.class );

		// Do we have an image opened?
		final boolean impOpen = WindowManager.getImageCount() > 0;

		final GenericDialogPlus dialog = new GenericDialogPlus( "TrackMate-Helper startup" );
		dialog.setIconImage( Icons.TRACKMATE_ICON.getImage() );

		dialog.addImage( Icons.TRACKMATE_ICON );
		dialog.addToSameRow();
		dialog.addMessage( "TrackMate-Helper", Fonts.BIG_FONT );
		dialog.addMessage( "v" + VersionUtils.getVersion( TrackMateParameterSweepPlugin.class ), Fonts.SMALL_FONT );
		dialog.addMessage( "__________________________________________________________" );

		dialog.addMessage( "Please select an image to run tracking on." );

		if ( impOpen )
			dialog.addImageChoice( "Source image:", "" );
		else
		{
			final String lastUsedImagePathFolder = prefService.get( getClass(), IMAGE_PATH_KEY, System.getProperty( "user.home" ) );
			dialog.addMessage( "Path to the source image:" );
			dialog.addFileField( "", lastUsedImagePathFolder, 50 );
		}

		dialog.addMessage( "__________________________________________________________" );

		dialog.addMessage( "Please browse to the folder in which the tracking ground-truth is stored." );
		dialog.addMessage( "The results will be written in the parent of this folder." );
		dialog.addMessage( "The ground truth must follow the cell-tracking challenge format. See\n"
				+ "Ulman, et al (2017). An objective comparison of cell-tracking algorithms.\n"
				+ "Nature Methods, 14(12), 1141–1152. https://doi.org/10.1038/nmeth.4473\n"
				+ "for details." );
		dialog.addMessage( "Path to tracking ground-truth folder:" );
		
		final String lastUsedGtFolder = prefService.get( getClass(), GT_FOLDER_KEY, System.getProperty( "user.home" ) );
		dialog.addDirectoryField( "", lastUsedGtFolder, 50 );

		dialog.showDialog();
		if ( dialog.wasCanceled() )
			return;


		final ImagePlus imp;
		if ( impOpen )
		{
			imp = dialog.getNextImage();
		}
		else
		{
			final String imagePath = dialog.getNextString();
			imp = IJ.openImage( imagePath );
			if ( imp == null )
			{
				IJ.error( "TrackMate-Helper", "Could not open image file " + imagePath );
				return;
			}
			prefService.put( getClass(), IMAGE_PATH_KEY, imagePath );
			imp.show();
		}

		final String gtPath = dialog.getNextString();
		prefService.put( getClass(), GT_FOLDER_KEY, gtPath );

		controller = new ParameterSweepController( imp, gtPath );
		controller.show();
	}

	public ParameterSweepController getController()
	{
		return controller;
	}
}
