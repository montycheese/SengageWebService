package io.sengage.webservice.auth;

import io.sengage.webservice.exception.UnauthorizedActionException;
import io.sengage.webservice.twitch.TwitchRole;

import javax.inject.Inject;

import com.auth0.jwt.interfaces.DecodedJWT;

public final class AuthorizationHelper {
	
	private final JwtUtils jwtUtils;
	
	@Inject
	public AuthorizationHelper(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}
	
	public DecodedJWT authenticateRequestAndVerifyToken(String token) {
		// TODO other request verification
		return jwtUtils.decode(token);
	}
	
	
	public DecodedJWT authenticateCreateGameRequest(String token) {
		DecodedJWT jwt =  authenticateRequestAndVerifyToken(token);
	
		if (TwitchRole.BROADCASTER.equals(TwitchRole.from(TwitchJWTField.ROLE.fromJWT(jwt))) 
				|| TwitchRole.MODERATOR.equals(TwitchRole.from(TwitchJWTField.ROLE.fromJWT(jwt)))) {
			return jwt;
		}
		
		throw new UnauthorizedActionException("Only the broadcaster or moderator can create a game");
	}
	
	public DecodedJWT authenticateCancelGameRequest(String token) {
		DecodedJWT jwt =  authenticateRequestAndVerifyToken(token);
	
		if (TwitchRole.BROADCASTER.equals(TwitchRole.from(TwitchJWTField.ROLE.fromJWT(jwt)))) {
			return jwt;
		}
		
		throw new UnauthorizedActionException("Only the broadcaster or moderator can create a game");
	}
}
