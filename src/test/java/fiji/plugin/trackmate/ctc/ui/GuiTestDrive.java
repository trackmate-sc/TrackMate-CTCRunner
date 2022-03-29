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
package fiji.plugin.trackmate.ctc.ui;

import java.io.File;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiji.plugin.trackmate.helper.TrackMateParameterSweepPlugin;
import net.imagej.ImageJ;

public class GuiTestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		final ImageJ ij = new ImageJ();
		ij.launch( args );

//		final String rootFolder = "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\TCells\\JY";
//		final String sourceImagePath = new File( rootFolder, "TCellsMigration.tif" ).getAbsolutePath();
//		final File groundTruthPath = new File( rootFolder, "01_GT" );

		// final String rootFolder =
		// "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\CellMigration";
//		final String sourceImagePath = new File( rootFolder, "CellMigration.tif" ).getAbsolutePath();
//		final File groundTruthPath = new File( rootFolder, "02_GT" );

//		final String rootFolder = "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\NMeningitidis";
//		final String sourceImagePath = new File( rootFolder, "NeisseriaMeningitidisGrowth.tif" ).getAbsolutePath();
//		final File groundTruthPath = new File( rootFolder, "01_GT" );

//		final String rootFolder = "D:\\Projects\\JYTinevez\\TrackMate-StarDist\\CTCMetrics\\";
//		final String sourceImagePath = new File( rootFolder, "NMeningitidis/NeisseriaMeningitidisGrowth.tif" ).getAbsolutePath();
//		final File groundTruthPath = new File( rootFolder, "01_GT" );
//
		final String rootFolder = "/Users/tinevez/Projects/JYTinevez/TrackMateDLPaper/Data/CTCMetrics/CellMigration/";
		final File sourceImagePath = new File( rootFolder, "CellMigration.tif" );
		final File groundTruthPath = new File( rootFolder, "02_GT" );

//		final String rootFolder = "/Users/tinevez/Projects/TSabate/Data/GroundTruth/";
//		final String sourceImagePath = new File( rootFolder, "20220131-1435_Lv4TetOinCuO-C4_t-000-106_p005.ome_ALN_MarginsCropped-cropped2.tif" ).getAbsolutePath();
//		final File groundTruthPath = new File( rootFolder, "GT_GFP/01_GT" );
//		final RoiManager roiManager = RoiManager.getRoiManager();
//		roiManager.runCommand( "Open", new File( rootFolder, "ROIs.zip" ).getAbsolutePath() );

		final TrackMateParameterSweepPlugin plugin = new TrackMateParameterSweepPlugin();
		plugin.run( sourceImagePath + ", " + groundTruthPath );

//		IJ.openImage( sourceImagePath ).show();
//		plugin.run( "" );
	}
}
