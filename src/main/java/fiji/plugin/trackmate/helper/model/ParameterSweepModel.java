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
package fiji.plugin.trackmate.helper.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.listeners.Listeners;
import org.scijava.log.LogService;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.helper.model.AbstractSweepModel.ModelListener;
import fiji.plugin.trackmate.helper.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.helper.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.util.TMUtils;

public class ParameterSweepModel
{

	private final transient Listeners.List< ModelListener > modelListeners;

	private final Map< String, DetectorSweepModel > detectorModels;

	private final Map< String, TrackerSweepModel > trackerModels;

	private final Map< String, Boolean > active = new HashMap<>();

	private final List< FeatureFilter > spotFilters = new ArrayList<>();

	private final List< FeatureFilter > trackFilters = new ArrayList<>();

	public ParameterSweepModel()
	{
		modelListeners = new Listeners.SynchronizedList<>();

		// Auto-detect detectors & trackers.
		detectorModels = autoDetect( DetectorSweepModel.class );
		trackerModels = autoDetect( TrackerSweepModel.class );

		// Default: everything is inactive.
		for ( final TrackerSweepModel m : trackerModels.values() )
			active.put( m.getName(), Boolean.FALSE );
		for ( final DetectorSweepModel m : detectorModels.values() )
			active.put( m.getName(), Boolean.FALSE );

		registerListeners();
	}

	public void registerListeners()
	{
		// Forward component changes to listeners.
		detectorModels().forEach( model -> model.listeners().add( () -> notifyListeners() ) );
		trackerModels().forEach( model -> model.listeners().add( () -> notifyListeners() ) );
	}

	public Collection< DetectorSweepModel > detectorModels()
	{
		return detectorModels.values();
	}

	public Collection< TrackerSweepModel > trackerModels()
	{
		return trackerModels.values();
	}

	public boolean isActive( final String name )
	{
		final Boolean val = active.get( name );
		if ( val == null )
			throw new IllegalArgumentException( "Unregistered model with name: " + name );

		return val.booleanValue();
	}

	public void setActive( final String name, final boolean active )
	{
		final Boolean previous = this.active.put( name, Boolean.valueOf( active ) );
		if ( active != previous.booleanValue() )
			notifyListeners();
	}

	public List< FeatureFilter > getSpotFilters()
	{
		return Collections.unmodifiableList( spotFilters );
	}

	public List< FeatureFilter > getTrackFilters()
	{
		return Collections.unmodifiableList( trackFilters );
	}

	public void setSpotFilters( final List< FeatureFilter > spotFilters )
	{
		this.spotFilters.clear();
		this.spotFilters.addAll( spotFilters );
	}

	public void setTrackFilters( final List< FeatureFilter > trackFilters )
	{
		this.trackFilters.clear();
		this.trackFilters.addAll( trackFilters );
	}

	public List< DetectorSweepModel > getActiveDetectors()
	{
		final List< DetectorSweepModel > activeDetectors = new ArrayList<>();
		for ( final String name : detectorModels.keySet() )
			if ( isActive( name ) )
				activeDetectors.add( detectorModels.get( name ) );

		return activeDetectors;
	}

	public List< TrackerSweepModel > getActiveTracker()
	{
		final List< TrackerSweepModel > activeTrackers = new ArrayList<>();
		for ( final String name : trackerModels.keySet() )
			if ( isActive( name ) )
				activeTrackers.add( trackerModels.get( name ) );

		return activeTrackers;
	}

	/**
	 * Returns the count of the different settings that will be generated from
	 * this model.
	 * 
	 * @return the count of settings.
	 */
	public int count()
	{
		return countDetectorSettings() * countTrackerSettings();
	}

	/**
	 * Returns the count of the different tracker settings that will be
	 * generated from this model.
	 * 
	 * @return the count of settings.
	 */
	public int countTrackerSettings()
	{
		final int targetChannel = 1;
		final Settings base = new Settings( null );
		int count = 0;
		for ( final TrackerSweepModel trackerModel : getActiveTracker() )
		{
			final Iterator< Settings > tit = trackerModel.iterator( base, targetChannel );
			while ( tit.hasNext() )
			{
				tit.next();
				count++;
			}
		}
		return count;
	}

	/**
	 * Returns the count of the different detector settings that will be
	 * generated from this model.
	 * 
	 * @return the count of settings.
	 */
	public int countDetectorSettings()
	{
		final int targetChannel = 1;
		final Settings base = new Settings( null );
		int count = 0;
		for ( final DetectorSweepModel detectorModel : getActiveDetectors() )
		{
			final Iterator< Settings > dit = detectorModel.iterator( base, targetChannel );
			while ( dit.hasNext() )
			{
				dit.next();
				count++;
			}
		}
		return count;
	}

	public Listeners.List< ModelListener > listeners()
	{
		return modelListeners;
	}

	protected void notifyListeners()
	{
		for ( final ModelListener l : modelListeners.list )
			l.modelChanged();
	}

	private static final < K extends AbstractSweepModel< ? > > Map< String, K > autoDetect( final Class< K > modelClass )
	{
		final Context context = TMUtils.getContext();
		final LogService log = context.getService( LogService.class );
		final PluginService pluginService = context.getService( PluginService.class );
		final List< PluginInfo< K > > infos = pluginService.getPluginsOfType( modelClass );

		final LinkedHashMap< String, K > models = new LinkedHashMap<>();
		for ( final PluginInfo< K > info : infos )
		{
			if ( !info.isEnabled() || !info.isVisible() )
				continue;

			try
			{
				final K instance = info.createInstance();
				final String name = instance.getName();
				models.put( name, instance );
			}
			catch ( final InstantiableException e )
			{
				log.error( "Could not instantiate " + info.getClassName(), e );
			}
		}
		return models;
	}
}
