package io.sengage.webservice.sengames.handler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import lombok.extern.log4j.Log4j2;

import com.google.common.collect.Sets;

import io.sengage.webservice.exception.GameInProgressException;
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

@Log4j2
public class CreateGameHandlerImpl extends CreateGameHandler {
	
	private static final Set<GameStatus> GAME_IN_PROGRESS_STATUSES = Sets.newHashSet(
			GameStatus.IN_PROGRESS,
			GameStatus.WAITING_FOR_PLAYERS
	);
	
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
		
		// see if game already is being played on the channel
		// ignore games that are more than 11 min old to avoid leaked games from screwing with the number 
		Instant timeWindowToIgnore = Instant.now().minus(11, ChronoUnit.MINUTES);
		int numGamesInProgress = gameDataProvider.getNumberOfGamesWithStatuses(streamContext.getChannelId(), GAME_IN_PROGRESS_STATUSES, timeWindowToIgnore);
		
		if (numGamesInProgress > 0) {
			log.warn("{} game(s) already in progress. Cannot create new game for channel {}", numGamesInProgress, streamContext.getChannelId());
			throw new GameInProgressException("Cannot start new game because one is currently in progress");
		}
		
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
			log.warn("Failed to notify channel to join game due to {}", e);
			item.setGameStatus(GameStatus.ERROR_STATE);
			item.setStatusReason("Failed to notify players of game start due to: " + e.getMessage());
			try {
				gameDataProvider.updateGame(item);
			} catch (ItemVersionMismatchException e1) {
				log.warn("Failed to update dynamo for game {}. {}", item.getGameId(), e1);
			}
			throw e;
		}
		
		try {
			int waitDuration = GameToWaitForPlayersToJoinDurationMapper.get(game);
			String message = String.format(Constants.GAME_START_MESSAGE_FORMAT, item.getGame().getFriendlyName(), waitDuration);
			twitchClient.sendExtensionChatMessage(item.getChannelId(), message);
		} catch (Exception e) {
			log.warn("Failed to send extension chat message to channel {}", streamContext.getChannelId());
		}
		
		
		String startGameStateMachineArn = sfExecutor.executeStartGameStateMachine(item);
		item.setStartGameStateMachineExecutionArn(startGameStateMachineArn);
		try {
			gameDataProvider.updateGame(item);
		} catch (ItemVersionMismatchException e) {
			log.warn("Item version mismatch while trying to update game: {}. {}" + item.getGameId(), e);
			// have a event or error path that gracefully fails a game if it encounters error state
			item = gameDataProvider.getGame(item.getGameId()).get();
			item.setGameStatus(GameStatus.ERROR_STATE);
			throw new RuntimeException("Item version mismatch while trying to update game, moving game to error state", e);
			
		}
		return item.getGameId();
	}

}
