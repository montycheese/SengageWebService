package io.sengage.webservice.dagger;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ExtensionModule {
	//env vars
	public static final String EXTENSION_SECRET = "Secret";
	public static final String CLIENT_ID = "ClientId";
	public static final String EXTENSION_VERSION = "ExtensionVersion";
	public static final String EXTENSION_OWNER_TWITCH_ID = "ExtensionOwnerTwitchId";

	
	@Named(CLIENT_ID)
	@Provides
	@Singleton
	static String provideClientId() {
		return System.getenv(CLIENT_ID);
	}
	
	@Named(EXTENSION_VERSION)
	@Provides
	@Singleton
	static String provideExtensionVersion() {
		return System.getenv(EXTENSION_VERSION);
	}
	
	@Named(EXTENSION_OWNER_TWITCH_ID)
	@Provides
	@Singleton
	static String provideExtensionOwnerTwitchId() {
		return System.getenv(EXTENSION_OWNER_TWITCH_ID);
	}
	
	@Named(EXTENSION_SECRET)
	@Provides
	@Singleton
	static String provideExtensionSecret() {
		return System.getenv(EXTENSION_SECRET);
	}
}
