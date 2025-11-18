/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
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

import static fiji.plugin.trackmate.helper.TrackingMetricsTable.echoFilters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.gui.displaysettings.Colormap;
import fiji.plugin.trackmate.helper.ResultsCrawler;
import fiji.plugin.trackmate.helper.ResultsCrawler.CrawlerListener;
import fiji.plugin.trackmate.helper.TrackingMetrics;
import fiji.plugin.trackmate.helper.TrackingMetricsTable;
import fiji.plugin.trackmate.helper.TrackingMetricsType;
import fiji.plugin.trackmate.helper.TrackingMetricsType.MetricValue;
import fiji.plugin.trackmate.helper.TrackingMetricsType.MetricValueBound;
import fiji.plugin.trackmate.helper.TrackingMetricsType.MetricValueOptimum;
import fiji.plugin.trackmate.helper.ctc.TrackMateCTCUtils;
import fiji.plugin.trackmate.providers.DetectorProvider;
import fiji.plugin.trackmate.providers.TrackerProvider;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;
import fiji.plugin.trackmate.util.TMUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import net.imglib2.util.ValuePair;

public class CrawlerResultsPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public CrawlerResultsPanel( final ResultsCrawler crawler, final ImagePlus imp )
	{
		// Tooltips last longer
		ToolTipManager.sharedInstance().setDismissDelay( 30_000 ); // 30s

		final MetricValue defaultMetrics = crawler.getType().defaultMetric();

		setLayout( new BorderLayout( 0, 0 ) );
		final JTabbedPane tabbedPane = new JTabbedPane( JTabbedPane.TOP );
		add( tabbedPane, BorderLayout.CENTER );

		/*
		 * Best detector and tracker for each metric.
		 */

		final JPanel panelBestDT = new JPanel();
		panelBestDT.setLayout( new BorderLayout( 0, 0 ) );
		tabbedPane.addTab( "Best detector and tracker", null, panelBestDT, null );

		final JPanel panelCount = new JPanel();
		panelBestDT.add( panelCount, BorderLayout.NORTH );
		panelCount.setLayout( new BoxLayout( panelCount, BoxLayout.X_AXIS ) );

		final JLabel lblCount = new JLabel();
		lblCount.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );
		panelCount.add( lblCount );
		panelCount.add( Box.createHorizontalGlue() );

		final JButton btnLaunchTrackMateDT = new JButton( "Launch TrackMate with selection" );
		btnLaunchTrackMateDT.setFont( Fonts.SMALL_FONT );
		btnLaunchTrackMateDT.setIcon( Icons.TRACKMATE_ICON_16x16 );
		panelCount.add( btnLaunchTrackMateDT );

		final JScrollPane scrollPaneBestDT = new JScrollPane();
		panelBestDT.add( scrollPaneBestDT, BorderLayout.CENTER );

		final BestDTTableModel bestDTTableModel = new BestDTTableModel( crawler, imp );
		final JTable tableDT = new JTable();
		tableDT.setFont( Fonts.FONT );
		tableDT.setBackground( getBackground() );
		tableDT.getTableHeader().setOpaque( false );
		tableDT.getTableHeader().setBackground( getBackground() );
		tableDT.setRowHeight( 24 );
		tableDT.setShowGrid( false );
		tableDT.setFillsViewportHeight( true );
		tableDT.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		tableDT.setModel( bestDTTableModel );
		tableDT.getTableHeader().setFont( Fonts.FONT.deriveFont( Font.ITALIC ) );
		tableDT.setDefaultRenderer( Double.class, bestDTTableModel );
		tableDT.setDefaultRenderer( String.class, new MyStringCellRenderer( r -> bestDTTableModel.tooltips[ r ] ) );
		tableDT.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
		scrollPaneBestDT.setViewportView( tableDT );

		/*
		 * Best results for each detector and tracker combination.
		 */

		final JPanel panelBestVal = new JPanel();
		panelBestVal.setLayout( new BorderLayout( 0, 0 ) );
		tabbedPane.addTab( "Best values", null, panelBestVal, null );

		final JPanel panelDescChoice = new JPanel();
		panelBestVal.add( panelDescChoice, BorderLayout.NORTH );
		panelDescChoice.setLayout( new BoxLayout( panelDescChoice, BoxLayout.X_AXIS ) );

		final JLabel lblChoice = new JLabel( "Best results for each detector and tracker combination according to:" );
		lblChoice.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );
		panelDescChoice.add( lblChoice );

		final JComboBox< MetricValue > cmbboxMetrics = new JComboBox< MetricValue >(
				new Vector<>( crawler.getType().metrics() ) );
		cmbboxMetrics.setFont( Fonts.FONT );
		cmbboxMetrics.setSelectedItem( defaultMetrics );
		panelDescChoice.add( cmbboxMetrics );
		panelDescChoice.add( Box.createHorizontalGlue() );
		final JButton btnLaunchTrackMateVal = new JButton( "Launch TrackMate with selection" );
		btnLaunchTrackMateVal.setFont( Fonts.SMALL_FONT );
		btnLaunchTrackMateVal.setIcon( Icons.TRACKMATE_ICON_16x16 );
		panelDescChoice.add( btnLaunchTrackMateVal );

		final JScrollPane scrollPaneBestVal = new JScrollPane();
		panelBestVal.add( scrollPaneBestVal, BorderLayout.CENTER );

		final BestValTableModel bestValTableModel = new BestValTableModel( crawler, imp, defaultMetrics );
		final JTable tableVal = new JTable();
		tableVal.setFont( Fonts.FONT );
		tableVal.setRowHeight( 24 );
		tableVal.setBackground( getBackground() );
		tableVal.getTableHeader().setOpaque( false );
		tableVal.getTableHeader().setBackground( getBackground() );
		tableVal.setShowGrid( false );
		tableVal.setFillsViewportHeight( true );
		tableVal.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		tableVal.setModel( bestValTableModel );
		tableVal.setDefaultRenderer( Double.class, bestValTableModel );
		tableVal.setDefaultRenderer( String.class, new MyStringCellRenderer( r -> bestValTableModel.getTooltip( r ) ) );
		tableVal.getTableHeader().setFont( Fonts.FONT.deriveFont( Font.ITALIC ) );
		tableVal.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
		scrollPaneBestVal.setViewportView( tableVal );

		/*
		 * Report.
		 */

		final JTextArea textArea = new JTextArea();
		textArea.setBorder( null );
		textArea.setEditable( false );
		textArea.setFont( Fonts.SMALL_FONT_MONOSPACED );
		final JScrollPane scrollPaneReport = new JScrollPane( textArea );
		tabbedPane.addTab( "Report", null, scrollPaneReport, null );

		/*
		 * Listeners.
		 */

		cmbboxMetrics.addActionListener( e -> bestValTableModel.setMetrics(
				( MetricValue ) cmbboxMetrics.getSelectedItem() ) );

		final CrawlerListener l = () -> {
			bestDTTableModel.update();
			bestValTableModel.update();
			resizeColumnWidth( tableDT );
			resizeColumnWidth( tableVal );
			lblCount.setText( "Optimum over " + crawler.count( true ) + " valid results and "
					+ crawler.count( false ) + " tests." );
			textArea.setText( crawler.printReport() );
		};
		l.crawled();
		crawler.listeners().add( l );

		bestDTTableModel.addTableModelListener( e -> this.repaint() );
		btnLaunchTrackMateDT.addActionListener( e -> {
			final int row = tableDT.getSelectedRow();
			final Settings settings = bestDTTableModel.getSettingsForRow( row );
			if ( settings == null )
				return;

			TrackMateCTCUtils.launchTrackMate( settings );
		} );

		bestValTableModel.addTableModelListener( e -> this.repaint() );
		btnLaunchTrackMateVal.addActionListener( e -> {
			final int row = tableVal.getSelectedRow();
			final Settings settings = bestValTableModel.getSettingsForRow( row );
			if ( settings == null )
				return;

			TrackMateCTCUtils.launchTrackMate( settings );
		} );
	}

	private static final class BestValTableModel extends AbstractTableModel implements TableCellRenderer
	{

		private static final long serialVersionUID = 1L;

		private final ResultsCrawler crawler;

		private MetricValue target;

		private final int ncols;

		private final String[] columnNames;

		private int nrows;

		private Object[][] objs;

		private String[] tooltips;

		private final DefaultTableCellRenderer renderer;

		private final double[] mint;

		private final double[] maxt;

		private final ImagePlus imp;

		private final TrackingMetricsType type;

		private final List< MetricValue > metricKeys;

		public BestValTableModel( final ResultsCrawler crawler, final ImagePlus imp, final MetricValue target )
		{
			this.crawler = crawler;
			this.imp = imp;
			this.target = target;
			this.type = crawler.getType();
			this.metricKeys = type.metrics();
			this.mint = new double[ metricKeys.size() ];
			this.maxt = new double[ metricKeys.size() ];
			this.ncols = metricKeys.size() + 2;
			this.columnNames = new String[ ncols ];
			columnNames[ 0 ] = "Detector";
			columnNames[ 1 ] = "Tracker";
			for ( int i = 0; i < metricKeys.size(); i++ )
				columnNames[ i + 2 ] = metricKeys.get( i ).key;
			this.renderer = new DefaultTableCellRenderer();
			update();
		}

		public Settings getSettingsForRow( final int row )
		{
			if ( row < 0 || row >= objs.length )
				return null;

			final String detector = ( String ) objs[ row ][ 0 ];
			final String tracker = ( String ) objs[ row ][ 1 ];
			final ValuePair< String, Integer > pair = crawler.bestFor( detector, tracker, target );
			final TrackingMetricsTable results = crawler.get( pair.getA() );
			if ( results == null )
			{
				IJ.error( "TrackMate Helper", "No good settings to optimize " + target.description );
				return null;
			}
			final int line = pair.getB();

			final String detector2 = results.getDetector( line );
			final SpotDetectorFactoryBase< ? > detectorFactory = new DetectorProvider().getFactory( detector2 );
			if ( detectorFactory == null )
			{
				IJ.error( "TrackMate-Helper", "Detector " + detector2
						+ " is not available in this Fiji installation." );
				return null;
			}

			final String tracker2 = results.getTracker( line );
			final SpotTrackerFactory trackerFactory = new TrackerProvider().getFactory( tracker2 );
			if ( trackerFactory == null )
			{
				IJ.error( "TrackMate Helper", "Tracker " + tracker2
						+ " is not available in this Fiji installation." );
				return null;
			}

			final Map< String, String > detectorParamsStr = results.getDetectorParams( line );
			final Map< String, Object > detectorParams = TrackMateCTCUtils.castToSettings( detectorParamsStr );
			final Map< String, String > trackerParamsStr = results.getTrackerParams( line );
			final Map< String, Object > trackerParams = TrackMateCTCUtils.castToSettings( trackerParamsStr );
			final List< FeatureFilter > spotFilters = results.getSpotFilters( line );
			final List< FeatureFilter > trackFilters = results.getTrackFilters( line );

			final ImagePlus tmp;
			if ( imp == null )
			{
				final GenericDialog dialog = new GenericDialog( "Generate a TrackMate configuration" );
				dialog.addMessage( "Please select an image" );
				dialog.addImageChoice( "Target image", null );
				dialog.showDialog();
				if ( dialog.wasCanceled() )
					return null;
				tmp = dialog.getNextImage();
			}
			else
			{
				tmp = imp;
			}
			final Settings settings = new Settings( tmp );
			settings.addAllAnalyzers();
			settings.detectorFactory = detectorFactory;
			settings.detectorSettings = detectorParams;
			settings.trackerFactory = trackerFactory;
			settings.trackerSettings = trackerParams;
			settings.setSpotFilters( spotFilters );
			settings.setTrackFilters( trackFilters );
			return settings;
		}

		public void setMetrics( final MetricValue desc )
		{
			target = desc;
			update();
		}

		public String getTooltip( final int row )
		{
			if ( row >= tooltips.length )
				return "";
			return tooltips[ row ];
		}

		@SuppressWarnings( "rawtypes" )
		private void update()
		{
			// Values
			final Set< String > dts = crawler.getDetectorTrackerCombination();
			if ( dts.isEmpty() )
			{
				this.nrows = 1;
				this.objs = new Object[ 1 ][ ncols ];
				objs[ 0 ][ 0 ] = "";
				objs[ 0 ][ 1 ] = "";
				for ( int i = 2; i < ncols; i++ )
					objs[ 0 ][ i ] = Double.NaN;

				this.tooltips = new String[] { "" };
				fireTableDataChanged();
				return;
			}

			final Set< String > detectors = crawler.getDetectors();
			final Set< String > trackers = crawler.getTrackers();

			this.nrows = dts.size() + detectors.size() + trackers.size();
			this.objs = new Object[ nrows ][ ncols ];
			// Row names.
			int id = 0;
			// Combination.
			for ( final String dt : dts )
			{
				final String[] split = dt.split( "," );
				objs[ id ][ 0 ] = split[ 0 ].trim();
				objs[ id ][ 1 ] = split[ 1 ].trim();
				id++;
			}
			// Detectors only.
			for ( final String detector : detectors )
			{
				objs[ id ][ 0 ] = detector;
				objs[ id ][ 1 ] = null;
				id++;
			}
			// Trackers only.
			for ( final String tracker : trackers )
			{
				objs[ id ][ 0 ] = null;
				objs[ id ][ 1 ] = tracker;
				id++;
			}

			this.tooltips = new String[ nrows ];

			// Min & max.
			for ( int i = 0; i < metricKeys.size(); i++ )
			{
				final MetricValue mv = metricKeys.get( i );
				if ( mv.boundType == MetricValueBound.ZERO_TO_ONE )
				{
					mint[ i ] = 0.;
					maxt[ i ] = 1.;
				}
				else if ( mv.boundType == MetricValueBound.UNBOUNDED )
				{
					// initialize so that we can compute true min & max later.
					mint[ i ] = Double.POSITIVE_INFINITY;
					maxt[ i ] = Double.NEGATIVE_INFINITY;
				}
				else
				{
					throw new IllegalArgumentException( "Unknown bound type for metric value: " + mv.boundType );
				}
			}

			for ( int r = 0; r < nrows; r++ )
			{
				final String detector = ( String ) objs[ r ][ 0 ];
				final String tracker = ( String ) objs[ r ][ 1 ];

				final ValuePair< String, Integer > pair = crawler.bestFor( detector, tracker, target );
				final TrackingMetricsTable results = crawler.get( pair.getA() );
				if ( results == null )
				{
					tooltips[ r ] = "No good results";
					for ( int c = 2; c < ncols; c++ )
						objs[ r ][ c ] = null;
				}
				else
				{
					final int line = pair.getB();
					final TrackingMetrics metrics = results.getMetrics( line );
					for ( int i = 0; i < metricKeys.size(); i++ )
						objs[ r ][ 2 + i ] = metrics.get( metricKeys.get( i ) );

					// Tooltips.
					final Map detectorParams = results.getDetectorParams( line );
					final Map trackerParams = results.getTrackerParams( line );
					final String detector2 = results.getDetector( line );
					final String tracker2 = results.getTracker( line );
					final List< FeatureFilter > spotFilters = results.getSpotFilters( line );
					final List< FeatureFilter > trackFilters = results.getTrackFilters( line );
					@SuppressWarnings( "unchecked" )
					String str = "<html>Detector parameters for " + detector2 + ":\n"
							+ TMUtils.echoMap( detectorParams, 0 )
							+ "<p>"
							+ "Tracker parameters for " + tracker2 + ":\n"
							+ TMUtils.echoMap( trackerParams, 0 );

					if ( !spotFilters.isEmpty() )
						str += "<p>Spot filters:\n" + echoFilters( spotFilters );
					if ( !trackFilters.isEmpty() )
						str += "<p>Track filters:\n" + echoFilters( trackFilters );

					str += "</html>";
					tooltips[ r ] = str.replaceAll( "[\\t|\\n|\\r]", "<br>" );

					// Min & max for unbounded values.
					for ( int i = 0; i < metricKeys.size(); i++ )
					{
						final MetricValue mv = metricKeys.get( i );
						if ( mv.boundType != MetricValueBound.UNBOUNDED )
							continue;

						final double val = metrics.get( mv );
						if ( val > maxt[ i ] )
							maxt[ i ] = val;
						if ( val < mint[ i ] )
							mint[ i ] = val;
					}
				}
			}
			SwingUtilities.invokeLater( () -> fireTableDataChanged() );
		}

		@Override
		public int getRowCount()
		{
			return nrows;
		}

		@Override
		public int getColumnCount()
		{
			return ncols;
		}

		@Override
		public Object getValueAt( final int rowIndex, final int columnIndex )
		{
			if ( objs.length == 0 || rowIndex >= objs.length || columnIndex >= objs[ 0 ].length )
				return null;

			return objs[ rowIndex ][ columnIndex ];
		}

		@Override
		public Class< ? > getColumnClass( final int columnIndex )
		{
			switch ( columnIndex )
			{
			case 0:
			case 1:
				return String.class;
			default:
				return Double.class;
			}
		}

		@Override
		public String getColumnName( final int column )
		{
			return columnNames[ column ];
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			renderer.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
			renderer.setBackground( null );
			renderer.setForeground( null );
			if ( value instanceof Double )
			{
				renderer.setHorizontalAlignment( SwingConstants.RIGHT );
				if ( null == value || Double.isNaN( ( ( Double ) value ).doubleValue() ) )
					renderer.setText( "Ø" );
				else
					renderer.setText( nf.format( value ) );

				if ( isSelected )
				{
					renderer.setForeground( table.getSelectionForeground() );
					renderer.setBackground( table.getSelectionBackground() );
				}
				else
				{
					// Color code performance.
					if ( column >= 2 && column < 2 + metricKeys.size() )
					{
						// Corresponding metric value
						final int i = column - 2;
						final double val = ( ( Double ) value ).doubleValue();
						double valt = ( val - mint[ i ] ) / ( maxt[ i ] - mint[ i ] );

						final MetricValue mv = metricKeys.get( i );
						if ( mv.optimumType == MetricValueOptimum.LOWER_IS_BETTER )
							valt = 1. - valt;

						final Color bg = Double.isNaN( val ) ? null : cmap.getPaint( valt );
						final Color fg = ( bg == null ) ? null : GuiUtils.textColorForBackground( bg );
						renderer.setBackground( bg );
						renderer.setForeground( fg );
					}
				}
				renderer.setPreferredSize( new Dimension( 60, 24 ) );
			}

			renderer.setBorder( padding );
			renderer.setToolTipText( row >= tooltips.length ? "" : tooltips[ row ] );
			return renderer;
		}
	}

	private static final class BestDTTableModel extends AbstractTableModel implements TableCellRenderer
	{

		private static final long serialVersionUID = 1L;

		private final ResultsCrawler crawler;

		private final int nrows;

		private final int ncols;

		private final String[] columnNames;

		private final Object[][] objs;

		private final String[] tooltips;

		private final DefaultTableCellRenderer renderer;

		private final double[] mint;

		private final double[] maxt;

		private final ImagePlus imp;

		private final List< MetricValue > metricKeys;

		public BestDTTableModel( final ResultsCrawler crawler, final ImagePlus imp )
		{
			this.crawler = crawler;
			this.imp = imp;
			this.metricKeys = crawler.getType().metrics();
			this.mint = new double[ metricKeys.size() ];
			this.maxt = new double[ metricKeys.size() ];
			this.nrows = metricKeys.size();
			this.ncols = metricKeys.size() + 4;
			this.columnNames = new String[ ncols ];
			columnNames[ 0 ] = "Best for";
			columnNames[ 1 ] = "Value";
			columnNames[ 2 ] = "Detector";
			columnNames[ 3 ] = "Tracker";
			for ( int i = 0; i < metricKeys.size(); i++ )
				columnNames[ i + 4 ] = metricKeys.get( i ).key;
			// Values
			this.objs = new Object[ nrows ][ ncols ];
			// Row names.
			for ( int i = 0; i < metricKeys.size(); i++ )
				objs[ i ][ 0 ] = metricKeys.get( i ).description;
			// Tooltips.
			this.tooltips = new String[ nrows ];
			this.renderer = new DefaultTableCellRenderer();
			update();
		}

		public Settings getSettingsForRow( final int row )
		{
			if ( row < 0 || row >= objs.length )
				return null;

			final MetricValue m = metricKeys.get( row );
			final ValuePair< String, Integer > pair = crawler.bestFor( m );
			final TrackingMetricsTable results = crawler.get( pair.getA() );
			if ( results == null )
			{
				IJ.error( "TrackMate Helper", "No good settings to optimize "
						+ m.description );
				return null;
			}
			else
			{
				// Values.
				final int line = pair.getB();
				final String detector = results.getDetector( line );
				final SpotDetectorFactoryBase< ? > detectorFactory = new DetectorProvider().getFactory( detector );
				if ( detectorFactory == null )
				{
					IJ.error( "TrackMate Helper", "Detector " + detector
							+ " is not available in this Fiji installation." );
					return null;
				}
				final String tracker = results.getTracker( line );
				final SpotTrackerFactory trackerFactory = new TrackerProvider().getFactory( tracker );
				if ( trackerFactory == null )
				{
					IJ.error( "TrackMate Helper", "Tracker " + tracker
							+ " is not available in this Fiji installation." );
					return null;
				}

				final Map< String, String > detectorParamsStr = results.getDetectorParams( line );
				final Map< String, Object > detectorParams = TrackMateCTCUtils.castToSettings( detectorParamsStr );
				final Map< String, String > trackerParamsStr = results.getTrackerParams( line );
				final Map< String, Object > trackerParams = TrackMateCTCUtils.castToSettings( trackerParamsStr );
				final List< FeatureFilter > spotFilters = results.getSpotFilters( line );
				final List< FeatureFilter > trackFilters = results.getTrackFilters( line );
				
				final ImagePlus tmp;
				if ( imp == null )
				{
					if ( WindowManager.getImageCount() == 0 )
					{
						IJ.error( "TrackMate Helper", "Please open an image first." );
						return null;
					}

					final GenericDialog dialog = new GenericDialog( "Generate a TrackMate configuration" );
					dialog.addMessage( "Please select an image" );
					dialog.addImageChoice( "Target image", null );
					dialog.showDialog();
					if ( dialog.wasCanceled() )
						return null;
					tmp = dialog.getNextImage();
				}
				else
				{
					tmp = imp;
				}

				final Settings settings = new Settings( tmp );
				settings.addAllAnalyzers();
				settings.detectorFactory = detectorFactory;
				settings.detectorSettings = detectorParams;
				settings.trackerFactory = trackerFactory;
				settings.trackerSettings = trackerParams;
				settings.setSpotFilters( spotFilters );
				settings.setTrackFilters( trackFilters );
				return settings;
			}
		}

		@SuppressWarnings( { "unchecked", "rawtypes" } )
		private void update()
		{
			// Min & max.
			for ( int i = 0; i < metricKeys.size(); i++ )
			{
				final MetricValue mv = metricKeys.get( i );
				if ( mv.boundType == MetricValueBound.ZERO_TO_ONE )
				{
					mint[ i ] = 0.;
					maxt[ i ] = 1.;
				}
				else if ( mv.boundType == MetricValueBound.UNBOUNDED )
				{
					// initialize so that we can compute true min & max later.
					mint[ i ] = Double.POSITIVE_INFINITY;
					maxt[ i ] = Double.NEGATIVE_INFINITY;
				}
				else
				{
					throw new IllegalArgumentException( "Unknown bound type for metric value: " + mv.boundType );
				}
			}

			for ( int r = 0; r < nrows; r++ )
			{
				final MetricValue m = metricKeys.get( r );
				final ValuePair< String, Integer > pair = crawler.bestFor( m );
				final TrackingMetricsTable results = crawler.get( pair.getA() );
				if ( results == null )
				{
					tooltips[ r ] = "No good results";
					for ( int c = 1; c < ncols; c++ )
						objs[ r ][ c ] = Double.NaN;
					objs[ r ][ 2 ] = null;
					objs[ r ][ 3 ] = null;
				}
				else
				{
					// Values.
					final int line = pair.getB();
					final TrackingMetrics metrics = results.getMetrics( line );
					final String detector = results.getDetector( line );
					final String tracker = results.getTracker( line );
					objs[ r ][ 1 ] = metrics.get( m );
					objs[ r ][ 2 ] = detector;
					objs[ r ][ 3 ] = tracker;
					for ( int i = 0; i < metricKeys.size(); i++ )
						objs[ r ][ 4 + i ] = metrics.get( metricKeys.get( i ) );

					// Tooltips.
					final Map detectorParams = results.getDetectorParams( line );
					final Map trackerParams = results.getTrackerParams( line );
					final List< FeatureFilter > spotFilters = results.getSpotFilters( line );
					final List< FeatureFilter > trackFilters = results.getTrackFilters( line );

					String str = "<html>Detector parameters for " + detector + ":\n"
							+ TMUtils.echoMap( detectorParams, 0 )
							+ "<p>"
							+ "Tracker parameters for " + tracker + ":\n"
							+ TMUtils.echoMap( trackerParams, 0 );

					if ( !spotFilters.isEmpty() )
						str += "<p>Spot filters:\n" + echoFilters( spotFilters );
					if ( !trackFilters.isEmpty() )
						str += "<p>Track filters:\n" + echoFilters( trackFilters );

					str += "</html>";
					
					tooltips[ r ] = str.replaceAll( "[\\t|\\n|\\r]", "<br>" );

					// Min & max for unbounded values.
					for ( int i = 0; i < metricKeys.size(); i++ )
					{
						final MetricValue mv = metricKeys.get( i );
						if ( mv.boundType != MetricValueBound.UNBOUNDED )
							continue;

						final double val = metrics.get( mv );
						if ( val > maxt[ i ] )
							maxt[ i ] = val;
						if ( val < mint[ i ] )
							mint[ i ] = val;
					}
				}
			}
			SwingUtilities.invokeLater( () -> fireTableDataChanged() );
		}

		@Override
		public int getRowCount()
		{
			return nrows;
		}

		@Override
		public int getColumnCount()
		{
			return ncols;
		}

		@Override
		public Object getValueAt( final int rowIndex, final int columnIndex )
		{
			if ( objs.length == 0 || rowIndex >= objs.length || columnIndex >= objs[ 0 ].length )
				return null;

			return objs[ rowIndex ][ columnIndex ];
		}

		@Override
		public Class< ? > getColumnClass( final int columnIndex )
		{
			switch ( columnIndex )
			{
			case 0:
			case 2:
			case 3:
				return String.class;
			default:
				return Double.class;
			}
		}

		@Override
		public String getColumnName( final int column )
		{
			return columnNames[ column ];
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			renderer.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
			renderer.setBackground( null );
			renderer.setForeground( null );
			if ( value instanceof Double )
			{
				renderer.setHorizontalAlignment( SwingConstants.RIGHT );
				if ( null == value || Double.isNaN( ( ( Double ) value ).doubleValue() ) )
					renderer.setText( "Ø" );
				else
					renderer.setText( nf.format( value ) );

				if ( isSelected )
				{
					renderer.setForeground( table.getSelectionForeground() );
					renderer.setBackground( table.getSelectionBackground() );
				}
				else
				{
					// Color code performance.
					if ( ( column >= 4 && column < 4 + metricKeys.size() ) || ( column == 1 ) )
					{
						final double val = ( ( Double ) value ).doubleValue();
						// Corresponding metric value
						final int i;
						if ( column >= 4 && column < 4 + metricKeys.size() )
							i = column - 4;
						else
							i = row;
						double valt = ( val - mint[ i ] ) / ( maxt[ i ] - mint[ i ] );

						final MetricValue mv = metricKeys.get( i );
						if ( mv.optimumType == MetricValueOptimum.LOWER_IS_BETTER )
							valt = 1. - valt;

						final Color bg = Double.isNaN( val ) ? null : cmap.getPaint( valt );
						final Color fg = ( bg == null ) ? null : GuiUtils.textColorForBackground( bg );
						renderer.setBackground( bg );
						renderer.setForeground( fg );
					}
				}
				renderer.setPreferredSize( new Dimension( 60, 24 ) );
			}

			renderer.setBorder( padding );
			renderer.setToolTipText( tooltips[ row ] );
			return renderer;
		}
	}

	private final static Border padding = BorderFactory.createEmptyBorder( 0, 10, 0, 10 );

	private static final NumberFormat nf = new DecimalFormat( "0.000" );

	private static final Colormap cmap = Colormap.Viridis;

	private static final void resizeColumnWidth( final JTable table )
	{
		final TableColumnModel columnModel = table.getColumnModel();
		for ( int column = 0; column < table.getColumnCount(); column++ )
		{
			int width = 75; // Min width
			for ( int row = 0; row < table.getRowCount(); row++ )
			{
				final TableCellRenderer renderer = table.getCellRenderer( row, column );
				final Component comp = table.prepareRenderer( renderer, row, column );
				width = Math.max( comp.getPreferredSize().width + 1, width );
			}
			columnModel.getColumn( column ).setPreferredWidth( width );
		}
	}

	private static final class MyStringCellRenderer extends DefaultTableCellRenderer
	{

		private static final long serialVersionUID = 1L;

		private final Function< Integer, String > tooltipSupplier;

		public MyStringCellRenderer( final Function< Integer, String > tooltipSupplier )
		{
			this.tooltipSupplier = tooltipSupplier;
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
			setBorder( padding );
			setToolTipText( tooltipSupplier.apply( row ) );
			return this;
		}

	}
}
