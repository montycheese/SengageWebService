package io.sengage.webservice.sengames.handler;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsAsync;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequest;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequestEntry;
import com.amazonaws.services.cloudwatchevents.model.PutEventsResult;
import com.amazonaws.services.cloudwatchevents.model.PutEventsResultEntry;
import com.google.gson.Gson;

import io.sengage.webservice.events.EventDetail;
import io.sengage.webservice.exception.GameCompletedException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameSpecificState;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.model.GameItem.GameItemDigest;
import io.sengage.webservice.sengames.model.HandleGameUpdateResponse;
import io.sengage.webservice.utils.Constants;

public abstract class GameUpdateHandler {
	
	private final AmazonCloudWatchEventsAsync cwe;
	private final Gson gson;
	
	public GameUpdateHandler(AmazonCloudWatchEventsAsync cwe, Gson gson) {
		this.cwe = cwe;
		this.gson = gson;
	}

	protected void notifyAllPlayersAreFinished(GameItem gameItem) {
		PutEventsRequest eventsRequest = new PutEventsRequest()
		.withEntries(new PutEventsRequestEntry()
			.withDetail(gson.toJson(gameItem.toDigest(), GameItemDigest.class))
			.withSource(Constants.CWE_EVENT_SOURCE)
			.withDetailType(EventDetail.ALL_PLAYERS_FINISHED.name())
		);
		PutEventsResult response = cwe.putEvents(eventsRequest);
		
		if (response.getFailedEntryCount() > 0) {
			System.out.println("Error creating event to end game: " + gameItem.getGameId() + " failure count: "  + response.getFailedEntryCount());
			for (PutEventsResultEntry entry: response.getEntries()) {
				System.out.println("Error: " + entry.getErrorMessage());
			}
			// It's ok to swallow this, we'll just let the game's time expire.
		}
		
	}
	public abstract HandleGameUpdateResponse handleGameUpdate(String gameId, GameSpecificState state, StreamContext streamContext) 
			throws GameCompletedException;
}
