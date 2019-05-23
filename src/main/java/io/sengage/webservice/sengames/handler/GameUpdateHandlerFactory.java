package io.sengage.webservice.sengames.handler;

import javax.inject.Inject;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;

public class GameUpdateHandlerFactory {
	
	private final PlayerDataProvider playerDataProvider;
	private final GameDataProvider gameDataProvider;
	
	@Inject
	public GameUpdateHandlerFactory(PlayerDataProvider playerDataProvider, GameDataProvider gameDataProvider) {
		this.playerDataProvider = playerDataProvider;
		this.gameDataProvider = gameDataProvider;
	}
	
	public GameUpdateHandler get(Game game) {
		switch (game) {
		case SINGLE_STROKE:
			return new SingleStrokeGameUpdateHandler(playerDataProvider, gameDataProvider);
		}
		throw new IllegalArgumentException("Could not find handler for type: " + game);
	}
}
