package io.sengage.webservice.function;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.model.CancelGameRequest;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.sengames.handler.CancelGameHandler;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j2;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;

@Log4j2
public class CancelGame extends BaseLambda<ServerlessInput, ServerlessOutput> {
	 
	@Inject
	Gson gson;
	
	@Inject
	AuthorizationHelper authHelper;
	
	@Inject
	CancelGameHandler cancelGameHandler;
	
	public CancelGame() {
		ExtensionComponent component = DaggerExtensionComponent.create();
		component.injectCancelGame(this);
	}
	
	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		log.info("CancelGameRequest: " + serverlessInput.getBody());
		
		authHelper.authenticateCancelGameRequest((parseAuthTokenFromHeaders(serverlessInput.getHeaders())));
		
		CancelGameRequest request = gson.fromJson(serverlessInput.getBody(), CancelGameRequest.class);
		
		cancelGameHandler.handleCancelGame(request.getGameId(), request.getCancellationReason());
		
		return ServerlessOutput.builder()
				.headers(getOutputHeaders())
				.statusCode(HttpStatus.SC_OK)
				.body(null)
				.build();
	}

}
