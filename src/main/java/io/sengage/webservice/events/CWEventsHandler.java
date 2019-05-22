package io.sengage.webservice.events;

import io.sengage.webservice.function.BaseLambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

public class CWEventsHandler extends BaseLambda<ScheduledEvent, Void> {

	private LambdaLogger logger;
	
	@Override
	public Void handleRequest(ScheduledEvent event, Context context) {
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
		
		return null;
	}
}
