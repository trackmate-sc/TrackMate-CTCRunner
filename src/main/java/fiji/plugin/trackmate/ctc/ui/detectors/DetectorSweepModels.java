package fiji.plugin.trackmate.ctc.ui.detectors;

import fiji.plugin.trackmate.cellpose.CellposeDetectorFactory;
import fiji.plugin.trackmate.cellpose.CellposeSettings.PretrainedModel;
import fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.DoubleParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.IntParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.NumberParamSweepModel.RangeType;
import fiji.plugin.trackmate.ctc.ui.components.StringRangeParamSweepModel;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.DogDetectorFactory;
import fiji.plugin.trackmate.detection.LabeImageDetectorFactory;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.detection.MaskDetectorFactory;
import fiji.plugin.trackmate.detection.SpotDetectorFactory;
import fiji.plugin.trackmate.detection.ThresholdDetectorFactory;
import fiji.plugin.trackmate.ilastik.IlastikDetectorFactory;
import fiji.plugin.trackmate.morpholibj.Connectivity;
import fiji.plugin.trackmate.morpholibj.MorphoLibJDetectorFactory;
import fiji.plugin.trackmate.stardist.StarDistCustomDetectorFactory;
import fiji.plugin.trackmate.stardist.StarDistDetectorFactory;
import fiji.plugin.trackmate.weka.WekaDetectorFactory;

public class DetectorSweepModels
{

	public static DetectorSweepModel stardistDetectorModel()
	{
		return DetectorSweepModel.create()
				.name( StarDistDetectorFactory.NAME )
				.factory( new StarDistDetectorFactory<>() )
				.get();
	}

	public static DetectorSweepModel stardistCustomDetectorModel()
	{
		final StringRangeParamSweepModel stardistBunblePath = new StringRangeParamSweepModel()
				.paramName( "StarDist model bundle path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );
		final DoubleParamSweepModel probaThreshold = new DoubleParamSweepModel()
				.paramName( "Score threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 0.2 )
				.max( 0.8 )
				.nSteps( 3 );
		final DoubleParamSweepModel overlapThreshold = new DoubleParamSweepModel()
				.paramName( "Overlap threshold" )
				.rangeType( RangeType.FIXED )
				.min( 0.5 );

		return DetectorSweepModel.create()
				.name( StarDistCustomDetectorFactory.NAME )
				.factory( new StarDistCustomDetectorFactory<>() )
				.add( StarDistCustomDetectorFactory.KEY_MODEL_FILEPATH, stardistBunblePath )
				.add( StarDistCustomDetectorFactory.KEY_SCORE_THRESHOLD, probaThreshold )
				.add( StarDistCustomDetectorFactory.KEY_OVERLAP_THRESHOLD, overlapThreshold )
				.get();
	}

	public static DetectorSweepModel cellposeDetectorModel( final String units )
	{
		final StringRangeParamSweepModel cellposePath = new StringRangeParamSweepModel()
				.paramName( "Cellpose Python path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );
		final EnumParamSweepModel< PretrainedModel > cellposeModel = new EnumParamSweepModel<>( PretrainedModel.class )
				.paramName( "Cellpose model" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel.RangeType.FIXED )
				.fixedValue( PretrainedModel.CYTO );
		final IntParamSweepModel channel1 = new IntParamSweepModel()
				.paramName( "Channel to segment" )
				.rangeType( RangeType.FIXED )
				.min( 0 )
				.max( 4 );
		final IntParamSweepModel channel2 = new IntParamSweepModel()
				.paramName( "Optional second channel" )
				.rangeType( RangeType.FIXED )
				.min( 0 )
				.max( 4 );
		final DoubleParamSweepModel cellDiameter = new DoubleParamSweepModel()
				.paramName( "Cell diameter" )
				.units( units )
				.rangeType( RangeType.FIXED )
				.min( 0. )
				.max( 50. );
		final BooleanParamSweepModel useGPU = new BooleanParamSweepModel()
				.paramName( "Use GPU" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		return DetectorSweepModel.create()
				.name( CellposeDetectorFactory.NAME )
				.factory( new CellposeDetectorFactory<>() )
				.add( CellposeDetectorFactory.KEY_CELLPOSE_PYTHON_FILEPATH, cellposePath )
				.add( CellposeDetectorFactory.KEY_CELLPOSE_MODEL, cellposeModel )
				.add( CellposeDetectorFactory.KEY_CELL_DIAMETER, cellDiameter )
				.add( DetectorKeys.KEY_TARGET_CHANNEL, channel1 )
				.add( CellposeDetectorFactory.KEY_OPTIONAL_CHANNEL_2, channel2 )
				.add( CellposeDetectorFactory.KEY_USE_GPU, useGPU )
				.add( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours )
				.get();
	}

	public static DetectorSweepModel ilastikDetectorModel()
	{
		final StringRangeParamSweepModel classifierPath = new StringRangeParamSweepModel()
				.paramName( "Ilastik project path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );
		final DoubleParamSweepModel probaThreshold = new DoubleParamSweepModel()
				.paramName( "Probability threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 0.2 )
				.max( 0.8 )
				.nSteps( 3 );
		final IntParamSweepModel classIndex = new IntParamSweepModel()
				.paramName( "Class index" )
				.rangeType( RangeType.FIXED )
				.min( 1 );

		return DetectorSweepModel.create()
				.name( IlastikDetectorFactory.NAME )
				.factory( new IlastikDetectorFactory<>() )
				.add( IlastikDetectorFactory.KEY_CLASSIFIER_FILEPATH, classifierPath )
				.add( IlastikDetectorFactory.KEY_CLASS_INDEX, classIndex )
				.add( IlastikDetectorFactory.KEY_PROBA_THRESHOLD, probaThreshold )
				.get();
	}

	public static DetectorSweepModel wekaDetectorModel()
	{
		final StringRangeParamSweepModel classifierPath = new StringRangeParamSweepModel()
				.paramName( "Weka classifier path" )
				.isFile( true )
				.add( System.getProperty( "user.home" ) );
		final DoubleParamSweepModel probaThreshold = new DoubleParamSweepModel()
				.paramName( "Probability threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 0.2 )
				.max( 0.8 )
				.nSteps( 3 );
		final IntParamSweepModel classIndex = new IntParamSweepModel()
				.paramName( "Class index" )
				.rangeType( RangeType.FIXED )
				.min( 1 );

		return DetectorSweepModel.create()
				.name( WekaDetectorFactory.NAME )
				.factory( new WekaDetectorFactory<>() )
				.add( WekaDetectorFactory.KEY_CLASSIFIER_FILEPATH, classifierPath )
				.add( WekaDetectorFactory.KEY_CLASS_INDEX, classIndex )
				.add( WekaDetectorFactory.KEY_PROBA_THRESHOLD, probaThreshold )
				.get();
	}

	public static final DetectorSweepModel morphoLibJDetectorModel()
	{
		final DoubleParamSweepModel toleranceParam = new DoubleParamSweepModel()
				.paramName( "Tolerance" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 40. )
				.max( 60. )
				.nSteps( 3 );
		final EnumParamSweepModel< Connectivity > connectivityParam = new EnumParamSweepModel<>( Connectivity.class )
				.paramName( "Connectivity" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel.RangeType.FIXED )
				.fixedValue( Connectivity.DIAGONAL );
		final BooleanParamSweepModel simplifyContourParam = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		return DetectorSweepModel.create()
				.name( MorphoLibJDetectorFactory.NAME )
				.factory( new MorphoLibJDetectorFactory<>() )
				.add( MorphoLibJDetectorFactory.KEY_TOLERANCE, toleranceParam )
				.add( MorphoLibJDetectorFactory.KEY_CONNECTIVITY, connectivityParam )
				.add( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContourParam )
				.get();
	}

	public static DetectorSweepModel labelImgDetectorModel()
	{
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		return DetectorSweepModel.create()
				.name( LabeImageDetectorFactory.NAME )
				.factory( new LabeImageDetectorFactory<>() )
				.add( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours )
				.get();
	}

	public static DetectorSweepModel thresholdDetectorModel()
	{
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final DoubleParamSweepModel intensityThreshold = new DoubleParamSweepModel()
				.paramName( "Intensity threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 50. )
				.max( 100. )
				.nSteps( 3 );

		return DetectorSweepModel.create()
				.name( ThresholdDetectorFactory.NAME )
				.factory( new ThresholdDetectorFactory<>() )
				.add( ThresholdDetectorFactory.KEY_INTENSITY_THRESHOLD, intensityThreshold )
				.add( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours )
				.get();
	}

	public static DetectorSweepModel maskDetectorModel()
	{
		final BooleanParamSweepModel simplifyContours = new BooleanParamSweepModel()
				.paramName( "Simplify contours" )
				.rangeType( fiji.plugin.trackmate.ctc.ui.components.BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );

		return DetectorSweepModel.create()
				.name( MaskDetectorFactory.NAME )
				.factory( new MaskDetectorFactory<>() )
				.add( ThresholdDetectorFactory.KEY_SIMPLIFY_CONTOURS, simplifyContours )
				.get();
	}

	public static DetectorSweepModel logDetectorModel( final String spaceUnits )
	{
		return dlogDetectorModel( new LogDetectorFactory<>(), spaceUnits );
	}

	public static DetectorSweepModel dogDetectorModel( final String spaceUnits )
	{
		return dlogDetectorModel( new DogDetectorFactory<>(), spaceUnits );
	}

	private static DetectorSweepModel dlogDetectorModel( final SpotDetectorFactory< ? > factory, final String spaceUnits )
	{
		final DoubleParamSweepModel estimatedRadius = new DoubleParamSweepModel()
				.paramName( "Estimated radius" )
				.units( spaceUnits )
				.rangeType( RangeType.FIXED )
				.min( 5. );
		final DoubleParamSweepModel threshold = new DoubleParamSweepModel()
				.paramName( "Threshold" )
				.rangeType( RangeType.LIN_RANGE )
				.min( 50. )
				.max( 100. )
				.nSteps( 3 );
		final BooleanParamSweepModel subpixelLocalization = new BooleanParamSweepModel()
				.paramName( "Sub-pixel localization" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( true );
		final BooleanParamSweepModel useMedian = new BooleanParamSweepModel()
				.paramName( "Median pre-processing" )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED )
				.fixedValue( false );

		return DetectorSweepModel.create()
				.name( factory.getName() )
				.factory( factory.copy() )
				.add( DetectorKeys.KEY_RADIUS, estimatedRadius )
				.add( DetectorKeys.KEY_THRESHOLD, threshold )
				.add( DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, subpixelLocalization )
				.add( DetectorKeys.KEY_DO_MEDIAN_FILTERING, useMedian )
				.get();
	}

	private DetectorSweepModels()
	{}
}
