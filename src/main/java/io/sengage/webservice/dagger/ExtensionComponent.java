package io.sengage.webservice.dagger;

import io.sengage.webservice.function.PutExtensionData;
import io.sengage.webservice.function.UpdateGameState;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {BaseModule.class, ExtensionModule.class})
public interface ExtensionComponent {
	void injectPutExtensionData(PutExtensionData putExtensionData);
	void injectUpdateGameState(UpdateGameState updateGameState);
}
