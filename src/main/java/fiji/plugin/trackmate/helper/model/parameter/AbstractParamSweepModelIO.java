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
package fiji.plugin.trackmate.helper.model.parameter;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AbstractParamSweepModelIO implements JsonSerializer< AbstractParamSweepModel< ? > >, JsonDeserializer< AbstractParamSweepModel< ? > >
{

	@Override
	public AbstractParamSweepModel< ? > deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException
	{
		final JsonObject jsonObject = json.getAsJsonObject();
		final String type = jsonObject.get( "type" ).getAsString();
		final JsonElement element = jsonObject.get( "properties" );
		try
		{
			final AbstractParamSweepModel< ? > deserialized = context.deserialize( element, Class.forName( "fiji.plugin.trackmate.helper.model.parameter." + type ) );
			deserialized.initialize();
			return deserialized;
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
