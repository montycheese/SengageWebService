package io.sengage.webservice.sengames.handler;

import java.util.Optional;

import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameCancellationReason;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.NotifyGameStartedRequest;
import io.sengage.webservice.twitch.TwitchClient;

@Log4j2
@Builder
public class StartGameHandler {

	protected final GameDataProvider gameDataProvider;
	protected final PlayerDataProvider playerDataProvider;
	protected final TwitchClient twitchClient;
	protected final StepFunctionTaskExecutor sfExecutor;
	
	public StartGameHandler(GameDataProvider gameDataProvider, PlayerDataProvider playerDataProvider, 
			TwitchClient twitchClient, StepFunctionTaskExecutor sfExecutor) {
		this.gameDataProvider = gameDataProvider;
		this.twitchClient = twitchClient;
		this.sfExecutor = sfExecutor;
		this.playerDataProvider = playerDataProvider;
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
			log.warn(String.format("Game status is already: %s, skipping update", GameStatus.IN_PROGRESS.name()));
			return;
		}
		
		int numPlayersJoined = playerDataProvider.getNumberOfPlayersInGame(game.getGameId(), PlayerStatus.PLAYING);
		
		if (numPlayersJoined == 0) {
			cancelGame(game);
		} else {
			game.setGameStatus(GameStatus.IN_PROGRESS);
			
			String gameTimeUpStateMachineArn = sfExecutor.executeGameTimeUpStateMachine(game);
			game.setGameTimeUpStateMachineExecutionArn(gameTimeUpStateMachineArn);
			
			try {
				gameDataProvider.updateGame(game);
			} catch (ItemVersionMismatchException e) {
				log.error("Encountered exception while trying to update game state", e);
				return;
			}
			
			twitchClient.notifyChannelGameStarted(NotifyGameStartedRequest.builder()
					.gameItem(game)
					.totalPlayers(numPlayersJoined)
					.build());
		}
	}
	
	protected void cancelGame(GameItem game) {
		game.setGameStatus(GameStatus.CANCELLED);
		game.setStatusReason(GameCancellationReason.NO_PLAYERS.name());
		try {
			gameDataProvider.updateGame(game);
		} catch (ItemVersionMismatchException e) {
			log.error("Encountered exception while trying to cancel game", e);
			return;
		}
		twitchClient.notifyChannelGameCancelled(game, GameCancellationReason.NO_PLAYERS);
		
		sfExecutor.cleanUpGameStateMachineResources(game);
	}
}
