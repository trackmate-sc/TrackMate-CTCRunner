package fiji.plugin.trackmate.ctc.model.detector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.ctc.ui.components.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.InfoParamSweepModel;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.providers.DetectorProvider;

public class MorphoLibJDetectorModel extends DetectorSweepModel
{

	public MorphoLibJDetectorModel()
	{
		super( "MorphoLibJ detector", createModels(), createFactory() );
	}

	@Override
	public Iterator< Settings > iterator( final Settings base, final int targetChannel )
	{
		return MorphoLibJOpt.iterator( models, base, targetChannel );
	}

	private static SpotDetectorFactoryBase< ? > createFactory()
	{
		if ( null == new DetectorProvider().getFactory( "MORPHOLIBJ_DETECTOR" ) )
			return null;
		else
			return MorphoLibJOpt.createFactory();
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		if ( null == new DetectorProvider().getFactory( "MORPHOLIBJ_DETECTOR" ) )
		{
			final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
			models.put( "", new InfoParamSweepModel()
					.info( "The TrackMate-MorphoLibJ module seems to be missing<br>"
							+ "from your Fiji installation. Please follow the link<br>"
							+ "below for installation instructions." )
					.url( "https://imagej.net/plugins/trackmate/trackmate-morpholibj" ) );
			return models;
		}
		else
		{
			return MorphoLibJOpt.createModels();
		}
	}
}
