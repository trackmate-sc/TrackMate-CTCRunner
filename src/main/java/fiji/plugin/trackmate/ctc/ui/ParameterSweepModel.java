package fiji.plugin.trackmate.ctc.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel.ModelListener;
import fiji.plugin.trackmate.ctc.ui.components.InfoParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.detectors.DetectorSweepModel;
import fiji.plugin.trackmate.ctc.ui.detectors.DetectorSweepModels;
import fiji.plugin.trackmate.ctc.ui.detectors.optional.CellposeDetector;
import fiji.plugin.trackmate.ctc.ui.detectors.optional.IlastikDetector;
import fiji.plugin.trackmate.ctc.ui.detectors.optional.MorphoLibJDetector;
import fiji.plugin.trackmate.ctc.ui.detectors.optional.StarDistDetector;
import fiji.plugin.trackmate.ctc.ui.detectors.optional.WekaDetector;
import fiji.plugin.trackmate.ctc.ui.trackers.TrackerSweepModel;
import fiji.plugin.trackmate.ctc.ui.trackers.TrackerSweepModels;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.providers.DetectorProvider;
import ij.ImagePlus;

public class ParameterSweepModel
{

	private final transient Listeners.List< ModelListener > modelListeners;

	private final Map< String, DetectorSweepModel > detectorModels = new LinkedHashMap<>();

	private final Map< String, TrackerSweepModel > trackerModels = new LinkedHashMap<>();

	private final Map< String, Boolean > active = new HashMap<>();

	private transient ImagePlus imp;

	private final List< FeatureFilter > spotFilters = new ArrayList<>();

	private final List< FeatureFilter > trackFilters = new ArrayList<>();

	private ParameterSweepModel()
	{
		modelListeners = new Listeners.SynchronizedList<>();
	}

	public ParameterSweepModel( final ImagePlus imp )
	{
		this();
		this.imp = imp;
		final String units = imp.getCalibration().getUnits();

		// Detectors.
		add( DetectorSweepModels.logDetectorModel( units ) );
		add( DetectorSweepModels.dogDetectorModel( units ) );
		add( DetectorSweepModels.maskDetectorModel() );
		add( DetectorSweepModels.thresholdDetectorModel() );
		add( DetectorSweepModels.labelImgDetectorModel() );

		// Optional modules.
		final DetectorProvider detectorProvider = new DetectorProvider();

		final DetectorSweepModel morphoLibJDetectorModel = ( null == detectorProvider.getFactory( "MORPHOLIBJ_DETECTOR" ) )
				? DetectorSweepModel.create()
						.name( "MorphoLibJ detector" )
						.add( "", new InfoParamSweepModel()
								.info( "The TrackMate-MorphoLibJ module seems to be missing<br>"
										+ "from your Fiji installation. Please follow the link<br>"
										+ "below for installation instructions." )
								.url( "https://imagej.net/plugins/trackmate/trackmate-morpholibj" ) )
						.get()
				: MorphoLibJDetector.morphoLibJDetectorModel();
		add( morphoLibJDetectorModel );

		final DetectorSweepModel wekaDetectorModel = ( null == detectorProvider.getFactory( "WEKA_DETECTOR" ) )
				? DetectorSweepModel.create()
						.name( "Weka detector" )
						.add( "", new InfoParamSweepModel()
								.info( "The TrackMate-Weka module seems to be missing<br>"
										+ "from your Fiji installation. Please follow the link<br>"
										+ "below for installation instructions." )
								.url( "https://imagej.net/plugins/trackmate/trackmate-weka" ) )
						.get()
				: WekaDetector.wekaDetectorModel();
		add( wekaDetectorModel );

		final DetectorSweepModel ilastikDetectorModel = ( null == detectorProvider.getFactory( "ILASTIK_DETECTOR" ) )
				? DetectorSweepModel.create()
						.name( "Ilastik detector" )
						.add( "", new InfoParamSweepModel()
								.info( "The TrackMate-Ilastik module seems to be missing<br>"
										+ "from your Fiji installation. Please follow the link<br>"
										+ "below for installation instructions." )
								.url( "https://imagej.net/plugins/trackmate/trackmate-ilastik" ) )
						.get()
				: IlastikDetector.ilastikDetectorModel();
		add( ilastikDetectorModel );

		final DetectorSweepModel cellposeDetectorModel = ( null == detectorProvider.getFactory( "CELLPOSE_DETECTOR" ) )
				? DetectorSweepModel.create()
						.name( "Cellpose detector" )
						.add( "", new InfoParamSweepModel()
								.info( "The TrackMate-Cellpose module seems to be missing<br>"
										+ "from your Fiji installation. Please follow the link<br>"
										+ "below for installation instructions." )
								.url( "https://imagej.net/plugins/trackmate/trackmate-cellpose" ) )
						.get()
				: CellposeDetector.cellposeDetectorModel( units );
		add( cellposeDetectorModel );

		final DetectorSweepModel stardistDetectorModel;
		final DetectorSweepModel stardistCustomDetectorModel;
		if ( null == detectorProvider.getFactory( "STARDIST_DETECTOR" ) )
		{
			stardistDetectorModel = DetectorSweepModel.create()
					.name( "StarDist detector" )
					.add( "", new InfoParamSweepModel()
							.info( "The TrackMate-StarDist module seems to be missing<br>"
									+ "from your Fiji installation. Please follow the link<br>"
									+ "below for installation instructions." )
							.url( "https://imagej.net/plugins/trackmate/trackmate-stardist" ) )
					.get();

			stardistCustomDetectorModel = DetectorSweepModel.create()
					.name( "StarDist detector custom model" )
					.add( "", new InfoParamSweepModel()
							.info( "The TrackMate-StarDist module seems to be missing<br>"
									+ "from your Fiji installation. Please follow the link<br>"
									+ "below for installation instructions." )
							.url( "https://imagej.net/plugins/trackmate/trackmate-stardist" ) )
					.get();
		}
		else
		{
			stardistDetectorModel = StarDistDetector.stardistDetectorModel();
			stardistCustomDetectorModel = StarDistDetector.stardistCustomDetectorModel();
		}
		add( stardistDetectorModel );
		add( stardistCustomDetectorModel );

		// Trackers.
		add( TrackerSweepModels.simpleLAPTrackerModel( units ) );
		add( TrackerSweepModels.lapTrackerModel( units ) );
		add( TrackerSweepModels.kalmanTrackerModel( units ) );
		add( TrackerSweepModels.overlapTrackerModel() );
		add( TrackerSweepModels.nearestNeighborTrackerModel( units ) );

		// Default: everything is inactive.
		for ( final TrackerSweepModel model : trackerModels.values() )
			active.put( model.name, Boolean.FALSE );
		for ( final DetectorSweepModel model : detectorModels.values() )
			active.put( model.name, Boolean.FALSE );

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
		this.detectorModels.put( model.name, model );
	}

	private void add( final TrackerSweepModel model )
	{
		this.trackerModels.put( model.name, model );
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

	public ImagePlus getImage()
	{
		return imp;
	}

	public void setImage( final ImagePlus imp )
	{
		this.imp = imp;
	}

	public List< FeatureFilter > spotFilters()
	{
		return Collections.unmodifiableList( spotFilters );
	}

	public List< FeatureFilter > trackFilters()
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
		final List< Settings > list = new ArrayList<>();
		final int targetChannel = 1;
		final Settings base = new Settings( imp );
		for ( final DetectorSweepModel detectorModel : getActiveDetectors() )
		{
			final List< Settings > detectorSettings = detectorModel.generateSettings( base, targetChannel );
			for ( final Settings ds : detectorSettings )
			{
				for ( final TrackerSweepModel trackerModel : getActiveTracker() )
				{
					final List< Settings > detectorAndTrackerSettings = trackerModel.generateSettings( ds, targetChannel );
					list.addAll( detectorAndTrackerSettings );
				}
			}
		}
		return list.size();
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
