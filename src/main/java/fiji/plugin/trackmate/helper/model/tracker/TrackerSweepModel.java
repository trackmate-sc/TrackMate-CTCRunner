/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2021 - 2022 The Institut Pasteur.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package fiji.plugin.trackmate.helper.model.tracker;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.helper.model.AbstractSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.Combinations;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;

public class TrackerSweepModel extends AbstractSweepModel< SpotTrackerFactory >
{

	protected TrackerSweepModel( final String name, final Map< String, AbstractParamSweepModel< ? > > models, final SpotTrackerFactory factory )
	{
		super( name, models, factory );
	}

	@Override
	public Iterator< Settings > iterator( final Settings base, final int targetChannel )
	{
		// Null factory signals not to create any settings.
		if ( factory == null )
			return Collections.emptyIterator();

		final Settings s = base.copyOn( base.imp );
		final Map< String, Object > ds = factory.getDefaultSettings();
		s.trackerFactory = factory.copy();
		s.trackerSettings = ds;

		return new MyIterator( s, models );
	}

	private static class MyIterator implements Iterator< Settings >
	{

		private final Settings base;

		private final Combinations combinations;

		public MyIterator( final Settings base, final Map< String, AbstractParamSweepModel< ? > > models )
		{
			this.base = base;
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
		public Settings next()
		{
			final Settings copy = base.copyOn( base.imp );
			final Map< String, Object > params = combinations.next();
			for ( final String key : params.keySet() )
			{
				final Object val = params.get( key );
				copy.trackerSettings.put( key, val );
			}
			return copy;
		}
	}
}
