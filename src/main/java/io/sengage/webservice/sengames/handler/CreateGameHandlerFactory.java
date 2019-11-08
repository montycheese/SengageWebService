package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.cache.GameCache;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CreateGameHandlerFactory {
	
	private final GameDataProvider gameDataProvider;
	private final TwitchClient twitchClient;
	private final StepFunctionTaskExecutor sfExecutor;
	private final GameCache gameCache;
	
	@Inject
	public CreateGameHandlerFactory(GameDataProvider gameDataProvider,
			TwitchClient twitchClient,
			StepFunctionTaskExecutor sfExecutor,
			GameCache gameCache) {
		this.gameDataProvider = gameDataProvider;
		this.twitchClient = twitchClient;
		this.sfExecutor = sfExecutor;
		this.gameCache = gameCache;
	}
	
	public CreateGameHandler get(Game game){
		switch (game) {
			case SINGLE_STROKE:
				// fall through
			case FLAPPY_BIRD_BR:
				return new CreateGameHandlerImpl(gameDataProvider,
						twitchClient,
						sfExecutor,
						gameCache
				);
			default:
				throw new IllegalArgumentException("Could not find handler for: " + game.name());
		}
	}
	

}
