/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2021 - 2023 TrackMate developers.
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
package fiji.plugin.trackmate.batcher.ui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.scijava.util.VersionUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fiji.plugin.trackmate.batcher.exporter.ExporterParam;
import fiji.plugin.trackmate.helper.model.ParameterSweepModelIO.ClassTypeAdapter;

public class BatcherModelIO
{

	private static File defaultSaveFile = new File( new File( System.getProperty( "user.home" ), ".trackmate" ), "trackmatebatchersettings.json" );

	public static void saveTo( final File modelFile, final BatcherModel model )
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

	public static void saveToDefault( final BatcherModel model )
	{
		saveTo( defaultSaveFile, model );
	}

	public static BatcherModel readFrom( final File modelFile )
	{
		if ( !modelFile.exists() )
		{
			final BatcherModel model = new BatcherModel();
			saveTo( modelFile, model );
			return model;
		}

		try (FileReader reader = new FileReader( modelFile ))
		{
			final String str = Files.lines( Paths.get( modelFile.getAbsolutePath() ) )
					.collect( Collectors.joining( System.lineSeparator() ) );

			final BatcherModel model = fromJson( str );
			final BatcherModel baseModel = new BatcherModel();

			// If version changed, override saved model.
			if ( !model.fileVersion.equalsIgnoreCase( VersionUtils.getVersion( BatcherModel.class ) ) )
				return baseModel;

			/*
			 * If saved known exporter differ from the current ones, override
			 * saved model.
			 */
			if ( !baseModel.getRunParamModel().getExporterKeys().equals( model.getRunParamModel().getExporterKeys() ) )
				return baseModel;

			return model;
		}
		catch ( final IOException e )
		{}
		return new BatcherModel();
	}

	public static BatcherModel readFromDefault()
	{
		return readFrom( defaultSaveFile );
	}

	private static Gson getGson()
	{
		final GsonBuilder builder = new GsonBuilder()
				.registerTypeAdapter( Class.class, new ClassTypeAdapter() )
				.registerTypeAdapter( ExporterParam.class, new ExporterParamAdapter() );
		return builder.setPrettyPrinting().create();
	}

	public static String toJson( final BatcherModel model )
	{
		return getGson().toJson( model );
	}

	public static BatcherModel fromJson( final String str )
	{
		final BatcherModel model = getGson().fromJson( str, BatcherModel.class );
		return model;
	}

	private static class ExporterParamAdapter implements JsonSerializer< ExporterParam >, JsonDeserializer< ExporterParam >
	{

		@Override
		public ExporterParam deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
		{
			final JsonObject jsonObject = json.getAsJsonObject();
			final String type = jsonObject.get( "type" ).getAsString();
			final JsonElement element = jsonObject.get( "properties" );

			try
			{
				return context.deserialize( element, Class.forName( "fiji.plugin.trackmate.batcher.exporter.ExporterParam$" + type ) );
			}
			catch ( final ClassNotFoundException cnfe )
			{
				throw new JsonParseException( "Unknown element type: " + type, cnfe );
			}
		}

		@Override
		public JsonElement serialize( final ExporterParam src, final Type typeOfSrc, final JsonSerializationContext context )
		{
			final JsonObject result = new JsonObject();
			result.add( "type", new JsonPrimitive( src.getClass().getSimpleName() ) );
			result.add( "properties", context.serialize( src, src.getClass() ) );
			return result;
		}
	}
}
