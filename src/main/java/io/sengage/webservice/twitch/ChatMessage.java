package io.sengage.webservice.twitch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import com.google.api.client.util.Key;

@Builder
@AllArgsConstructor
@NoArgsConstructor
class ChatMessage {

	@Key
	public String text;

}