package io.sengage.webservice.sengames.handler.singlestroke;


import lombok.AllArgsConstructor;
import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.handler.EndGameHandler;
import io.sengage.webservice.sengames.model.pubsub.EndGameMessage;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;


@AllArgsConstructor
public class SingleStrokeEndGameHandler implements EndGameHandler {

	private final TwitchClient twitchClient;
	private final GameDataProvider gameDataProvider;
	private final PlayerDataProvider playerDataProvider;
	private final StepFunctionTaskExecutor sfExecutor;
	
	@Override
	public void handleEndGame(String gameId) {
		GameItem gameItem = gameDataProvider.getGame(gameId)
				.orElseThrow(() -> new RuntimeException("Could not find game with id: " + gameId));
		
		if (gameItem.getGameStatus().isOnOrAfter(GameStatus.COMPLETED)) {
			System.out.println(String.format("Game has a status of %s which is higher or equal ordinal to completed."
					+ " Assuming game already complete", gameItem.getGameStatus()));
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
			System.out.println(String.format("Game has a status of %s which is higher or equal ordinal to completed."
					+ " Assuming game already complete", gameItem.getGameStatus()));
			return;
		}
		
		// mark game as completed
		gameItem.setGameStatus(GameStatus.TIME_UP);
		handleEndGame(gameItem);
		
	}
	
	private void handleEndGame(GameItem gameItem) {		
		try {		
			boolean success = twitchClient.notifyChannelGameEnded(gameItem.getChannelId(), 
					EndGameMessage.builder()
					.game(gameItem.getGame())
					.gameId(gameItem.getGameId())
					.gameStatus(gameItem.getGameStatus())
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
		} finally {
			sfExecutor.cleanUpGameStateMachineResources(gameItem);
		}
	}
	
}
