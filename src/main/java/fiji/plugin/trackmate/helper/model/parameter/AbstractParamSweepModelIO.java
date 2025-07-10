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
