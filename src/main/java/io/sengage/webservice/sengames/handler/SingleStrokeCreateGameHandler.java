package io.sengage.webservice.sengames.handler;

import java.util.Date;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsync;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequest;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequestEntry;
import com.amazonaws.services.cloudwatchevents.model.PutEventsResult;
import com.amazonaws.services.cloudwatchevents.model.PutEventsResultEntry;
import com.google.gson.Gson;

import io.sengage.webservice.events.EventDetail;
import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameItem.GameItemDigest;
import io.sengage.webservice.model.GameSpecificParameters;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.sengames.model.GameToWaitForPlayersToJoinDurationMapper;
import io.sengage.webservice.twitch.TwitchClient;
import io.sengage.webservice.utils.Constants;

public class SingleStrokeCreateGameHandler extends CreateGameHandler {
	
	private final GameDataProvider gameDataProvider;
	private final TwitchClient twitchClient;
	private final AmazonCloudWatchEventsAsync cwe;
	private final Gson gson;
	
	public SingleStrokeCreateGameHandler(GameDataProvider gameDataProvider, TwitchClient twitchClient, AmazonCloudWatchEventsAsync cwe,
			Gson gson) {
		this.gameDataProvider = gameDataProvider;
		this.twitchClient = twitchClient;
		this.cwe = cwe;
		this.gson = gson;
	}

	@Override
	public String handleCreateGame(Game game,
			GameSpecificParameters parameters, int duration,
			StreamContext streamContext) {
		
		// create game persistence object
		GameItem item = GameItem.from(game,  duration, streamContext);
		
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
		
		try {
			gameDataProvider.updateGame(item);
		} catch (ItemVersionMismatchException e) {
			System.out.println("Item version mismatch while trying to update game: " + item.getGameId());
			// have a event or error path that gracesfully fails a game if it encounters error state
			item = gameDataProvider.getGame(item.getGameId()).get();
			item.setGameStatus(GameStatus.ERROR_STATE);
			throw new RuntimeException("Item version mismatch while trying to update game, moving game to error state", e);
			
		}
		
		Date gameStartTime = 
				new Date(item.getCreatedAt().plusMillis(GameToWaitForPlayersToJoinDurationMapper.get(item.getGame())).toEpochMilli());
		
		// send CWE to notify of next event.
		PutEventsRequest eventsRequest = new PutEventsRequest()
		.withEntries(new PutEventsRequestEntry()
			.withTime(gameStartTime)
			.withDetail(gson.toJson(item.toDigest(), GameItemDigest.class))
			.withSource(Constants.CWE_EVENT_SOURCE)
			.withDetailType(EventDetail.WAITING_FOR_PLAYERS_COMPLETE.name())
		);
		PutEventsResult response = cwe.putEvents(eventsRequest);
		
		if (response.getFailedEntryCount() > 0) {
			System.out.println("Error creating event to start game: " + item.getGameId() + " failure count: "  + response.getFailedEntryCount());
			for (PutEventsResultEntry entry: response.getEntries()) {
				System.out.println("Error: " + entry.getErrorMessage());
			}
			try {
				gameDataProvider.updateGame(item.toBuilder().gameStatus(GameStatus.ERROR_STATE).build());
			} catch (ItemVersionMismatchException e) {
			}
			throw new IllegalStateException("Failed to create event to notify users to join game");
			// notify channel that game ended or failed to start?
		}
		
		return item.getGameId();
	}

}
