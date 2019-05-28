package io.sengage.webservice.dagger;

import javax.inject.Singleton;

import io.sengage.webservice.events.CWEventsHandler;
import io.sengage.webservice.function.GameTaskHandler;
import dagger.Component;

@Singleton
@Component(modules = { BaseModule.class, DataModule.class,
		ExtensionModule.class, NetworkingModule.class, StateMachineModule.class })
public interface TaskComponent {

	void injectGameTaskHandler(GameTaskHandler handler);
	void injectCWEventsHandler(CWEventsHandler handler);
	
}
