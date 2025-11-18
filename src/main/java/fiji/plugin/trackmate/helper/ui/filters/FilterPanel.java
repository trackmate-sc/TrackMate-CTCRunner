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
package fiji.plugin.trackmate.helper.ui.filters;

import static fiji.plugin.trackmate.helper.model.filter.FilterSweepModel.FEATURE;
import static fiji.plugin.trackmate.helper.model.filter.FilterSweepModel.ISABOVE;
import static fiji.plugin.trackmate.helper.model.filter.FilterSweepModel.VALUE;
import static fiji.plugin.trackmate.helper.ui.ModuleParameterSweepPanel.createPanelFor;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import fiji.plugin.trackmate.helper.model.filter.FilterSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.ArrayParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.BooleanParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.DoubleParamSweepModel;

public class FilterPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public FilterPanel( final FilterSweepModel model )
	{
		setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );
		setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder( 5, 5, 5, 5 ),
						BorderFactory.createCompoundBorder(
								BorderFactory.createLineBorder( Color.GRAY ),
								BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ) ) );

		@SuppressWarnings( "unchecked" )
		final ArrayParamSweepModel< String > featureParam = ( ArrayParamSweepModel< String > ) model.getModels().get( FEATURE );
		final DoubleParamSweepModel thresholdParam = ( DoubleParamSweepModel ) model.getModels().get( VALUE );
		final BooleanParamSweepModel isAboveParam = ( BooleanParamSweepModel ) model.getModels().get( ISABOVE );

		add( createPanelFor( featureParam, "", "" ) );
		add( new JSeparator() );
		add( createPanelFor( thresholdParam, "", "" ) );
		add( new JSeparator() );
		add( createPanelFor( isAboveParam, "", "" ) );
	}
}
