package io.sengage.webservice.sengames.handler;

import javax.inject.Inject;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsync;
import com.google.gson.Gson;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;

public class GameUpdateHandlerFactory {
	
	private final PlayerDataProvider playerDataProvider;
	private final GameDataProvider gameDataProvider;
	private final AmazonCloudWatchEventsAsync cwe;
	private final Gson gson;
	
	@Inject
	public GameUpdateHandlerFactory(PlayerDataProvider playerDataProvider, 
			GameDataProvider gameDataProvider,
			AmazonCloudWatchEventsAsync cwe,
			Gson gson) {
		this.playerDataProvider = playerDataProvider;
		this.gameDataProvider = gameDataProvider;
		this.cwe = cwe;
		this.gson = gson;
	}
	
	public GameUpdateHandler get(Game game) {
		switch (game) {
		case SINGLE_STROKE:
			return new SingleStrokeGameUpdateHandler(playerDataProvider, gameDataProvider, cwe, gson);
		case FLAPPY_BIRD_BR:
		default:
			throw new IllegalArgumentException("Could not find handler for type: " + game);
		}
	}
}
