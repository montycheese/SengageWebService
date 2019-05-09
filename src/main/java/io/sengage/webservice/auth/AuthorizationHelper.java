package io.sengage.webservice.auth;

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
}
