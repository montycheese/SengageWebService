package io.sengage.webservice.model;

public enum Platform {
	TWITCH
	;
	
	public static Platform of(String platformAsString) {
		for (Platform platform : values()) {
			if (platform.name().equalsIgnoreCase(platformAsString)) {
				return platform;
			}
		}
		throw new IllegalArgumentException("No matching platform for: " + platformAsString);
	}
}
