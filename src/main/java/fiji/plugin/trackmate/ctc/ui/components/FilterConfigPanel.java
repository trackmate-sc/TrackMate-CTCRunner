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
package fiji.plugin.trackmate.ctc.ui.components;

import static fiji.plugin.trackmate.features.FeatureUtils.collectFeatureKeys;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.gui.Icons.ADD_ICON;
import static fiji.plugin.trackmate.gui.Icons.REMOVE_ICON;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.trackmate.providers.SpotMorphologyAnalyzerProvider;
import ij.ImagePlus;

public class FilterConfigPanel extends JPanel
{

	private static final long serialVersionUID = -1L;

	private final Stack< FilterPanel > filterPanels = new Stack<>();

	private final Stack< Component > struts = new Stack<>();

	private final JPanel allThresholdsPanel;

	private final TrackMateObject target;

	private final Settings settings;

	private final String defaultFeature;

	/*
	 * CONSTRUCTOR
	 */

	public FilterConfigPanel( 
			final TrackMateObject target,
			final String defaultFeature,
			final ImagePlus imp,
			final List< FeatureFilter > filters )
	{
		// Config a settings so that we can get all available features.
		this.settings = new Settings( imp );
		settings.addAllAnalyzers();
		final SpotMorphologyAnalyzerProvider spotMorphologyAnalyzerProvider = new SpotMorphologyAnalyzerProvider( imp.getNChannels() );
		final List< String > spotMorphologyAnaylyzerKeys = spotMorphologyAnalyzerProvider.getKeys();
		for ( final String key : spotMorphologyAnaylyzerKeys )
			settings.addSpotAnalyzerFactory( spotMorphologyAnalyzerProvider.getFactory( key ) );

		this.target = target;
		this.defaultFeature = defaultFeature;

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

		allThresholdsPanel = new JPanel();
		final BoxLayout jPanelAllThresholdsLayout = new BoxLayout( allThresholdsPanel, BoxLayout.Y_AXIS );
		allThresholdsPanel.setLayout( jPanelAllThresholdsLayout );
		scrollPaneThresholds.setViewportView( allThresholdsPanel );

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

		buttonsPanel.add( Box.createHorizontalStrut( 5 ) );
		final JButton btnRemoveFilter = new JButton();
		buttonsPanel.add( btnRemoveFilter );
		btnRemoveFilter.setIcon( REMOVE_ICON );
		btnRemoveFilter.setFont( SMALL_FONT );
		btnRemoveFilter.setPreferredSize( new java.awt.Dimension( 24, 24 ) );
		btnRemoveFilter.setSize( 24, 24 );
		btnRemoveFilter.setMinimumSize( new java.awt.Dimension( 24, 24 ) );

		buttonsPanel.add( Box.createHorizontalGlue() );
		buttonsPanel.add( Box.createHorizontalStrut( 5 ) );
		
		/*
		 * Default values.
		 */

		for ( final FeatureFilter filter : filters )
			addFilter( filter );

		/*
		 * Listeners & co.
		 */

		btnAddFilter.addActionListener( e -> addFilter() );
		btnRemoveFilter.addActionListener( e -> removeFilter() );
	}

	private void addFilter()
	{
		addFilter( guessNextFeature() );
	}

	private void addFilter( final String feature )
	{
		final FeatureFilter filter = new FeatureFilter( feature, 0., true );
		addFilter( filter );
	}

	private void addFilter( final FeatureFilter filter )
	{
		final Map< String, String > featureNames = collectFeatureKeys( target, null, settings );
		final FilterPanel tp = new FilterPanel( featureNames, filter );

		final Component strut = Box.createVerticalStrut( 5 );
		struts.push( strut );
		filterPanels.push( tp );
		allThresholdsPanel.add( tp );
		allThresholdsPanel.add( strut );
		allThresholdsPanel.revalidate();
	}

	private String guessNextFeature()
	{
		final Map< String, String > featureNames = collectFeatureKeys( target, null, settings );
		final Iterator< String > it = featureNames.keySet().iterator();
		if ( !it.hasNext() )
			return ""; // It's likely something is not right.

		final List< FeatureFilter > featureFilters = getFeatureFilters();
		if ( featureFilters.isEmpty() )
			return ( defaultFeature == null || !featureNames.keySet().contains( defaultFeature ) ) ? it.next() : defaultFeature;

		final FeatureFilter lastFilter = featureFilters.get( featureFilters.size() - 1 );
		final String lastFeature = lastFilter.feature;
		while ( it.hasNext() )
			if ( it.next().equals( lastFeature ) && it.hasNext() )
				return it.next();

		return featureNames.keySet().iterator().next();
	}

	private void removeFilter()
	{
		try
		{
			final FilterPanel tp = filterPanels.pop();
			final Component strut = struts.pop();
			allThresholdsPanel.remove( strut );
			allThresholdsPanel.remove( tp );
			allThresholdsPanel.repaint();
		}
		catch ( final EmptyStackException ese )
		{}
	}

	public List< FeatureFilter > getFeatureFilters()
	{
		final List< FeatureFilter > list = new ArrayList<>( filterPanels.size() );
		filterPanels.forEach( fp -> list.add( fp.getFilter() ) );
		return list;
	}
}
