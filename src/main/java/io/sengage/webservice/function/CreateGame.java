package io.sengage.webservice.function;

import javax.inject.Inject;

import org.apache.http.HttpStatus;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.auth.JwtUtils;
import io.sengage.webservice.model.CreateGameRequest;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.sengames.handler.CreateGameHandler;
import io.sengage.webservice.sengames.handler.CreateGameHandlerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

public class CreateGame extends BaseLambda<ServerlessInput, ServerlessOutput> {
	 
	@Inject
	Gson gson;
	
	@Inject
	CreateGameHandlerFactory createGameHandlerFactory;
	
	@Inject
	AuthorizationHelper authHelper;
	
	private LambdaLogger logger;
	
	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		logger = context.getLogger();
		logger.log("Request: " + serverlessInput.getBody());
		
		DecodedJWT jwt = authHelper.authenticateCreateGameRequest(parseAuthTokenFromHeaders(serverlessInput.getHeaders()));
		
		CreateGameRequest request = gson.fromJson(serverlessInput.getBody(), CreateGameRequest.class);
		
		CreateGameHandler handler = createGameHandlerFactory.get(request.getGame());
		
		StreamContext streamContext = JwtUtils.getStreamContext(jwt);
		
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
