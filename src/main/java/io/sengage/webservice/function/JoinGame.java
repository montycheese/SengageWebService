package io.sengage.webservice.function;

import javax.inject.Inject;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
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

public class JoinGame extends BaseLambda<ServerlessInput, ServerlessOutput> {
	
	@Inject
	Gson gson;
	
	@Inject
	AuthorizationHelper authHelper;
	
	@Inject 
	JoinGameHandler joinGameHandler;
	
	private LambdaLogger logger;
	
	public JoinGame() {
		ExtensionComponent component = DaggerExtensionComponent.create();
		component.injectJoinGame(this);
	}

	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		// TODO Auto-generated method stub
		logger = context.getLogger();
		logger.log("JoinGame(): input: " + serverlessInput);
		
		
		DecodedJWT jwt = authHelper.authenticateRequestAndVerifyToken(parseAuthTokenFromHeaders(serverlessInput.getHeaders()));
		
		// todo addd username here
		StreamContext streamContext = getStreamInfo(jwt);
		
		JoinGameRequest request = gson.fromJson(serverlessInput.getBody(), JoinGameRequest.class);
		boolean joinedSuccessfully = true;
		String failureReason = null;
		try {
			joinGameHandler.handleJoinGame(request.getGameId(), streamContext);
			// todo catch certain exceptions only
		} catch (Exception e) {
			joinedSuccessfully = false;
			failureReason = e.getMessage();
			e.printStackTrace();
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
