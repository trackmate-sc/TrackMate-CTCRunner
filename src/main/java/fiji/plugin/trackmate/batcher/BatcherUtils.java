package fiji.plugin.trackmate.batcher;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.io.TmXmlReader;
import fiji.plugin.trackmate.util.TMUtils;
import loci.formats.ImageReader;

public class BatcherUtils
{

	public static final Set< Path > collectRegularFiles( final List< String > list )
	{
		final Set< Path > paths = new LinkedHashSet<>();
		for ( final String pathStr : list )
		{
			final Path path = Paths.get( pathStr );
			if ( !Files.exists( path ) )
				continue;

			if ( Files.isDirectory( path ) )
			{
				try (final Stream< Path > children = Files.list( path ))
				{
					final List< Path > collect = children.filter( Files::isRegularFile ).collect( Collectors.toList() );
					paths.addAll( collect );
				}
				catch ( final IOException e )
				{}
				continue;
			}

			paths.add( path );
		}
		return paths;
	}

	public static final Set< Path > filterImageFiles( final Set< Path > paths )
	{
		// Test which are images. Thanks Curtis.
		final Set< Path > imageFiles = new LinkedHashSet<>();
		try (final ImageReader r = new ImageReader())
		{
			final boolean allowOpen = false;
			for ( final Path path : paths )
			{
				final boolean isImageFile = r.isThisType( path.toString(), allowOpen );
				if ( isImageFile )
					imageFiles.add( path );
			}
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
		return imageFiles;
	}

	private BatcherUtils()
	{}

	public static Workbook toExcelWorkbook( final Model model )
	{
		final XSSFWorkbook wb = new XSSFWorkbook();
		final XSSFSheet sheetSpot = wb.createSheet( "Spots" );
		writeSpotTable( sheetSpot, model );

		return wb;
	}

	private static void writeSpotTable( final XSSFSheet sheet, final Model model )
	{
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
		 * Cell styles.
		 */

		final short integerFormat = sheet.getWorkbook().createDataFormat().getFormat( "0" );
		final XSSFCellStyle integerStyle = sheet.getWorkbook().createCellStyle();
		integerStyle.setDataFormat( integerFormat );

		final short doubleFormat = sheet.getWorkbook().createDataFormat().getFormat( "0.00" );
		final XSSFCellStyle doubleStyle = sheet.getWorkbook().createCellStyle();
		doubleStyle.setDataFormat( doubleFormat );

		// Map of cell styles to store colors.
		final Map< Color, XSSFCellStyle > cellStyles = new HashMap<>();

		// Bold
		final XSSFCellStyle boldStyle = sheet.getWorkbook().createCellStyle();
		final XSSFFont font = sheet.getWorkbook().createFont();
		font.setBold( true );
		boldStyle.setFont( font );

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
			objects.addAll( model.getTrackModel().trackSpots( trackID ) );

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
						XSSFCellStyle cellStyle = cellStyles.get( color );
						if ( cellStyle == null )
						{
							cellStyle = sheet.getWorkbook().createCellStyle();
							cellStyle.setFillForegroundColor( new XSSFColor( color ) );
							cellStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );
							cellStyles.put( color, cellStyle );
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

	public static void main( final String[] args )
	{
		final TmXmlReader reader = new TmXmlReader( new File( "../TrackMate/samples/FakeTracks.xml" ) );
		final Model model = reader.getModel();
		
		final Workbook wb = toExcelWorkbook( model );
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
	}
}
