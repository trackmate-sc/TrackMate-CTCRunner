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
package fiji.plugin.trackmate.performance.spt;

public enum SPTMetricsDescription
{
	
	ALPHA( "alpha", "Matching with ground-truth" ),
	BETA( "beta", "Matching with ground-truth, penalizing spurious tracks" ),
	JSC( "JSC", "Jaccard similarity coefficient for track points" ),
	JSCtheta( "JSCtheta", "Jaccard similarity coefficient for whole tracks" ),
	RMSE( "RMSE", "Overall localization accuracy" ),
	TIM( "TIM", "Execution time" ),
	DETECTION_TIME( "DETECTION_TIME", "Detection time" ),
	TRACKING_TIME( "TRACKING_TIME", "Tracking time" );

	private final String name;

	private final String description;

	SPTMetricsDescription( final String name, final String description )
	{
		this.name = name;
		this.description = description;
	}

	public String ctcName()
	{
		return name;
	}

	public String description()
	{
		return description;
	}

	public static final String[] toHeader()
	{
		final String[] out = new String[ values().length ];
		for ( int i = 0; i < values().length; i++ )
			out[ i ] = values()[ i ].name;

		return out;
	}
}
