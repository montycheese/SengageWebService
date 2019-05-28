package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;

import javax.inject.Inject;

import com.google.gson.Gson;

public class CreateGameHandlerFactory {
	
	private final GameDataProvider gameDataProvider;
	private final Gson gson;
	private final TwitchClient twitchClient;
	private final StepFunctionTaskExecutor sfExecutor;
	
	@Inject
	public CreateGameHandlerFactory(GameDataProvider gameDataProvider,
			Gson gson,
			TwitchClient twitchClient,
			StepFunctionTaskExecutor sfExecutor) {
		this.gameDataProvider = gameDataProvider;
		this.gson = gson;
		this.twitchClient = twitchClient;
		this.sfExecutor = sfExecutor;
	}
	
	public CreateGameHandler get(Game game){
		switch (game) {
			case SINGLE_STROKE:
				return new SingleStrokeCreateGameHandler(gameDataProvider,
						twitchClient,
						gson,
						sfExecutor
						);
			case FLAPPY_BIRD_BR:
				// TODO
			default:
				throw new IllegalArgumentException("Could not find handler for: " + game.name());
		}
	}
	

}
