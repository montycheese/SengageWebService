package io.sengage.webservice.sengames.handler;

import javax.inject.Inject;

import io.sengage.webservice.model.Game;

public class GameUpdateHandlerFactory {
	
	@Inject
	public GameUpdateHandlerFactory() {
	}
	
	public GameUpdateHandler get(Game game) {
		switch (game) {
		case SINGLE_STROKE:
			return new SingleStrokeGameUpdateHandler();
		}
		throw new IllegalArgumentException("Could not find handler for type: " + game);
	}
}
