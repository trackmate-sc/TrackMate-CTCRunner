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
package fiji.plugin.trackmate.batcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Cancelable;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Logger.StringBuilderLogger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.batcher.exporter.BatchResultExporter;
import fiji.plugin.trackmate.batcher.exporter.ExporterParam;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.util.TMUtils;
import ij.IJ;
import ij.ImagePlus;
import loci.formats.FormatException;
import loci.plugins.BF;
import net.imglib2.algorithm.Algorithm;
import net.imglib2.algorithm.MultiThreaded;

public class TrackMateBatcher implements Cancelable, MultiThreaded, Algorithm
{

	private int numThreads;

	private String cancelReason;

	private String errorMessage;

	private final Collection< Path > inputImagePaths;

	private final Settings settings;

	private final DisplaySettings displaySettings;

	private final RunParamModel runParams;

	private final Logger logger;

	private TrackMate currentTM;

	public TrackMateBatcher(
			final Collection< Path > inputImagePaths,
			final Settings settings,
			final DisplaySettings displaySettings,
			final RunParamModel runParams,
			final Logger logger )
	{
		this.inputImagePaths = inputImagePaths;
		this.settings = settings;
		this.displaySettings = displaySettings;
		this.runParams = runParams;
		this.logger = logger;
	}

	@Override
	public boolean checkInput()
	{
		errorMessage = "";

		if ( inputImagePaths == null || inputImagePaths.isEmpty() )
		{
			errorMessage = "Input file list is null or empty.";
			return false;
		}
		if ( settings == null )
		{
			errorMessage = "Settings object is null";
			return false;
		}
		if ( displaySettings == null )
		{
			errorMessage = "Display settings are null";
			return false;
		}
		if ( runParams == null )
		{
			errorMessage = "Run parameters are null";
			return false;
		}
		if ( logger == null )
		{
			errorMessage = "Logger is null";
			return false;
		}
		return true;
	}

	@Override
	public boolean process()
	{
		errorMessage = "";

		final int todo = inputImagePaths.size();
		int done = 0;
		for ( final Path path : inputImagePaths )
		{
			if ( isCanceled() )
				break;
			logger.log( "\n_______________________________\n" );
			logger.log( TMUtils.getCurrentTimeString() + "\n" );
			logger.log( "Processing file " );
			logger.log( path.toString() + '\n', Logger.BLUE_COLOR );
			logger.setStatus( path.getFileName().toString() );

			/*
			 * Load image.
			 */

			logger.log( " - Loading image... " );
			ImagePlus[] imps;
			final ImagePlus tmpimp = IJ.openImage( path.toString() );
			if ( tmpimp != null )
			{
				imps = new ImagePlus[] { tmpimp };
			}
			else
			{
				try
				{
					imps = BF.openImagePlus( path.toString() );
				}
				catch ( FormatException | IOException e )
				{
					logger.error( "\nProblem opening image. Error message is:\n" + e.getMessage() + '\n' );
					continue;
				}
			}
			logger.log( " Done.\n" );
			if ( imps.length > 1 )
				logger.log( " - File contains " + imps.length + " images. Processing all of them.\n" );

			/*
			 * Build save name root.
			 */

			Path exportFolder;
			if ( runParams.isSaveToInputFolder() )
				exportFolder = path.getParent();
			else
				exportFolder = Paths.get( runParams.getOutputFolderPath() );

			/*
			 * Exec TrackMate.
			 */

			for ( int i = 0; i < imps.length; i++ )
			{
				if ( isCanceled() )
					break;

				final ImagePlus imp = imps[ i ];
				final Settings localSettings = settings.copyOn( imp );

				if ( imps.length > 1 )
					logger.log( " - " + imp.getTitle() + '\n', Logger.BLUE_COLOR );

				logger.log( " - Running TrackMate... " );
				final TrackMate trackmate = new TrackMate( localSettings );
				trackmate.getModel().setLogger( new StringBuilderLogger() );
				// So the log is stored and can be saved later.
				trackmate.setNumThreads( numThreads );
				this.currentTM = trackmate;

				if ( !trackmate.checkInput() )
				{
					logger.error( '\n' + trackmate.getErrorMessage() + '\n' );
					continue;
				}
				if ( !trackmate.process() )
				{
					logger.error( '\n' + trackmate.getErrorMessage() + '\n' );
					continue;
				}
				logger.log( " Done.\n" );

				/*
				 * Export results.
				 */
				if ( isCanceled() )
					break;

				logger.log( " - Exporting results...\n" );

				String baseName;
				if ( imps.length > 1 )
					baseName = imp.getTitle();
				else
					baseName = FilenameUtils.removeExtension( path.getFileName().toString() );

				final Set< String > exporterKeys = runParams.getExporterKeys();
				for ( final String exporterKey : exporterKeys )
				{
					final List< ExporterParam > params = runParams.getExporterExtraParameters( exporterKey );
					final List< String > exportables = runParams.getSelectedExportables( exporterKey );
					final BatchResultExporter exporter = runParams.getExporter( exporterKey );
					if ( null == exporter )
					{
						logger.error( "Could not find the exporter to export to " + exporterKey + '\n' );
						continue;
					}
					exporter.export( trackmate, displaySettings, exportables, params, exportFolder, baseName, logger );
				}
				trackmate.getSettings().imp.close();
				logger.log( "Done.\n" );
			}
			logger.setProgress( ( double ) ++done / todo );
		}

		logger.log( "\n_______________________________\n" );
		logger.log( TMUtils.getCurrentTimeString() + "\n" );
		logger.log( "Finished!\n" );
		logger.setProgress( 0. );
		logger.setStatus( "" );

		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public void setNumThreads()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors() / 2;
	}

	@Override
	public void setNumThreads( final int numThreads )
	{
		this.numThreads = numThreads;
	}

	@Override
	public int getNumThreads()
	{
		return numThreads;
	}

	@Override
	public void cancel( final String cancelReason )
	{
		this.cancelReason = cancelReason;
		if ( currentTM != null )
			currentTM.cancel( cancelReason );
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}

	@Override
	public boolean isCanceled()
	{
		return cancelReason != null;
	}
}
