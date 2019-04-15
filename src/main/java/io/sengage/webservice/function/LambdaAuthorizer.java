package io.sengage.webservice.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import io.sengage.webservice.model.AuthPolicy;
import io.sengage.webservice.model.ServerlessInput;

public class LambdaAuthorizer extends BaseLambda<ServerlessInput, AuthPolicy> {
	
	private LambdaLogger logger;
	
	@Override
	public AuthPolicy handleRequest(ServerlessInput serverlessInput, Context context) {
		logger = context.getLogger();
		logger.log("LambdaAuthorizer: " + serverlessInput.toString());
		
		// TODO JWT DECODE
		
		return new AuthPolicy("user", AuthPolicy.PolicyDocument.getAllowStarPolicy(
				"us-east-1",
				context.getInvokedFunctionArn().split(":")[4],  // TODO INJECT
				serverlessInput.getRequestContext().getApiId(),
				serverlessInput.getRequestContext().getStage()));
	}

}
