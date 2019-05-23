package io.sengage.webservice.sengames.handler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.sengage.webservice.exception.GameCompletedException;
import io.sengage.webservice.exception.ItemNotFoundException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameSpecificState;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.model.SingleStrokePlayer;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.model.HandleGameUpdateResponse;
import io.sengage.webservice.sengames.model.SendLineRequest;
import io.sengage.webservice.sengames.model.SingleStrokeGameUpdateResponse;
import io.sengage.webservice.sengames.model.Stroke;

public class SingleStrokeGameUpdateHandler extends GameUpdateHandler {

	private final PlayerDataProvider playerDataProvider;
	private final GameDataProvider gameDataProvider;
	
	public SingleStrokeGameUpdateHandler(PlayerDataProvider playerDataProvider, GameDataProvider gameDataProvider) {
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
		
		return SingleStrokeGameUpdateResponse.builder()
				.strokes(playerDatum.stream()
						.map(data -> Stroke.builder()
								.colorHex(data.getColorHex())
								.pointA(data.getPointA())
								.pointB(data.getPointB())
								.strokeType(data.getStrokeType())
								.width(data.getWidth())
								.build())
						.collect(Collectors.toList()))
				.build();
	}

}
