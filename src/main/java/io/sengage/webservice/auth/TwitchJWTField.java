package io.sengage.webservice.auth;

import com.auth0.jwt.interfaces.DecodedJWT;

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
	
	public String fromJWT(DecodedJWT jwt) {
		return jwt.getClaim(this.value).asString();
	}
}
