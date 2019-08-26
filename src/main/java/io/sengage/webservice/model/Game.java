package io.sengage.webservice.model;

import lombok.Getter;

@Getter
public enum Game {
	SINGLE_STROKE("Paint Party"),
	FLAPPY_BIRD_BR("Flappy Bird Battle Royale")
	;
	
	private String friendlyName;
	
	private Game(String friendlyName) {
		this.friendlyName = friendlyName;
	}
}
