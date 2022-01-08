package fiji.plugin.trackmate.ctc.model.detector;

import java.util.HashMap;
import java.util.Map;

import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.ctc.model.parameter.InfoParamSweepModel;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.providers.DetectorProvider;

public class CellposeDetectorModel extends DetectorSweepModel
{

	public CellposeDetectorModel()
	{
		super( "Cellpose detector", createModels(), createFactory() );
	}

	private static SpotDetectorFactoryBase< ? > createFactory()
	{
		if ( null == new DetectorProvider().getFactory( "CELLPOSE_DETECTOR" ) )
			return null;
		else
			return CellposeOpt.createFactory();
	}

	private static Map< String, AbstractParamSweepModel< ? > > createModels()
	{
		if ( null == new DetectorProvider().getFactory( "CELLPOSE_DETECTOR" ) )
		{
			final Map< String, AbstractParamSweepModel< ? > > models = new HashMap<>();
			models.put( "", new InfoParamSweepModel()
					.info( "The TrackMate-Cellpose module seems to be missing<br>"
							+ "from your Fiji installation. Please follow the link<br>"
							+ "below for installation instructions." )
					.url( "https://imagej.net/plugins/trackmate/trackmate-cellpose" ) );
			return models;
		}
		else
		{
			return CellposeOpt.createModels();
		}
	}
}
