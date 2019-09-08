package io.sengage.webservice.sengames.handler;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.log4j.Log4j2;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsync;
import com.google.gson.Gson;

import io.sengage.webservice.exception.GameCompletedException;
import io.sengage.webservice.exception.ItemNotFoundException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameSpecificState;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.model.HandleGameUpdateResponse;
import io.sengage.webservice.twitch.TwitchClient;

@Singleton
@Log4j2
public class QuitGameHandler extends GameUpdateHandler {

	private final PlayerDataProvider playerDataProvider;
	private final GameDataProvider gameDataProvider;
	
	@Inject
	public QuitGameHandler(PlayerDataProvider playerDataProvider,
			GameDataProvider gameDataProvider, TwitchClient twitchClient,
			AmazonCloudWatchEventsAsync cwe, Gson gson) {
		super(cwe, gson);
		this.playerDataProvider = playerDataProvider;
		this.gameDataProvider = gameDataProvider;
	}
	
	@Override
	public HandleGameUpdateResponse handleGameUpdate(String gameId,
			GameSpecificState state, StreamContext streamContext)
			throws GameCompletedException {
		
		// make sure game is still accepting requests
		Optional<GameItem> optionalGame = gameDataProvider.getGame(gameId);
		GameItem game = optionalGame.orElseThrow(() -> new RuntimeException("Could not find game with id: " + gameId));
		
		if (game.getGameStatus().isOnOrAfter(GameStatus.COMPLETED)) {
			throw new GameCompletedException(String.format("Game %s already completed. Could not accept request.", gameId));
		} else if (game.getGameStatus().isBefore(GameStatus.WAITING_FOR_PLAYERS)) {
			throw new IllegalStateException("Cannot cancel a game that has not started queueing up");
		}
		
		Player player;
		try {
			player = playerDataProvider.getPlayer(gameId, streamContext.getOpaqueId());
			
			if (!PlayerStatus.PLAYING.equals(player.getPlayerStatus())
					&& !PlayerStatus.WAITING_TO_PLAY.equals(player.getPlayerStatus())) {
				log.warn("Player is in {} status and does not need to quit game. This is a no-op", player.getPlayerStatus());
				return null;
			}
			
			player.setPlayerStatus(PlayerStatus.QUIT);
			playerDataProvider.updateGamePlayer(player);
		} catch (ItemNotFoundException e) {
			throw new IllegalStateException("Could not find player in game", e);
		}
		
		if (GameStatus.IN_PROGRESS.equals(game.getGameStatus())) {
			int playersRemaining = 
					playerDataProvider.getNumberOfPlayersInGame(gameId, PlayerStatus.PLAYING);
			
			if (playersRemaining <= 0 && GameStatus.IN_PROGRESS.equals(game.getGameStatus())) {
				log.info("All players are finished in game {}, creating CWE event.", gameId);
				notifyAllPlayersAreFinished(game);
			}
		}

		
		return null;
	}
	
}
