/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2024 TrackMate developers.
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
package fiji.plugin.trackmate.helper.spt;

import java.util.Arrays;
import java.util.List;

import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.TrackingMetricsType;

public class SPTTrackingMetricsType extends TrackingMetricsType
{

	public static final MetricValue ALPHA = new MetricValue( "alpha", "Matching score against ground-truth", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue BETA = new MetricValue( "beta", "Matching score against ground-truth, penalizing spurious tracks", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue JSC = new MetricValue( "JSC", "Jaccard similarity coefficient for track points", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue JSCTHETA = new MetricValue( "JSCtheta", "Jaccard similarity coefficient for whole tracks", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue RMSE = new MetricValue( "RMSE", "Overall localization accuracy", MetricValueOptimum.LOWER_IS_BETTER, MetricValueBound.UNBOUNDED );

	private static final List< MetricValue > KEYS = Arrays.asList( new MetricValue[] { ALPHA, BETA, JSC, JSCTHETA, RMSE } );

	public static final String NAME = "Single-Particle Tracking (SPT) Challenge metrics";

	public static final String CSV_SUFFIX = "SPTMetrics";

	public static final String URL = "https://doi.org/10.1038/nmeth.2808";

	public static final String INFO = "<html>"
			+ "The Single-Particle Tracking challenge (SPT) metrics focus on measuring the "
			+ "performance of tracking pipelines applied to small objects that have no shape "
			+ "and a possibly complex motion, possibly very dense with overlapping trajectories. "
			+ "They typically apply to tracking sub-resolved "
			+ "particles or cell organelles in Life-Sciences. "
			+ "<p>"
			+ "The SPT metrics are derived from an ISBI Grand Challenge published here:"
			+ "</html>";

	private final double maxDist;

	private final String units;

	/**
	 * Builds a new metrics type based on the SPT challenge, with the specified
	 * max pairing distance <b>given in physical units</b>.
	 * <p>
	 * The max pairing distance is the maximal distance acceptable from a ground
	 * truth detection to be accepted as correct by the metrics. In the ISBI
	 * challenge, it was set to 5 pixels everywhere, but for practical usages
	 * here it can be varied. It must be specifed in the same units that of the
	 * physical units in the image used.
	 * 
	 * @param maxDist
	 *            the max pairing distance.
	 * @param units
	 *            the physical units in which <code>maxDist</code> is specified.
	 */
	public SPTTrackingMetricsType( final double maxDist, final String units )
	{
		super( KEYS );
		this.maxDist = maxDist;
		this.units = units;
	}

	@Override
	public MetricsRunner runner( final String gtPath, final String saveFolder )
	{
		return new SPTMetricsRunner( gtPath, saveFolder, maxDist, units );
	}

	@Override
	public String name()
	{
		return NAME;
	}

	@Override
	public String csvSuffix()
	{
		return CSV_SUFFIX;
	}

	@Override
	public String url()
	{
		return URL;
	}

	@Override
	public String info()
	{
		return INFO;
	}

	@Override
	public MetricValue defaultMetric()
	{
		return ALPHA;
	}
}
