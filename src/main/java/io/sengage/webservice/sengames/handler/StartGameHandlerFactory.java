package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.handler.singlestroke.SingleStrokeEndGameHandler;
import io.sengage.webservice.sengames.handler.singlestroke.SingleStrokeStartGameHandler;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;

import javax.inject.Inject;

public class StartGameHandlerFactory {
	private final GameDataProvider gameDataProvider;
	private final PlayerDataProvider playerDataProvider;
	private final TwitchClient twitchClient;
	private final StepFunctionTaskExecutor sfExecutor;
	private final EndGameHandlerFactory endGameHandlerFactory;
	
	@Inject
	public StartGameHandlerFactory(GameDataProvider gameDataProvider, PlayerDataProvider playerDataProvider, 
			TwitchClient twitchClient, StepFunctionTaskExecutor sfExecutor, EndGameHandlerFactory endGameHandlerFactory) {
		this.gameDataProvider = gameDataProvider;
		this.twitchClient = twitchClient;
		this.sfExecutor = sfExecutor;
		this.playerDataProvider = playerDataProvider;
		this.endGameHandlerFactory = endGameHandlerFactory;
	}
	
	
	public StartGameHandler get(Game game) {
		switch (game) {
		case FLAPPY_BIRD_BR:
			return StartGameHandler.builder()
					.gameDataProvider(gameDataProvider)
					.playerDataProvider(playerDataProvider)
					.sfExecutor(sfExecutor)
					.twitchClient(twitchClient)
					.build();
		case SINGLE_STROKE:
			SingleStrokeEndGameHandler handler = (SingleStrokeEndGameHandler) endGameHandlerFactory.get(game);
			return new SingleStrokeStartGameHandler(gameDataProvider, playerDataProvider, twitchClient, sfExecutor, handler);
		default:
			throw new IllegalArgumentException("Unsupported game: " + game);
		}
	}
}
