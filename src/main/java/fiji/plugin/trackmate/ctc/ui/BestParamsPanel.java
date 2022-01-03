package fiji.plugin.trackmate.ctc.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import fiji.plugin.trackmate.ctc.CTCResultsCrawler;
import fiji.plugin.trackmate.gui.Fonts;

public class BestParamsPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final CTCResultsCrawler crawler;

	private final JTextArea textArea;

	public BestParamsPanel( final CTCResultsCrawler crawler )
	{
		this.crawler = crawler;
		setLayout( new BorderLayout( 0, 0 ) );

		textArea = new JTextArea();
		textArea.setEditable( false );
		textArea.setFont( Fonts.SMALL_FONT_MONOSPACED );
		final JScrollPane scrollPane = new JScrollPane( textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		add( scrollPane, BorderLayout.CENTER );
		update();
	}

	public void update()
	{
		textArea.setText( null );
		textArea.setText( crawler.printReport() );
	}
}
