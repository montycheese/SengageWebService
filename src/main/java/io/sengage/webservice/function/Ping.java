package io.sengage.webservice.function;

import java.time.Instant;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.extern.log4j.Log4j2;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.dagger.BaseModule;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.sengames.handler.CancelGameHandler;
import io.sengage.webservice.sengames.handler.CreateGameHandlerFactory;
import io.sengage.webservice.sengames.handler.EndGameHandlerFactory;
import io.sengage.webservice.sengames.handler.GameUpdateHandlerFactory;
import io.sengage.webservice.sengames.handler.JoinGameHandler;
import io.sengage.webservice.sengames.handler.StartGameHandlerFactory;

/**
 * Class to help reduce lambda cold starts. This should be called periodically so that static singletons are reinitalized
 *
 */
@Log4j2
public class Ping extends BaseLambda<Object, ServerlessOutput> {
	
	private static final String KEEP_WARM_JSON_INPUT = "{\"httpMethod\": \"GET\",  \"path\": \"/keepWarm\"}";
	private static final String CONCURRENT_EXECUTIONS_ENV_VAR = "ConcurrentExecutions";
	
	@Inject
	GameDataProvider gameDataProvider;

	@Inject
	Gson gson;
	
	@Inject
	CreateGameHandlerFactory createGameHandlerFactory;
	
	@Inject 
	JoinGameHandler joinGameHandler;
	
	@Inject
	GameUpdateHandlerFactory handlerFactory;
	
	@Inject
	EndGameHandlerFactory endGameHandlerFactory;
	
	@Inject
	StartGameHandlerFactory startGameHandlerFactory;
	
	@Inject
	CancelGameHandler cancelGameHandler;
	
	@Inject
	AuthorizationHelper authHelper;
	
	@Inject
	AWSLambdaAsync lambdaClient;
	
	@Inject
	@Named(BaseModule.SENGAGE_WS_LAMBDA_ARN)
	String sengageWSLambdaArn;
	
	public Ping() {
		ExtensionComponent component = DaggerExtensionComponent.create();
		component.injectPing(this);
	}
	
	@Override
	public ServerlessOutput handleRequest(Object input, Context context) {
		log.debug("Ping(): received ping");
		// HACKY WAY TO REDUCE DDB COLD START FOR NOW.
		gameDataProvider.getGame("046ef9ae-ea3d-419c-adf1-0897981aed8f");
		
		final InvokeRequest request = new InvokeRequest()
			.withFunctionName(sengageWSLambdaArn)
			.withPayload(KEEP_WARM_JSON_INPUT)
			.withInvocationType(InvocationType.Event);
		IntStream.range(0, getNumConcurrentExecutions()).parallel().forEach(i -> {
			log.debug("Invoking lambda {}. Iteration {}", sengageWSLambdaArn, i);
			try {
				lambdaClient.invoke(request);
			} catch (Exception e) {
				log.warn("Caught exception while invoking lambda async: {}", e);
			}
		});
		
		
		ServerlessOutput output = new ServerlessOutput();
		output.setStatusCode(HttpStatus.SC_OK);
	    output.setHeaders(getOutputHeaders());
	    output.setBody(Instant.now().toString());
	    return output;
	}
	
	private int getNumConcurrentExecutions() {
		return Integer.parseInt(System.getenv(CONCURRENT_EXECUTIONS_ENV_VAR));
	}
}
