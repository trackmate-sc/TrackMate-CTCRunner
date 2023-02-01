/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
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
package fiji.plugin.trackmate.helper.model.parameter;

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
