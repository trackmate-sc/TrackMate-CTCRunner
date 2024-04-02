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
package fiji.plugin.trackmate.batcher.util;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jgrapht.graph.DefaultWeightedEdge;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.util.TMUtils;

public class ExcelExporter
{

	private final Model model;

	private final XSSFWorkbook wb;

	private final XSSFCellStyle integerStyle;

	private final XSSFCellStyle doubleStyle;

	private final XSSFCellStyle boldStyle;

	private final Map< Color, XSSFCellStyle > colorStyles;

	private ExcelExporter( final Model model )
	{
		this.model = model;
		this.wb = new XSSFWorkbook();

		/*
		 * Cell styles.
		 */

		// Integer.
		final short integerFormat = wb.createDataFormat().getFormat( "0" );
		this.integerStyle = wb.createCellStyle();
		integerStyle.setDataFormat( integerFormat );

		// Double.
		final short doubleFormat = wb.createDataFormat().getFormat( "0.00" );
		this.doubleStyle = wb.createCellStyle();
		doubleStyle.setDataFormat( doubleFormat );

		// Bold
		boldStyle = wb.createCellStyle();
		final XSSFFont font = wb.createFont();
		font.setBold( true );
		boldStyle.setFont( font );

		// Map of cell styles to store colors.
		this.colorStyles = new HashMap<>();
	}

	private void writeSpotTable()
	{
		final XSSFSheet sheet = wb.createSheet( "Spots" );

		/*
		 * Data.
		 */

		final List< String > features = new ArrayList<>( model.getFeatureModel().getSpotFeatures() );
		final Map< String, String > featureNames = model.getFeatureModel().getSpotFeatureNames();
		final Map< String, String > featureShortNames = model.getFeatureModel().getSpotFeatureShortNames();
		final Map< String, String > featureUnits = new HashMap<>();
		for ( final String feature : features )
		{
			final Dimension dimension = model.getFeatureModel().getSpotFeatureDimensions().get( feature );
			final String units = TMUtils.getUnitsFor( dimension, model.getSpaceUnits(), model.getTimeUnits() );
			featureUnits.put( feature, units );
		}
		final Map< String, Boolean > isInts = model.getFeatureModel().getSpotFeatureIsInt();

		/*
		 * Header.
		 */

		int rowid = 0;

		// Header 1st line.
		final Row row1 = sheet.createRow( rowid++ );
		row1.createCell( 0, CellType.STRING ).setCellValue( "LABEL" );
		row1.createCell( 1, CellType.STRING ).setCellValue( "ID" );
		row1.createCell( 2, CellType.STRING ).setCellValue( "TRACK_ID" );
		for ( int i = 0; i < features.size(); i++ )
		{
			final Cell cell = row1.createCell( 3 + i, CellType.STRING );
			cell.setCellValue( features.get( i ) );
		}
		row1.cellIterator().forEachRemaining( c -> c.setCellStyle( boldStyle ) );

		// Header 2nd line.
		final Row row2 = sheet.createRow( rowid++ );
		row2.createCell( 0, CellType.STRING ).setCellValue( "Label" );
		row2.createCell( 1, CellType.STRING ).setCellValue( "Spot ID" );
		row2.createCell( 2, CellType.STRING ).setCellValue( "Track ID" );
		for ( int i = 0; i < features.size(); i++ )
			row2.createCell( 3 + i, CellType.STRING ).setCellValue( featureNames.get( features.get( i ) ) );

		// Header 3rd line.
		final Row row3 = sheet.createRow( rowid++ );
		row3.createCell( 0, CellType.STRING ).setCellValue( "Label" );
		row3.createCell( 1, CellType.STRING ).setCellValue( "Spot ID" );
		row3.createCell( 2, CellType.STRING ).setCellValue( "Track ID" );
		for ( int i = 0; i < features.size(); i++ )
			row3.createCell( 3 + i, CellType.STRING ).setCellValue( featureShortNames.get( features.get( i ) ) );

		// Header 4th line.
		final Row row4 = sheet.createRow( rowid++ );
		row4.createCell( 0, CellType.STRING ).setCellValue( "" );
		row4.createCell( 1, CellType.STRING ).setCellValue( "" );
		row4.createCell( 2, CellType.STRING ).setCellValue( "" );
		for ( int i = 0; i < features.size(); i++ )
		{
			final String units = featureUnits.get( features.get( i ) );
			final String unitsStr = ( units == null || units.isEmpty() ) ? "" : "(" + units + ")";
			row4.createCell( 3 + i, CellType.STRING ).setCellValue( unitsStr );
		}

		final CellRangeAddress cra = new CellRangeAddress( 3, 3, 0, 2 + features.size() );
		RegionUtil.setBorderBottom( BorderStyle.THIN, cra, sheet );

		/*
		 * Content.
		 */

		final List< Spot > objects = new ArrayList<>();
		for ( final Integer trackID : model.getTrackModel().unsortedTrackIDs( true ) )
		{
			final Set< Spot > set = model.getTrackModel().trackSpots( trackID );
			final List< Spot > track = new ArrayList<>( set );
			track.sort( Spot.frameComparator );
			objects.addAll( track );
		}

		final int nRows = objects.size();
		for ( int r = 0; r < nRows; r++ )
		{
			final Row row = sheet.createRow( rowid++ );
			final Spot spot = objects.get( r );

			row.createCell( 0, CellType.STRING ).setCellValue( spot.getName() );
			row.createCell( 1, CellType.NUMERIC ).setCellValue( spot.ID() );
			row.createCell( 2, CellType.NUMERIC ).setCellValue( model.getTrackModel().trackIDOf( spot ) );

			for ( int i = 0; i < features.size(); i++ )
			{
				final String feature = features.get( i );

				// Color feature.
				if ( feature.equals( "MANUAL_SPOT_COLOR" ) )
				{
					final Double obj = spot.getFeature( feature );
					final Cell cell = row.createCell( 3 + i, CellType.STRING );
					if ( obj != null )
					{
						final int colorInt = obj.intValue();
						final Color color = new Color( colorInt, true );
						final String str = String.format( "r=%d g=%d b=%d", color.getRed(), color.getGreen(), color.getBlue() );
						XSSFCellStyle cellStyle = colorStyles.get( color );
						if ( cellStyle == null )
						{
							cellStyle = sheet.getWorkbook().createCellStyle();
							cellStyle.setFillForegroundColor( new XSSFColor( color, null ) );
							cellStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );
							colorStyles.put( color, cellStyle );
						}
						cell.setCellStyle( cellStyle );
						cell.setCellValue( str );
					}
					else
					{
						cell.setCellStyle( null );
					}
					continue;
				}

				// Other features.
				Double val = spot.getFeature( feature );
				if ( val == null )
					val = Double.NaN;
				final Cell cell = row.createCell( 3 + i, CellType.NUMERIC );
				cell.setCellValue( val );
				if ( isInts.getOrDefault( feature, Boolean.FALSE ) )
					cell.setCellStyle( integerStyle );
				else
					cell.setCellStyle( doubleStyle );
			}
		}
	}

	private void writeEdgeTable()
	{
		final XSSFSheet sheet = wb.createSheet( "Edges" );

		/*
		 * Data.
		 */

		final List< String > features = new ArrayList<>( model.getFeatureModel().getEdgeFeatures() );
		final Map< String, String > featureNames = model.getFeatureModel().getEdgeFeatureNames();
		final Map< String, String > featureShortNames = model.getFeatureModel().getEdgeFeatureShortNames();
		final Map< String, String > featureUnits = new HashMap<>();
		for ( final String feature : features )
		{
			final Dimension dimension = model.getFeatureModel().getEdgeFeatureDimensions().get( feature );
			final String units = TMUtils.getUnitsFor( dimension, model.getSpaceUnits(), model.getTimeUnits() );
			featureUnits.put( feature, units );
		}
		final Map< String, Boolean > isInts = model.getFeatureModel().getEdgeFeatureIsInt();

		/*
		 * Header.
		 */

		int rowid = 0;

		// Header 1st line.
		final Row row1 = sheet.createRow( rowid++ );
		row1.createCell( 0, CellType.STRING ).setCellValue( "LABEL" );
		row1.createCell( 1, CellType.STRING ).setCellValue( "TRACK_ID" );
		for ( int i = 0; i < features.size(); i++ )
		{
			final Cell cell = row1.createCell( 2 + i, CellType.STRING );
			cell.setCellValue( features.get( i ) );
		}
		row1.cellIterator().forEachRemaining( c -> c.setCellStyle( boldStyle ) );

		// Header 2nd line.
		final Row row2 = sheet.createRow( rowid++ );
		row2.createCell( 0, CellType.STRING ).setCellValue( "Label" );
		row2.createCell( 1, CellType.STRING ).setCellValue( "Track ID" );
		for ( int i = 0; i < features.size(); i++ )
			row2.createCell( 2 + i, CellType.STRING ).setCellValue( featureNames.get( features.get( i ) ) );

		// Header 3rd line.
		final Row row3 = sheet.createRow( rowid++ );
		row3.createCell( 0, CellType.STRING ).setCellValue( "Label" );
		row3.createCell( 1, CellType.STRING ).setCellValue( "Track ID" );
		for ( int i = 0; i < features.size(); i++ )
			row3.createCell( 2 + i, CellType.STRING ).setCellValue( featureShortNames.get( features.get( i ) ) );

		// Header 4th line.
		final Row row4 = sheet.createRow( rowid++ );
		row4.createCell( 0, CellType.STRING ).setCellValue( "" );
		row4.createCell( 1, CellType.STRING ).setCellValue( "" );
		for ( int i = 0; i < features.size(); i++ )
		{
			final String units = featureUnits.get( features.get( i ) );
			final String unitsStr = ( units == null || units.isEmpty() ) ? "" : "(" + units + ")";
			row4.createCell( 2 + i, CellType.STRING ).setCellValue( unitsStr );
		}

		final CellRangeAddress cra = new CellRangeAddress( 3, 3, 0, 1 + features.size() );
		RegionUtil.setBorderBottom( BorderStyle.THIN, cra, sheet );

		/*
		 * Content.
		 */

		final List< DefaultWeightedEdge > objects = new ArrayList<>();
		for ( final Integer trackID : model.getTrackModel().unsortedTrackIDs( true ) )
		{
			final Set< DefaultWeightedEdge > set = model.getTrackModel().trackEdges( trackID );
			final List< DefaultWeightedEdge > track = new ArrayList<>( set );
			track.sort( new Comparator< DefaultWeightedEdge >()
			{

				@Override
				public int compare( final DefaultWeightedEdge e1, final DefaultWeightedEdge e2 )
				{
					final int f1 = model.getTrackModel().getEdgeSource( e1 ).getFeature( Spot.FRAME ).intValue();
					final int f2 = model.getTrackModel().getEdgeSource( e2 ).getFeature( Spot.FRAME ).intValue();
					return f1 - f2;
				}
			} );
			objects.addAll( track );
		}

		final int nRows = objects.size();
		for ( int r = 0; r < nRows; r++ )
		{
			final Row row = sheet.createRow( rowid++ );
			final DefaultWeightedEdge edge = objects.get( r );
			final String label = String.format( "%s → %s",
					model.getTrackModel().getEdgeSource( edge ).getName(),
					model.getTrackModel().getEdgeTarget( edge ).getName() );
			row.createCell( 0, CellType.STRING ).setCellValue( label );
			row.createCell( 1, CellType.NUMERIC ).setCellValue( model.getTrackModel().trackIDOf( edge ) );

			for ( int i = 0; i < features.size(); i++ )
			{
				final String feature = features.get( i );

				// Color feature.
				if ( feature.equals( "MANUAL_EGE_COLOR" ) )
				{
					final Double obj = model.getFeatureModel().getEdgeFeature( edge, feature );
					final Cell cell = row.createCell( 2 + i, CellType.STRING );
					if ( obj != null )
					{
						final int colorInt = obj.intValue();
						final Color color = new Color( colorInt, true );
						final String str = String.format( "r=%d g=%d b=%d", color.getRed(), color.getGreen(), color.getBlue() );
						XSSFCellStyle cellStyle = colorStyles.get( color );
						if ( cellStyle == null )
						{
							cellStyle = sheet.getWorkbook().createCellStyle();
							cellStyle.setFillForegroundColor( new XSSFColor( color, null ) );
							cellStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );
							colorStyles.put( color, cellStyle );
						}
						cell.setCellStyle( cellStyle );
						cell.setCellValue( str );
					}
					else
					{
						cell.setCellStyle( null );
					}
					continue;
				}

				// Other features.
				Double val = model.getFeatureModel().getEdgeFeature( edge, feature );
				if ( val == null )
					val = Double.NaN;
				final Cell cell = row.createCell( 2 + i, CellType.NUMERIC );
				cell.setCellValue( val );
				if ( isInts.getOrDefault( feature, Boolean.FALSE ) )
					cell.setCellStyle( integerStyle );
				else
					cell.setCellStyle( doubleStyle );
			}
		}
	}

	private void writeTrackTable()
	{
		final XSSFSheet sheet = wb.createSheet( "Tracks" );

		/*
		 * Data.
		 */

		final List< String > features = new ArrayList<>( model.getFeatureModel().getTrackFeatures() );
		final Map< String, String > featureNames = model.getFeatureModel().getTrackFeatureNames();
		final Map< String, String > featureShortNames = model.getFeatureModel().getTrackFeatureShortNames();
		final Map< String, String > featureUnits = new HashMap<>();
		for ( final String feature : features )
		{
			final Dimension dimension = model.getFeatureModel().getTrackFeatureDimensions().get( feature );
			final String units = TMUtils.getUnitsFor( dimension, model.getSpaceUnits(), model.getTimeUnits() );
			featureUnits.put( feature, units );
		}
		final Map< String, Boolean > isInts = model.getFeatureModel().getTrackFeatureIsInt();

		/*
		 * Header.
		 */

		int rowid = 0;

		// Header 1st line.
		final Row row1 = sheet.createRow( rowid++ );
		row1.createCell( 0, CellType.STRING ).setCellValue( "LABEL" );
		for ( int i = 0; i < features.size(); i++ )
		{
			final Cell cell = row1.createCell( 1 + i, CellType.STRING );
			cell.setCellValue( features.get( i ) );
		}
		row1.cellIterator().forEachRemaining( c -> c.setCellStyle( boldStyle ) );

		// Header 2nd line.
		final Row row2 = sheet.createRow( rowid++ );
		row2.createCell( 0, CellType.STRING ).setCellValue( "Label" );
		for ( int i = 0; i < features.size(); i++ )
			row2.createCell( 1 + i, CellType.STRING ).setCellValue( featureNames.get( features.get( i ) ) );

		// Header 3rd line.
		final Row row3 = sheet.createRow( rowid++ );
		row3.createCell( 0, CellType.STRING ).setCellValue( "Label" );
		for ( int i = 0; i < features.size(); i++ )
			row3.createCell( 1 + i, CellType.STRING ).setCellValue( featureShortNames.get( features.get( i ) ) );

		// Header 4th line.
		final Row row4 = sheet.createRow( rowid++ );
		row4.createCell( 0, CellType.STRING ).setCellValue( "" );
		for ( int i = 0; i < features.size(); i++ )
		{
			final String units = featureUnits.get( features.get( i ) );
			final String unitsStr = ( units == null || units.isEmpty() ) ? "" : "(" + units + ")";
			row4.createCell( 1 + i, CellType.STRING ).setCellValue( unitsStr );
		}

		final CellRangeAddress cra = new CellRangeAddress( 3, 3, 0, features.size() );
		RegionUtil.setBorderBottom( BorderStyle.THIN, cra, sheet );

		/*
		 * Content.
		 */

		for ( final Integer trackID : model.getTrackModel().trackIDs( true ) )
		{
			final Row row = sheet.createRow( rowid++ );
			final String label = model.getTrackModel().name( trackID );
			row.createCell( 0, CellType.STRING ).setCellValue( label );

			for ( int i = 0; i < features.size(); i++ )
			{
				final String feature = features.get( i );
				Double val = model.getFeatureModel().getTrackFeature( trackID, feature );
				if ( val == null )
					val = Double.NaN;
				final Cell cell = row.createCell( 1 + i, CellType.NUMERIC );
				cell.setCellValue( val );
				if ( isInts.getOrDefault( feature, Boolean.FALSE ) )
					cell.setCellStyle( integerStyle );
				else
					cell.setCellStyle( doubleStyle );
			}
		}
	}

	public static XSSFWorkbook exportToWorkBook( final Model model )
	{
		final ExcelExporter exporter = new ExcelExporter( model );
		exporter.writeSpotTable();
		exporter.writeEdgeTable();
		exporter.writeTrackTable();
		return exporter.wb;
	}

	public static void main( final String[] args )
	{
		final TmXmlReader reader = new TmXmlReader( new File( "../TrackMate/samples/FakeTracks.xml" ) );
		final Model model = reader.getModel();

		final XSSFWorkbook wb = ExcelExporter.exportToWorkBook( model );
		try (FileOutputStream fileOut = new FileOutputStream( "test.xlsx" ))
		{
			wb.write( fileOut );
			wb.close();
		}
		catch ( final FileNotFoundException e )
		{
			e.printStackTrace();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
		System.out.println( "Done." );
	}
}
