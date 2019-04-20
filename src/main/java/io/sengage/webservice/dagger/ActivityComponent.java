package io.sengage.webservice.dagger;

import io.sengage.webservice.function.RequestHandler;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {BaseModule.class})
public interface ActivityComponent {
	void injectRequestHandler(RequestHandler requestHandler); 
}
