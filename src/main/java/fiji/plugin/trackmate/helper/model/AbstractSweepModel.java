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
package fiji.plugin.trackmate.helper.model;

import java.util.Map;

import javax.swing.ImageIcon;

import fiji.plugin.trackmate.TrackMateModule;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;

public abstract class AbstractSweepModel< F extends TrackMateModule > extends AbstractSweepModelBase
{

	protected final F factory;

	protected AbstractSweepModel( final String name, final Map< String, AbstractParamSweepModel< ? > > models, final F factory )
	{
		super( name, models );
		this.factory = factory;
	}

	public ImageIcon getIcon()
	{
		return factory.getIcon();
	}
}
