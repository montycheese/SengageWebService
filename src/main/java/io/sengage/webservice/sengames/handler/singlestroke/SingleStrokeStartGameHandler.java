package io.sengage.webservice.sengames.handler.singlestroke;

import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.handler.StartGameHandler;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.NotifyGameStartedRequest;
import io.sengage.webservice.twitch.TwitchClient;

@Log4j2
public class SingleStrokeStartGameHandler extends StartGameHandler {
	
	private final SingleStrokeEndGameHandler endGameHandler;
	
	public SingleStrokeStartGameHandler(GameDataProvider gameDataProvider,
			PlayerDataProvider playerDataProvider, TwitchClient twitchClient,
			StepFunctionTaskExecutor sfExecutor, SingleStrokeEndGameHandler endGameHandler) {
		super(gameDataProvider, playerDataProvider, twitchClient, sfExecutor);
		this.endGameHandler = endGameHandler;
	}
	
	@Override
	public void startGame(String gameId) {
		Optional<GameItem> optionalGame = gameDataProvider.getGame(gameId);
		
		if (!optionalGame.isPresent()) {
			throw new IllegalStateException(
					String.format("Could not start game with id [%s] because it was not found in the persistence", gameId)
				);
		}
		GameItem game = optionalGame.get();
		
		if (game.getGameStatus().isOnOrAfter(GameStatus.IN_PROGRESS)) {
			log.warn(String.format("Game status is already: %s, skipping update", GameStatus.IN_PROGRESS.name()));
			return;
		}
		
		int numPlayersJoined = playerDataProvider.getNumberOfPlayersInGame(game.getGameId(), PlayerStatus.PLAYING);
		
		if (numPlayersJoined == 0) {
			// For SS, players can start sending requests during the Join Game Phase. 
			// If the Game starts and all the players who requested to join the game are already completed, 
			// Then just end the game early so that the final results are shown and people aren't stuck waiting.
			int numPlayersCompleted = playerDataProvider.getNumberOfPlayersInGame(game.getGameId(), PlayerStatus.COMPLETED);
			if (numPlayersCompleted > 0) {
				game.setGameStatus(GameStatus.COMPLETED);
				game.setStatusReason("Game completed early during the join game phase.");
				endGameHandler.handleEndGame(game);
			} else {
				cancelGame(game);
			}
			return;
		} else {
			game.setGameStatus(GameStatus.IN_PROGRESS);
			
			String gameTimeUpStateMachineArn = sfExecutor.executeGameTimeUpStateMachine(game);
			game.setGameTimeUpStateMachineExecutionArn(gameTimeUpStateMachineArn);
			
			try {
				gameDataProvider.updateGame(game);
			} catch (ItemVersionMismatchException e) {
				log.error("Encountered exception while updating game state");
				return;
			}
			
			twitchClient.notifyChannelGameStarted(NotifyGameStartedRequest.builder()
					.gameItem(game)
					.totalPlayers(numPlayersJoined)
					.build());
		}
	}

}
