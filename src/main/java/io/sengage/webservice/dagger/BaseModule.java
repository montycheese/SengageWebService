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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import io.sengage.webservice.balance.TwitchPaymentMetadata;
import io.sengage.webservice.function.CancelGame;
import io.sengage.webservice.function.CreateGame;
import io.sengage.webservice.function.FetchChannelActivity;
import io.sengage.webservice.function.GetFinalGameResults;
import io.sengage.webservice.function.GetUserBalance;
import io.sengage.webservice.function.JoinGame;
import io.sengage.webservice.function.KeepWarm;
import io.sengage.webservice.function.Ping;
import io.sengage.webservice.function.QuitGame;
import io.sengage.webservice.function.UpdateGameState;
import io.sengage.webservice.function.UpdateUserBalance;
import io.sengage.webservice.model.EndGameResult;
import io.sengage.webservice.model.GameSpecificParameters;
import io.sengage.webservice.model.GameSpecificState;
import io.sengage.webservice.model.StreamingServicePaymentMetadata;
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
	public static final String STREAMING_SERVICE_PAYMENT_METADATA_LABEL = "StreamingServicePaymentMetadata";
	public static final String SENGAGE_WS_LAMBDA_ARN = "SengageWSLambdaArn";
	public static final String S3_BUCKET_NAME = "S3BucketDomainName";
	
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
					.build())
			.registerActivity(Resource.builder()
					.className(UpdateUserBalance.class.getName())
					.httpMethod("PUT")
					.pattern(Pattern.compile("^/balance$"))
					.build())
			.registerActivity(Resource.builder()
					.className(GetUserBalance.class.getName())
					.httpMethod("GET")
					.pattern(Pattern.compile("^/balance$"))
					.build())
			.registerActivity(Resource.builder()
					.className(FetchChannelActivity.class.getName())
					.httpMethod("GET")
					.pattern(Pattern.compile("^/channel$"))
					.build());
	}
	
	@Provides
	@Singleton
	static Gson provideGson(@Named(CREATE_GAME_REQUEST_LABEL) TypeAdapterFactory typeAdapterFactory,
			@Named(GAME_SPECIFIC_STATE_LABEL) TypeAdapterFactory gameSpecificStateTypeAdapterFactory,
			@Named(END_GAME_RESULT_LABEL) TypeAdapterFactory endGameResultTypeAdapterFactory,
			@Named(STREAMING_SERVICE_PAYMENT_METADATA_LABEL) TypeAdapterFactory streamingServicePaymentMetadataTypeAdapterFactory
			) {
		return new GsonBuilder()
			.registerTypeAdapter(Instant.class, new InstantTypeConverter())
			.registerTypeAdapterFactory(typeAdapterFactory)
			.registerTypeAdapterFactory(gameSpecificStateTypeAdapterFactory)
			.registerTypeAdapterFactory(endGameResultTypeAdapterFactory)
			.registerTypeAdapterFactory(streamingServicePaymentMetadataTypeAdapterFactory)
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
	@Named(STREAMING_SERVICE_PAYMENT_METADATA_LABEL)
	static TypeAdapterFactory provideStreamingServicePaymentMetadata() {
		return RuntimeTypeAdapterFactory
				.of(StreamingServicePaymentMetadata.class, "type")
				.registerSubtype(TwitchPaymentMetadata.class, TwitchPaymentMetadata.typeName);
	}
	
	@Provides
	@Singleton
	static AmazonCloudWatchEventsAsync provideAmazonCloudWatchEventsAsync(EnvironmentVariableCredentialsProvider credProvider) {
		return AmazonCloudWatchEventsAsyncClientBuilder
				.standard()
				.withCredentials(credProvider)
				.build();
	}
	
	@Provides
	@Singleton
	static AWSLambdaAsync provideAWSLambdaAsync(EnvironmentVariableCredentialsProvider credProvider) {
		return AWSLambdaAsyncClientBuilder
				.standard()
				.withCredentials(credProvider)
				.build();
	}
	
	@Provides
	@Singleton
	static AmazonS3 provideAmazonS3(EnvironmentVariableCredentialsProvider credProvider) {
		return AmazonS3ClientBuilder.standard()
				.withCredentials(credProvider)
				.build();
	}
	
	@Provides
	@Singleton
	static EnvironmentVariableCredentialsProvider provideCredentialsProvider() {
		return new EnvironmentVariableCredentialsProvider();
	}
	
	@Provides
	@Singleton
	@Named(SENGAGE_WS_LAMBDA_ARN)
	static String provideSengageWSLambdaFunctionName() {
		return System.getenv(SENGAGE_WS_LAMBDA_ARN);
	}
	
	@Provides
	@Singleton
	@Named(S3_BUCKET_NAME)
	static String provideS3BucketName() {
		// streamminigames.s3.amazonaws.com
		String domain = System.getenv(S3_BUCKET_NAME);
		int i = domain.indexOf(".s3.");
		return domain.substring(0, i);
	}
}
