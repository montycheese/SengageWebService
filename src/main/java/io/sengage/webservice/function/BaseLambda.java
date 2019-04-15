package io.sengage.webservice.function;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public abstract class BaseLambda<I, O> 
  implements RequestHandler<I,O> {
	
	protected Map<String, String> getOutputHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("X-Requested-With", "*");
		headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,x-requested-with");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, OPTIONS, PATCH");
		return headers;
	}
	protected void logException(LambdaLogger logger, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        logger.log("logException() " + sw.toString());
	}
}
