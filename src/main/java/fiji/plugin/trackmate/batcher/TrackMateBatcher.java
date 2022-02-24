package fiji.plugin.trackmate.batcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.scijava.Cancelable;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Logger.StringBuilderLogger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SelectionModel;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.action.CaptureOverlayAction;
import fiji.plugin.trackmate.action.ExportStatsTablesAction;
import fiji.plugin.trackmate.batcher.util.ExcelExporter;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings;
import fiji.plugin.trackmate.gui.wizard.descriptors.ConfigureViewsDescriptor;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.util.TMUtils;
import fiji.plugin.trackmate.visualization.hyperstack.HyperStackDisplayer;
import fiji.plugin.trackmate.visualization.table.TablePanel;
import fiji.plugin.trackmate.visualization.table.TrackTableView;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.AVI_Writer;
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

				// TrackMate files.
				if ( runParams.isExportTrackMateFile() )
					exportToTrackMate( trackmate, exportFolder, baseName );
				
				// CSV tables.
				if ( runParams.isExportSpotTable() || runParams.isExportEdgeTable() || runParams.isExportTrackTable() )
				{
					final TrackTableView tables = ExportStatsTablesAction.createTrackTables(
							trackmate.getModel(),
							new SelectionModel( trackmate.getModel() ),
							displaySettings );
					if ( runParams.isExportSpotTable() )
						exportTable( tables.getSpotTable(), exportFolder, baseName, "spots" );
					if ( runParams.isExportEdgeTable() )
						exportTable( tables.getEdgeTable(), exportFolder, baseName, "edges" );
					if ( runParams.isExportTrackTable() )
						exportTable( tables.getTrackTable(), exportFolder, baseName, "tracks" );
				}

				// Excel spreadsheet.
				if ( runParams.isExportAllTables() )
					exportExcel( trackmate, exportFolder, baseName );
				
				// AVI movie.
				if (runParams.isExportAVIMovie())
					exportAVIMovie( trackmate, exportFolder, baseName );

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

	private void exportAVIMovie( final TrackMate trackmate, final Path exportFolder, final String baseName )
	{
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
		movie.getCalibration().fps = runParams.getMovieFps();

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

	private void exportExcel( final TrackMate trackmate, final Path exportFolder, final String baseName )
	{
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

	private void exportTable( final TablePanel< ? > table, final Path exportFolder, final String baseName, final String suffix )
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

	private void exportToTrackMate( final TrackMate trackmate, final Path exportFolder, final String baseName )
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
