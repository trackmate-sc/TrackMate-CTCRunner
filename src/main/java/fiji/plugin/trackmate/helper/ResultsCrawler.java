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
package fiji.plugin.trackmate.helper;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.scijava.listeners.Listeners;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Settings;
import net.imglib2.util.ValuePair;

public class ResultsCrawler
{

	public interface CrawlerListener
	{
		public void crawled();
	}

	private final transient Listeners.List< CrawlerListener > listeners = new Listeners.SynchronizedList<>();

	private final Logger batchLogger;

	private final Map< String, TrackingMetricsTable > tables;

	private ResultsFolderWatcher folderWatcher;

	private final TrackingMetricsType type;

	public ResultsCrawler( final TrackingMetricsType type, final Logger batchLogger )
	{
		this.type = type;
		this.batchLogger = batchLogger;
		this.tables = new HashMap<>();
	}

	public void watch( final String folder )
	{
		stopWatching();
		this.folderWatcher = new ResultsFolderWatcher( this, folder, batchLogger );
		folderWatcher.start();
	}

	public void stopWatching()
	{
		if ( folderWatcher != null )
			folderWatcher.stopWatching();
	}

	public void reset()
	{
		tables.clear();
	}

	public String printReport()
	{
		final StringBuilder str = new StringBuilder();
		str.append( "Optimum for each " + type.name() + " metrics, over " + count( true )
				+ " valid results and " + count( false ) + " different tests." );
		str.append( "\n\n________________________________________________________________\n" );
		for ( final String key : type.metrics() )
		{
			final ValuePair< String, Integer > pair = bestFor( key );
			final TrackingMetricsTable results = get( pair.getA() );
			if ( results == null )
			{
				str.append( "There is no good configuration for " + type.description( key ) );
			}
			else
			{
				final String s = results.printLine( pair.getB() );
				str.append( String.format( "Best configuration for %s with a score of %.3f\n",
						type.description( key ),
						results.getMetrics( pair.getB() ).get( key ) ) );
				str.append( s );
			}
			str.append( "\n\n________________________________________________________________\n" );
		}

		return str.toString();
	}

	public ValuePair< String, Integer > bestFor( final String detector, final String tracker, final String key )
	{
		final BiFunction< Double, Double, Boolean > betterThan;
		double best;
		int bestLine = -1;
		String bestCSVFile = null;
		if ( TrackingMetricsType.COMMON_KEYS.contains( key ) )
		{
			// We assume that for the common keys (timing), faster is better.
			betterThan = ( val, b ) -> val < b;
			best = Double.POSITIVE_INFINITY;
		}
		else
		{
			// For the rest, we assume higher score is better.
			betterThan = ( val, b ) -> val > b;
			best = Double.NEGATIVE_INFINITY;
		}

		for ( final String csvFile : tables.keySet() )
		{
			final TrackingMetricsTable results = tables.get( csvFile );
			final int line = results.bestFor( detector, tracker, key );
			if ( line < 0 )
				continue;

			final TrackingMetrics m = results.getMetrics( line );
			final double val = m.get( key );
			if ( betterThan.apply( val, best ) )
			{
				best = val;
				bestLine = line;
				bestCSVFile = csvFile;
			}
		}
		return new ValuePair<>( bestCSVFile, bestLine );
	}

	public ValuePair< String, Integer > bestFor( final String key )
	{
		return bestFor( null, null, key );
	}

	public TrackingMetricsTable get( final String csvFile )
	{
		return tables.get( csvFile );
	}

	public synchronized void crawl( final String resultsFolder ) throws IOException
	{
		final List< String > csvFiles = findFiles( resultsFolder, "csv" );
		for ( final String csvFile : csvFiles )
		{
			try (CSVReader csvReader = new CSVReaderBuilder( new FileReader( csvFile ) ).build())
			{
				final String[] readHeader = csvReader.readNext();
				if ( !type.isHeader( readHeader ) )
				{
					batchLogger.log( String.format(
							"CSV file %s is not a CTC results file. Skipping.\n", csvFile ) );
					continue;
				}

				final TrackingMetricsTableBuilder builder = type.tableBuilder().addHeader( readHeader );
				String[] line;
				while ( ( line = csvReader.readNext() ) != null )
					builder.addFromCSV( line );

				final TrackingMetricsTable results = builder.get();
				tables.put( csvFile, results );
			}
			catch ( final IOException | CsvValidationException e )
			{
				batchLogger.error( "Cannot open CSV file " + csvFile + " for reading:\n" + e.getMessage() );
				e.printStackTrace();
			}
		}
		notifyListeners();
	}

	private static final List< String > findFiles( final String folder, final String fileExtension )
	{
		final File root = new File( folder );
		if ( !root.isDirectory() )
			throw new IllegalArgumentException( "Path must be a directory!" );

		final String fe = fileExtension.toLowerCase();
		final File[] list = root.listFiles();
		if ( list == null )
			return Collections.emptyList();

		final List< String > out = new ArrayList< String >();
		for ( final File f : list )
		{
			// Skip CTC export folders.
			final String name = f.getName();
			if ( name.endsWith( "GT" ) || name.endsWith( "ST" ) || name.endsWith( "RES" ) )
				continue;

			if ( f.isDirectory() )
				out.addAll( findFiles( f.getAbsolutePath(), fileExtension ) );
			else if ( f.getName().toLowerCase().endsWith( fe ) )
				out.add( f.getAbsolutePath() );
		}
		return out;
	}

	public synchronized boolean isSettingsPresent( final Settings settings )
	{
		for ( final TrackingMetricsTable results : tables.values() )
		{
			// Test detector.
			final int ntests = results.size();
			LINE: for ( int i = 0; i < ntests; i++ )
			{
				final String detectorKey = results.getDetector( i );
				if ( !detectorKey.equals( settings.detectorFactory.getKey() ) )
					continue;
				// Detectors are equal.

				final Map< String, Object > ds = settings.detectorSettings;
				final Map< String, String > dp = results.getDetectorParams( i );
				for ( final String key : ds.keySet() )
				{
					final Object o1 = ds.get( key );
					final String o2 = dp.get( key );
					if ( !o1.toString().equals( o2 ) )
						continue LINE;
				}
				// Detector params are equal.

				final String trackerKey = results.getTracker( i );
				if ( !trackerKey.equals( settings.trackerFactory.getKey() ) )
					continue;
				// Tracker are equal.

				final Map< String, Object > ts = settings.trackerSettings;
				final Map< String, String > tp = results.getTrackerParams( i );
				for ( final String key : ts.keySet() )
				{
					final Object o1 = ts.get( key );
					final String o2 = tp.get( key );
					if ( !o1.toString().equals( o2 ) )
						continue LINE;
				}
				// Tracker params are equal.

				return true;
			}
		}
		return false;
	}

	public Listeners.List< CrawlerListener > listeners()
	{
		return listeners;
	}

	protected void notifyListeners()
	{
		for ( final CrawlerListener l : listeners.list )
			l.crawled();
	}

	public Set< String > getDetectors()
	{
		final Set< String > set = new HashSet<>();
		for ( final TrackingMetricsTable results : tables.values() )
		{
			for ( int i = 0; i < results.size(); i++ )
				set.add( results.getDetector( i ) );
		}
		final ArrayList< String > list = new ArrayList<>( set );
		list.sort( null );
		return new LinkedHashSet<>( list );
	}

	public Set< String > getTrackers()
	{
		final Set< String > set = new HashSet<>();
		for ( final TrackingMetricsTable results : tables.values() )
		{
			for ( int i = 0; i < results.size(); i++ )
				set.add( results.getTracker( i ) );
		}
		final ArrayList< String > list = new ArrayList<>( set );
		list.sort( null );
		return new LinkedHashSet<>( list );
	}

	public Set< String > getDetectorTrackerCombination()
	{
		final Set< String > set = new HashSet<>();
		for ( final TrackingMetricsTable results : tables.values() )
		{
			for ( int i = 0; i < results.size(); i++ )
			{
				final String detector = results.getDetector( i );
				final String tracker = results.getTracker( i );
				set.add( detector + ", " + tracker );
			}
		}
		final ArrayList< String > list = new ArrayList<>( set );
		list.sort( null );
		return new LinkedHashSet<>( list );
	}

	/**
	 * Returns how many parameter combinations were discovered by this crawler.
	 * The boolean flag discriminates between counting only combinations that
	 * return a valid metrics (non-NaN results) or all combinations.
	 * 
	 * @param validOnly
	 *            if <code>true</code> will only count combinations that return
	 *            valid metrics.
	 * @return the number of parameter combinations.
	 */
	public int count( final boolean validOnly )
	{
		if ( !validOnly )
			return tables.values().stream().mapToInt( r -> r.size() ).sum();

		int count = 0;
		for ( final TrackingMetricsTable results : tables.values() )
			for ( int i = 0; i < results.size(); i++ )
				if ( !Double.isNaN( results.getMetrics( i ).isNaN() ) )
					count++;

		return count;
	}

	private static final class ResultsFolderWatcher extends Thread
	{

		private final ResultsCrawler crawler;

		private WatchService watcher;

		private final Path dir;

		private final Logger logger;

		private boolean stopped;

		public ResultsFolderWatcher( final ResultsCrawler crawler, final String folder, final Logger logger )
		{
			super( "ResultsWatcher_" + folder );
			this.crawler = crawler;
			this.logger = logger;
			this.dir = Paths.get( folder );
			try
			{
				this.watcher = FileSystems.getDefault().newWatchService();
				dir.register( watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY );
			}
			catch ( final IOException e )
			{
				e.printStackTrace();
			}
		}

		public void stopWatching()
		{
			stopped = true;
		}

		@Override
		public void run()
		{
			logger.log( "Watching folder " + dir.toString() + " for results files.\n" );
			stopped = false;
			try
			{
				while ( stopped == false )
				{
					final WatchKey key = watcher.take();
					if ( key == null )
						break;

					for ( final WatchEvent< ? > event : key.pollEvents() )
					{
						final Kind< ? > kind = event.kind();
						if ( kind == OVERFLOW )
							continue;

						// Crawl only if we have touched a CSV file.
						@SuppressWarnings( "unchecked" )
						final WatchEvent< Path > ev = ( WatchEvent< Path > ) event;
						final Path filename = ev.context();
						if ( filename.toString().toLowerCase().endsWith( "csv" ) )
						{
							try
							{
								crawler.reset();
								crawler.crawl( dir.toString() );
							}
							catch ( final IOException e )
							{
								logger.error( "Error while crawling the folder " + dir.toString() + " for CSV results file:\n" );
								logger.error( e.getMessage() );
								e.printStackTrace();
							}
						}
					}
					final boolean valid = key.reset();
					if ( !valid )
						break;
				}
				logger.log( "Stopped watching folder " + dir.toString() + " for metric results files.\n" );
			}
			catch ( final InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}
}
