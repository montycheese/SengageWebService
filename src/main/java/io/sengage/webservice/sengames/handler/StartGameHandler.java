package io.sengage.webservice.sengames.handler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import io.sengage.webservice.events.EventDetail;
import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.GameItem.GameItemDigest;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.twitch.TwitchClient;
import io.sengage.webservice.utils.Constants;

import javax.inject.Inject;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsync;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequest;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequestEntry;
import com.amazonaws.services.cloudwatchevents.model.PutEventsResult;
import com.amazonaws.services.cloudwatchevents.model.PutEventsResultEntry;
import com.google.gson.Gson;

public class StartGameHandler {

	private final GameDataProvider gameDataProvider;
	private final TwitchClient twitchClient;
	private final AmazonCloudWatchEventsAsync cwe;
	private final Gson gson;
	
	@Inject
	public StartGameHandler(GameDataProvider gameDataProvider, TwitchClient twitchClient, AmazonCloudWatchEventsAsync cwe, Gson gson) {
		this.gameDataProvider = gameDataProvider;
		this.twitchClient = twitchClient;
		this.cwe = cwe;
		this.gson = gson;
	}
	
	public void startGame(String gameId) {
		Optional<GameItem> optionalGame = gameDataProvider.getGame(gameId);
		
		if (!optionalGame.isPresent()) {
			throw new IllegalStateException(
					String.format("Could not start game with id [%s] because it was not found in the persistence", gameId)
				);
		}
		GameItem game = optionalGame.get();
		
		if (game.getGameStatus().isOnOrAfter(GameStatus.IN_PROGRESS)) {
			System.out.println(String.format("Game status is already: %s, skipping update", GameStatus.IN_PROGRESS.name()));
			return;
		}
		
		game.setGameStatus(GameStatus.IN_PROGRESS);
		try {
			gameDataProvider.updateGame(game);
		} catch (ItemVersionMismatchException e) {
			e.printStackTrace();
			return;
		}
		
		Date gameExpiryTime = new Date(
				Instant.now().plusSeconds(game.getDuration()).toEpochMilli()
		);
		
		System.out.println("Setting game end time to: " + gameExpiryTime.toString());
		
		// send CWE to notify of next event.
		PutEventsRequest eventsRequest = new PutEventsRequest()
		.withEntries(new PutEventsRequestEntry()
			.withTime(gameExpiryTime)
			.withDetail(gson.toJson(game.toDigest(), GameItemDigest.class))
			.withSource(Constants.CWE_EVENT_SOURCE)
			.withDetailType(EventDetail.GAME_OUT_OF_TIME.name())
		);
		PutEventsResult response = cwe.putEvents(eventsRequest);
		
		if (response.getFailedEntryCount() > 0) {
			System.out.println("Error creating event to start game: " + game.getGameId() + " failure count: "  + response.getFailedEntryCount());
			for (PutEventsResultEntry entry: response.getEntries()) {
				System.out.println("Error: " + entry.getErrorMessage());
			}
			try {
				gameDataProvider.updateGame(game.toBuilder().gameStatus(GameStatus.ERROR_STATE).build());
			} catch (ItemVersionMismatchException e) {
			}
			throw new IllegalStateException("Failed to create event to start game");
			// notify channel that game ended or failed to start?
		}
		
		twitchClient.notifyChannelGameStarted(game);
	}
}
