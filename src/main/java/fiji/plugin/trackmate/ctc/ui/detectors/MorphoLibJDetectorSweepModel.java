package fiji.plugin.trackmate.ctc.ui.detectors;

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;
import fiji.plugin.trackmate.morpholibj.Connectivity;
import fiji.plugin.trackmate.morpholibj.MorphoLibJDetectorFactory;

public class MorphoLibJDetectorSweepModel extends DetectorSweepModel
{

	public static final MorphoLibJDetectorSweepModel make()
	{
		final DoubleParamSweepModel toleranceParam = new DoubleParamSweepModel()
				.paramName( "Tolerance" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 40. )
				.max( 60. )
				.nSteps( 3 );
		final BooleanParamSweepModel diagonalConnectivityParam = new BooleanParamSweepModel()
				.paramName( "Diagonal connectivity" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final BooleanParamSweepModel simplifyContourParam = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
		models.put( MorphoLibJDetectorFactory.KEY_TOLERANCE, toleranceParam );
		models.put( MorphoLibJDetectorFactory.KEY_CONNECTIVITY, diagonalConnectivityParam );
		models.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContourParam );
		return new MorphoLibJDetectorSweepModel( models );
	}

	private MorphoLibJDetectorSweepModel( final Map< String, AbstractParamSweepModel< ? > > map )
	{
		super( MorphoLibJDetectorFactory.NAME, map, new MorphoLibJDetectorFactory<>() );
	}

	@Override
	public List< Settings > generateSettings( final Settings base, final int targetChannel )
	{
		final DoubleParamSweepModel toleranceParam = ( DoubleParamSweepModel ) models.get( MorphoLibJDetectorFactory.KEY_TOLERANCE );
		final BooleanParamSweepModel diagonalConnectivityParam = ( BooleanParamSweepModel ) models.get( MorphoLibJDetectorFactory.KEY_CONNECTIVITY );
		final BooleanParamSweepModel simplifyContourParam = ( BooleanParamSweepModel ) models.get( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS );

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
