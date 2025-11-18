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
package fiji.plugin.trackmate.ctc;

import java.io.File;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.helper.HelperRunner;
import fiji.plugin.trackmate.helper.HelperRunner.Builder;
import fiji.plugin.trackmate.helper.TrackingMetricsType;
import fiji.plugin.trackmate.helper.spt.SPTTrackingMetricsType;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import net.imagej.ImageJ;

public class CLITestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final ImageJ ij = new ImageJ();
		ij.launch( args );

//		final String rootFolder = "/Users/tinevez/Projects/TSabate/Data/GroundTruth/";
		final String rootFolder = "D:/Projects/TSabate/Data/GroundTruth";
		final String sourceImagePath = new File( rootFolder, "20220131-1435_Lv4TetOinCuO-C4_t-000-106_p005.ome_ALN_MarginsCropped-cropped2.tif" ).getAbsolutePath();
		final File saveFolder = new File( rootFolder, "GT_Halo" );
		final File groundTruthPath = new File( saveFolder, "20220131-1435_Lv4TetOinCuO-C4_t-000-106_p005_ISBI.xml" );
		final File modelPath = new File( saveFolder, "helperrunnersettings.json" );
		
		final RoiManager roiManager = RoiManager.getRoiManager();
		roiManager.runCommand( "Open", new File( rootFolder, "ROIs-Halo.zip" ).getAbsolutePath() );

		final ImagePlus imp = IJ.openImage( sourceImagePath );
		final double maxDist = 1.;
		final String units = "image units";
		final TrackingMetricsType type = new SPTTrackingMetricsType( maxDist, units );

		final Builder builder = HelperRunner.create();
		final HelperRunner runner = builder
				.trackingMetricsType( type )
				.groundTruth( groundTruthPath.getAbsolutePath() )
				.savePath( saveFolder.getAbsolutePath() )
				.runSettings( modelPath.getAbsolutePath() )
				.image( imp )
				.batchLogger( Logger.DEFAULT_LOGGER )
				.get();

		if ( runner == null )
		{
			System.err.println( builder.getErrorMessage() );
			return;
		}
		runner.run();
	}
}
