package io.sengage.webservice.sengames.handler;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.log4j.Log4j2;
import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameCancellationReason;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;

@Log4j2
@Singleton
public class CancelGameHandler {
	
	private final GameDataProvider gameDataProvider;
	private final TwitchClient twitchClient;
	private final StepFunctionTaskExecutor sfExecutor;
	
	@Inject
	public CancelGameHandler(GameDataProvider gameDataProvider,
			TwitchClient twitchClient,
			StepFunctionTaskExecutor sfExecutor) {
		this.gameDataProvider = gameDataProvider;
		this.twitchClient = twitchClient;
		this.sfExecutor = sfExecutor;
	}
	
	public void handleCancelGame(String gameId, String cancellationReason) {
			Optional<GameItem> optionalGame = gameDataProvider.getGame(gameId);
			
			if (!optionalGame.isPresent()) {
				log.warn("Game {} not present.", gameId);
				return;
			}
			
			GameItem game = optionalGame.get();

			try {
				// Do not allow cancelling games that are already completed, timed up, cancelled, errored out
				if (game.getGameStatus().isOnOrAfter(GameStatus.COMPLETED)) {
					log.info("Game {} has a ordinal {} that is equal to or greater than Completed", gameId, game.getGameStatus());
					return;
				}

				game.setGameStatus(GameStatus.CANCELLED);
				game.setStatusReason(cancellationReason);
				
				gameDataProvider.updateGame(game);
				twitchClient.notifyChannelGameCancelled(game, GameCancellationReason.BROADCASTER_CANCELLED);
			} catch (ItemVersionMismatchException e) {
				log.warn("Failed to update game {} to cancelled", gameId, e);
				throw new RuntimeException("Failed to update game to cancelled", e);
			} finally {
				sfExecutor.cleanUpGameStateMachineResources(game);
			}
		
	}
}
