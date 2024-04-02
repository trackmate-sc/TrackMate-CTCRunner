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
package fiji.plugin.trackmate.helper.ctc;

import java.util.Arrays;
import java.util.List;

import fiji.plugin.trackmate.helper.MetricsRunner;
import fiji.plugin.trackmate.helper.TrackingMetricsType;
import fiji.plugin.trackmate.util.TMUtils;

public class CTCTrackingMetricsType extends TrackingMetricsType
{

	public static final MetricValue SEG = new MetricValue( "SEG", "Segmentation accuracy", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue TRA = new MetricValue( "TRA", "Tracking accuracy", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue DET = new MetricValue( "DET", "Detection quality", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue CT = new MetricValue( "CT", "Complete tracks", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue TF = new MetricValue( "TF", "Track fractions", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue CCA = new MetricValue( "CCA", "Cell-cycle accuracy", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	public static final MetricValue BC = new MetricValue( "BC", "Branching correctness", MetricValueOptimum.HIGHER_IS_BETTER, MetricValueBound.ZERO_TO_ONE );

	private static final List< MetricValue > KEYS = Arrays.asList( new MetricValue[] { SEG, TRA, DET, CT, TF, CCA, BC } );

	public static final String NAME = "Cell-Tracking challenge (CTC) metrics";

	public static final String CSV_SUFFIX = "CTCMetrics";

	public static final String URL = "https://doi.org/10.1038/nmeth.4473";

	public static final String INFO = "<html>"
			+ "The Cell-Tracking challenge (CTC) metrics focus on measuring the performance "
			+ "of tracking pipelines applied to objects that have a shape, move and possibly "
			+ "divide, typically cells in Life-Sciences. They include metrics for tracking "
			+ "accuracy and segmentation accuracy. "
			+ "The CTC framework supports tracking algorithms that detect object division and "
			+ "offers metrics to quantify the detection accuracy of such events. "
			+ "<p>"
			+ "The CTC metrics are derived from an ISBI Grand Challenge published here:"
			+ "</html>";

	public CTCTrackingMetricsType()
	{
		super( KEYS );
	}

	@Override
	public MetricsRunner runner( final String gtPath, final String saveFolder )
	{
		return new CTCMetricsRunner( gtPath, saveFolder, TMUtils.getContext() );
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
		return DET;
	}
}
