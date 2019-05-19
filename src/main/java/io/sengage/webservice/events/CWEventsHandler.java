package io.sengage.webservice.events;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

public class CWEventsHandler {

	private LambdaLogger logger;
	
	public void handleEvent(ScheduledEvent event, Context context) {
		logger = context.getLogger();
		logger.log("handleEvent(): input: " + event);
		
		EventDetail detailType = EventDetail.valueOf(event.getDetailType());
		
		switch (detailType) {
		case GAME_OUT_OF_TIME:
			break;
		case WAITING_FOR_PLAYERS_COMPLETE:
			break;
		default:
			break;
		
		}
	}
}
