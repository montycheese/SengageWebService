package io.sengage.webservice.function;

import java.util.Map;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.auth.TwitchJWTField;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.exception.GameCompletedException;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.sengames.handler.QuitGameHandler;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j2;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.auth0.jwt.interfaces.DecodedJWT;

@Log4j2
public class QuitGame extends BaseLambda<ServerlessInput, ServerlessOutput> {

	@Inject
	AuthorizationHelper authHelper;
	
	@Inject
	QuitGameHandler handler;
	
	public QuitGame() {
		 ExtensionComponent extensionComponent = DaggerExtensionComponent.create();
		 extensionComponent.injectQuitGame(this);
	}

	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		log.debug("UpdateGameState: body {}", serverlessInput.getBody());
		Map<String, String> authTokenMap = serverlessInput.getHeaders();
		
		boolean isWebBeacon = isWebBeacon(serverlessInput);
		
		if (isWebBeacon) {
			// Web Beacons cannot have headers other than content type.
			// Need to provide JWT via query param
			authTokenMap = serverlessInput.getQueryStringParameters();
		}
		
		String token = parseAuthTokenFromHeaders(authTokenMap);
		DecodedJWT decodedJWT = authHelper.authenticateRequestAndVerifyToken(token);
		
		
		log.debug(String.format("Sent by user [%s] watching channel [%s] with role [%s]",
				decodedJWT.getClaim(TwitchJWTField.USER_ID.getValue()).asString(),
				decodedJWT.getClaim(TwitchJWTField.CHANNEL_ID.getValue()).asString(),
				decodedJWT.getClaim(TwitchJWTField.ROLE.getValue()).asString()));
		
		StreamContext streamContext = getStreamInfo(decodedJWT);
		
		String gameId = getPathParameter(serverlessInput.getPath(), PathParameter.GAME_ID);

		
		try {
			handler.handleGameUpdate(gameId, null, streamContext);
		} catch (GameCompletedException e) {
			throw new RuntimeException(e);
		}
		
        return ServerlessOutput.builder()
        		.headers(getOutputHeaders())
        		.statusCode(HttpStatus.SC_OK)
        		.body("Success")
        		.build();
	}
	
	// Clients will send web beacons if the user is leaving the page or scrolling out of context.
	private boolean isWebBeacon(ServerlessInput serverlessInput) {
		
		return "POST".equalsIgnoreCase(serverlessInput.getHttpMethod())
				&& serverlessInput.getQueryStringParameters().containsKey(TOKEN_KEY);
	}
}
