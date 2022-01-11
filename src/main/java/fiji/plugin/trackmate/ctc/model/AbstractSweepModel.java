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
package fiji.plugin.trackmate.ctc.model;

import java.util.Iterator;
import java.util.Map;

import org.scijava.listeners.Listeners;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMateModule;
import fiji.plugin.trackmate.ctc.model.parameter.AbstractParamSweepModel;

public abstract class AbstractSweepModel< F extends TrackMateModule >
{

	public interface ModelListener
	{
		public void modelChanged();
	}

	private transient Listeners.List< ModelListener > modelListeners;

	protected final String name;

	protected final Map< String, AbstractParamSweepModel< ? > > models;

	protected final F factory;
	
	protected AbstractSweepModel( final String name, final Map< String, AbstractParamSweepModel< ? > > models, final F factory )
	{
		super();
		this.name = name;
		this.models = models;
		this.factory = factory;

		// Register models.
		models.values().forEach( m -> m.listeners().add( () -> notifyListeners() ) );
	}

	public abstract Iterator< Settings > iterator( final Settings base, final int targetChannel );

	public String getName()
	{
		return name;
	}

	public Map< String, AbstractParamSweepModel< ? > > getModels()
	{
		return models;
	}

	public Listeners.List< ModelListener > listeners()
	{
		if ( modelListeners == null )
		{
			/*
			 * Work around the listeners field being null after deserialization.
			 * We also need to register again the sub-models.
			 */
			this.modelListeners = new Listeners.SynchronizedList<>();
			for ( final AbstractParamSweepModel< ? > model : models.values() )
				model.listeners().add( () -> notifyListeners() );
		}
		return modelListeners;
	}

	protected void notifyListeners()
	{
		for ( final ModelListener l : modelListeners.list )
			l.modelChanged();
	}
}
