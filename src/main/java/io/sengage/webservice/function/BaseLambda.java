package io.sengage.webservice.function;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public abstract class BaseLambda<I, O> implements RequestHandler<I,O> {
	
	protected static final String TOKEN_KEY = "Authorization";
	private static final String AUTH_TOKEN_PREFIX = "Bearer ";
	private static final String SECRET_KEY = "Secret";
	private static final int TOKEN_EXPIRATION_LEEWAY_SECONDS = 120;
	
	protected Map<String, String> getOutputHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Requested-With", "*");
		headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,x-requested-with");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, OPTIONS, PATCH");
		return headers;
	}
	protected void logException(LambdaLogger logger, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        logger.log("logException() " + sw.toString());
	}
	
	protected DecodedJWT parseAuthTokenFromHeaders(Map<String, String> headers) {
		String jwtToken = headers.get(TOKEN_KEY);
		if (jwtToken != null && jwtToken.startsWith(AUTH_TOKEN_PREFIX)) {
			jwtToken = jwtToken.replaceFirst(AUTH_TOKEN_PREFIX, "");
		} else {
			throw new IllegalStateException("JWT was not provided or malformed");
		}
		// TODO replace with dynamically injected secret (protected by SM)
		Algorithm algorithm = Algorithm.HMAC256(Base64.getDecoder().decode(System.getenv(SECRET_KEY)));
	    JWTVerifier verifier = JWT.require(algorithm)
                .acceptLeeway(TOKEN_EXPIRATION_LEEWAY_SECONDS)
                .build();
	    return verifier.verify(jwtToken);
	}
}
