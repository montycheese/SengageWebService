package io.sengage.webservice.function;

import io.sengage.webservice.dagger.ActivityComponent;
import io.sengage.webservice.dagger.DaggerActivityComponent;
import io.sengage.webservice.exception.GameCompletedException;
import io.sengage.webservice.exception.GameInProgressException;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.router.LambdaRouter;

import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j2;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.util.Throwables;
import com.auth0.jwt.exceptions.JWTVerificationException;

@Log4j2
public final class RequestHandler extends BaseLambda<ServerlessInput, ServerlessOutput> {
	
	@Inject 
	LambdaRouter router;
	
	public RequestHandler() {
		ActivityComponent component = DaggerActivityComponent.create();
		component.injectRequestHandler(this);
	}
	
	private LambdaLogger logger;
	
	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		log.info("Received request with METHOD {}, at PATH {}", serverlessInput.getHttpMethod(), serverlessInput.getPath());
		logger = context.getLogger();
		
		ServerlessOutput output = new ServerlessOutput();
		
		try {
			Instant before = Instant.now();
			BaseLambda<ServerlessInput, ServerlessOutput> activity = 
					router.getMatchingActivity(serverlessInput.getPath(), serverlessInput.getHttpMethod()).get();
			Instant after = Instant.now();
			long timeElapsed = Duration.between(before, after).toMillis();
			log.debug("RequestHandler#handleRequest(): Time to initalize activity: {} milliseconds", timeElapsed);
			log.info("RequestHandler#handleRequest(): Routing request at path " + serverlessInput.getPath() +
					" with method: " + serverlessInput.getHttpMethod() + " to activity: " + activity.getClass().getName());
			output = activity.handleRequest(serverlessInput,  context);
		} catch(NoSuchElementException e) {
			log.warn("Could not find matching route for: " + serverlessInput.getPath());
			output.setStatusCode(HttpStatus.SC_NOT_FOUND);
        	logException(logger, e);
            output.setBody("Could not find path " + serverlessInput.getPath());
		} catch(JWTVerificationException e) {
		 	log.warn("RequestHandler#handleRequest(): JWT token verification error: " + e.getMessage());
			output.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
        	logException(logger, e);
            output.setBody("Unauthorized caller.");
		} catch (GameInProgressException e) {
			log.warn("RequestHandler#handleRequest()", e);
			output.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			output.setBody(e.getMessage());
		} catch (Exception e) {
			Throwable throwable = Throwables.getRootCause(e);
			if (throwable instanceof GameCompletedException) {
	        	log.warn("RequestHandler#handleRequest(): GameCompletedException thrown in server: " + e.getMessage());
	        	output.setStatusCode(HttpStatus.SC_BAD_REQUEST);
	        	logException(logger, e);
	        	output.setBody("Game has already completed.");
			} else {
	        	log.warn("RequestHandler#handleRequest(): unexpected exception thrown during execution " + e.getMessage());
	        	output.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	        	logException(logger, e);
	            output.setBody("Unexpected error occurred while processing request");
			}
		}
		
        output.setHeaders(getOutputHeaders());
		return output;
	}
}
