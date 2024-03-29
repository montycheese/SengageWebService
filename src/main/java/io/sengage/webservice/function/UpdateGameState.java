package io.sengage.webservice.function;

import java.util.Map;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j2;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.auth.TwitchJWTField;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.exception.GameCompletedException;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.model.UpdateGameStateRequest;
import io.sengage.webservice.sengames.handler.GameUpdateHandler;
import io.sengage.webservice.sengames.handler.GameUpdateHandlerFactory;
import io.sengage.webservice.sengames.model.HandleGameUpdateResponse;

@Log4j2
public class UpdateGameState extends BaseLambda<ServerlessInput, ServerlessOutput> {

	@Inject
	AuthorizationHelper authHelper;
	
	@Inject
	GameUpdateHandlerFactory handlerFactory;
	
	@Inject
	Gson gson;
	
	public UpdateGameState() {
		 ExtensionComponent extensionComponent = DaggerExtensionComponent.create();
		 extensionComponent.injectUpdateGameState(this);
	}
	
	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		log.debug("UpdateGameState: body {}", serverlessInput.getBody());
		
		Map<String, String> authTokenMap = serverlessInput.getHeaders();
		
		String token = parseAuthTokenFromHeaders(authTokenMap);
		DecodedJWT decodedJWT = authHelper.authenticateRequestAndVerifyToken(token);
		
		
		log.debug(String.format("Sent by user [%s] watching channel [%s] with role [%s]",
				decodedJWT.getClaim(TwitchJWTField.USER_ID.getValue()).asString(),
				decodedJWT.getClaim(TwitchJWTField.CHANNEL_ID.getValue()).asString(),
				decodedJWT.getClaim(TwitchJWTField.ROLE.getValue()).asString()));
		
		
		UpdateGameStateRequest request = gson.fromJson(serverlessInput.getBody(), UpdateGameStateRequest.class);
		StreamContext streamContext = getStreamInfo(decodedJWT);
		streamContext.setUserName(request.getUsername());
		
		GameUpdateHandler handler = handlerFactory.get(request.getGame());
		HandleGameUpdateResponse response = null;
		
		try {
			response = handler.handleGameUpdate(request.getGameId(), request.getGameSpecificState(), streamContext);
		} catch (GameCompletedException e) {
			throw new RuntimeException(e);
		}
		
        return ServerlessOutput.builder()
        		.headers(getOutputHeaders())
        		.statusCode(HttpStatus.SC_OK)
        		.body(gson.toJson(response, response.getClass()))
        		.build();
	}

}
