package io.sengage.webservice.events;

import javax.inject.Inject;

import io.sengage.webservice.dagger.DaggerEventsComponent;
import io.sengage.webservice.dagger.EventsComponent;
import io.sengage.webservice.function.BaseLambda;
import io.sengage.webservice.model.GameItem.GameItemDigest;
import io.sengage.webservice.sengames.handler.StartGameHandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

public class CWEventsHandler extends BaseLambda<ScheduledEvent, Void> {


	@Inject
	StartGameHandler startGameHandler;
	
	private LambdaLogger logger;
	
	public CWEventsHandler() {
		EventsComponent component = DaggerEventsComponent.create();
		component.injectCWEventsHandler(this);
	}
	
	@Override
	public Void handleRequest(ScheduledEvent event, Context context) {
		logger = context.getLogger();
		logger.log("handleEvent(): input: " + event);
		
		EventDetail detailType = EventDetail.valueOf(event.getDetailType());
		String gameId = (String) event.getDetail().get(GameItemDigest.GAME_ID_ATTR_KEY);
		
		switch (detailType) {
		case GAME_OUT_OF_TIME:
			break;
		case WAITING_FOR_PLAYERS_COMPLETE:
			startGameHandler.startGame(gameId);
			break;
		default:
			break;
		
		}
		
		return null;
	}
}
