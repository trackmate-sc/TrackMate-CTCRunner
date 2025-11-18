/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2025 TrackMate developers.
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

import java.util.ArrayList;
import java.util.List;

import fiji.plugin.trackmate.util.cli.CLIUtils;

public class CondaEnvParamSweepModel extends AbstractArrayParamSweepModel< String, CondaEnvParamSweepModel >
{

	public CondaEnvParamSweepModel()
	{
		super( getEnvList().toArray( new String[] {} ) );
		final List< String > allEnvs = getAllValues();
		if ( allEnvs != null && !allEnvs.isEmpty() )
		{
			addValue( allEnvs.get( 0 ) );
			fixedValue( allEnvs.get( 0 ) );
		}
	}

	protected static final List< String > getEnvList()
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
		return envList;
	}

	@Override
	void initialize()
	{
		super.initialize();
		allValues.clear();
		allValues.addAll( getEnvList() );
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
