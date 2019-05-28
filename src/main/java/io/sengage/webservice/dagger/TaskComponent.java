package io.sengage.webservice.dagger;

import javax.inject.Singleton;

import io.sengage.webservice.function.GameTaskHandler;
import dagger.Component;

@Singleton
@Component(modules = { TaskModule.class, BaseModule.class, DataModule.class,
		ExtensionModule.class, NetworkingModule.class })
public interface TaskComponent {

	void injectGameTaskHandler(GameTaskHandler handler);
	
}
