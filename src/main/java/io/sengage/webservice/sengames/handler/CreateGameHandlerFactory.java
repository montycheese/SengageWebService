package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.model.Game;

import javax.inject.Inject;

public class CreateGameHandlerFactory {
	
	@Inject
	public CreateGameHandlerFactory() {
		
	}
	
	public CreateGameHandler get(Game game){
		switch (game) {
			case SINGLE_STROKE:
				return new SingleStrokeCreateGameHandler();
			case FLAPPY_BIRD_BR:
				// TODO
			default:
				throw new IllegalArgumentException("Could not find handler for: " + game.name());
		}
	}
	

}
