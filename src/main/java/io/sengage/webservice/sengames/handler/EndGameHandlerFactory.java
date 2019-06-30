package io.sengage.webservice.sengames.handler;

import javax.inject.Inject;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.handler.flappybird.FlappyBirdEndGameHandler;
import io.sengage.webservice.sengames.handler.singlestroke.SingleStrokeEndGameHandler;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;

public class EndGameHandlerFactory {
	
	private final TwitchClient twitchClient;
	private final GameDataProvider gameDataProvider;
	private final StepFunctionTaskExecutor sfExecutor;
	private final PlayerDataProvider playerDataProvider;

	@Inject
	public EndGameHandlerFactory(TwitchClient twitchClient, 
			GameDataProvider gameDataProvider,
			PlayerDataProvider playerDataProvider,
			StepFunctionTaskExecutor sfExecutor) {
		this.twitchClient = twitchClient;
		this.gameDataProvider = gameDataProvider;
		this.sfExecutor = sfExecutor;
		this.playerDataProvider = playerDataProvider;
		
	}
	
	public EndGameHandler get(Game game) {
		
		switch (game) {
		case SINGLE_STROKE:
			return new SingleStrokeEndGameHandler(gameDataProvider, twitchClient, sfExecutor);
		case FLAPPY_BIRD_BR:
			return new FlappyBirdEndGameHandler(gameDataProvider, playerDataProvider, twitchClient, sfExecutor);
		default: 
			throw new IllegalArgumentException("Game: " + game.name() + " not supported");
		}
	}
}
