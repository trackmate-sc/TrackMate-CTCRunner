package fiji.plugin.trackmate.helper.model.filter;

import java.util.Iterator;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;

public class SpotFilterSweepModel extends FilterSweepModel
{

	public SpotFilterSweepModel( final Map< String, String > featureNames )
	{
		super( "Spot filter", featureNames );
	}

	@Override
	public Iterator< Settings > iterator( final Settings base, final int targetChannel )
	{
		final Settings s = base.copyOn( base.imp );
		return new SpotFilterIterator( s, models );
	}

	private static class SpotFilterIterator implements Iterator< Settings >
	{

		private final Settings base;

		private final FeatureFilterIterator it;

		public SpotFilterIterator( final Settings base, final Map< String, AbstractParamSweepModel< ? > > models )
		{
			this.base = base;
			this.it = new FeatureFilterIterator( models );
		}

		@Override
		public boolean hasNext()
		{
			return it.hasNext();
		}

		@Override
		public Settings next()
		{
			final FeatureFilter ff = it.next();
			final Settings copy = base.copyOn( base.imp );
			copy.addSpotFilter( ff );
			return copy;
		}
	}
}
