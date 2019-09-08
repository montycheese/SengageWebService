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
import io.sengage.webservice.sengames.model.flappybird.ResurrectUserRequest;
import io.sengage.webservice.sengames.model.flappybird.SendFlightResultRequest;
import io.sengage.webservice.sengames.model.pubsub.ViewAction;
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
			player = playerDataProvider.getPlayer(gameId, streamContext.getOpaqueId(), FlappyBirdPlayer.class);
		} catch (ItemNotFoundException e) {
			throw new RuntimeException("Could not find player in game", e);
		}
		
		boolean isResurrectingPlayer = isResurrectingPlayer(state);
		if (isResurrectingPlayer) {
			if (PlayerStatus.PLAYING.equals(player.getPlayerStatus())) {
				// You cannot revive a player that is currently playing. Maybe the previous call to submit the last flight failed.
				// For now warn on the issue but allow the player to keep playing.
				log.warn("Received resurrect request for player {} in game {} but"
						+ " player is currently alive in data store. Assuming previous call to update state failed."
						, player.getOpaqueId(), gameId);
			}
			
			log.debug("Resurrecting player {} ", player.getOpaqueId());
			player.setPlayerStatus(PlayerStatus.PLAYING);
		} else {
			// TODO if we want to let people submit more than once in the same session remove this line of code.
			if (!player.getPlayerStatus().equals(PlayerStatus.PLAYING)) {
				throw new RuntimeException("Player already submitted a flight " + player.getOpaqueId());
			}
			SendFlightResultRequest request = (SendFlightResultRequest) state;

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
			
		}
		

		try {
			playerDataProvider.updateGamePlayer(player);
		} catch (ItemNotFoundException e) {
			throw new IllegalStateException("Could not find player in game to update: " + gameId, e);
		}
		
		notifyChannelOfActions(game, player, isResurrectingPlayer);

		return new FlappyBirdGameUpdateResponse();
	}
	
	protected void notifyChannelOfActions(GameItem game, Player player, boolean isResurrectingPlayer) {
		int playersRemaining = 
				playerDataProvider.getNumberOfPlayersInGame(game.getGameId(), PlayerStatus.PLAYING);
		
		// we don't want the game to end if a player joins and submits the flight before any other player has the chance
		// to join the game
		if (playersRemaining <= 0 && GameStatus.IN_PROGRESS.equals(game.getGameStatus()) && !isResurrectingPlayer) {
			// if last player send end game notification.
			log.debug("All players of game {} are finished, creating CWE event.", game.getGameId());
			notifyAllPlayersAreFinished(game);
		} else {
			ViewAction viewAction = isResurrectingPlayer ? ViewAction.REVIVE : ViewAction.KILL_FEED;
			PlayerCompleteRequest playerCompleteRequest = FlappyBirdPlayerCompleteRequest.builder()
					.gameItem(game)
					.player((FlappyBirdPlayer) player)
					.playersRemaining(playersRemaining)
					.channelId(game.getChannelId())
					.viewAction(viewAction)
					.build();
			twitchClient.notifyChannelPlayerComplete(playerCompleteRequest);
		}
	}
	
	private boolean isResurrectingPlayer(GameSpecificState state) {
		return ResurrectUserRequest.typeName.equals(state.getType());
	}

}
