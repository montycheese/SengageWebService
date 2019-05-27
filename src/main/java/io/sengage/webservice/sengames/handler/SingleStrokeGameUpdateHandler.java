package io.sengage.webservice.sengames.handler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsync;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequest;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequestEntry;
import com.amazonaws.services.cloudwatchevents.model.PutEventsResult;
import com.amazonaws.services.cloudwatchevents.model.PutEventsResultEntry;
import com.google.gson.Gson;

import io.sengage.webservice.events.EventDetail;
import io.sengage.webservice.exception.GameCompletedException;
import io.sengage.webservice.exception.ItemNotFoundException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameSpecificState;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.model.SingleStrokePlayer;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.model.GameItem.GameItemDigest;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.model.HandleGameUpdateResponse;
import io.sengage.webservice.sengames.model.SendLineRequest;
import io.sengage.webservice.sengames.model.SingleStrokeGameUpdateResponse;
import io.sengage.webservice.sengames.model.Stroke;
import io.sengage.webservice.utils.Constants;

public class SingleStrokeGameUpdateHandler extends GameUpdateHandler {

	private final PlayerDataProvider playerDataProvider;
	private final GameDataProvider gameDataProvider;
	private final AmazonCloudWatchEventsAsync cwe;
	private final Gson gson;
	
	public SingleStrokeGameUpdateHandler(PlayerDataProvider playerDataProvider, 
			GameDataProvider gameDataProvider,
			AmazonCloudWatchEventsAsync cwe, 
			Gson gson) {
		this.playerDataProvider = playerDataProvider;
		this.gameDataProvider = gameDataProvider;
		this.cwe = cwe;
		this.gson = gson;
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
		
		if (playersRemaining <= 0) {
			notifyAllPlayersAreFinished(game);
		}
		
		return SingleStrokeGameUpdateResponse.builder()
				.strokes(playerDatum.stream()
						.map(SingleStrokeGameUpdateHandler::fromPlayerData)
						.collect(Collectors.toList()))
				.build();
	}
	
	private void notifyAllPlayersAreFinished(GameItem gameItem) {
		PutEventsRequest eventsRequest = new PutEventsRequest()
		.withEntries(new PutEventsRequestEntry()
			.withDetail(gson.toJson(gameItem.toDigest(), GameItemDigest.class))
			.withSource(Constants.CWE_EVENT_SOURCE)
			.withDetailType(EventDetail.ALL_PLAYERS_FINISHED.name())
		);
		PutEventsResult response = cwe.putEvents(eventsRequest);
		
		if (response.getFailedEntryCount() > 0) {
			System.out.println("Error creating event to start game: " + gameItem.getGameId() + " failure count: "  + response.getFailedEntryCount());
			for (PutEventsResultEntry entry: response.getEntries()) {
				System.out.println("Error: " + entry.getErrorMessage());
			}
			// It's ok to swallow this, we'll just let the game's time expire.
		}
		
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
