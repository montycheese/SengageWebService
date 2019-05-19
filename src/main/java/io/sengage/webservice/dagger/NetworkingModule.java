package io.sengage.webservice.dagger;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;

import dagger.Module;
import dagger.Provides;

@Module(includes = BaseModule.class)
public class NetworkingModule {
	@Provides
	@Singleton
	static HttpTransport provideHttpTransport() {
		return new NetHttpTransport();
	}
	
	@Provides
	@Singleton
	static JsonFactory provideJsonFactory() {
		return new GsonFactory();
	}
	
	@Provides
	@Singleton
	static HttpRequestFactory provideHttpRequestFactory(HttpTransport httpTransport, 
			JsonFactory jsonFactory,
			@Named(ExtensionModule.CLIENT_ID) String clientId) {
		return httpTransport.createRequestFactory(new HttpRequestInitializer() {

			@Override
			public void initialize(HttpRequest request) throws IOException {
				request.setParser(new JsonObjectParser(jsonFactory));
				
				HttpHeaders baseHeaders = new HttpHeaders();
				baseHeaders.setContentType("application/json");
				baseHeaders.set("Client-ID", clientId);
			}
			
		});
	}
}
