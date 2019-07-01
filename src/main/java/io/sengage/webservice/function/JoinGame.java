package io.sengage.webservice.function;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j2;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.model.JoinGameRequest;
import io.sengage.webservice.model.JoinGameResponse;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.sengames.handler.JoinGameHandler;

@Log4j2
public class JoinGame extends BaseLambda<ServerlessInput, ServerlessOutput> {
	
	@Inject
	Gson gson;
	
	@Inject
	AuthorizationHelper authHelper;
	
	@Inject 
	JoinGameHandler joinGameHandler;
	
	public JoinGame() {
		ExtensionComponent component = DaggerExtensionComponent.create();
		component.injectJoinGame(this);
	}

	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		log.debug("JoinGame(): input: " + serverlessInput);
		
		
		DecodedJWT jwt = authHelper.authenticateRequestAndVerifyToken(parseAuthTokenFromHeaders(serverlessInput.getHeaders()));
		
		JoinGameRequest request = gson.fromJson(serverlessInput.getBody(), JoinGameRequest.class);
		boolean joinedSuccessfully = true;
		String failureReason = null;
		
		StreamContext streamContext = getStreamInfo(jwt);
		streamContext.setUserName(request.getUserName());
		try {
			joinGameHandler.handleJoinGame(request.getGameId(), streamContext);
			// todo catch certain exceptions only
		} catch (Exception e) {
			joinedSuccessfully = false;
			failureReason = e.getMessage();
			log.warn("Failed to join user {} to game {}", streamContext.getOpaqueId(), request.getGameId(), e);
		}
		
		return ServerlessOutput.builder()
				.headers(getOutputHeaders())
				.statusCode(HttpStatus.SC_OK)
				.body(gson.toJson(JoinGameResponse.builder()
						.joinedSuccessfully(joinedSuccessfully)
						.failureReason(failureReason)
						.build(), JoinGameResponse.class))
				.build();
	}

}
