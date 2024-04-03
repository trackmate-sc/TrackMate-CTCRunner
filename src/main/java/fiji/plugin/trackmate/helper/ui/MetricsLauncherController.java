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

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.scijava.Cancelable;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.TrackingMetricsType;
import fiji.plugin.trackmate.helper.ctc.CTCTrackingMetricsType;
import fiji.plugin.trackmate.helper.spt.SPTTrackingMetricsType;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import net.imagej.ImageJ;

public class MetricsLauncherController implements Cancelable
{

	private final Logger logger = Logger.IJ_LOGGER;

	private boolean isCanceled;

	private String cancelReason;

	public MetricsLauncherController()
	{
		final MetricsLauncherPanel gui = new MetricsLauncherPanel();
		final JFrame frame = new JFrame( "TrackMate tracking metrics" );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
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
						computeMetrics( ctcSelected, gtPath, inputPath, maxDist );
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

	private void computeMetrics( final boolean ctcSelected, final String gtPath, final String inputPath, final double maxDist )
	{
		isCanceled = false;

		// Save folder.
		final String saveFolder;
		final File input = new File( inputPath );
		if ( input.isDirectory() )
			saveFolder = input.getAbsolutePath();
		else
			saveFolder = input.getParent();

		logger.log( "Performing tracking metrics measurements.\n"
				+ " - Tracking metrics type: " + ( ctcSelected ? "Cell Tracking Challenge" : "Single-Particle Tracking Challenge" ) + "\n"
				+ " - Ground-truth file: " + gtPath +"\n" );
		
		// Prepare the runner.
		final TrackingMetricsType type = ctcSelected
				? new CTCTrackingMetricsType()
				: new SPTTrackingMetricsType( maxDist );

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
					process( xmlFile, runner );
				}
			}
			else
			{
				process( input, runner );
			}
			logger.log( "____________________________________\nDone.\n" );
		}
		catch ( final Exception e )
		{
			logger.error( "The ground-path file " + gtPath + " cannot be used:\n" + e.getMessage() );
		}
	}

	private void process( final File xmlFile, final MetricsRunner runner )
	{
		logger.log( "____________________________________\nProcessing file " + xmlFile + "\n" );
		try
		{
			final TmXmlReader reader = new TmXmlReader( xmlFile );
			final Model model = reader.getModel();
			/*
			 * Warning! We don't load the image for the sake of time. We will
			 * then get a default CSV name for the output.
			 */
			final Settings settings = reader.readSettings( null );
			final TrackMate trackmate = new TrackMate( model, settings );
			runner.performMetricsMeasurements( trackmate, Double.NaN, Double.NaN );
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

	public static final void main( final String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );
		new MetricsLauncherController();
	}

}
