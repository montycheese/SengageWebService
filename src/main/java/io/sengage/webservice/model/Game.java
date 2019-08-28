package io.sengage.webservice.model;

import lombok.Getter;

@Getter
public enum Game {
	SINGLE_STROKE("Paint Party"),
	FLAPPY_BIRD_BR("FlightMania")
	;
	
	private String friendlyName;
	
	private Game(String friendlyName) {
		this.friendlyName = friendlyName;
	}
}
