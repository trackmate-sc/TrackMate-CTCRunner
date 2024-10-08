package fiji.plugin.trackmate.helper.model.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.helper.model.AbstractSweepModelBase;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.ArrayParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.Combinations;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.NumberParamSweepModel;

public abstract class FilterSweepModel extends AbstractSweepModelBase
{

	protected static final String FEATURE = "FEATURE";

	protected static final String VALUE = "VALUE";

	protected static final String ISABOVE = "ISABOVE";

	protected FilterSweepModel( final String name, final Map< String, String > featureNames )
	{
		super( name, createModels( featureNames ) );
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels( final Map< String, String > featureNames )
	{
		final String[] arr = new ArrayList<>( featureNames.values() ).toArray( new String[] {} );
		final ArrayParamSweepModel< String > featureNameParam = new ArrayParamSweepModel<>( arr )
				.paramName( "Feature" )
				.fixedValue( arr[ 0 ] )
				.rangeType( ArrayParamSweepModel.RangeType.FIXED );

		final DoubleParamSweepModel thresholdParam = new DoubleParamSweepModel()
				.paramName( "Threshold" )
				.dimension( Dimension.NONE )
				.rangeType( NumberParamSweepModel.RangeType.FIXED )
				.min( 5. );

		final BooleanParamSweepModel isAboveParam = new BooleanParamSweepModel()
				.paramName( "Above?" )
				.fixedValue( true )
				.rangeType( BooleanParamSweepModel.RangeType.FIXED );

		final Map< String, AbstractParamSweepModel< ? > > models = new LinkedHashMap<>();
		models.put( FEATURE, featureNameParam );
		models.put( VALUE, thresholdParam );
		models.put( ISABOVE, isAboveParam );
		return models;
	}
	
	protected static class FeatureFilterIterator implements Iterator< FeatureFilter >
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
