package fiji.plugin.trackmate.helper.ui;

import static fiji.plugin.trackmate.gui.Fonts.BIG_FONT;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.scijava.util.VersionUtils;

import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.ui.components.MetricsChooserPanel;
import net.imagej.ImageJ;

public class MetricsLauncherPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public MetricsLauncherPanel()
	{
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] { 0, 0, 15, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0 };
		setLayout( gridBagLayout );

		final Image im = Icons.TRACKMATE_ICON.getImage();
		final Image newimg = im.getScaledInstance( 32, 32, java.awt.Image.SCALE_SMOOTH );
		final ImageIcon icon = new ImageIcon( newimg );

		final JLabel lblTitle = new JLabel( "TrackMate tracking metrics", icon, JLabel.LEADING );
		lblTitle.setFont( BIG_FONT );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.gridwidth = 2;
		gbcLblTitle.insets = new Insets( 0, 0, 5, 0 );
		gbcLblTitle.fill = GridBagConstraints.HORIZONTAL;
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		add( lblTitle, gbcLblTitle );

		final JLabel lblVersion = new JLabel( "v" + VersionUtils.getVersion( getClass() ) );
		lblVersion.setFont( SMALL_FONT );
		final GridBagConstraints gbcLblVersion = new GridBagConstraints();
		gbcLblVersion.anchor = GridBagConstraints.WEST;
		gbcLblVersion.gridwidth = 2;
		gbcLblVersion.insets = new Insets( 0, 0, 5, 0 );
		gbcLblVersion.gridx = 0;
		gbcLblVersion.gridy = 1;
		add( lblVersion, gbcLblVersion );

		final GridBagConstraints gbcSeparator = new GridBagConstraints();
		gbcSeparator.gridwidth = 2;
		gbcSeparator.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator.fill = GridBagConstraints.BOTH;
		gbcSeparator.gridx = 0;
		gbcSeparator.gridy = 2;
		add( new JSeparator(), gbcSeparator );

		final MetricsChooserPanel metricsChooserPanel = new MetricsChooserPanel();
		final GridBagConstraints gbcMetricsPanel = new GridBagConstraints();
		gbcMetricsPanel.gridwidth = 2;
		gbcMetricsPanel.insets = new Insets( 0, 0, 5, 0 );
		gbcMetricsPanel.fill = GridBagConstraints.BOTH;
		gbcMetricsPanel.gridx = 0;
		gbcMetricsPanel.gridy = 3;
		add( metricsChooserPanel, gbcMetricsPanel );
	}

	public static final void main( final String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );
		final JFrame frame = new JFrame();
		frame.getContentPane().add( new MetricsLauncherPanel() );
		frame.setSize( 400, 600 );
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}

}
