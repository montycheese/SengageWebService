package io.sengage.webservice.function;

import java.nio.ByteBuffer;

import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.WebSocketInput;

import org.apache.http.HttpStatus;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class SendMessage extends BaseLambda<WebSocketInput, ServerlessOutput> {
	
	private LambdaLogger logger;
 
	@Override
	public ServerlessOutput handleRequest(WebSocketInput input, Context context) {
        logger = context.getLogger();
		
		final ServerlessOutput output = new ServerlessOutput();
        output.setBody("Hello!");
        output.setStatusCode(HttpStatus.SC_OK);
        output.setHeaders(getOutputHeaders());
		
        
        logger.log("SendMessage: input: " + input);
        
        EndpointConfiguration endpointConfig = new EndpointConfiguration(
            input.getRequestContext().getDomainName() + "/" + input.getRequestContext().getStage(),
            System.getenv("AWS_REGION")
        );
        // TODO use async
        AmazonApiGatewayManagementApi client = 
        		AmazonApiGatewayManagementApiClientBuilder.standard()
        		.withEndpointConfiguration(endpointConfig).build();
       
        PostToConnectionRequest request = new PostToConnectionRequest()
        .withConnectionId(input.getRequestContext().getConnectionId())
        .withData(ByteBuffer.wrap(input.getBody().getBytes()));
        try {
            PostToConnectionResult result = client.postToConnection(request);
    		logger.log("SendMessage: message response successful");
        } catch(Exception e) {
        	logException(logger, e);
        }

        return output;
	}

}
