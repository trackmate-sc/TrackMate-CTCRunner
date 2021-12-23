package fiji.plugin.trackmate.ctc.ui.components;

import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.gui.Icons.ADD_ICON;
import static fiji.plugin.trackmate.gui.Icons.REMOVE_ICON;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import fiji.plugin.trackmate.util.EverythingDisablerAndReenabler;
import fiji.plugin.trackmate.util.FileChooser;
import fiji.plugin.trackmate.util.FileChooser.DialogType;
import fiji.plugin.trackmate.util.FileChooser.SelectionMode;

public class StringRangeParamSweepPanel extends JPanel
{

	private static final long serialVersionUID = -1L;

	private final List< StringPanel > stringPanels = new ArrayList<>();

	private final List< Component > struts = new ArrayList<>();

	private final JPanel allStringPanels;

	private final StringRangeParamSweepModel values;

	private final EverythingDisablerAndReenabler enabler;

	private final boolean showBrowse;

	/*
	 * CONSTRUCTOR
	 */

	public StringRangeParamSweepPanel( final StringRangeParamSweepModel val, final boolean showBrowse )
	{
		// TODO
		this.values = val;
		this.showBrowse = showBrowse;
		this.enabler = new EverythingDisablerAndReenabler( this, new Class[] { JLabel.class } );

		this.setLayout( new BorderLayout() );
		setPreferredSize( new Dimension( 270, 500 ) );

		final JPanel topPanel = new JPanel();
		add( topPanel, BorderLayout.NORTH );
		topPanel.setLayout( new BorderLayout( 0, 0 ) );

		final JScrollPane scrollPaneThresholds = new JScrollPane();
		this.add( scrollPaneThresholds, BorderLayout.CENTER );
		scrollPaneThresholds.setPreferredSize( new java.awt.Dimension( 250, 389 ) );
		scrollPaneThresholds.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPaneThresholds.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

		allStringPanels = new JPanel();
		final BoxLayout jPanelAllThresholdsLayout = new BoxLayout( allStringPanels, BoxLayout.Y_AXIS );
		allStringPanels.setLayout( jPanelAllThresholdsLayout );
		scrollPaneThresholds.setViewportView( allStringPanels );

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout( new BorderLayout() );
		this.add( bottomPanel, BorderLayout.SOUTH );

		final JPanel buttonsPanel = new JPanel();
		bottomPanel.add( buttonsPanel, BorderLayout.NORTH );
		final BoxLayout jPanelButtonsLayout = new BoxLayout( buttonsPanel, javax.swing.BoxLayout.X_AXIS );
		buttonsPanel.setLayout( jPanelButtonsLayout );
		buttonsPanel.setPreferredSize( new java.awt.Dimension( 270, 22 ) );
		buttonsPanel.setSize( 270, 25 );
		buttonsPanel.setMaximumSize( new java.awt.Dimension( 32767, 25 ) );

		buttonsPanel.add( Box.createHorizontalStrut( 5 ) );
		final JButton btnAddFilter = new JButton();
		buttonsPanel.add( btnAddFilter );
		btnAddFilter.setIcon( ADD_ICON );
		btnAddFilter.setFont( SMALL_FONT );
		btnAddFilter.setPreferredSize( new java.awt.Dimension( 24, 24 ) );
		btnAddFilter.setSize( 24, 24 );
		btnAddFilter.setMinimumSize( new java.awt.Dimension( 24, 24 ) );

		buttonsPanel.add( Box.createHorizontalGlue() );
		buttonsPanel.add( Box.createHorizontalStrut( 5 ) );

		/*
		 * Listeners & co.
		 */

		btnAddFilter.addActionListener( e -> addStringPanel() );
	}

	public void addStringPanel()
	{
		addStringPanel( System.getProperty( "user.home" ) );
	}

	public void addStringPanel( final String str )
	{
		final StringPanel panel = new StringPanel( str, showBrowse );
		addStringPanel( panel );
	}

	public void addStringPanel( final StringPanel panel )
	{
		final Component strut = Box.createVerticalStrut( 5 );
		struts.add( strut );
		stringPanels.add( panel );
		allStringPanels.add( panel );
		allStringPanels.add( strut );

		if ( stringPanels.size() > 1 )
			stringPanels.forEach( sp -> sp.btnRemove.setVisible( true ) );

		final int id = stringPanels.size() - 1;
		panel.btnRemove.addActionListener( e -> removeStringPanel( id ) );

		allStringPanels.revalidate();
	}

	/*
	 * PRIVATE METHODS
	 */

	private void removeStringPanel( final int id )
	{
		final StringPanel stringPanel = stringPanels.remove( id );
		final Component strut = struts.remove( id );
		allStringPanels.remove( strut );
		allStringPanels.remove( stringPanel );
		allStringPanels.repaint();
	}

	/*
	 * PRIVATE CLASS.
	 */

	private class StringPanel extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final JButton btnRemove;

		private final JTextField tfStr;

		public StringPanel( final String str, final boolean showBrowse )
		{
			setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );

			btnRemove = new JButton();
			add( btnRemove );
			btnRemove.setIcon( REMOVE_ICON );
			btnRemove.setFont( SMALL_FONT );
			btnRemove.setPreferredSize( new java.awt.Dimension( 24, 24 ) );
			btnRemove.setSize( 24, 24 );
			btnRemove.setMinimumSize( new java.awt.Dimension( 24, 24 ) );

			add( Box.createHorizontalStrut( 5 ) );
			tfStr = new JTextField( str );
			tfStr.setFont( SMALL_FONT );

			if ( showBrowse )
			{
				add( Box.createHorizontalStrut( 5 ) );
				final JButton btnBrowse = new JButton( "Browse" );
				btnBrowse.setFont( SMALL_FONT );
				btnBrowse.addActionListener( e -> browse() );
			}

		}

		private void browse()
		{
			enabler.disable();
			try
			{
				final File file = FileChooser.chooseFile(
						this,
						tfStr.getText(),
						null,
						"Browse to a file",
						DialogType.LOAD,
						SelectionMode.FILES_ONLY );
				if ( file != null )
					tfStr.setText( file.getAbsolutePath() );
			}
			finally
			{
				enabler.reenable();
			}
		}
	}
}
