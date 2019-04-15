package io.sengage.webservice.function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.WebSocketInput;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.IOUtils;
import com.google.gson.Gson;

public class OnConnect implements RequestStreamHandler {

	private LambdaLogger logger;


	@Override
	public void handleRequest(final InputStream input, final OutputStream output, Context context) throws IOException {
	    logger = context.getLogger();
		
	    
	    String s = IOUtils.toString(input);
	    
	    logger.log("SendMessage: input: " + s);
	    ServerlessOutput serverlessOutput = new ServerlessOutput();
	    serverlessOutput.setBody("Hello!");
	    serverlessOutput.setStatusCode(HttpStatus.SC_OK);
	    serverlessOutput.setHeaders(getOutputHeaders());
	    Gson gson = new Gson();
	    output.write(gson.toJson(serverlessOutput, ServerlessOutput.class).getBytes());
	}

	protected Map<String, String> getOutputHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Requested-With", "*");
		headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,x-requested-with");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, OPTIONS, PATCH");
		return headers;
	}
}
