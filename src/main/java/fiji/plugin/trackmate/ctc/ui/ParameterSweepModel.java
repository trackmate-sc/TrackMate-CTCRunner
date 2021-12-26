package fiji.plugin.trackmate.ctc.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
import fiji.plugin.trackmate.providers.DetectorProvider;
import ij.ImagePlus;

public class ParameterSweepModel
{

	private final Map< String, DetectorSweepModel > detectorModels = new LinkedHashMap<>();

	private final Map< String, TrackerSweepModel > trackerModels = new LinkedHashMap<>();

	private final Map< String, Boolean > active = new HashMap<>();

	private final ImagePlus imp;

	public ParameterSweepModel( final ImagePlus imp )
	{
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
		this.active.put( name, Boolean.valueOf( active ) );
	}

	public ImagePlus getImage()
	{
		return imp;
	}
}
