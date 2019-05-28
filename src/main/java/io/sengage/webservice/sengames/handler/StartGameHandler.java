package io.sengage.webservice.sengames.handler;

import java.util.Optional;

import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;

import javax.inject.Inject;

public class StartGameHandler {

	private final GameDataProvider gameDataProvider;
	private final TwitchClient twitchClient;
	private final StepFunctionTaskExecutor sfExecutor;
	
	@Inject
	public StartGameHandler(GameDataProvider gameDataProvider, TwitchClient twitchClient, StepFunctionTaskExecutor sfExecutor) {
		this.gameDataProvider = gameDataProvider;
		this.twitchClient = twitchClient;
		this.sfExecutor = sfExecutor;
	}
	
	public void startGame(String gameId) {
		Optional<GameItem> optionalGame = gameDataProvider.getGame(gameId);
		
		if (!optionalGame.isPresent()) {
			throw new IllegalStateException(
					String.format("Could not start game with id [%s] because it was not found in the persistence", gameId)
				);
		}
		GameItem game = optionalGame.get();
		
		if (game.getGameStatus().isOnOrAfter(GameStatus.IN_PROGRESS)) {
			System.out.println(String.format("Game status is already: %s, skipping update", GameStatus.IN_PROGRESS.name()));
			return;
		}
		
		game.setGameStatus(GameStatus.IN_PROGRESS);
		
		String gameTimeUpStateMachineArn = sfExecutor.executeGameTimeUpStateMachine(game);
		game.setGameTimeUpStateMachineExecutionArn(gameTimeUpStateMachineArn);
		
		try {
			gameDataProvider.updateGame(game);
		} catch (ItemVersionMismatchException e) {
			e.printStackTrace();
			return;
		}
		
		twitchClient.notifyChannelGameStarted(game);
	}
}
