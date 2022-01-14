package fiji.plugin.trackmate.ctc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Splitter;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.TrackMatePlugIn;
import fiji.plugin.trackmate.ctc.model.detector.CellposeOpt;
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
	 * @param detectorParamsStr
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
