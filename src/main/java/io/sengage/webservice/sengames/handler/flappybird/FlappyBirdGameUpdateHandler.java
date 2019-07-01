package io.sengage.webservice.sengames.handler.flappybird;

import java.util.Optional;

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
import io.sengage.webservice.model.flappybird.FlappyBirdPlayer;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.handler.GameUpdateHandler;
import io.sengage.webservice.sengames.model.HandleGameUpdateResponse;
import io.sengage.webservice.sengames.model.flappybird.FlappyBirdGameUpdateResponse;
import io.sengage.webservice.sengames.model.flappybird.SendFlightResultRequest;
import io.sengage.webservice.twitch.FlappyBirdPlayerCompleteRequest;
import io.sengage.webservice.twitch.PlayerCompleteRequest;
import io.sengage.webservice.twitch.TwitchClient;

@Log4j2
public class FlappyBirdGameUpdateHandler extends GameUpdateHandler {

	private final PlayerDataProvider playerDataProvider;
	private final GameDataProvider gameDataProvider;
	private final TwitchClient twitchClient;
	
	public FlappyBirdGameUpdateHandler(PlayerDataProvider playerDataProvider, 
			GameDataProvider gameDataProvider,
			TwitchClient twitchClient,
			AmazonCloudWatchEventsAsync cwe, 
			Gson gson) {
		super(cwe, gson);
		this.playerDataProvider = playerDataProvider;
		this.gameDataProvider = gameDataProvider;
		this.twitchClient = twitchClient;
	}
	
	@Override
	public HandleGameUpdateResponse handleGameUpdate(String gameId,
			GameSpecificState state, StreamContext streamContext)
			throws GameCompletedException {
		SendFlightResultRequest request = (SendFlightResultRequest) state;
		// make sure game is still accepting requests
		Optional<GameItem> optionalGame = gameDataProvider.getGame(gameId);
		GameItem game = optionalGame.orElseThrow(() -> new RuntimeException("Could not find game with id: " + gameId));
		
		if (game.getGameStatus().isOnOrAfter(GameStatus.COMPLETED)) {
			throw new GameCompletedException(String.format("Game %s already completed. Could not accept request.", gameId));
		} else if (game.getGameStatus().isBefore(GameStatus.IN_PROGRESS)) {
			throw new IllegalStateException("Cannot submit game input before game is started");
		}
		
		Player player;
		try {
			player = playerDataProvider.getPlayer(gameId, streamContext.getOpaqueId());
		} catch (ItemNotFoundException e) {
			throw new RuntimeException("Could not find player in game", e);
		}
		
		// TODO since we want to let people submit more than once maybe remove this line of code.
		if (!player.getPlayerStatus().equals(PlayerStatus.PLAYING)) {
			throw new RuntimeException("Player already submitted a flight " + player.getOpaqueId());
		}
		// TODO handle case where player submit more than 1 attempt and save highest scoring attempt
		player = new FlappyBirdPlayer(
				player.getGameId(),
				player.getOpaqueId(),
				player.getUserId(),
				player.getUserName(),
				player.getJoinedAt(),
				player.getModifiedAt(),
				PlayerStatus.COMPLETED,
				request.getFlightResult().getCharacter(),
				request.getFlightResult().getDistance(),
				request.getFlightResult().getAttempt()
		);
		
		try {
			playerDataProvider.updateGamePlayer(player);
		} catch (ItemNotFoundException e) {
			throw new RuntimeException("Could not find player in game to update: " + gameId, e);
		}
		
		// if last player send end game notification.
		int playersRemaining = 
				playerDataProvider.getNumberOfPlayersInGame(gameId, PlayerStatus.PLAYING);
		
		// we don't want the game to end if a player joins and submits the flight before any other player has the chance
		// to join the game
		if (playersRemaining <= 0 && GameStatus.IN_PROGRESS.equals(game.getGameStatus())) {
			log.debug("All players are finished, creating CWE event.");
			notifyAllPlayersAreFinished(game);
		} else {
			PlayerCompleteRequest playerCompleteRequest = FlappyBirdPlayerCompleteRequest.builder()
					.gameItem(game)
					.player((FlappyBirdPlayer) player)
					.playersRemaining(playersRemaining)
					.channelId(game.getChannelId())
					.build();					
			twitchClient.notifyChannelPlayerComplete(playerCompleteRequest);
		}
		return new FlappyBirdGameUpdateResponse();
	}

}
