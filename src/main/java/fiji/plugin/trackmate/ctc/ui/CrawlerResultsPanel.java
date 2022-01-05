package fiji.plugin.trackmate.ctc.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.itextpdf.text.Font;

import fiji.plugin.trackmate.ctc.CTCMetrics;
import fiji.plugin.trackmate.ctc.CTCMetricsDescription;
import fiji.plugin.trackmate.ctc.CTCResults;
import fiji.plugin.trackmate.ctc.CTCResultsCrawler;
import fiji.plugin.trackmate.ctc.CTCResultsCrawler.CrawlerListener;
import fiji.plugin.trackmate.gui.Fonts;
import fiji.plugin.trackmate.gui.GuiUtils;
import fiji.plugin.trackmate.gui.displaysettings.Colormap;
import fiji.plugin.trackmate.util.TMUtils;
import net.imglib2.util.ValuePair;

public class CrawlerResultsPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final static CTCMetricsDescription[] descs = CTCMetricsDescription.values();

	private final static CTCMetricsDescription[] timingDescs = new CTCMetricsDescription[] {
			CTCMetricsDescription.TIM,
			CTCMetricsDescription.DETECTION_TIME,
			CTCMetricsDescription.TRACKING_TIME };

	public CrawlerResultsPanel( final CTCResultsCrawler crawler )
	{
		final CTCMetricsDescription defaultMetrics = CTCMetricsDescription.DET;

		setLayout( new BorderLayout( 0, 0 ) );
		final JTabbedPane tabbedPane = new JTabbedPane( JTabbedPane.TOP );
		add( tabbedPane, BorderLayout.CENTER );

		/*
		 * Best detector and tracker for each CTC metric.
		 */

		final JPanel panelBestDT = new JPanel();
		panelBestDT.setLayout( new BorderLayout( 0, 0 ) );
		tabbedPane.addTab( "Best detector and tracker", null, panelBestDT, null );

		final JPanel panelCount = new JPanel();
		final FlowLayout flowLayout1 = ( FlowLayout ) panelCount.getLayout();
		flowLayout1.setAlignment( FlowLayout.LEFT );
		panelBestDT.add( panelCount, BorderLayout.NORTH );

		final JLabel lblCount = new JLabel();
		lblCount.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );
		panelCount.add( lblCount );

		final JScrollPane scrollPaneBestDT = new JScrollPane();
		panelBestDT.add( scrollPaneBestDT, BorderLayout.CENTER );

		final BestDTTableModel bestDTTableModel = new BestDTTableModel( crawler );
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
		scrollPaneBestDT.setViewportView( tableDT );
		tableDT.setDefaultRenderer( Double.class, bestDTTableModel );
		tableDT.setDefaultRenderer( String.class, new MyStringCellRenderer( r -> bestDTTableModel.tooltips[ r ] ) );

		/*
		 * Best results for each detector and tracker combination.
		 */

		final JPanel panelBestVal = new JPanel();
		panelBestVal.setLayout( new BorderLayout( 0, 0 ) );
		tabbedPane.addTab( "Best values", null, panelBestVal, null );

		final JPanel panelDescChoice = new JPanel();
		final FlowLayout flowLayout2 = ( FlowLayout ) panelDescChoice.getLayout();
		flowLayout2.setAlignment( FlowLayout.LEFT );
		panelBestVal.add( panelDescChoice, BorderLayout.NORTH );

		final JLabel lblChoice = new JLabel( "Best results for each detector and tracker combination according to:" );
		lblChoice.setFont( Fonts.FONT.deriveFont( Font.BOLD ) );
		panelDescChoice.add( lblChoice );

		final JComboBox< CTCMetricsDescription > cmbboxMetrics = new JComboBox< CTCMetricsDescription >(
				new Vector<>( Arrays.asList( CTCMetricsDescription.values() ) ) )
		{

			private static final long serialVersionUID = 1L;

			// Never disable.
			@Override
			public void setEnabled( final boolean enabled )
			{}
		};
		cmbboxMetrics.setFont( Fonts.FONT );
		cmbboxMetrics.setSelectedItem( defaultMetrics );
		panelDescChoice.add( cmbboxMetrics );

		final JScrollPane scrollPaneBestVal = new JScrollPane();
		panelBestVal.add( scrollPaneBestVal, BorderLayout.CENTER );

		final BestValTableModel bestValTableModel = new BestValTableModel( crawler, defaultMetrics );
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
				( CTCMetricsDescription ) cmbboxMetrics.getSelectedItem() ) );

		final CrawlerListener l = () -> {
			bestDTTableModel.update();
			bestValTableModel.update();
			resizeColumnWidth( tableDT );
			resizeColumnWidth( tableVal );
			lblCount.setText( "Optimum over " + crawler.count() + " tests." );
			textArea.setText( crawler.printReport() );
		};
		l.crawled();
		crawler.listeners().add( l );
	}

	private static final class BestValTableModel extends AbstractTableModel implements TableCellRenderer
	{

		private static final long serialVersionUID = 1L;

		private final static CTCMetricsDescription[] descs = CTCMetricsDescription.values();

		private final CTCResultsCrawler crawler;

		private CTCMetricsDescription target;

		private final int ncols;

		private final String[] columnNames;

		private int nrows;

		private Object[][] objs;

		private String[] tooltips;

		private final DefaultTableCellRenderer renderer;

		private final double[] mint = new double[ 3 ];

		private final double[] maxt = new double[ 3 ];

		public BestValTableModel( final CTCResultsCrawler crawler, final CTCMetricsDescription target )
		{
			this.crawler = crawler;
			this.target = target;
			this.ncols = descs.length + 2;
			this.columnNames = new String[ ncols ];
			columnNames[ 0 ] = "Detector";
			columnNames[ 1 ] = "Tracker";
			for ( int i = 0; i < descs.length; i++ )
				columnNames[ i + 2 ] = descs[ i ].ctcName();
			this.renderer = new DefaultTableCellRenderer();
			update();
		}

		public void setMetrics( final CTCMetricsDescription desc )
		{
			target = desc;
			update();
		}

		public String getTooltip( final int row )
		{
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
			Arrays.fill( mint, Double.POSITIVE_INFINITY );
			Arrays.fill( maxt, Double.NEGATIVE_INFINITY );
			for ( int r = 0; r < nrows; r++ )
			{
				final String detector = ( String ) objs[ r ][ 0 ];
				final String tracker = ( String ) objs[ r ][ 1 ];

				final ValuePair< String, Integer > pair = crawler.bestFor( detector, tracker, target );
				final CTCResults results = crawler.get( pair.getA() );
				if ( results == null )
				{
					tooltips[ r ] = "No good results";
					for ( int c = 2; c < ncols; c++ )
						objs[ r ][ c ] = null;
				}
				else
				{
					final int line = pair.getB();
					final CTCMetrics metrics = results.getMetrics( line );
					for ( int i = 0; i < descs.length; i++ )
						objs[ r ][ 2 + i ] = metrics.get( descs[ i ] );

					// Tooltips.
					final Map detectorParams = results.getDetectorParams( line );
					final Map trackerParams = results.getTrackerParams( line );
					final String detector2 = results.getDetector( line );
					final String tracker2 = results.getTracker( line );
					@SuppressWarnings( "unchecked" )
					final String str = "<html>Detector parameters for " + detector2 + ":\n"
							+ TMUtils.echoMap( detectorParams, 0 )
							+ "<p>"
							+ "Tracker parameters for " + tracker2 + ":\n"
							+ TMUtils.echoMap( trackerParams, 0 )
							+ "</html>";
					tooltips[ r ] = str.replaceAll( "[\\t|\\n|\\r]", "<br>" );

					// Min & max timings.
					for ( int i = 0; i < timingDescs.length; i++ )
					{
						final double t = metrics.get( timingDescs[ i ] );
						if ( t < mint[ i ] )
							mint[ i ] = t;
						if ( t > maxt[ i ] )
							maxt[ i ] = t;
					}
				}
			}
			fireTableDataChanged();
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
					// Color code performance from 0 to 1.
					final double val = ( ( Double ) value ).doubleValue();
					if ( column >= 2 && column < 2 + descs.length - 3 )
					{
						// Metrics from 0 to 1.
						final Color bg = Double.isNaN( val ) ? null : cmap.getPaint( val );
						final Color fg = ( bg == null ) ? null : GuiUtils.textColorForBackground( bg );
						renderer.setBackground( bg );
						renderer.setForeground( fg );
					}
					else if ( column >= 9 )
					{
						// Timings. Shorter is better.
						final int i = column - 9;
						final double valt = 1. - ( val - mint[ i ] ) / ( maxt[ i ] - mint[ i ] );
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

	private static final class BestDTTableModel extends AbstractTableModel implements TableCellRenderer
	{

		private static final long serialVersionUID = 1L;

		private final CTCResultsCrawler crawler;

		private final int nrows;

		private final int ncols;

		private final String[] columnNames;

		private final Object[][] objs;

		private final String[] tooltips;

		private final DefaultTableCellRenderer renderer;

		private final double[] mint = new double[ 3 ];

		private final double[] maxt = new double[ 3 ];

		public BestDTTableModel( final CTCResultsCrawler crawler )
		{
			this.crawler = crawler;
			this.nrows = descs.length;
			this.ncols = descs.length + 4;
			this.columnNames = new String[ ncols ];
			columnNames[ 0 ] = "Best for";
			columnNames[ 1 ] = "Value";
			columnNames[ 2 ] = "Detector";
			columnNames[ 3 ] = "Tracker";
			for ( int i = 0; i < descs.length; i++ )
				columnNames[ i + 4 ] = descs[ i ].ctcName();
			// Values
			this.objs = new Object[ nrows ][ ncols ];
			// Row names.
			for ( int i = 0; i < descs.length; i++ )
				objs[ i ][ 0 ] = descs[ i ].description();
			// Tooltips.
			this.tooltips = new String[ nrows ];
			this.renderer = new DefaultTableCellRenderer();
			update();
		}

		@SuppressWarnings( { "unchecked", "rawtypes" } )
		private void update()
		{
			Arrays.fill( mint, Double.POSITIVE_INFINITY );
			Arrays.fill( maxt, Double.NEGATIVE_INFINITY );
			for ( int r = 0; r < nrows; r++ )
			{
				final CTCMetricsDescription m = descs[ r ];
				final ValuePair< String, Integer > pair = crawler.bestFor( m );
				final CTCResults results = crawler.get( pair.getA() );
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
					final CTCMetrics metrics = results.getMetrics( line );
					final String detector = results.getDetector( line );
					final String tracker = results.getTracker( line );
					objs[ r ][ 1 ] = metrics.get( m );
					objs[ r ][ 2 ] = detector;
					objs[ r ][ 3 ] = tracker;
					for ( int i = 0; i < descs.length; i++ )
						objs[ r ][ 4 + i ] = metrics.get( descs[ i ] );

					// Tooltips.
					final Map detectorParams = results.getDetectorParams( line );
					final Map trackerParams = results.getTrackerParams( line );
					final String str = "<html>Detector parameters for " + detector + ":\n"
							+ TMUtils.echoMap( detectorParams, 0 )
							+ "<p>"
							+ "Tracker parameters for " + tracker + ":\n"
							+ TMUtils.echoMap( trackerParams, 0 )
							+ "</html>";
					tooltips[ r ] = str.replaceAll( "[\\t|\\n|\\r]", "<br>" );

					// Min & max timings.
					for ( int i = 0; i < timingDescs.length; i++ )
					{
						final double t = metrics.get( timingDescs[ i ] );
						if ( t < mint[ i ] )
							mint[ i ] = t;
						if ( t > maxt[ i ] )
							maxt[ i ] = t;
					}
				}
			}
			fireTableDataChanged();
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
					// Color code performance from 0 to 1.
					final double val = ( ( Double ) value ).doubleValue();
					if ( ( column >= 4 && column < 4 + descs.length - 3 ) || ( column == 1 && row < 7 ) )
					{
						// Metrics from 0 to 1.
						final Color bg = Double.isNaN( val ) ? null : cmap.getPaint( val );
						final Color fg = ( bg == null ) ? null : GuiUtils.textColorForBackground( bg );
						renderer.setBackground( bg );
						renderer.setForeground( fg );
					}
					else if ( ( column >= 4 + descs.length - 3 ) || ( column == 1 && row >= 7 ) )
					{
						// Timings. Shorter is better.
						final int i = ( column == 1 )
								? row - 7
								: column - ( 4 + descs.length - 3 );
						final double valt = 1. - ( val - mint[ i ] ) / ( maxt[ i ] - mint[ i ] );
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
