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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Splitter;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMatePlugIn;
import fiji.plugin.trackmate.helper.model.detector.CellposeOpt;
import ij.ImagePlus;

public class TrackMateCTCUtils
{

	public static void launchTrackMate( final Settings settings )
	{
		new Thread()
		{
			@Override
			public void run()
			{
				new TrackMatePlugIn()
				{

					@Override
					protected Settings createSettings( final ImagePlus imp )
					{
						return settings;
					}

				}.run( null );
			}

		}.start();
	}

	/**
	 * Tries to recreate a settings map (Map &lt; String, Object &gt; ) from its
	 * string representation.
	 * 
	 * @param in
	 *            a map of strings to strings.
	 * @return a map of strings to objects.
	 */
	public static final Map< String, Object > castToSettings( final Map< String, String > in )
	{
		final Map< String, Object > out = new LinkedHashMap<>();
		for ( final String key : in.keySet() )
		{
			final String str = in.get( key );
			// Case by case with decreasing priority :/
			final Object obj;
			if ( isParsableAsInteger( str ) )
				obj = Integer.parseInt( str );
			else if ( isParsableAsDouble( str ) )
				obj = Double.parseDouble( str );
			else if ( isParsableAsBoolean( str ) )
				obj = Boolean.parseBoolean( str );
			else if ( isEmptyMap( str ) )
				obj = Collections.emptyMap();
			else if ( isMap( str ) )
				obj = toMap( str );
			else if ( key.equals( "CELLPOSE_MODEL" ) )
				obj = CellposeOpt.castPretrainedModel( str );
			else
				obj = str;
			out.put( key, obj );
		}
		return out;
	}

	private static boolean isParsableAsBoolean( final String str )
	{
		return "true".equalsIgnoreCase( str.trim() ) || "false".equalsIgnoreCase( str.trim() );
	}

	private static Map< String, Object > toMap( final String str )
	{
		final Map< String, String > split = Splitter
				.on( "," )
				.trimResults()
				.withKeyValueSeparator( "=" )
				.split( str.substring( 1, str.length() - 1 ) );
		return castToSettings( split );
	}

	private static boolean isMap( final String str )
	{
		return str.charAt( 0 ) == '{' && str.charAt( str.length() - 1 ) == '}';
	}

	private static boolean isEmptyMap( final String str )
	{
		return str.trim().equals( "{}" );
	}

	private static boolean isParsableAsInteger( final String s )
	{
		try
		{
			Integer.valueOf( s );
			return true;
		}
		catch ( final NumberFormatException numberFormatException )
		{
			return false;
		}
	}

	private static boolean isParsableAsDouble( final String s )
	{
		try
		{
			Double.valueOf( s );
			return true;
		}
		catch ( final NumberFormatException numberFormatException )
		{
			return false;
		}
	}
}
