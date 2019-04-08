package io.sengage.webservice.function;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class SendMessage extends BaseLambda{

private LambdaLogger logger;
	
	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        logger = context.getLogger();
		
		int letter;
        String eventObject = "";

        while ((letter = input.read()) > -1) {
            char inputChar= (char) letter;
            eventObject += inputChar;
        }
        
        logger.log("SendMessage: input: " + eventObject);
        
        //Passing a custom response as the output string
        String response = "{\n" +
                "    \"statusCode\": 200,\n" +
                "    \"headers\": {\"Content-Type\": \"application/json\"},\n" +
                "    \"body\": \"plain text response\"\n" +
                "}";
        
        logger.log("SendMessage: response: " + response);
        
        output.write(response.getBytes());
	}

}
