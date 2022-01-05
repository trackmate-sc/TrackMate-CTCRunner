package fiji.plugin.trackmate.ctc.ui.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fiji.plugin.trackmate.ctc.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.ctc.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.ctc.ui.ParameterSweepModel;
import fiji.plugin.trackmate.ctc.ui.components.EnumParamSweepModel.RangeType;
import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.providers.DetectorProvider;
import fiji.plugin.trackmate.providers.TrackerProvider;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;

public class ParameterSweepModelIO
{

	private static File defaultSaveFile = new File( new File( System.getProperty( "user.home" ), ".trackmate" ), "ctcrunnersettings.json" );

	/**
	 * Makes a file object that will save settings in the folder containing the
	 * specified ground-truth folder.
	 * 
	 * @param groundTruthPath
	 *            the ground-truth folder.
	 * @return a {@link File}.
	 */
	public static File makeSettingsFileForGTPath( final String groundTruthPath )
	{
		return new File( new File( groundTruthPath ).getParent(), "ctcrunnersettings.json" );
	}

	public static void saveTo( final File modelFile, final ParameterSweepModel model )
	{
		final String str = toJson( model );

		if ( !modelFile.exists() )
			modelFile.getParentFile().mkdirs();

		try (FileWriter writer = new FileWriter( modelFile ))
		{
			writer.append( str );
		}
		catch ( final IOException e )
		{
			System.err.println( "Could not write the settings to " + modelFile );
			e.printStackTrace();
		}
	}

	public static void saveToDefault( final ParameterSweepModel model )
	{
		saveTo( defaultSaveFile, model );
	}

	public static ParameterSweepModel readFrom( final File modelFile )
	{
		if ( !modelFile.exists() )
		{
			final ParameterSweepModel model = new ParameterSweepModel();
			saveTo( modelFile, model );
			return model;
		}

		try (FileReader reader = new FileReader( modelFile ))
		{
			final String str = Files.lines( Paths.get( modelFile.getAbsolutePath() ) )
					.collect( Collectors.joining( System.lineSeparator() ) );

			return fromJson( str );
		}
		catch ( final FileNotFoundException e )
		{
			System.err.println( "Could not find the settings file: " + modelFile
					+ ". Using built-in default setting." );
			e.printStackTrace();
		}
		catch ( final IOException e )
		{
			System.err.println( "Could not read the settings file: " + modelFile
					+ ". Using built-in default setting." );
			e.printStackTrace();
		}
		return new ParameterSweepModel();
	}

	public static ParameterSweepModel readFromDefault()
	{
		return readFrom( defaultSaveFile );
	}

	private static Gson getGson()
	{
		final GsonBuilder builder = new GsonBuilder()
				.registerTypeAdapter( EnumParamSweepModel.class, new EnumParamSweepModelAdapter<>() )
				.registerTypeAdapter( SpotDetectorFactoryBase.class, new SpotDetectorFactoryBaseAdapter() )
				.registerTypeAdapter( SpotTrackerFactory.class, new SpotTrackerFactoryAdapter() )
				.registerTypeAdapter( AbstractParamSweepModel.class, new AbstractParamSweepModelAdapter() )
				.registerTypeAdapter( DetectorSweepModel.class, new DetectorSweepModelAdapter() )
				.registerTypeAdapter( TrackerSweepModel.class, new TrackerSweepModelAdapter() )
				.registerTypeAdapter( Class.class, new ClassTypeAdapter() );
		return builder.setPrettyPrinting().create();
	}

	public static String toJson( final ParameterSweepModel model )
	{
		return getGson().toJson( model );
	}

	public static ParameterSweepModel fromJson( final String str )
	{
		final ParameterSweepModel model = getGson().fromJson( str, ParameterSweepModel.class );
		model.registerListeners();
		return model;
	}

	private static class AbstractParamSweepModelAdapter implements JsonSerializer< AbstractParamSweepModel< ? > >, JsonDeserializer< AbstractParamSweepModel< ? > >
	{

		@Override
		public AbstractParamSweepModel< ? > deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
		{
			final JsonObject jsonObject = json.getAsJsonObject();
			final String type = jsonObject.get( "type" ).getAsString();
			final JsonElement element = jsonObject.get( "properties" );

			try
			{
				return context.deserialize( element, Class.forName( "fiji.plugin.trackmate.ctc.ui.components." + type ) );
			}
			catch ( final ClassNotFoundException cnfe )
			{
				throw new JsonParseException( "Unknown element type: " + type, cnfe );
			}
		}

		@Override
		public JsonElement serialize( final AbstractParamSweepModel< ? > src, final Type typeOfSrc, final JsonSerializationContext context )
		{
			final JsonObject result = new JsonObject();
			result.add( "type", new JsonPrimitive( src.getClass().getSimpleName() ) );
			result.add( "properties", context.serialize( src, src.getClass() ) );
			return result;
		}
	}

	private static class DetectorSweepModelAdapter implements JsonSerializer< DetectorSweepModel >, JsonDeserializer< DetectorSweepModel >
	{

		@Override
		public DetectorSweepModel deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
		{
			final JsonObject jsonObject = json.getAsJsonObject();
			final String type = jsonObject.get( "type" ).getAsString();
			final JsonElement element = jsonObject.get( "properties" );

			try
			{
				return context.deserialize( element, Class.forName( "fiji.plugin.trackmate.ctc.model.detector." + type ) );
			}
			catch ( final ClassNotFoundException cnfe )
			{
				throw new JsonParseException( "Unknown element type: " + type, cnfe );
			}
		}

		@Override
		public JsonElement serialize( final DetectorSweepModel src, final Type typeOfSrc, final JsonSerializationContext context )
		{
			final JsonObject result = new JsonObject();
			result.add( "type", new JsonPrimitive( src.getClass().getSimpleName() ) );
			result.add( "properties", context.serialize( src, src.getClass() ) );
			return result;
		}
	}

	private static class TrackerSweepModelAdapter implements JsonSerializer< TrackerSweepModel >, JsonDeserializer< TrackerSweepModel >
	{

		@Override
		public TrackerSweepModel deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
		{
			final JsonObject jsonObject = json.getAsJsonObject();
			final String type = jsonObject.get( "type" ).getAsString();
			final JsonElement element = jsonObject.get( "properties" );

			try
			{
				return context.deserialize( element, Class.forName( "fiji.plugin.trackmate.ctc.model.tracker." + type ) );
			}
			catch ( final ClassNotFoundException cnfe )
			{
				throw new JsonParseException( "Unknown element type: " + type, cnfe );
			}
		}

		@Override
		public JsonElement serialize( final TrackerSweepModel src, final Type typeOfSrc, final JsonSerializationContext context )
		{
			final JsonObject result = new JsonObject();
			result.add( "type", new JsonPrimitive( src.getClass().getSimpleName() ) );
			result.add( "properties", context.serialize( src, src.getClass() ) );
			return result;
		}
	}

	public static class ClassTypeAdapter implements JsonSerializer< Class< ? > >, JsonDeserializer< Class< ? > >
	{

		@Override
		public JsonElement serialize( final Class< ? > src, final Type typeOfSrc, final JsonSerializationContext context )
		{
			return new JsonPrimitive( src.getName() );
		}

		@Override
		public Class< ? > deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context )
				throws JsonParseException
		{
			try
			{
				return Class.forName( json.getAsString() );
			}
			catch ( final ClassNotFoundException e )
			{
				throw new RuntimeException( e );
			}
		}
	}

	private static class SpotDetectorFactoryBaseAdapter implements JsonSerializer< SpotDetectorFactoryBase< ? > >, JsonDeserializer< SpotDetectorFactoryBase< ? > >
	{

		@Override
		public SpotDetectorFactoryBase< ? > deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
		{
			final String key = json.getAsJsonPrimitive().getAsString();
			final DetectorProvider provider = new DetectorProvider();
			return provider.getFactory( key );
		}

		@Override
		public JsonElement serialize( final SpotDetectorFactoryBase< ? > src, final Type typeOfSrc, final JsonSerializationContext context )
		{
			return new JsonPrimitive( src.getKey() );
		}
	}

	private static class SpotTrackerFactoryAdapter implements JsonSerializer< SpotTrackerFactory >, JsonDeserializer< SpotTrackerFactory >
	{

		@Override
		public SpotTrackerFactory deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
		{
			final String key = json.getAsJsonPrimitive().getAsString();
			final TrackerProvider provider = new TrackerProvider();
			return provider.getFactory( key );
		}

		@Override
		public JsonElement serialize( final SpotTrackerFactory src, final Type typeOfSrc, final JsonSerializationContext context )
		{
			return new JsonPrimitive( src.getKey() );
		}
	}

	private static class EnumParamSweepModelAdapter< T extends Enum< T > > implements JsonSerializer< EnumParamSweepModel< T > >, JsonDeserializer< EnumParamSweepModel< T > >
	{

		@Override
		public EnumParamSweepModel< T > deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
		{
			final JsonObject obj = json.getAsJsonObject();
			final String enumClassStr = obj.get( "enumClass" ).getAsString();
			try
			{
				@SuppressWarnings( "unchecked" )
				final Class< T > enumClass = ( Class< T > ) Class.forName( enumClassStr );
				final EnumParamSweepModel< T > model = new EnumParamSweepModel<>( enumClass );
				model.paramName( obj.get( "paramName" ).getAsString() );
				model.fixedValue( Enum.valueOf( enumClass, obj.get( "fixedValue" ).getAsString() ) );
				model.rangeType( Enum.valueOf( RangeType.class, obj.get( "rangeType" ).getAsString() ) );
				final JsonArray arr = obj.get( "set" ).getAsJsonArray();
				for ( final JsonElement el : arr )
					model.addValue( Enum.valueOf( enumClass, el.getAsString() ) );

				return model;
			}
			catch ( final ClassNotFoundException e )
			{
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public JsonElement serialize( final EnumParamSweepModel< T > src, final Type typeOfSrc, final JsonSerializationContext context )
		{
			final JsonObject obj = new JsonObject();
			obj.addProperty( "paramName", src.paramName );
			obj.addProperty( "rangeType", src.rangeType.name() );
			obj.addProperty( "fixedValue", src.fixedValue.name() );
			final JsonArray arr = new JsonArray( src.set.size() );
			for ( final T t : src.set )
				arr.add( t.name() );
			obj.add( "set", arr );
			obj.add( "enumClass", new ClassTypeAdapter().serialize(
					src.fixedValue.getClass(),
					src.fixedValue.getClass(),
					context ) );

			return obj;
		}
	}
}
