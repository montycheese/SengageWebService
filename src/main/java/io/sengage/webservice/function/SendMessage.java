package io.sengage.webservice.function;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.WebSocketInput;

import org.apache.http.HttpStatus;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.GoneException;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class SendMessage extends BaseLambda<WebSocketInput, ServerlessOutput> {
	
	private static final List<String> COLORS = new ArrayList<>();
	private static final Random rand = new Random();
	
	private LambdaLogger logger;
	
	static {
		COLORS.add("red");
		COLORS.add("blue");
		COLORS.add("green");
		COLORS.add("yellow");
		COLORS.add("pink");
		COLORS.add("black");
		COLORS.add("white");
		COLORS.add("orange");
		COLORS.add("purple");
		COLORS.add("cyan");
	}
 
	@Override
	public ServerlessOutput handleRequest(WebSocketInput input, Context context) {
        logger = context.getLogger();
        logger.log("SendMessage: input: " + input);
        
        EndpointConfiguration endpointConfig = new EndpointConfiguration(
            input.getRequestContext().getDomainName() + "/" + input.getRequestContext().getStage(),
            System.getenv("AWS_REGION")
        );
        AmazonApiGatewayManagementApi client = 
        		AmazonApiGatewayManagementApiClientBuilder.standard()
        		.withEndpointConfiguration(endpointConfig).build();
       
        PostToConnectionRequest request = new PostToConnectionRequest()
        .withConnectionId(input.getRequestContext().getConnectionId())
        .withData(ByteBuffer.wrap(getRandomColor().getBytes()));
        try {
            PostToConnectionResult result = client.postToConnection(request);
    		logger.log("SendMessage: message response successful");
        } catch(GoneException e) {
        	logger.log(String.format("SendMessage: The connection with the id %s no longer exists.", 
        			input.getRequestContext().getConnectionId()));
        	// TODO: error handling.
        } catch(Exception e) {
        	logException(logger, e);
        }

		final ServerlessOutput output = new ServerlessOutput();
        output.setBody("Hello!");
        output.setStatusCode(HttpStatus.SC_OK);
        output.setHeaders(getOutputHeaders());
        return output;
	}
	
	private String getRandomColor() {
		return COLORS.get(rand.nextInt(COLORS.size() - 1));
	}

}
