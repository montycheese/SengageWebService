package io.sengage.webservice.sengames.handler;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsync;
import com.google.gson.Gson;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.handler.flappybird.FlappyBirdGameUpdateHandler;
import io.sengage.webservice.sengames.handler.singlestroke.SingleStrokeGameUpdateHandler;
import io.sengage.webservice.twitch.TwitchClient;

@Singleton
public class GameUpdateHandlerFactory {
	
	private final PlayerDataProvider playerDataProvider;
	private final GameDataProvider gameDataProvider;
	private final AmazonCloudWatchEventsAsync cwe;
	private final Gson gson;
	private final TwitchClient twitchClient;
	
	@Inject
	public GameUpdateHandlerFactory(PlayerDataProvider playerDataProvider, 
			GameDataProvider gameDataProvider,
			AmazonCloudWatchEventsAsync cwe,
			Gson gson,
			TwitchClient twitchClient) {
		this.playerDataProvider = playerDataProvider;
		this.gameDataProvider = gameDataProvider;
		this.cwe = cwe;
		this.gson = gson;
		this.twitchClient = twitchClient;
	}
	
	public GameUpdateHandler get(Game game) {
		switch (game) {
		case SINGLE_STROKE:
			return new SingleStrokeGameUpdateHandler(playerDataProvider, gameDataProvider, cwe, gson);
		case FLAPPY_BIRD_BR:
			return new FlappyBirdGameUpdateHandler(playerDataProvider, gameDataProvider, twitchClient, cwe, gson);
		default:
			throw new IllegalArgumentException("Could not find handler for type: " + game);
		}
	}
}
