package io.sengage.webservice.utils.gson;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class InstantTypeConverter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
	@Override
	public JsonElement serialize(Instant src, Type srcType, JsonSerializationContext context) {
		return new JsonPrimitive(src.toEpochMilli());
	}
	
	@Override
	public Instant deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		return Instant.ofEpochMilli(json.getAsLong());
	}
}