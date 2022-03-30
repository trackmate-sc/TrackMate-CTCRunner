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
package fiji.plugin.trackmate.helper.ui;

import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JFrame;

import org.scijava.Cancelable;
import org.scijava.Context;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.ctc.CTCMetricsRunner;
import fiji.plugin.trackmate.helper.ctc.CTCResultsCrawler;
import fiji.plugin.trackmate.helper.model.ParameterSweepModel;
import fiji.plugin.trackmate.helper.model.ParameterSweepModelIO;
import fiji.plugin.trackmate.helper.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.helper.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.io.TmXmlWriter;
import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import fiji.plugin.trackmate.util.TMUtils;
import ij.ImagePlus;
import net.imglib2.util.ValuePair;

public class ParameterSweepController implements Cancelable
{

	private final ParameterSweepPanel gui;

	private final JFrame frame;

	private final ParameterSweepModel model;

	private String cancelReason;

	private final CTCResultsCrawler crawler;

	private final ImagePlus imp;

	private final String gtPath;

	public ParameterSweepController( final ImagePlus imp, final String gtPath, final boolean ctcSelected )
	{
		this.imp = imp;
		this.gtPath = gtPath;
		final File modelFile = ParameterSweepModelIO.makeSettingsFileForGTPath( gtPath );
		final File saveFolder = modelFile.getParentFile();
		model = ParameterSweepModelIO.readFrom( modelFile );
		crawler = new CTCResultsCrawler( Logger.DEFAULT_LOGGER );

		gui = new ParameterSweepPanel( imp, model, crawler, gtPath );
		gui.btnRun.addActionListener( e -> run() );
		gui.btnStop.addActionListener( e -> cancel( "User pressed the stop button." ) );
		gui.btnStop.setVisible( false );

		crawler.reset();
		try
		{
			crawler.crawl( saveFolder.getAbsolutePath() );
		}
		catch ( final IOException e )
		{
			gui.logger.error( "Error while crawling the folder " + saveFolder + " for CSV results file:\n" );
			gui.logger.error( e.getMessage() );
			e.printStackTrace();
		}
		crawler.watch( saveFolder.getAbsolutePath() );

		// Save on model modification.
		model.listeners().add( () -> 
		{
			gui.refresh();
			ParameterSweepModelIO.saveTo( modelFile, model );
		} );

		frame = new JFrame( "TrackMate Helper" );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final java.awt.event.WindowEvent e )
			{
				crawler.stopWatching();
			}
		} );
		frame.setIconImage( Icons.TRACKMATE_ICON.getImage() );
		frame.getContentPane().add( gui );
		frame.setSize( 600, 700 );
		frame.setLocationRelativeTo( null );
	}

	private void run()
	{
		cancelReason = null;
		// Refresh model :(
		gui.refresh();
		gui.enablers.forEach( EverythingDisablerAndReenabler::disable );
		gui.btnRun.setVisible( false );
		gui.btnStop.setVisible( true );
		gui.btnStop.setEnabled( true );
		gui.logger.setProgress( 0. );
		gui.tabbedPane.setSelectedIndex( 0 );
		final int count = model.count();
		final boolean saveEachTime = gui.chckbxSaveTrackMateFile.isSelected();
		new Thread( "TrackMate CTC runner thread" )
		{
			@Override
			public void run()
			{
				try
				{
					final int targetChannel = gui.sliderChannel.getValue();
					final Context context = TMUtils.getContext();
					final CTCMetricsRunner runner = new CTCMetricsRunner( gtPath, context );
					runner.setBatchLogger( gui.logger );

					final Settings base = new Settings( imp );
					base.setSpotFilters( model.getSpotFilters() );
					base.setTrackFilters( model.getTrackFilters() );
					int progress = 0;
					for ( final DetectorSweepModel detectorModel : model.getActiveDetectors() )
					{
						final Iterator< Settings > dit = detectorModel.iterator( base, targetChannel );
						while ( dit.hasNext() )
						{
							final Settings ds = dit.next();
							if ( isCanceled() )
								return;

							gui.logger.log( "\n________________________________________\n" );
							gui.logger.log( TMUtils.getCurrentTimeString() + "\n" );
							gui.logger.setStatus( ds.detectorFactory.getName() );

							final ValuePair< TrackMate, Double > detectionResult = runner.execDetection( ds );
							final TrackMate trackmate = detectionResult.getA();
							// Detection failed?
							if ( null == trackmate )
							{
								gui.logger.error( "Error running TrackMate with these parameters.\nSkipping.\n" );
								progress += model.countTrackerSettings();
								gui.logger.setProgress( ( double ) ++progress / count );
								continue;
							}
							// Got 0 spots to track?
							if ( trackmate.getModel().getSpots().getNSpots( true ) == 0 )
							{
								gui.logger.log( "Settings result in having 0 spots to track.\nSkipping.\n" );
								progress += model.countTrackerSettings();
								gui.logger.setProgress( ( double ) ++progress / count );
								continue;
							}
							final double detectionTiming = detectionResult.getB();

							for ( final TrackerSweepModel trackerModel : model.getActiveTracker() )
							{
								final Iterator< Settings > tit = trackerModel.iterator( ds, targetChannel );
								while ( tit.hasNext() )
								{
									final Settings dts = tit.next();
									if ( isCanceled() )
										return;

									gui.logger.setProgress( ( double ) ++progress / count );
									gui.logger.log( "________________________________________\n" );

									if ( crawler.isSettingsPresent( dts ) )
									{
										gui.logger.log( "Settings for detector " + dts.detectorFactory.getKey() + " with parameters:\n" );
										gui.logger.log( TMUtils.echoMap( dts.detectorSettings, 2 ) );
										gui.logger.log( "and tracker " + dts.trackerFactory.getKey() + " with parameters:\n" );
										gui.logger.log( TMUtils.echoMap( dts.trackerSettings, 2 ) );
										gui.logger.log( "were already tested. Skipping.\n" );
										continue;
									}

									final Settings settings = trackmate.getSettings();
									settings.trackerFactory = dts.trackerFactory;
									settings.trackerSettings = dts.trackerSettings;
									gui.logger.setStatus( settings.detectorFactory.getName() + " + " + settings.trackerFactory.getName() );

									// Exec tracking.
									final double trackingTiming = runner.execTracking( trackmate );

									// Perform and save CTC metrics measurements.
									runner.performMetricsMeasurements( trackmate, detectionTiming, trackingTiming );

									// Save TrackMate file if required.
									if ( saveEachTime )
									{
										final String nameGen = "TrackMate_%s_%s_%03d.xml";
										int i = 1;
										File trackmateFile;
										do
										{
											trackmateFile = new File( new File( gtPath ).getParent(),
													String.format( nameGen,
															settings.detectorFactory.getKey(),
															settings.trackerFactory.getKey(),
															i++ ) );
										}
										while ( trackmateFile.exists() );

										final TmXmlWriter writer = new TmXmlWriter( trackmateFile, Logger.VOID_LOGGER );
										writer.appendModel( trackmate.getModel() );
										writer.appendSettings( trackmate.getSettings() );
										writer.appendGUIState( "ConfigureViews" );
										try
										{
											writer.writeToFile();
											gui.logger.log( "Saved results to TrackMate file: " + trackmateFile + "\n" );
										}
										catch ( final IOException e )
										{
											gui.logger.error( e.getMessage() );
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
				finally
				{
					gui.btnRun.setVisible( true );
					gui.btnStop.setVisible( false );
					gui.enablers.forEach( EverythingDisablerAndReenabler::reenable );
				}
			}
		}.start();
	}

	public void show()
	{
		// It still cannot stand the Metal L&F...
		fiji.plugin.trackmate.gui.GuiUtils.setSystemLookAndFeel();
		frame.setVisible( true );
	}

	@Override
	public void cancel( final String cancelReason )
	{
		gui.btnStop.setEnabled( false );
		gui.logger.log( TMUtils.getCurrentTimeString() + " - " + cancelReason + '\n' );
		this.cancelReason = cancelReason;
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
