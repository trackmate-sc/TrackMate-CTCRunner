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
package fiji.plugin.trackmate.batcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.listeners.Listeners;
import org.scijava.log.LogService;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

import fiji.plugin.trackmate.batcher.exporter.BatchResultExporter;
import fiji.plugin.trackmate.batcher.exporter.ExporterParam;
import fiji.plugin.trackmate.util.TMUtils;
import net.imagej.ImageJ;

public class RunParamModel
{

	private final transient Listeners.List< RunParamListener > listeners;

	private final transient Map< String, BatchResultExporter > instances;

	private boolean saveToInputFolder = true;

	private String outputFolderPath = System.getProperty( "user.home" );

	private final Map< String, List< String > > exporterKeys;

	private final Map< String, Boolean > selectedExporters;

	private final Map< String, List< ExporterParam > > extraParameters;

	public interface RunParamListener
	{
		public void runParamChanged();
	}

	public RunParamModel()
	{
		this.listeners = new Listeners.SynchronizedList<>();
		this.exporterKeys = new LinkedHashMap<>();
		this.selectedExporters = new HashMap<>();
		this.extraParameters = new HashMap<>();
		this.instances = new HashMap<>();
		discover();
	}

	public BatchResultExporter getExporter( final String exporterKey )
	{
		if ( instances.isEmpty() )
			discover();
		return instances.get( exporterKey );
	}

	public Set< String > getExporterKeys()
	{
		return exporterKeys.keySet();
	}

	public List< String > getExportables( final String exporterKey )
	{
		return exporterKeys.getOrDefault( exporterKey, Collections.emptyList() );
	}

	public List< String > getSelectedExportables( final String exporterKey )
	{
		final List< String > out = new ArrayList<>();
		for ( final String exportable : getExportables( exporterKey ) )
		{
			if ( isExportActive( exportable ) )
				out.add( exportable );
		}
		return out;
	}

	public List< ExporterParam > getExporterExtraParameters( final String exporterKey )
	{
		return extraParameters.getOrDefault( exporterKey, Collections.emptyList() );
	}

	public boolean isExportActive( final String exportable )
	{
		return selectedExporters.getOrDefault( exportable, Boolean.FALSE );
	}

	public void setExportActive( final String exportable, final boolean active )
	{
		final boolean previousValue = isExportActive( exportable );
		if ( previousValue != active )
		{
			selectedExporters.put( exportable, Boolean.valueOf( active ) );
			notifyListeners();
		}
	}

	public boolean isSaveToInputFolder()
	{
		return saveToInputFolder;
	}

	public void setSaveToInputFolder( final boolean saveToInputFolder )
	{
		if ( this.saveToInputFolder != saveToInputFolder )
		{
			this.saveToInputFolder = saveToInputFolder;
			notifyListeners();
		}
	}

	public String getOutputFolderPath()
	{
		return outputFolderPath;
	}

	public void setOutputFolderPath( final String outputFolderPath )
	{
		if ( !this.outputFolderPath.equals( outputFolderPath ) )
		{
			this.outputFolderPath = outputFolderPath;
			notifyListeners();
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() );
		final List< String[] > args = new ArrayList<>( 2 + exporterKeys.size() );
		args.add( new String[] { "saveToInputFolder", "" + saveToInputFolder } );
		args.add( new String[] { "outputFolderPath", "" + outputFolderPath } );
		for ( final String exporterKey : exporterKeys.keySet() )
		{
			final List< String > exportables = exporterKeys.get( exporterKey );
			for ( final String exportable : exportables )
				args.add( new String[] { exportable, "" + isExportActive( exportable ) } );
			final List< ExporterParam > extra = extraParameters.get( exporterKey );
			if ( extra != null && !extra.isEmpty() )
				for ( final ExporterParam paramKey : extra )
					args.add( new String[] { " + " + paramKey.name(), "" + paramKey.value() } );
		}
		for ( final String[] arg : args )
			str.append( String.format( "\n - %-30s: %s", arg[ 0 ], arg[ 1 ] ) );

		return str.toString();
	}

	public Listeners.List< RunParamListener > listeners()
	{
		return listeners;
	}

	public void notifyListeners()
	{
		for ( final RunParamListener l : listeners.list )
			l.runParamChanged();
	}

	private final void discover()
	{
		// Discover exporters.
		final Context context = TMUtils.getContext();
		final LogService log = context.getService( LogService.class );
		final PluginService pluginService = context.getService( PluginService.class );
		final List< PluginInfo< BatchResultExporter > > infos = pluginService.getPluginsOfType( BatchResultExporter.class );
		for ( final PluginInfo< BatchResultExporter > info : infos )
		{
			if ( !info.isEnabled() || !info.isVisible() )
				continue;
			try
			{
				final BatchResultExporter implementation = info.createInstance();
				instances.put( implementation.getKey(), implementation );
				exporterKeys.put( implementation.getKey(), implementation.getExportables() );
				final List< ExporterParam > ec = implementation.getExtraParameters();
				extraParameters.put( implementation.getKey(), ec );
			}
			catch ( final InstantiableException e )
			{
				log.error( "Could not instantiate " + info.getClassName(), e );
			}
		}
	}

	public static void main( final String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.launch( args );

		final RunParamModel model = new RunParamModel();
		System.out.println( model.toString() );
	}
}
