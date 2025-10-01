package fiji.plugin.trackmate.helper.model.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.gui.displaysettings.DisplaySettings.TrackMateObject;
import fiji.plugin.trackmate.helper.model.AbstractSweepModelBase;
import fiji.plugin.trackmate.helper.model.parameter.AbstractArrayParamSweepModel.ArrayRangeType;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.ArrayParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel.BooleanRangeType;
import fiji.plugin.trackmate.helper.model.parameter.Combinations;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel.RangeType;

public class FilterSweepModel extends AbstractSweepModelBase implements Iterable< FeatureFilter >
{

	public static final String FEATURE = "FEATURE";

	public static final String VALUE = "VALUE";

	public static final String ISABOVE = "ISABOVE";

	private final transient Listeners.List< ModelListener > modelListeners = new Listeners.SynchronizedList<>();

	public FilterSweepModel( final TrackMateObject target, final Map< String, String > featureNames, final FeatureFilter filter, final int index )
	{
		this( target, featureNames, index );
		set( filter );
	}

	public FilterSweepModel( final TrackMateObject target, final Map< String, String > featureNames, final int index )
	{
		super( "Filter on " + target.toString(), createModels( featureNames, index ) );
	}

	public final Listeners.List< ModelListener > listeners()
	{
		return modelListeners;
	}

	protected final void notifyListeners()
	{
		for ( final ModelListener l : listeners().list )
			l.modelChanged();
	}

	/**
	 * Sets this model to represent the single feature filter specified.
	 *
	 * @param ff
	 *            the feature filter.
	 */
	public void set( final FeatureFilter ff )
	{
		@SuppressWarnings( "unchecked" )
		final ArrayParamSweepModel< String > featureParam = ( ArrayParamSweepModel< String > ) getModels().get( FEATURE );
		final DoubleParamSweepModel thresholdParam = ( DoubleParamSweepModel ) getModels().get( VALUE );
		final BooleanParamSweepModel isAboveParam = ( BooleanParamSweepModel ) getModels().get( ISABOVE );

		featureParam
				.fixedValue( ff.feature )
				.rangeType( ArrayRangeType.FIXED );
		thresholdParam.rangeType( RangeType.FIXED )
				.min( ff.value );
		isAboveParam.fixedValue( ff.isAbove )
				.rangeType( BooleanRangeType.FIXED );
	}

	@Override
	public Iterator< Settings > iterator( final Settings base )
	{
		throw new UnsupportedOperationException( "Not implemented" );
	}

	@Override
	public Iterator< FeatureFilter > iterator()
	{
		return new FeatureFilterIterator( models );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels( final Map< String, String > featureNames, final int index )
	{
		final String[] arr = new ArrayList<>( featureNames.keySet() ).toArray( new String[] {} );
		final ArrayParamSweepModel< String > featureNameParam = new ArrayParamSweepModel<>( arr )
				.paramName( "Feature " + index )
				.fixedValue( arr[ 0 ] )
				.rangeType( ArrayRangeType.FIXED );

		final DoubleParamSweepModel thresholdParam = new DoubleParamSweepModel()
				.paramName( "Threshold" )
				.dimension( Dimension.NONE )
				.rangeType( RangeType.FIXED )
				.min( 5. );

		final BooleanParamSweepModel isAboveParam = new BooleanParamSweepModel()
				.paramName( "Above?" )
				.fixedValue( true )
				.rangeType( BooleanRangeType.FIXED );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( FEATURE, featureNameParam );
		models.put( VALUE, thresholdParam );
		models.put( ISABOVE, isAboveParam );
		return models;
	}

	private static class FeatureFilterIterator implements Iterator< FeatureFilter >
	{

		private final Combinations combinations;

		public FeatureFilterIterator( final Map< String, AbstractParamSweepModel< ? > > models )
		{
			final Map< String, List< Object > > values = new LinkedHashMap<>( models.size() );
			for ( final String key : models.keySet() )
			{
				@SuppressWarnings( "unchecked" )
				final List< Object > range = ( List< Object > ) models.get( key ).getRange();
				values.put( key, range );
			}
			this.combinations = new Combinations( values );
		}

		@Override
		public boolean hasNext()
		{
			return combinations.hasNext();
		}

		@Override
		public FeatureFilter next()
		{
			final Map< String, Object > params = combinations.next();
			final String feature = ( String ) params.get( FEATURE );
			final Double value = ( Double ) params.get( VALUE );
			final Boolean isAbove = ( Boolean ) params.get( ISABOVE );
			return new FeatureFilter( feature, value, isAbove );
		}
	}
}
