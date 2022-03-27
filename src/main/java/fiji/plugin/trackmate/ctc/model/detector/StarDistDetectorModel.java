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
package fiji.plugin.trackmate.ctc.model.detector;

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.InfoParamSweepModel;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.providers.DetectorProvider;

@Plugin( type = DetectorSweepModel.class, priority = 1000000 - 11 )
public class StarDistDetectorModel extends DetectorSweepModel
{

	private final boolean unavailable;

	public StarDistDetectorModel()
	{
		super( "StarDist detector", createModels(), createFactory() );
		this.unavailable = ( null == new DetectorProvider().getFactory( "STARDIST_DETECTOR" ) );
	}

	@Override
	public Iterator< Settings > iterator( final Settings base, final int targetChannel )
	{
		if ( unavailable )
			return Collections.emptyIterator();

		final Settings s = base.copyOn( base.imp );
		final Map< String, Object > ds = factory.getDefaultSettings();
		ds.put( KEY_TARGET_CHANNEL, targetChannel );
		s.detectorFactory = factory.copy();
		s.detectorSettings = ds;
		return Collections.singleton( s ).iterator();
	}

	private static SpotDetectorFactoryBase< ? > createFactory()
	{
		if ( null == new DetectorProvider().getFactory( "STARDIST_DETECTOR" ) )
			return null;
		else
			return StadDistOpt.createFactoryBuiltin();
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		if ( null == new DetectorProvider().getFactory( "STARDIST_DETECTOR" ) )
		{
			final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
			models.put( "", new InfoParamSweepModel()
					.info( "The TrackMate-StarDist module seems to be missing<br>"
							+ "from your Fiji installation. Please follow the link<br>"
							+ "below for installation instructions." )
					.url( "https://imagej.net/plugins/trackmate/trackmate-stardist" ) );
			return models;
		}
		else
		{
			return StadDistOpt.createModelsBuiltin();
		}
	}
}
