package fiji.plugin.trackmate.ctc.model.parameter;

import java.util.Collections;
import java.util.List;

public class InfoParamSweepModel extends AbstractParamSweepModel< Void >
{

	protected String info = "";

	protected String url = "";

	@Override
	public List< Void > getRange()
	{
		return Collections.emptyList();
	}

	@Override
	public InfoParamSweepModel paramName( final String paramName )
	{
		return ( InfoParamSweepModel ) super.paramName( paramName );
	}

	public InfoParamSweepModel info( final String info )
	{
		if ( !this.info.equals( info ) )
		{
			this.info = info;
			notifyListeners();
		}
		return this;
	}

	public InfoParamSweepModel url( final String url )
	{
		if ( !this.url.equals( url ) )
		{
			this.url = url;
			notifyListeners();
		}
		return this;
	}

	public String getInfo()
	{
		return info;
	}

	public String getUrl()
	{
		return url;
	}
}
