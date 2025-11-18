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
package fiji.plugin.trackmate.helper.ui.filters;

import static fiji.plugin.trackmate.features.FeatureUtils.collectFeatureKeys;
import static fiji.plugin.trackmate.gui.Fonts.SMALL_FONT;
import static fiji.plugin.trackmate.gui.Icons.ADD_ICON;
import static fiji.plugin.trackmate.gui.Icons.REMOVE_ICON;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.trackmate.helper.model.filter.FilterSweepModel;
import fiji.plugin.trackmate.providers.SpotMorphologyAnalyzerProvider;
import ij.ImagePlus;

public abstract class AbstractFilterConfigPanel extends JPanel
{

	private static final long serialVersionUID = -1L;

	private final Stack< FilterPanel > filterPanels = new Stack<>();

	private final Stack< Component > struts = new Stack<>();

	private final Stack< FilterSweepModel > filterSweepModels = new Stack<>();

	private final JPanel allFilterPanel;

	private final TrackMateObject target;

	private final String defaultFeature;

	private final Settings settings;

	private final Consumer< FilterSweepModel > modelAdder;

	private final Consumer< FilterSweepModel > modelRemover;

	/*
	 * CONSTRUCTOR
	 */

	protected AbstractFilterConfigPanel(
			final TrackMateObject target,
			final String defaultFeature,
			final ImagePlus imp,
			final Consumer< FilterSweepModel > modelAdder,
			final Consumer< FilterSweepModel > modelRemover )
	{
		this.modelAdder = modelAdder;
		this.modelRemover = modelRemover;

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
		scrollPaneThresholds.getVerticalScrollBar().setUnitIncrement( 16 );
		this.add( scrollPaneThresholds, BorderLayout.CENTER );
		scrollPaneThresholds.setPreferredSize( new java.awt.Dimension( 250, 389 ) );
		scrollPaneThresholds.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPaneThresholds.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

		allFilterPanel = new JPanel();
		final BoxLayout jPanelAllThresholdsLayout = new BoxLayout( allFilterPanel, BoxLayout.Y_AXIS );
		allFilterPanel.setLayout( jPanelAllThresholdsLayout );
		scrollPaneThresholds.setViewportView( allFilterPanel );

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
		 * Listeners & co.
		 */

		btnAddFilter.addActionListener( e -> addFilter() );
		btnRemoveFilter.addActionListener( e -> removeFilter() );
	}

	private void addFilter()
	{
		addFilter( defaultFeature );
	}

	private void addFilter( final String feature )
	{
		final FeatureFilter filter = new FeatureFilter( feature, 0., true );
		addFilter( filter );
	}

	private void addFilter( final FeatureFilter filter )
	{
		final Map< String, String > featureNames = collectFeatureKeys( target, null, settings );
		final FilterSweepModel filterSweepModel = new FilterSweepModel( target, featureNames, filter, filterSweepModels.size() + 1 );
		modelAdder.accept( filterSweepModel );
		addFilterSilently( filterSweepModel );
	}

	/**
	 * Adds a filter panel corresponding to the specified FilterSweepModel, and
	 * does <b>not</b> notifies listeners.
	 * 
	 * @param filterSweepModel
	 *            the feature filter model.
	 */
	protected void addFilterSilently( final FilterSweepModel filterSweepModel )
	{
		final FilterPanel filterPanel = new FilterPanel( filterSweepModel );
		final Component strut = Box.createVerticalStrut( 5 );
		struts.push( strut );
		filterPanels.push( filterPanel );
		filterSweepModels.push( filterSweepModel );
		allFilterPanel.add( filterPanel );
		allFilterPanel.add( strut );
		allFilterPanel.revalidate();
	}

	private void removeFilter()
	{
		try
		{
			final FilterPanel tp = filterPanels.pop();
			final Component strut = struts.pop();
			final FilterSweepModel filterSweepModel = filterSweepModels.pop();
			modelRemover.accept( filterSweepModel );
			allFilterPanel.remove( strut );
			allFilterPanel.remove( tp );
			allFilterPanel.repaint();
		}
		catch ( final EmptyStackException ese )
		{}
	}
}
