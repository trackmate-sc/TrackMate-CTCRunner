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
package fiji.plugin.trackmate.helper.spt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fiji.plugin.trackmate.helper.spt.importer.SPTFormatImporter;
import fiji.plugin.trackmate.helper.spt.measure.DistanceTypes;
import fiji.plugin.trackmate.helper.spt.measure.PerformanceAnalyzer;
import fiji.plugin.trackmate.helper.spt.measure.TrackProcessorPerformance;
import fiji.plugin.trackmate.helper.spt.measure.TrackSegment;

public class ISBIScoring
{

	public static final double[] score( final String referenceTrackPath, final String candidateTrackPath, final double maxDist, final DistanceTypes distType )
	{
		final List< TrackSegment > references = SPTFormatImporter.fromXML( new File( referenceTrackPath ) );
		final List< TrackSegment > candidates = SPTFormatImporter.fromXML( new File( candidateTrackPath ) );
		return score( references, candidates, maxDist, distType );
	}

	/**
	 * Returns a <code>double[]</code> array with alpha, beta, JSC, JSCtheta and
	 * RMSE.
	 * 
	 * @param references
	 *            the list of reference track segments.
	 * @param candidates
	 *            the list of candidate track segments.
	 * @return the ISBI SPT scores.
	 */
	public static final double[] score( final List< TrackSegment > references, final List< TrackSegment > candidates, final double maxDist, final DistanceTypes distType )
	{
		/*
		 * Alpha, beta and RMSE.
		 */
		final TrackProcessorPerformance processor = new TrackProcessorPerformance();
		final PerformanceAnalyzer analyzer = processor.pairTracks( references, candidates, maxDist );
		final double alpha = analyzer.getPairedTracksNormalizedDistance( distType, maxDist );
		final double beta = analyzer.getFullTrackingScore( distType, maxDist );
		final double rmse = analyzer.getDistanceDetectionData( maxDist )[ 0 ];

		/*
		 * Detection and track Jaccard indices.
		 */
		final int numSpuriousTracks = analyzer.getNumSpuriousTracks();
		final int numMissedTracks = analyzer.getNumMissedTracks();
		final int numCorrectTracks = analyzer.getNumPairedTracks();
		final int numRecoveredDetections = analyzer.getNumPairedDetections( maxDist );
		final int numMissedDetections = analyzer.getNumMissedDetections( maxDist );
		final int numWrongDetections = analyzer.getNumWrongDetections( maxDist );

		final double detectionsSimilarity = numRecoveredDetections / ( ( double ) numRecoveredDetections + ( double ) numMissedDetections + numWrongDetections );
		final double tracksSimilarity = numCorrectTracks / ( ( double ) numCorrectTracks + ( double ) numMissedTracks + numSpuriousTracks );

		return new double[] { alpha, beta, detectionsSimilarity, tracksSimilarity, rmse };
	}

	public static final void batch( final String referenceTrackPath, final String candidatesFolder, final double maxDist, final DistanceTypes distType )
	{
		System.out.println( "Processing " + candidatesFolder );
		final File folder = new File( candidatesFolder );
		final String parent = folder.getParent();
		final String outputFileName = new File( referenceTrackPath ).getName().replace( ".xml", ".csv" );
		final File outputFile = new File( parent, outputFileName );

		/*
		 * Check whether the output files exist if parse it to know what is
		 * already done.
		 */

		final Set< String > alreadyComputed = new HashSet<>();
		if ( outputFile.isFile() )
		{
			try (final BufferedReader reader = new BufferedReader( new FileReader( outputFile ) ))
			{
				String line = reader.readLine();
				while ( line != null )
				{
					final int idx = line.indexOf( ',' );
					alreadyComputed.add( line.substring( 0, idx ) );
					line = reader.readLine();
				}
			}
			catch ( final FileNotFoundException e2 )
			{
				System.out.println( "Cannot find target file " + outputFile );
				e2.printStackTrace();
			}
			catch ( final IOException e2 )
			{
				System.out.println( "Problem reading target file " + outputFile );
				e2.printStackTrace();
			}
		}
		else
		{
			try (FileWriter fw = new FileWriter( outputFile, false ))
			{
				fw.write( String.format( "%s, %s, %s, %s, %s, %s\n", "name", "alpha", "beta", "detectionsJaccard", "tracksJaccard", "rmse" ) );
			}
			catch ( final IOException e1 )
			{
				System.out.println( "Cannot write to target file " + outputFile );
				e1.printStackTrace();
			}
		}

		final File[] files = folder.listFiles( ( d, name ) -> name.endsWith( ".xml" ) );
		for ( final File file : files )
		{
			// Test whether we already processed the file.
			if ( alreadyComputed.contains( file.getName() ) )
			{
				System.out.println( " - Found results in target file for " + file.getName() + ". Skipping." );
				continue;
			}
			System.out.println( " - Processing " + file.getName() );

			try
			{
				final long start = System.currentTimeMillis();
				final double[] score = score( referenceTrackPath, file.getAbsolutePath(), maxDist, distType );
				try (FileWriter fw = new FileWriter( outputFile, true ))
				{
					fw.write( String.format( "%s, %f, %f, %f, %f, %f\n", file.getName(), score[ 0 ], score[ 1 ], score[ 2 ], score[ 3 ], score[ 4 ] ) );
				}
				final long end = System.currentTimeMillis();
				System.out.println( String.format( " - Processed %s in %.1f minutes.", file.getName(), ( end - start ) / 1000. / 60. ) );
			}
			catch ( final Exception e )
			{
				System.out.println( "Trouble dealing with file " + file + "\nSkipping." );
				e.printStackTrace();
				continue;
			}
		}
		System.out.println( "Finished processing " + candidatesFolder );
	}

	public static final void parallelise( final Map< String, String > refToFolders, final int nThreads, final double maxDist, final DistanceTypes distType )
	{
		final ExecutorService service = Executors.newFixedThreadPool( nThreads );

		refToFolders.forEach( ( ref, folder ) -> service.submit( () -> batch( ref, folder, maxDist, distType ) ) );
		service.shutdown();
	}

	public static void main( final String[] args, final double maxDist, final DistanceTypes distType )
	{
		final String gtFolder = ( args.length < 1 )
				? "/Users/tinevez/Desktop/TrackMatePaper/Data/ISBIChallengeAccuracy/gt/"
//				? "C:/Users/tinevez/Desktop/TrackMatePaper/Data/ISBIChallengeAccuracy/gt/"
				: args[ 0 ];
		final String candidateFolder = ( args.length < 2 )
				? "/Users/tinevez/Desktop/TrackMatePaper/Data/ISBIChallengeAccuracy/results"
//				? "C:/Users/tinevez/Desktop/TrackMatePaper/Data/ISBIChallengeAccuracy/results"
				: args[ 1 ];

		final String[] categories;
		if ( args.length < 3 )
		{
			categories = new String[] { "MICROTUBULE", "VESICLE", "RECEPTOR" };
		}
		else
		{
			categories = new String[ args.length - 2 ];
			for ( int i = 0; i < categories.length; i++ )
				categories[ i ] = args[ i + 2 ];
		}

		final Map< String, String > map = new HashMap<>();
		for ( final String category : categories )
		{
			final String rootGT = Paths.get( gtFolder, category ).toString();
			final String rootCF = Paths.get( candidateFolder, category ).toString();

			final File[] gtFiles = new File( rootGT ).listFiles( ( d, name ) -> name.endsWith( ".xml" ) );
			if ( null == gtFiles )
			{
				System.err.println( "Could not find any XML file in the ground-truth folder " + rootGT );
				return;
			}
			for ( final File gtFile : gtFiles )
			{
				final String candidateFolderName = gtFile.getName().substring( 0, gtFile.getName().lastIndexOf( '.' ) );
				map.put( gtFile.getAbsolutePath(), new File( rootCF, candidateFolderName ).getAbsolutePath() );
			}
		}

		parallelise( map, map.size(), maxDist, distType );
	}
}
