package fiji.plugin.trackmate.ctc.model.detector;

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.model.detector.DetectorSweepModel.ModelsIterator;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;
import fiji.plugin.trackmate.morpholibj.Connectivity;
import fiji.plugin.trackmate.morpholibj.MorphoLibJDetectorFactory;

public class MorphoLibJOpt
{

	private MorphoLibJOpt()
	{}

	public static SpotDetectorFactoryBase< ? > createFactory()
	{
		return new MorphoLibJDetectorFactory<>();
	}

	public static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		final DoubleParamSweepModel toleranceParam = new DoubleParamSweepModel()
				.paramName( "Tolerance" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 40. )
				.max( 60. )
				.nSteps( 3 );
		final EnumParamSweepModel< Connectivity > connectivityParam = new EnumParamSweepModel<>( Connectivity.class )
				.paramName( "Connectivity" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.ArrayParamSweepModel.RangeType.FIXED )
				.fixedValue( Connectivity.DIAGONAL );
		final BooleanParamSweepModel simplifyContourParam = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( MorphoLibJDetectorFactory.KEY_TOLERANCE, toleranceParam );
		models.put( MorphoLibJDetectorFactory.KEY_CONNECTIVITY, connectivityParam );
		models.put( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContourParam );
		return models;
	}

	public static Iterator< Settings > iterator( final Map< String, AbstractParamSweepModel< ? > > models, final Settings base, final int targetChannel )
	{
		final Settings s = base.copyOn( base.imp );
		final Map< String, Object > ds = createFactory().getDefaultSettings();
		ds.put( KEY_TARGET_CHANNEL, targetChannel );
		s.detectorFactory = createFactory();
		s.detectorSettings = ds;

		// Substitute the Connectivity model, that must return an integer...
		@SuppressWarnings( "unchecked" )
		final EnumParamSweepModel< Connectivity > connectivityModel = ( EnumParamSweepModel< Connectivity > ) models.get( MorphoLibJDetectorFactory.KEY_CONNECTIVITY );
		final Integer[] vals = connectivityModel.getRange().stream()
				.map( Connectivity::getConnectivity )
				.collect( Collectors.toList() )
				.toArray( new Integer[] {} );
		final IntParamSweepModel connIntModel = new IntParamSweepModel()
				.paramName( connectivityModel.getParamName() )
				.rangeType( RangeType.MANUAL )
				.manualRange( vals );
		final Map< String, AbstractParamSweepModel< ? > > mappedModels = new HashMap<>( models );
		mappedModels.put( MorphoLibJDetectorFactory.KEY_CONNECTIVITY, connIntModel );
		return new ModelsIterator( s, mappedModels );
	}
}
