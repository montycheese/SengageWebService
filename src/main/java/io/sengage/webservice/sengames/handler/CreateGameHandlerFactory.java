package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.twitch.TwitchClient;

import javax.inject.Inject;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsync;
import com.google.gson.Gson;

public class CreateGameHandlerFactory {
	
	private final GameDataProvider gameDataProvider;
	private final Gson gson;
	private final TwitchClient twitchClient;
	private final AmazonCloudWatchEventsAsync cwe;
	
	@Inject
	public CreateGameHandlerFactory(GameDataProvider gameDataProvider,
			Gson gson,
			TwitchClient twitchClient,
			AmazonCloudWatchEventsAsync cwe) {
		this.gameDataProvider = gameDataProvider;
		this.gson = gson;
		this.twitchClient = twitchClient;
		this.cwe = cwe;
	}
	
	public CreateGameHandler get(Game game){
		switch (game) {
			case SINGLE_STROKE:
				return new SingleStrokeCreateGameHandler(gameDataProvider,
						twitchClient,
						cwe,
						gson
						);
			case FLAPPY_BIRD_BR:
				// TODO
			default:
				throw new IllegalArgumentException("Could not find handler for: " + game.name());
		}
	}
	

}
