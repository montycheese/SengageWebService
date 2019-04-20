package io.sengage.webservice.function;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;

public class GetExtensionData extends BaseLambda<ServerlessInput, ServerlessOutput> {

	private LambdaLogger logger;

	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		logger = context.getLogger();
		logger.log("GetExtensionData() input:" + serverlessInput);
		
		return ServerlessOutput.builder()
        		.headers(getOutputHeaders())
        		.statusCode(HttpStatus.SC_OK)
        		.body("Hello world")
        		.build();
	}
	
	
}
