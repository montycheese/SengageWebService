package io.sengage.webservice.function;

import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.WebSocketInput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class OnConnect extends BaseLambda<WebSocketInput, ServerlessOutput> {

	private LambdaLogger logger;


	@Override
	public ServerlessOutput handleRequest(WebSocketInput input, Context context) {
	    logger = context.getLogger();
		
		final ServerlessOutput output = new ServerlessOutput();;
		
	    
	    logger.log("SendMessage: input: " + input);
	    
	 
	    
	    output.setBody("Hello!");
	    output.setStatusCode(HttpStatus.SC_OK);
	    output.setHeaders(getOutputHeaders());
	    return output;
	}

}
