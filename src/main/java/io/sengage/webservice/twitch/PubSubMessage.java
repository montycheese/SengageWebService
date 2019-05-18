package io.sengage.webservice.twitch;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.api.client.util.Key;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
class PubSubMessage {
	
	public static final String JSON = "application/json";

	@Key("content_type")
	public String contentType;
	
	@Key
	public String message;
	
	@Key
	public List<String> targets;
	
	@Data
	@Builder
	@AllArgsConstructor
	static class Payload {
	}
	
}
