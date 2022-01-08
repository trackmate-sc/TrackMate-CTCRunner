package fiji.plugin.trackmate.ctc.model.detector;

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.InfoParamSweepModel;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.providers.DetectorProvider;

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
