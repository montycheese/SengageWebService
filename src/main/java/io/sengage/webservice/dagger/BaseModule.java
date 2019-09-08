package io.sengage.webservice.dagger;

import java.time.Instant;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsync;
import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsyncClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import io.sengage.webservice.function.CancelGame;
import io.sengage.webservice.function.CreateGame;
import io.sengage.webservice.function.GetFinalGameResults;
import io.sengage.webservice.function.JoinGame;
import io.sengage.webservice.function.KeepWarm;
import io.sengage.webservice.function.Ping;
import io.sengage.webservice.function.QuitGame;
import io.sengage.webservice.function.UpdateGameState;
import io.sengage.webservice.model.EndGameResult;
import io.sengage.webservice.model.GameSpecificParameters;
import io.sengage.webservice.model.GameSpecificState;
import io.sengage.webservice.model.flappybird.FlappyBirdEndGameResult;
import io.sengage.webservice.model.singlestroke.SingleStrokeEndGameResult;
import io.sengage.webservice.router.LambdaRouter;
import io.sengage.webservice.router.Resource;
import io.sengage.webservice.sengames.model.flappybird.CreateFlappyBirdGameParameters;
import io.sengage.webservice.sengames.model.flappybird.ResurrectUserRequest;
import io.sengage.webservice.sengames.model.flappybird.SendFlightResultRequest;
import io.sengage.webservice.sengames.model.singlestroke.CreateSingleStrokeGameParameters;
import io.sengage.webservice.sengames.model.singlestroke.SendLineRequest;
import io.sengage.webservice.utils.gson.InstantTypeConverter;
import io.sengage.webservice.utils.gson.RuntimeTypeAdapterFactory;
import dagger.Module;
import dagger.Provides;

@Module
public class BaseModule {

	public static final String CREATE_GAME_REQUEST_LABEL = "CreateGameRequest";
	public static final String GAME_SPECIFIC_STATE_LABEL = "GameSpecificState";
	public static final String END_GAME_RESULT_LABEL = "EndGameResult";
	public static final String SENGAGE_WS_LAMBDA_ARN = "SengageWSLambdaArn";
	
	@Provides
	@Singleton
	static LambdaRouter provideLambdaRouter() {
		return new LambdaRouter()
			.registerActivity(Resource.builder()
					.className(Ping.class.getName())
					.httpMethod("GET")
					.pattern(Pattern.compile("^/ping$"))
					.build())
			.registerActivity(Resource.builder()
					.className(KeepWarm.class.getName())
					.httpMethod("GET")
					.pattern(Pattern.compile("^/keepWarm$"))
					.build())
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
		    		.className(QuitGame.class.getName())
		    		.httpMethod("POST")
		    		.pattern(Pattern.compile("^/game/(.*?)\\/quit$"))
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
					.build())
			.registerActivity(Resource.builder()
					.className(CancelGame.class.getName())
					.httpMethod("POST")
					.pattern(Pattern.compile("^/game/(.*?)\\/cancel$"))
					.build());
	}
	
	@Provides
	@Singleton
	static Gson provideGson(@Named(CREATE_GAME_REQUEST_LABEL) TypeAdapterFactory typeAdapterFactory,
			@Named(GAME_SPECIFIC_STATE_LABEL) TypeAdapterFactory gameSpecificStateTypeAdapterFactory,
			@Named(END_GAME_RESULT_LABEL) TypeAdapterFactory endGameResultTypeAdapterFactory) {
		return new GsonBuilder()
			.registerTypeAdapter(Instant.class, new InstantTypeConverter())
			.registerTypeAdapterFactory(typeAdapterFactory)
			.registerTypeAdapterFactory(gameSpecificStateTypeAdapterFactory)
			.registerTypeAdapterFactory(endGameResultTypeAdapterFactory)
			.serializeNulls()
			.create();
	}
	
	@Provides
	@Singleton
	@Named(CREATE_GAME_REQUEST_LABEL)
	static TypeAdapterFactory provideRuntimeAdapterFactory() {
		return RuntimeTypeAdapterFactory
				.of(GameSpecificParameters.class, "type")
				.registerSubtype(CreateSingleStrokeGameParameters.class, CreateSingleStrokeGameParameters.type)
				.registerSubtype(CreateFlappyBirdGameParameters.class, CreateFlappyBirdGameParameters.type);
	}
	
	@Provides
	@Singleton
	@Named(GAME_SPECIFIC_STATE_LABEL)
	static TypeAdapterFactory provideGameSpecificStateRuntimeAdapterFactory() {
		return RuntimeTypeAdapterFactory
				.of(GameSpecificState.class, "type")
				.registerSubtype(SendLineRequest.class, SendLineRequest.typeName)
				.registerSubtype(SendFlightResultRequest.class, SendFlightResultRequest.typeName)
				.registerSubtype(ResurrectUserRequest.class, ResurrectUserRequest.typeName);
	}
	
	@Provides
	@Singleton
	@Named(END_GAME_RESULT_LABEL)
	static TypeAdapterFactory provideEndGameResultRuntimeAdapterFactory() {
		return RuntimeTypeAdapterFactory
				.of(EndGameResult.class, "type")
				.registerSubtype(SingleStrokeEndGameResult.class, SingleStrokeEndGameResult.type)
				.registerSubtype(FlappyBirdEndGameResult.class, FlappyBirdEndGameResult.type);
	}
	
	@Provides
	@Singleton
	static AmazonCloudWatchEventsAsync provideAmazonCloudWatchEventsAsync() {
		return AmazonCloudWatchEventsAsyncClientBuilder
				.standard()
				.withCredentials(new EnvironmentVariableCredentialsProvider())
				.build();
	}
	
	@Provides
	@Singleton
	static AWSLambdaAsync provideAWSLambdaAsync() {
		return AWSLambdaAsyncClientBuilder
				.standard()
				.withCredentials(new EnvironmentVariableCredentialsProvider())
				.build();
	}
	
	@Provides
	@Singleton
	@Named(SENGAGE_WS_LAMBDA_ARN)
	static String provideSengageWSLambdaFunctionName() {
		return System.getenv(SENGAGE_WS_LAMBDA_ARN);
	}
}
