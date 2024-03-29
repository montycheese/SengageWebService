package io.sengage.webservice.function;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j2;

import org.apache.http.HttpStatus;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.auth.JwtUtils;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.model.CreateGameRequest;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.sengames.handler.CreateGameHandler;
import io.sengage.webservice.sengames.handler.CreateGameHandlerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

@Log4j2
public class CreateGame extends BaseLambda<ServerlessInput, ServerlessOutput> {
	 
	@Inject
	Gson gson;
	
	@Inject
	CreateGameHandlerFactory createGameHandlerFactory;
	
	@Inject
	AuthorizationHelper authHelper;
	
	public CreateGame() {
		ExtensionComponent component = DaggerExtensionComponent.create();
		component.injectCreateGame(this);
	}
	
	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		log.info("Request: " + serverlessInput.getBody());
		
		DecodedJWT jwt = authHelper.authenticateCreateGameRequest(parseAuthTokenFromHeaders(serverlessInput.getHeaders()));
		
		CreateGameRequest request = gson.fromJson(serverlessInput.getBody(), CreateGameRequest.class);
		
		CreateGameHandler handler = createGameHandlerFactory.get(request.getGame());
		
		StreamContext streamContext = JwtUtils.getStreamContext(jwt);
		log.debug("About to handle create game");
		String gameId = handler.handleCreateGame(request.getGame(), 
				request.getGameSpecificParameters(),
				request.getDuration(), 
				streamContext);
		
		return ServerlessOutput.builder()
				.headers(getOutputHeaders())
				.statusCode(HttpStatus.SC_OK)
				.body(ImmutableMap.of("gameId", gameId).toString())
				.build();
	}
}
