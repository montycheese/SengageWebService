package io.sengage.webservice.sengames.handler.singlestroke;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import io.sengage.webservice.model.singlestroke.SingleStrokePlayer;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.handler.GameUpdateHandler;
import io.sengage.webservice.sengames.model.HandleGameUpdateResponse;
import io.sengage.webservice.sengames.model.singlestroke.SendLineRequest;
import io.sengage.webservice.sengames.model.singlestroke.SingleStrokeGameUpdateResponse;
import io.sengage.webservice.sengames.model.singlestroke.Stroke;

public class SingleStrokeGameUpdateHandler extends GameUpdateHandler {

	private final PlayerDataProvider playerDataProvider;
	private final GameDataProvider gameDataProvider;
	
	public SingleStrokeGameUpdateHandler(PlayerDataProvider playerDataProvider, 
			GameDataProvider gameDataProvider,
			AmazonCloudWatchEventsAsync cwe, 
			Gson gson) {
		super(cwe, gson);
		this.playerDataProvider = playerDataProvider;
		this.gameDataProvider = gameDataProvider;
	}
	
	@Override
	public HandleGameUpdateResponse handleGameUpdate(String gameId, GameSpecificState state, StreamContext streamContext) 
			throws GameCompletedException {
		SendLineRequest request = (SendLineRequest) state;
		// make sure game is still accepting requests
		Optional<GameItem> optionalGame = gameDataProvider.getGame(gameId);
		GameItem game = optionalGame.orElseThrow(() -> new RuntimeException("Could not find game with id: " + gameId));
		
		if (game.getGameStatus().isOnOrAfter(GameStatus.COMPLETED)) {
			throw new GameCompletedException(String.format("Game %s already completed. Could not accept request.", gameId));
		} else if (game.getGameStatus().isBefore(GameStatus.WAITING_FOR_PLAYERS)) {
			// allow players to submit in the waiting for player phase in single stroke.
			throw new IllegalStateException("Cannot submit game input before game is started");
		}
		
		Player player;
		try {
			player = playerDataProvider.getPlayer(gameId, streamContext.getOpaqueId());
		} catch (ItemNotFoundException e) {
			throw new RuntimeException("Could not find player in game", e);
		}
		
		if (!player.getPlayerStatus().equals(PlayerStatus.PLAYING)) {
			throw new RuntimeException("Player already submitted a stroke " + player.getOpaqueId());
		}
		
		player = new SingleStrokePlayer(
				player.getGameId(),
				player.getOpaqueId(),
				player.getUserId(),
				player.getUserName(),
				player.getJoinedAt(),
				player.getModifiedAt(),
				PlayerStatus.COMPLETED,
				request.getStroke().getStrokeType(),
				request.getStroke().getPointA(),
				request.getStroke().getPointB(),
				request.getStroke().getWidth(),
				request.getStroke().getColorHex()
				);
		
		try {
			playerDataProvider.updateGamePlayer(player);
		} catch (ItemNotFoundException e) {
			throw new RuntimeException("Could not find player in game to update: " + gameId, e);
		}
		
		// send existing strokes to player to display.
		@SuppressWarnings("unchecked")
		List<SingleStrokePlayer> playerDatum = (List<SingleStrokePlayer>)
				playerDataProvider.listPlayers(gameId, PlayerStatus.COMPLETED, SingleStrokePlayer.class);
		
		// if last player send end game notification.
		int playersRemaining = 
				playerDataProvider.getNumberOfPlayersInGame(gameId, PlayerStatus.PLAYING);
		
		// we don't want the game to end if a player joins and submits a stroke before any other player has the chance
		// to join the game
		if (playersRemaining <= 0 && GameStatus.IN_PROGRESS.equals(game.getGameStatus())) {
			System.out.println("All players are finished, creating CWE event.");
			notifyAllPlayersAreFinished(game);
		}
		
		return SingleStrokeGameUpdateResponse.builder()
				.strokes(playerDatum.stream()
						.map(SingleStrokeGameUpdateHandler::fromPlayerData)
						.collect(Collectors.toList()))
				.build();
	}
	
	private static Stroke fromPlayerData(SingleStrokePlayer player) {
		return Stroke.builder()
				.colorHex(player.getColorHex())
				.pointA(player.getPointA())
				.pointB(player.getPointB())
				.strokeType(player.getStrokeType())
				.width(player.getWidth())
				.userName(player.getUserName())
				.opaqueId(player.getOpaqueId())
				.build();
	}

}
