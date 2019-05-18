package io.sengage.webservice.auth;

import static io.sengage.webservice.dagger.ExtensionModule.EXTENSION_SECRET;
import io.sengage.webservice.model.StreamContext;

import java.util.Base64;

import javax.inject.Inject;
import javax.inject.Named;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtUtils {
	private static final int TOKEN_EXPIRATION_LEEWAY_SECONDS = 120;
	
	// TODO replace with dynamically injected secret (protected by SM)
	// also need to fetch by client id/extension id
	private final String secretKey;
	
	@Inject
	public JwtUtils(@Named(EXTENSION_SECRET) String secretKey) {
		this.secretKey = secretKey;
	}
	
	public DecodedJWT decode(String token) {
		Algorithm algorithm = Algorithm.HMAC256(Base64.getDecoder().decode(secretKey));
	    JWTVerifier verifier = JWT.require(algorithm)
                .acceptLeeway(TOKEN_EXPIRATION_LEEWAY_SECONDS)
                .build();
	    return verifier.verify(token);
	}
	
	public static StreamContext getStreamContext(DecodedJWT jwt) {
		return StreamContext.builder()
		.channelId(TwitchJWTField.CHANNEL_ID.fromJWT(jwt))
		.userId(TwitchJWTField.USER_ID.fromJWT(jwt))
		.opaqueId(TwitchJWTField.OPAQUE_USER_ID.fromJWT(jwt))
		.build();
	}
}
