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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

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

import fiji.plugin.trackmate.detection.SpotDetectorFactoryBase;
import fiji.plugin.trackmate.gui.Icons;
import fiji.plugin.trackmate.helper.model.detector.DetectorSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.AbstractArrayParamSweepModel.ArrayRangeType;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.AbstractParamSweepModelIO;
import fiji.plugin.trackmate.helper.model.parameter.EnumParamSweepModel;
import fiji.plugin.trackmate.helper.model.parameter.InfoParamSweepModel;
import fiji.plugin.trackmate.helper.model.tracker.TrackerSweepModel;
import fiji.plugin.trackmate.providers.DetectorProvider;
import fiji.plugin.trackmate.providers.TrackerProvider;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;

public class ParameterSweepModelIO
{

	private static final String CONFIG_FILENAME = "helperrunnersettings.json";

	private static File defaultSaveFile = new File( new File( System.getProperty( "user.home" ), ".trackmate" ), CONFIG_FILENAME );

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
		return new File( new File( groundTruthPath ).getParent(), CONFIG_FILENAME );
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
		catch ( final JsonParseException e )
		{
			final String msg = "Error when reading TrackMate-Helper parameter file. "
					+ "One class is not found in your Fiji installation: "
					+ " "
					+ e.getMessage()
					+ " "
					+ "It is likely an error with conflicting versions and upgrades "
					+ "of modules. You may fix this error by removing the existing "
					+ "TrackMate-Helper parameter file in: "
					+ " "
					+ modelFile;
			final String title = "TrackMate-Helper error";
			JOptionPane.showMessageDialog( null, msg, title, JOptionPane.ERROR_MESSAGE, Icons.TRACKMATE_ICON );
			e.printStackTrace();
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
				.registerTypeAdapter( AbstractParamSweepModel.class, new AbstractParamSweepModelIO() )
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
		model.reRegisterListeners();
		return model;
	}

	private static class DetectorSweepModelAdapter implements JsonSerializer< DetectorSweepModel >, JsonDeserializer< DetectorSweepModel >
	{

		@Override
		public DetectorSweepModel deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
		{
			final JsonObject jsonObject = json.getAsJsonObject();
			final String type = jsonObject.get( "type" ).getAsString();
			final JsonElement element = jsonObject.get( "properties" );

			/*
			 * Is the module of the model we are trying to deserialize
			 * available?
			 */
			try
			{
				final Object obj = Class.forName( "fiji.plugin.trackmate.helper.model.detector." + type ).getConstructor().newInstance();
				final DetectorSweepModel m = ( DetectorSweepModel ) obj;
				if ( m.factory == null )
				{
					/*
					 * Non de-serialized version (will correctly show a message
					 * about a missing module, even if the JSon files referred
					 * to an installed module.
					 */
					return m;
				}
			}
			catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e )
			{
				e.printStackTrace();
			}

			/*
			 * The module is available. But does the JSon file refers to a
			 * missing module?
			 */
			try
			{
				final JsonElement properiesElement = json.getAsJsonObject().get( "properties" );
				final JsonElement factoryElement = properiesElement.getAsJsonObject().get( "factory" );
				if ( factoryElement == null )
				{
					/*
					 * The JSon says that when it was saved it could not find
					 * the module. But now we can. Substitute a default version
					 * properly initialized.
					 */
					try
					{
						final Object obj2 = Class.forName( "fiji.plugin.trackmate.helper.model.detector." + type ).getConstructor().newInstance();
						return ( DetectorSweepModel ) obj2;
					}
					catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e )
					{
						e.printStackTrace();
					}
				}

				// Normal case.
				return context.deserialize( element, Class.forName( "fiji.plugin.trackmate.helper.model.detector." + type ) );
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

			/*
			 * Is the module of the model we are trying to deserialize
			 * available?
			 */
			try
			{
				final Object obj = Class.forName( "fiji.plugin.trackmate.helper.model.tracker." + type ).getConstructor().newInstance();
				final TrackerSweepModel m = ( TrackerSweepModel ) obj;
				if ( m.factory == null )
				{
					/*
					 * Non de-serialized version (will correctly show a message
					 * about a missing module, even if the JSon files referred
					 * to an installed module.
					 */
					return m;
				}
			}
			catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e )
			{
				e.printStackTrace();
			}

			/*
			 * The module is available. But does the JSon file refers to a
			 * missing module?
			 */
			try
			{
				final JsonElement properiesElement = json.getAsJsonObject().get( "properties" );
				final JsonElement factoryElement = properiesElement.getAsJsonObject().get( "factory" );
				if ( factoryElement == null )
				{
					/*
					 * The JSon says that when it was saved it could not find
					 * the module. But now we can. Substitute a default version
					 * properly initialized.
					 */
					try
					{
						final Object obj2 = Class.forName( "fiji.plugin.trackmate.helper.model.tracker." + type ).getConstructor().newInstance();
						return ( TrackerSweepModel ) obj2;
					}
					catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e )
					{
						e.printStackTrace();
					}
				}

				// Deserialize from Json
				final TrackerSweepModel ds = ( TrackerSweepModel ) context.deserialize( element, Class.forName( "fiji.plugin.trackmate.helper.model.tracker." + type ) );

				/*
				 * We may have serialized that the module was not available or
				 * properly configured at the time of serialization. In that
				 * case, the submodel is only one information parameter. Instead
				 * of returning it, we try to instantiate a new model, so that
				 * the user is presented something par with the current
				 * configuration they have.
				 */
				// Test if we serialized an error.
				if ( ds.models.size() == 1 )
				{
					final AbstractParamSweepModel< ? > sm = ds.models.values().iterator().next();
					if ( sm instanceof InfoParamSweepModel )
					{
						try
						{
							final Object obj2 = Class.forName( "fiji.plugin.trackmate.helper.model.tracker." + type ).getConstructor().newInstance();
							return ( TrackerSweepModel ) obj2;
						}
						catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e )
						{
							e.printStackTrace();
						}
					}
				}
				// Otherwise we return it.
				return ds;
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
				model.rangeType( Enum.valueOf( ArrayRangeType.class, obj.get( "rangeType" ).getAsString() ) );
				final JsonArray arr = obj.get( "set" ).getAsJsonArray();
				for ( final JsonElement el : arr )
					model.addValue( Enum.valueOf( enumClass, el.getAsString() ) );

				return model;
			}
			catch ( final ClassNotFoundException e )
			{
				throw new JsonParseException( "Class not found: " + e.getMessage(), e.getCause() );
			}
		}

		@Override
		public JsonElement serialize( final EnumParamSweepModel< T > src, final Type typeOfSrc, final JsonSerializationContext context )
		{
			final JsonObject obj = new JsonObject();
			obj.addProperty( "paramName", src.getParamName() );
			obj.addProperty( "rangeType", src.getRangeType().name() );
			obj.addProperty( "fixedValue", src.getFixedValue().name() );
			final JsonArray arr = new JsonArray( src.getSelection().size() );
			for ( final T t : src.getSelection() )
				arr.add( t.name() );
			obj.add( "set", arr );
			obj.add( "enumClass", new ClassTypeAdapter().serialize(
					src.getFixedValue().getClass(),
					src.getFixedValue().getClass(),
					context ) );

			return obj;
		}
	}
}
