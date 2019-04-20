package io.sengage.webservice.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TwitchJWTField {
	CHANNEL_ID("channel_id"),
	OPAQUE_USER_ID("opaque_user_id"),
	USER_ID("user_id"),
	ROLE("role"),
	EXP("exp"),
	IAT("iat"),
	PUBSUB_PERMS("pubsub_perms")
	;
	
	@Getter
	public final String value;
}
