package fiji.plugin.trackmate.helper.model.parameter;

import java.util.ArrayList;
import java.util.List;

import fiji.plugin.trackmate.util.cli.CLIUtils;

public class CondaEnvParamSweepModel extends AbstractArrayParamSweepModel< String, CondaEnvParamSweepModel >
{

	public CondaEnvParamSweepModel()
	{
		super( getEnvList() );
		final List< String > allEnvs = getAllValues();
		if ( allEnvs != null && !allEnvs.isEmpty() )
		{
			addValue( allEnvs.get( 0 ) );
			fixedValue( allEnvs.get( 0 ) );
		}
	}

	protected static final String[] getEnvList()
	{
		final List< String > envList = new ArrayList<>();
		try
		{
			final List< String > l = CLIUtils.getEnvList();
			envList.addAll( l );
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
		return envList.toArray( new String[] {} );
	}
//	if ( envList == null || envList.isEmpty() )
//	{
//		models.put( "", new InfoParamSweepModel()
//				.info( "The conda executable seems not to be configured, <br>"
//						+ "or no conda environment could be found. Please <br>"
//						+ "follow the link below for installation instructions." )
//				.url( "https://imagej.net/plugins/trackmate/trackers/trackmate-trackastra" ) );
//		return models;
//	}
}
