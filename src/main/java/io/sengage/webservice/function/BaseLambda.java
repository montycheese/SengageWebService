package io.sengage.webservice.function;

import io.sengage.webservice.auth.TwitchJWTField;
import io.sengage.webservice.model.StreamContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.auth0.jwt.interfaces.DecodedJWT;

public abstract class BaseLambda<I, O> implements RequestHandler<I,O> {
	
	protected static final String TOKEN_KEY = "Authorization";
	protected static final String PLATFORM_KEY = "Platform";
	private static final String AUTH_TOKEN_PREFIX = "Bearer ";
	
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
	
	protected String parseAuthTokenFromHeaders(Map<String, String> headers) {
		String jwtToken = headers.get(TOKEN_KEY);
		if (jwtToken != null && jwtToken.startsWith(AUTH_TOKEN_PREFIX)) {
			jwtToken = jwtToken.replaceFirst(AUTH_TOKEN_PREFIX, "");
		} else {
			throw new IllegalStateException("JWT was not provided or malformed");
		}
		return jwtToken;
	}
	
	protected StreamContext getStreamInfo(DecodedJWT jwt) {
		return StreamContext.builder()
		.channelId(TwitchJWTField.CHANNEL_ID.fromJWT(jwt))
		.userId(TwitchJWTField.USER_ID.fromJWT(jwt))
		.opaqueId(TwitchJWTField.OPAQUE_USER_ID.fromJWT(jwt))
		.build();
	}
	
	
	/**
	 * Hacky but works for now.
	 * @param path http path e.g. /threads/thread123
	 * @param resource The path resource to fetch e.g. threadId
	 * @return The pathparameter or null if not found
	 */
	protected String getPathParameter(String path, PathParameter parameter) {
		String[] pathPieces = path.split("/");
		
		for (int i = 0; i < pathPieces.length; i++) {
			if (pathPieces[i].equalsIgnoreCase(parameter.parentResource)) {
				if (i + 1 >= pathPieces.length) {
					throw new IllegalStateException("Requested path parameter: " + parameter + " but was not found in path: " +path);
				}
				return pathPieces[i+1];
			}
		}
		throw new IllegalStateException("Requested path parameter: " + parameter + " but was not found in path: " + path);
	}
	

	public static enum PathParameter {
		GAME_ID("game")
		;
		
		public String parentResource;
		private PathParameter(String parentResource) {
			this.parentResource = parentResource;
		}
	}
}
