package io.sengage.webservice.dagger;

import javax.inject.Singleton;

import io.sengage.webservice.events.CWEventsHandler;
import dagger.Component;

@Singleton
@Component(modules = { BaseModule.class, DataModule.class, ExtensionModule.class, NetworkingModule.class })
public interface EventsComponent {

	void injectCWEventsHandler(CWEventsHandler handler);
	
}
