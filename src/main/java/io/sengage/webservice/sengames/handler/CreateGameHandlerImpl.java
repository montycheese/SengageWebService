package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameSpecificParameters;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;
import io.sengage.webservice.utils.Constants;
import io.sengage.webservice.utils.GameToWaitForPlayersToJoinDurationMapper;

public class CreateGameHandlerImpl extends CreateGameHandler {
	
	private final GameDataProvider gameDataProvider;
	private final TwitchClient twitchClient;
	private final StepFunctionTaskExecutor sfExecutor;
	
	public CreateGameHandlerImpl(GameDataProvider gameDataProvider, TwitchClient twitchClient,
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
			item.setGameStatus(GameStatus.ERROR_STATE);
			item.setStatusReason("Failed to notify players of game start due to: " + e.getMessage());
			try {
				gameDataProvider.updateGame(item);
			} catch (ItemVersionMismatchException e1) {
				e1.printStackTrace();
			}
			throw e;
		}
		
		try {
			int waitDuration = GameToWaitForPlayersToJoinDurationMapper.get(game);
			String message = String.format(Constants.GAME_START_MESSAGE_FORMAT, item.getGame().getFriendlyName(), waitDuration);
			twitchClient.sendExtensionChatMessage(item.getChannelId(), message);
		} catch (Exception e) {
			e.printStackTrace();
			// OK to swallow this.
		}
		
		
		String startGameStateMachineArn = sfExecutor.executeStartGameStateMachine(item);
		item.setStartGameStateMachineExecutionArn(startGameStateMachineArn);
		try {
			gameDataProvider.updateGame(item);
		} catch (ItemVersionMismatchException e) {
			System.out.println("Item version mismatch while trying to update game: " + item.getGameId());
			// have a event or error path that gracefully fails a game if it encounters error state
			item = gameDataProvider.getGame(item.getGameId()).get();
			item.setGameStatus(GameStatus.ERROR_STATE);
			throw new RuntimeException("Item version mismatch while trying to update game, moving game to error state", e);
			
		}
		return item.getGameId();
	}

}
