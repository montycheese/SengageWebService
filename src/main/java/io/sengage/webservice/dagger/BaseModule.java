package io.sengage.webservice.dagger;

import java.time.Instant;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import io.sengage.webservice.function.CreateGame;
import io.sengage.webservice.function.UpdateGameState;
import io.sengage.webservice.model.GameSpecificState;
import io.sengage.webservice.router.LambdaRouter;
import io.sengage.webservice.router.Resource;
import io.sengage.webservice.sengames.model.SendLineRequest;
import io.sengage.webservice.utils.gson.InstantTypeConverter;
import io.sengage.webservice.utils.gson.RuntimeTypeAdapterFactory;
import dagger.Module;
import dagger.Provides;

@Module
public class BaseModule {


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
		    		.build());
	}
	
	@Provides
	@Singleton
	static Gson provideGson(TypeAdapterFactory typeAdapterFactory) {
		return new GsonBuilder()
		.registerTypeAdapter(Instant.class, new InstantTypeConverter())
		.registerTypeAdapterFactory(typeAdapterFactory)
		.serializeNulls()
		.create();
	}
	
	@Provides
	@Singleton
	static TypeAdapterFactory provideRuntimeAdapterFactory() {
		return RuntimeTypeAdapterFactory
				.of(GameSpecificState.class, "type")
				.registerSubtype(SendLineRequest.class, "SendLineRequest");
	}

}
