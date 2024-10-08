package fiji.plugin.trackmate.helper.model.filter;

import java.util.Iterator;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.features.FeatureFilter;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;

public class TrackFilterSweepModel extends FilterSweepModel
{

	public TrackFilterSweepModel( final Map< String, String > featureNames )
	{
		super( "Track filter", featureNames );
	}

	@Override
	public Iterator< Settings > iterator( final Settings base, final int targetChannel )
	{
		final Settings s = base.copyOn( base.imp );
		return new TrackFilterIterator( s, models );
	}

	private static class TrackFilterIterator implements Iterator< Settings >
	{

		private final Settings base;

		private final FeatureFilterIterator it;

		public TrackFilterIterator( final Settings base, final Map< String, AbstractParamSweepModel< ? > > models )
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
			copy.addTrackFilter( ff );
			return copy;
		}
	}
}
