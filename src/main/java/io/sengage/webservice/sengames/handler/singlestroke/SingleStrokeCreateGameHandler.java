package io.sengage.webservice.sengames.handler.singlestroke;

import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameSpecificParameters;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.sengames.handler.CreateGameHandler;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;

public class SingleStrokeCreateGameHandler extends CreateGameHandler {
	
	private final GameDataProvider gameDataProvider;
	private final TwitchClient twitchClient;
	private final StepFunctionTaskExecutor sfExecutor;
	
	public SingleStrokeCreateGameHandler(GameDataProvider gameDataProvider, TwitchClient twitchClient,
			StepFunctionTaskExecutor sfExecutor) {
		this.gameDataProvider = gameDataProvider;
		this.twitchClient = twitchClient;
		this.sfExecutor = sfExecutor;
	}

	@Override
	public String handleCreateGame(Game game,
			GameSpecificParameters parameters, int duration,
			StreamContext streamContext) {
		
		// create game persistence object
		GameItem item = GameItem.from(game, parameters, duration, streamContext);
		
		gameDataProvider.createGame(item);
		// send pubsub message
		
		item = item.toBuilder().gameStatus(GameStatus.WAITING_FOR_PLAYERS)
				.version(1L)
				.build();
		
		try {
			twitchClient.notifyChannelJoinGame(item);	
		} catch (Exception e) {
			// have a event or error path that gracesfully fails a game if it encounters error state
			throw e;
		}
		
		
		String startGameStateMachineArn = sfExecutor.executeStartGameStateMachine(item);
		item.setStartGameStateMachineExecutionArn(startGameStateMachineArn);
		try {
			gameDataProvider.updateGame(item);
		} catch (ItemVersionMismatchException e) {
			System.out.println("Item version mismatch while trying to update game: " + item.getGameId());
			// have a event or error path that gracesfully fails a game if it encounters error state
			item = gameDataProvider.getGame(item.getGameId()).get();
			item.setGameStatus(GameStatus.ERROR_STATE);
			throw new RuntimeException("Item version mismatch while trying to update game, moving game to error state", e);
			
		}
		return item.getGameId();
	}

}
