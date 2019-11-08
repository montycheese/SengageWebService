package io.sengage.webservice.function;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.auth.JwtUtils;
import io.sengage.webservice.cache.GameCache;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.StreamContext;

import javax.inject.Inject;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.auth0.jwt.interfaces.DecodedJWT;

public class FetchChannelActivity extends BaseLambda<ServerlessInput, ServerlessOutput> {
	
	@Inject
	AuthorizationHelper authHelper;
	
	@Inject
	GameCache gameCache;
	
	
	public FetchChannelActivity() {
		ExtensionComponent component = DaggerExtensionComponent.create();
		component.injectFetchChannelActivity(this);
	}
	
	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		DecodedJWT jwt = authHelper.authenticateRequestAndVerifyToken((parseAuthTokenFromHeaders(serverlessInput.getHeaders())));
		
		StreamContext streamContext = JwtUtils.getStreamContext(jwt);
		
		String output = (String) gameCache.getGameDetails(streamContext.getChannelId());
		
		
		return ServerlessOutput.builder()
				.headers(getOutputHeaders())
				.statusCode(HttpStatus.SC_OK)
				.body(output)
				.build();
	}
}