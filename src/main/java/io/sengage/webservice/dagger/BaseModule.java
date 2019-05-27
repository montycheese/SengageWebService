package io.sengage.webservice.dagger;

import java.time.Instant;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsync;
import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsyncClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import io.sengage.webservice.function.CreateGame;
import io.sengage.webservice.function.GetFinalGameResults;
import io.sengage.webservice.function.JoinGame;
import io.sengage.webservice.function.UpdateGameState;
import io.sengage.webservice.model.GameSpecificParameters;
import io.sengage.webservice.model.GameSpecificState;
import io.sengage.webservice.router.LambdaRouter;
import io.sengage.webservice.router.Resource;
import io.sengage.webservice.sengames.model.CreateSingleStrokeGameParameters;
import io.sengage.webservice.sengames.model.SendLineRequest;
import io.sengage.webservice.utils.gson.InstantTypeConverter;
import io.sengage.webservice.utils.gson.RuntimeTypeAdapterFactory;
import dagger.Module;
import dagger.Provides;

@Module
public class BaseModule {

	public static final String CREATE_GAME_REQUEST_LABEL = "CreateGameRequest";
	public static final String GAME_SPECIFIC_STATE_LABEL = "GameSpecificState";

	@Provides
	@Singleton
	static LambdaRouter provideLambdaRouter() {
		return new LambdaRouter()
			.registerActivity(Resource.builder()
					.className(CreateGame.class.getName())
					.httpMethod("POST")
					.pattern(Pattern.compile("^/game$"))
					.build())
		    .registerActivity(Resource.builder()
		    		.className(UpdateGameState.class.getName())
		    		.httpMethod("PUT")
		    		.pattern(Pattern.compile("^/game/([^\\/]*)$"))
		    		.build())
    		.registerActivity(Resource.builder()
    				.className(JoinGame.class.getName())
    				.httpMethod("POST")
    				.pattern(Pattern.compile("^/game/(.*?)\\/join$"))
    				.build())
			.registerActivity(Resource.builder()
					.className(GetFinalGameResults.class.getName())
					.httpMethod("GET")
    				.pattern(Pattern.compile("^/game/(.*?)\\/results$"))
					.build()
					);
	}
	
	@Provides
	@Singleton
	static Gson provideGson(@Named(CREATE_GAME_REQUEST_LABEL) TypeAdapterFactory typeAdapterFactory,
			@Named(GAME_SPECIFIC_STATE_LABEL) TypeAdapterFactory gameSpecificStateTypeAdapterFactory) {
		return new GsonBuilder()
		.registerTypeAdapter(Instant.class, new InstantTypeConverter())
		.registerTypeAdapterFactory(typeAdapterFactory)
		.registerTypeAdapterFactory(gameSpecificStateTypeAdapterFactory)
		.serializeNulls()
		.create();
	}
	
	@Provides
	@Singleton
	@Named(CREATE_GAME_REQUEST_LABEL)
	static TypeAdapterFactory provideRuntimeAdapterFactory() {
		return RuntimeTypeAdapterFactory
				.of(GameSpecificParameters.class, "type")
				.registerSubtype(CreateSingleStrokeGameParameters.class, "SINGLE_STROKE");
	}
	
	@Provides
	@Singleton
	@Named(GAME_SPECIFIC_STATE_LABEL)
	static TypeAdapterFactory provideGameSpecificStateRuntimeAdapterFactory() {
		return RuntimeTypeAdapterFactory
				.of(GameSpecificState.class, "type")
				.registerSubtype(SendLineRequest.class, SendLineRequest.type);
	}
	
	@Provides
	@Singleton
	static AmazonCloudWatchEventsAsync provideAmazonCloudWatchEventsAsync() {
		return AmazonCloudWatchEventsAsyncClientBuilder.defaultClient();
	}

}
