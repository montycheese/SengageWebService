package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.persistence.GameDataProvider;

public abstract class EndGameHandler {

	protected final GameDataProvider gameDataProvider;
	
	protected abstract void handleEndGame(GameItem gameItem);
	
	
	public EndGameHandler(GameDataProvider gameDataProvider) {
		this.gameDataProvider = gameDataProvider;
	}
	
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

	public void handleGameTimeout(String gameId) {
		GameItem gameItem = gameDataProvider.getGame(gameId)
				.orElseThrow(() -> new RuntimeException("Could not find game with id: " + gameId));
		
		if (gameItem.getGameStatus().isOnOrAfter(GameStatus.COMPLETED)) {
			System.out.println(String.format("Game has a status of %s which is higher or equal ordinal to completed."
					+ " Assuming game already complete", gameItem.getGameStatus()));
			return;
		}
		
		// mark game as time up
		gameItem.setGameStatus(GameStatus.TIME_UP);
		handleEndGame(gameItem);
		
	}

}
