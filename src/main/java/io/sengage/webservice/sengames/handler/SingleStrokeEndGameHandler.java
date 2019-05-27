package io.sengage.webservice.sengames.handler;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.EndGameResult;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.model.SingleStrokeEndGameResult;
import io.sengage.webservice.model.SingleStrokePlayer;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.model.Stroke;
import io.sengage.webservice.sengames.model.pubsub.EndGameMessage;
import io.sengage.webservice.twitch.TwitchClient;


@AllArgsConstructor
public class SingleStrokeEndGameHandler implements EndGameHandler {

	private final TwitchClient twitchClient;
	private final GameDataProvider gameDataProvider;
	private final PlayerDataProvider playerDataProvider;
	
	@Override
	public void handleEndGame(String gameId) {
		GameItem gameItem = gameDataProvider.getGame(gameId)
				.orElseThrow(() -> new RuntimeException("Could not find game with id: " + gameId));
		
		if (gameItem.getGameStatus().isOnOrAfter(GameStatus.COMPLETED)) {
			System.out.println("Game has a higher or equal ordinal to completed. Assuming already complete");
			return;
		}
		
		// mark game as completed
		gameItem.setGameStatus(GameStatus.COMPLETED);
		handleEndGame(gameItem);
	}

	@Override
	public void handleGameTimeout(String gameId) {
		GameItem gameItem = gameDataProvider.getGame(gameId)
				.orElseThrow(() -> new RuntimeException("Could not find game with id: " + gameId));
		
		if (gameItem.getGameStatus().isOnOrAfter(GameStatus.COMPLETED)) {
			System.out.println("Game has a higher or equal ordinal to completed. Assuming already complete");
			return;
		}
		
		// mark game as completed
		gameItem.setGameStatus(GameStatus.TIME_UP);
		handleEndGame(gameItem);
		
	}
	
	private void handleEndGame(GameItem gameItem) {
		if (gameItem.getGameStatus().isOnOrAfter(GameStatus.COMPLETED)) {
			System.out.println("Game has a higher or equal ordinal to completed. Assuming already complete");
			return;
		}
		

		
		
		try {
			// fetch results for game
			@SuppressWarnings("unchecked")
			List<SingleStrokePlayer> players = (List<SingleStrokePlayer>)
					playerDataProvider.listPlayers(gameItem.getGameId(), PlayerStatus.COMPLETED, SingleStrokePlayer.class);
			/* notify users of results
			List<EndGameResult> results = players.stream()
					.map(player -> new SingleStrokeEndGameResult(
							player.getOpaqueId(),
							player.getUserName(),
							Stroke.builder()
							.colorHex(player.getColorHex())
							.opaqueId(player.getOpaqueId())
							.pointA(player.getPointA())
							.pointB(player.getPointB())
							.strokeType(player.getStrokeType())
							.userName(player.getUserName())
							.build()
							))
					.collect(Collectors.toList());*/
			
			boolean success = twitchClient.notifyChannelGameEnded(gameItem.getChannelId(), 
					EndGameMessage.builder()
					.game(gameItem.getGame())
					.gameId(gameItem.getGameId())
					.gameStatus(gameItem.getGameStatus())
//					.results(results)
					.build());
			
			try {
				gameDataProvider.updateGame(gameItem);
			} catch (ItemVersionMismatchException e) {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			gameItem.setGameStatus(GameStatus.ERROR_STATE);
			try {
				gameDataProvider.updateGame(gameItem);	
			} catch(Exception e2) {
				e2.printStackTrace();
				System.out.println("SingleStrokeEndGameHandler(): Failed to persist moving game to error state");
				throw e;
			}
		}
	}
	
}
