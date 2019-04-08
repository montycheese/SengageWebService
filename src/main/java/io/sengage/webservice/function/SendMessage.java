package io.sengage.webservice.function;

import java.io.IOException;
import java.nio.ByteBuffer;

import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.WebSocketInput;

import org.apache.http.HttpStatus;

import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class SendMessage extends BaseLambda<WebSocketInput, ServerlessOutput> {

	private static final String CONNECTIONS_CALLBACK_URL = 
			"https://ohex0fwh97.execute-api.us-east-1.amazonaws.com/Beta/@connections";
	
	private LambdaLogger logger;
 
	@Override
	public ServerlessOutput handleRequest(WebSocketInput input, Context context) {
        logger = context.getLogger();
		
		final ServerlessOutput output = new ServerlessOutput();
        output.setBody("Hello!");
        output.setStatusCode(HttpStatus.SC_OK);
        output.setHeaders(getOutputHeaders());
		
        
        logger.log("SendMessage: input: " + input);
        
        // TODO use async
        AmazonApiGatewayManagementApi client = 
        		AmazonApiGatewayManagementApiClientBuilder.defaultClient();
       
        PostToConnectionRequest request = new PostToConnectionRequest()
        .withConnectionId(input.getRequestContext().getConnectionId())
        .withData(ByteBuffer.wrap(input.getBody().getBytes()));
        try {
            PostToConnectionResult result = client.postToConnection(request);
    		logger.log("SendMessage: " + result.toString());
        } catch(Exception e) {
        	logException(logger, e);
        }

        return output;
	}

}
