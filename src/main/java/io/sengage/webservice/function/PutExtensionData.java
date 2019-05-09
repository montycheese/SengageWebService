package io.sengage.webservice.function;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.auth.TwitchJWTField;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;

public class PutExtensionData extends BaseLambda<ServerlessInput, ServerlessOutput> {

	@Inject
	AuthorizationHelper authHelper;
	
	private LambdaLogger logger;
	
	public PutExtensionData() {
		 ExtensionComponent extensionComponent = DaggerExtensionComponent.create();
		 extensionComponent.injectPutExtensionData(this);
	}
	
	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		logger = context.getLogger();
		logger.log("PostExtensionData() input: " + serverlessInput);
		
		String token = parseAuthTokenFromHeaders(serverlessInput.getHeaders());
		DecodedJWT decodedJWT = authHelper.authenticateRequestAndVerifyToken(token);
		
		logger.log(String.format("Sent by user [%s] watching channel [%s] with role [%s]",
				decodedJWT.getClaim(TwitchJWTField.USER_ID.getValue()).asString(),
				decodedJWT.getClaim(TwitchJWTField.CHANNEL_ID.getValue()).asString(),
				decodedJWT.getClaim(TwitchJWTField.ROLE.getValue()).asString()));
		
		
        return ServerlessOutput.builder()
        		.headers(getOutputHeaders())
        		.statusCode(HttpStatus.SC_OK)
        		.body(UUID.randomUUID().toString())
        		.build();
	}

}
