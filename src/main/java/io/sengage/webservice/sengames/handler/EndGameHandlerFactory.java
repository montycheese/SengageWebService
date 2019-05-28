package io.sengage.webservice.sengames.handler;

import javax.inject.Inject;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;

public class EndGameHandlerFactory {
	
	private final TwitchClient twitchClient;
	private final GameDataProvider gameDataProvider;
	private final PlayerDataProvider playerDataProvider;
	private final StepFunctionTaskExecutor sfExecutor;

	@Inject
	public EndGameHandlerFactory(TwitchClient twitchClient, 
			GameDataProvider gameDataProvider,
			PlayerDataProvider playerDataProvider,
			StepFunctionTaskExecutor sfExecutor) {
		this.twitchClient = twitchClient;
		this.gameDataProvider = gameDataProvider;
		this.playerDataProvider = playerDataProvider;
		this.sfExecutor = sfExecutor;
		
	}
	
	public EndGameHandler get(Game game) {
		
		switch (game) {
		case SINGLE_STROKE:
			return new SingleStrokeEndGameHandler(twitchClient, gameDataProvider, playerDataProvider, sfExecutor);
		case FLAPPY_BIRD_BR:
		default: 
			throw new IllegalArgumentException("Game: " + game.name() + " not supported");
		}
	}
}
