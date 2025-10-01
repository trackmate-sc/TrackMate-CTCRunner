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
package fiji.plugin.trackmate.helper.ui;

import static fiji.plugin.trackmate.helper.ui.components.GuiUtils.HELPER_ICON;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Cancelable;

import com.opencsv.CSVWriter;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.TrackingMetrics;
import fiji.plugin.trackmate.helper.TrackingMetricsType;
import fiji.plugin.trackmate.helper.ctc.CTCTrackingMetricsType;
import fiji.plugin.trackmate.helper.spt.SPTTrackingMetricsType;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import ij.ImagePlus;

public class MetricsLauncherController implements Cancelable
{

	private final Logger logger = Logger.IJ_LOGGER;

	private boolean isCanceled;

	private String cancelReason;

	public MetricsLauncherController()
	{
		final MetricsLauncherPanel gui = new MetricsLauncherPanel();
		final JFrame frame = new JFrame( "TrackMate tracking metrics" );
		frame.setIconImage( HELPER_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.setSize( 350, 550 );
		frame.setLocationRelativeTo( null );

		gui.btnCancel.addActionListener( e -> cancel( "User pressed the cancel button" ) );
		gui.btnOK.addActionListener( e -> {

			final EverythingDisablerAndReenabler disabler = new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
			disabler.disable();
			new Thread( "TrackMate tracking metrics computation thread" )
			{
				@Override
				public void run()
				{
					try
					{
						final boolean ctcSelected = gui.isCTCSelected();
						final String gtPath = gui.tfGTPath.getText();
						final String inputPath = gui.tfInputPath.getText();
						final double maxDist = gui.getSPTMaxPairingDistance();
						final String units = gui.getUnits();
						computeMetrics( ctcSelected, gtPath, inputPath, maxDist, units );
					}
					finally
					{
						disabler.reenable();
					}
				}
			}.start();
		} );
		frame.setVisible( true );
	}

	private void computeMetrics( final boolean ctcSelected, final String gtPath, final String inputPath, final double maxDist, final String units )
	{
		isCanceled = false;

		// Save folder.
		final String saveFolder;
		final File input = new File( inputPath );
		if ( input.isDirectory() )
			saveFolder = input.getAbsolutePath();
		else
			saveFolder = input.getParent();

		// Prepare the runner.
		final TrackingMetricsType type = ctcSelected
				? new CTCTrackingMetricsType()
				: new SPTTrackingMetricsType( maxDist, units );

		// Prepare the CSV file.
		final String gtName = FilenameUtils.removeExtension( new File( gtPath ).getName() );
		final String csvFileName = type.csvSuffix() + "_" + gtName + ".csv";
		final File csvFile = new File( saveFolder, csvFileName );

		// Echo info
		logger.log( "Performing tracking metrics measurements.\n"
				+ " - Tracking metrics type: " + ( ctcSelected ? "Cell Tracking Challenge" : "Single-Particle Tracking Challenge" ) + "\n"
				+ " - Ground-truth file: " + gtPath +"\n"
				+ " - Saving metrics to CSV file: " + csvFile + "\n" );
		final StringBuilder str = new StringBuilder();
		for ( int i = 0; i < type.metrics().size(); i++ )
			str.append( " - " + type.metrics().get( i ).key + ": " + type.metrics().get( i ).description + '\n' );
		logger.log( str.toString() );

		// Save CSV header.
		final String[] csvHeader = new String[ 1 + type.metrics().size() ];
		csvHeader[ 0 ] = "File";
		for ( int i = 0; i < type.metrics().size(); i++ )
			csvHeader[ i + 1 ] = type.metrics().get( i ).key;

		try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile, false ),
				CSVWriter.DEFAULT_SEPARATOR,
				CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER,
				CSVWriter.DEFAULT_LINE_END ))
		{
			csvWriter.writeNext( csvHeader );
		}
		catch ( final IOException e )
		{
			logger.error( "Error saving results to CSV file " + csvFile + ":\n" + e.getMessage() );
			return;
		}

		// Process input.
		try
		{
			final MetricsRunner runner = type.runner( gtPath, saveFolder );
			runner.setBatchLogger( logger );
			// Process candidate files.
			if ( input.isDirectory() )
			{
				// Loop over the TrackMate files it contains.
				final File[] xmlFiles = input.listFiles( ( dir, name ) -> name.toLowerCase().endsWith( ".xml" ) );
				for ( final File xmlFile : xmlFiles )
				{
					if ( isCanceled )
					{
						logger.log( "Canceled" );
						return;
					}
					process( xmlFile, runner, csvFile );
				}
			}
			else
			{
				process( input, runner, csvFile );
			}
			logger.log( "____________________________________\nDone.\n" );
		}
		catch ( final Exception e )
		{
			logger.error( "The ground-path file " + gtPath + " cannot be used:\n" + e.getMessage() );
		}
	}

	private void process( final File xmlFile, final MetricsRunner runner, final File csvFile )
	{
		logger.log( "____________________________________\nProcessing file " + xmlFile + "\n" );
		try
		{
			final TmXmlReader reader = new TmXmlReader( xmlFile );
			final Model model = reader.getModel();
			final ImagePlus imp = reader.readImage();
			final Settings settings = reader.readSettings( imp );
			final TrackMate trackmate = new TrackMate( model, settings );
			final TrackingMetrics metrics = runner.performMetricsMeasurements( trackmate );

			logger.log( metrics.toString() );

			final double[] values = metrics.toArray();
			final String[] line = new String[ 1 + values.length ];
			line[ 0 ] = xmlFile.getName();
			for ( int i = 0; i < values.length; i++ )
				line[ i + 1 ] = "" + values[ i ];

			try (CSVWriter csvWriter = new CSVWriter( new FileWriter( csvFile, true ),
					CSVWriter.DEFAULT_SEPARATOR,
					CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END ))
			{
				csvWriter.writeNext( line );
			}
			catch ( final IOException e )
			{
				logger.error( "Error saving results to CSV file " + csvFile + ":\n" + e.getMessage() );
			}

			// Loop
			if ( imp != null )
			{
				imp.changes = false;
				imp.close();
			}
		}
		catch ( final Exception ex )
		{
			logger.error( "File " + xmlFile + " is not a TrackMate file. Skipping.\n" );
			ex.printStackTrace();
		}
	}

	// --- org.scijava.Cancelable methods ---

	@Override
	public boolean isCanceled()
	{
		return isCanceled;
	}

	@Override
	public void cancel( final String reason )
	{
		isCanceled = true;
		cancelReason = reason;
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}
}
