package io.sengage.webservice.dagger;

import java.util.regex.Pattern;

import javax.inject.Singleton;

import io.sengage.webservice.function.GetExtensionData;
import io.sengage.webservice.function.PostExtensionData;
import io.sengage.webservice.router.LambdaRouter;
import io.sengage.webservice.router.Resource;
import dagger.Module;
import dagger.Provides;

@Module
public class BaseModule {

	@Provides
	@Singleton
	static LambdaRouter provideLambdaRouter() {
		return new LambdaRouter()
		    .registerActivity(Resource.builder()
		    		.className(GetExtensionData.class.getName())
		    		.httpMethod("GET")
		    		.pattern(Pattern.compile("^/extensiondata$"))
		    		.build())
		    .registerActivity(Resource.builder()
		    		.className(PostExtensionData.class.getName())
		    		.httpMethod("POST")
		    		.pattern(Pattern.compile("^/extensiondata$"))
		    		.build());
	}
}
