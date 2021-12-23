package fiji.plugin.trackmate.ctc.ui.detectors;

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;
import fiji.plugin.trackmate.morpholibj.Connectivity;
import fiji.plugin.trackmate.morpholibj.MorphoLibJDetectorFactory;

public class MorphoLibJDetectorSweepModel extends AbstractSweepModel
{

	final DoubleParamSweepModel toleranceParam;

	final BooleanParamSweepModel diagonalConnectivityParam;

	final BooleanParamSweepModel simplifyContourParam;

	public MorphoLibJDetectorSweepModel()
	{
		this( new DoubleParamSweepModel(),
				new BooleanParamSweepModel(),
				new BooleanParamSweepModel() );
	}

	public MorphoLibJDetectorSweepModel(
			final DoubleParamSweepModel toleranceParam,
			final BooleanParamSweepModel diagonalConnectivityParam,
			final BooleanParamSweepModel simplifyContourParam )
	{
		this.toleranceParam = toleranceParam;
		this.diagonalConnectivityParam = diagonalConnectivityParam;
		this.simplifyContourParam = simplifyContourParam;
		// Pass listeners.
		toleranceParam.listeners().add( () -> notifyListeners() );
		diagonalConnectivityParam.listeners().add( () -> notifyListeners() );
		simplifyContourParam.listeners().add( () -> notifyListeners() );
	}

	@Override
	public List< Settings > generateSettings( final Settings base, final int targetChannel )
	{
		final List< Settings > list = new ArrayList<>();
		for ( final Number tolerance : toleranceParam.getRange() )
			for ( final Boolean diagonalConnectivity : diagonalConnectivityParam.getRange() )
				for ( final Boolean simplifyContour : simplifyContourParam.getRange() )
				{
					final Settings s = base.copyOn( base.imp );
					s.detectorFactory = new MorphoLibJDetectorFactory<>();
					final Map< String, Object > ds = s.detectorFactory.getDefaultSettings();
					ds.put( KEY_TARGET_CHANNEL, targetChannel );
					ds.put( MorphoLibJDetectorFactory.KEY_TOLERANCE, tolerance.doubleValue() );
					final Integer connectivity = diagonalConnectivity
							? Connectivity.DIAGONAL.getConnectivity()
							: Connectivity.STRAIGHT.getConnectivity();
					ds.put( MorphoLibJDetectorFactory.KEY_CONNECTIVITY, connectivity );
					ds.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContour );
					s.detectorSettings = ds;
					list.add( s );
				}

		return list;
	}
}
