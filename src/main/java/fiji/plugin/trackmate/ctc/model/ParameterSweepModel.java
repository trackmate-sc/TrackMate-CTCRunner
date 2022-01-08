package fiji.plugin.trackmate.ctc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.model.AbstractSweepModel.ModelListener;
import fiji.plugin.trackmate.ctc.model.detector.CellposeDetectorModel;
import fiji.plugin.trackmate.ctc.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.ctc.model.detector.DogDetectorModel;
import fiji.plugin.trackmate.ctc.model.detector.IlastikDetectorModel;
import fiji.plugin.trackmate.ctc.model.detector.LabelImgDetectorModel;
import fiji.plugin.trackmate.ctc.model.detector.LogDetectorModel;
import fiji.plugin.trackmate.ctc.model.detector.MaskDetectorModel;
import fiji.plugin.trackmate.ctc.model.detector.MorphoLibJDetectorModel;
import fiji.plugin.trackmate.ctc.model.detector.StarDistCustomDetectorModel;
import fiji.plugin.trackmate.ctc.model.detector.StarDistDetectorModel;
import fiji.plugin.trackmate.ctc.model.detector.ThresholdDetectorModel;
import fiji.plugin.trackmate.ctc.model.detector.WekaDetectorModel;
import fiji.plugin.trackmate.ctc.model.tracker.KalmanTrackerModel;
import fiji.plugin.trackmate.ctc.model.tracker.LAPTrackerModel;
import fiji.plugin.trackmate.ctc.model.tracker.NearestNeighborTrackerModel;
import fiji.plugin.trackmate.ctc.model.tracker.OverlapTrackerModel;
import fiji.plugin.trackmate.ctc.model.tracker.SimpleLAPTrackerModel;
import fiji.plugin.trackmate.ctc.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.features.FeatureFilter;

public class ParameterSweepModel
{

	private final transient Listeners.List< ModelListener > modelListeners;

	private final Map< String, DetectorSweepModel > detectorModels = new LinkedHashMap<>();

	private final Map< String, TrackerSweepModel > trackerModels = new LinkedHashMap<>();

	private final Map< String, Boolean > active = new HashMap<>();

	private final List< FeatureFilter > spotFilters = new ArrayList<>();

	private final List< FeatureFilter > trackFilters = new ArrayList<>();

	public ParameterSweepModel()
	{
		modelListeners = new Listeners.SynchronizedList<>();

		// Detectors.
		add( new LogDetectorModel() );
		add( new DogDetectorModel() );
		add( new MaskDetectorModel() );
		add( new ThresholdDetectorModel() );
		add( new LabelImgDetectorModel() );

		// Optional detector modules. Stuff that might not be installed.
		add( new MorphoLibJDetectorModel() );
		add( new IlastikDetectorModel() );
		add( new CellposeDetectorModel() );
		add( new WekaDetectorModel() );
		add( new StarDistDetectorModel() );
		add( new StarDistCustomDetectorModel() );

		// Trackers.
		add( new SimpleLAPTrackerModel() );
		add( new LAPTrackerModel() );
		add( new KalmanTrackerModel() );
		add( new OverlapTrackerModel() );
		add( new NearestNeighborTrackerModel() );

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

	private void add( final DetectorSweepModel model )
	{
		this.detectorModels.put( model.getName(), model );
	}

	private void add( final TrackerSweepModel model )
	{
		this.trackerModels.put( model.getName(), model );
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
		final int targetChannel = 1;
		final Settings base = new Settings( null );
		int count = 0;
		for ( final DetectorSweepModel detectorModel : getActiveDetectors() )
		{
			final Iterator< Settings > dit = detectorModel.iterator( base, targetChannel );
			while ( dit.hasNext() )
			{
				final Settings ds = dit.next();
				for ( final TrackerSweepModel trackerModel : getActiveTracker() )
				{
					final Iterator< Settings > tit = trackerModel.iterator( ds, targetChannel );
					while ( tit.hasNext() )
					{
						tit.next();
						count++;
					}
				}
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
}
