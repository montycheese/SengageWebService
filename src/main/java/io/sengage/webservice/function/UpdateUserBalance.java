package io.sengage.webservice.function;

import javax.inject.Inject;

import org.apache.http.HttpStatus;

import lombok.extern.log4j.Log4j2;

import com.amazonaws.services.lambda.runtime.Context;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.auth.JwtUtils;
import io.sengage.webservice.balance.TransactionProcessor;
import io.sengage.webservice.balance.TransactionProcessorFactory;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.exception.FundsLimitExceededException;
import io.sengage.webservice.exception.InsufficientFundsException;
import io.sengage.webservice.model.MinigamesUserBalance;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.model.UpdateUserBalanceRequest;
import io.sengage.webservice.model.UserBalanceResponse;

@Log4j2
public class UpdateUserBalance extends BaseLambda<ServerlessInput, ServerlessOutput> {
	@Inject
	AuthorizationHelper authHelper;
	
	@Inject
	Gson gson;
	
	@Inject
	TransactionProcessorFactory processorFactory;
	
	public UpdateUserBalance() {
		 ExtensionComponent extensionComponent = DaggerExtensionComponent.create();
		 extensionComponent.injectUpdateUserBalance(this);
	}

	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		log.info("Request input: {}", serverlessInput.getBody());
		
		DecodedJWT jwt = authHelper.authenticateBalanceRequest(parseAuthTokenFromHeaders(serverlessInput.getHeaders()));
		StreamContext streamContext = JwtUtils.getStreamContext(jwt);
		
		UpdateUserBalanceRequest request = gson.fromJson(serverlessInput.getBody(), UpdateUserBalanceRequest.class);

		TransactionProcessor processor = processorFactory.get(request.getPlatform());
		MinigamesUserBalance newUserBalance;
		
		try {
			if (request.getStreamingServicePaymentMetadata() == null) {
				// then this is not the bits TX
				newUserBalance = processor.handleBalanceUpdate(streamContext.getChannelId(), streamContext.getUserId(), 
							request.getAmount(), request.getVersion());
			} else {
				newUserBalance = processor.handleBalanceUpdate(streamContext.getChannelId(), streamContext.getUserId(), 
						request.getAmount(), request.getStreamingServicePaymentMetadata(), request.getVersion());
			}
		} catch (InsufficientFundsException ex) {
			log.warn("Could not process transaction due to lack of sufficient funds", ex);
			throw new IllegalStateException("Could not process transaction due to lack of sufficient funds", ex);
		} catch (FundsLimitExceededException ex) {
			log.warn("Could not process transaction due to breeching max balance limit", ex);
			throw new IllegalStateException("Could not process transaction due to lack of sufficient funds", ex);
		}
		UserBalanceResponse response = UserBalanceResponse.builder()
				.balance(newUserBalance.getBalance())
				.version(newUserBalance.getVersion())
				.build();
		
		return ServerlessOutput.builder()
				.body(gson.toJson(response, UserBalanceResponse.class))
				.headers(getOutputHeaders())
				.statusCode(HttpStatus.SC_OK)
				.build();
	}
}
