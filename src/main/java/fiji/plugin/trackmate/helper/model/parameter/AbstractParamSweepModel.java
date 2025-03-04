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
package fiji.plugin.trackmate.helper.model.parameter;

import java.util.List;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.helper.model.AbstractSweepModelBase.ModelListener;

public abstract class AbstractParamSweepModel< T >
{

	public abstract List< T > getRange();

	private final transient Listeners.List< ModelListener > modelListeners;

	protected String paramName = " ";

	public AbstractParamSweepModel()
	{
		this.modelListeners = new Listeners.SynchronizedList<>();
	}

	public AbstractParamSweepModel< T > paramName( final String paramName )
	{
		if ( !this.paramName.equals( paramName ) )
		{
			this.paramName = paramName;
			notifyListeners();
		}
		return this;
	}

	public String getParamName()
	{
		return paramName;
	}

	public Listeners.List< ModelListener > listeners()
	{
		return modelListeners;
	}

	protected void notifyListeners()
	{
		for ( final ModelListener l : modelListeners.list )
			l.modelChanged();
	}
}
