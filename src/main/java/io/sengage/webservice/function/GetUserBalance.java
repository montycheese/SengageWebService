package io.sengage.webservice.function;

import java.util.Optional;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.auth.JwtUtils;
import io.sengage.webservice.balance.TransactionProcessor;
import io.sengage.webservice.balance.TransactionProcessorFactory;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.model.MinigamesUserBalance;
import io.sengage.webservice.model.Platform;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.model.UserBalanceResponse;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j2;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;

@Log4j2
public class GetUserBalance extends BaseLambda<ServerlessInput, ServerlessOutput> {
	private static final long UNINITIALIZED_BALANCE = -1; // balance if user has never claimed coins
	private static final long STARTING_VERSION = 0; 
	
	@Inject
	AuthorizationHelper authHelper;
	
	@Inject
	Gson gson;
	
	@Inject
	TransactionProcessorFactory processorFactory;
	
	public GetUserBalance() {
		 ExtensionComponent extensionComponent = DaggerExtensionComponent.create();
		 extensionComponent.injectGetUserBalance(this);
	}

	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		log.debug("Request input: {}", serverlessInput);
		
		DecodedJWT jwt = authHelper.authenticateBalanceRequest(parseAuthTokenFromHeaders(serverlessInput.getHeaders()));
		StreamContext streamContext = JwtUtils.getStreamContext(jwt);
		
		if (!serverlessInput.getQueryStringParameters().containsKey(PLATFORM_KEY)) {
			throw new IllegalArgumentException("Must provide Platfrom parameter, one of: " + Platform.values());
		}
		
		Platform platform = Platform.of(serverlessInput.getQueryStringParameters().get(PLATFORM_KEY));
		
		TransactionProcessor processor = processorFactory.get(platform);
		Optional<MinigamesUserBalance> userBalance = processor.getUserBalance(streamContext.getChannelId(), streamContext.getUserId());
		
		UserBalanceResponse response = UserBalanceResponse.builder()
				.balance(userBalance.map(balance -> balance.getBalance()).orElse(UNINITIALIZED_BALANCE))
				.version(userBalance.map(balance -> balance.getVersion()).orElse(STARTING_VERSION))
				.build();
		
		log.debug("Returning balance of {} for user {} on channel {} on platform {} with version", response.getBalance(), streamContext.getUserId(),
				streamContext.getChannelId(), platform.name(), response.getVersion());
		
		return ServerlessOutput.builder()
				.body(gson.toJson(response, UserBalanceResponse.class))
				.headers(getOutputHeaders())
				.statusCode(HttpStatus.SC_OK)
				.build();
	}
}
