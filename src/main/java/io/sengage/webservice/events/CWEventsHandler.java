package io.sengage.webservice.events;

import javax.inject.Inject;

import io.sengage.webservice.dagger.DaggerEventsComponent;
import io.sengage.webservice.dagger.EventsComponent;
import io.sengage.webservice.function.BaseLambda;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem.GameItemDigest;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.sengames.handler.EndGameHandler;
import io.sengage.webservice.sengames.handler.EndGameHandlerFactory;
import io.sengage.webservice.sengames.handler.StartGameHandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

public class CWEventsHandler extends BaseLambda<ScheduledEvent, Void> {


	@Inject
	StartGameHandler startGameHandler;
	
	@Inject
	EndGameHandlerFactory endGameHandlerFactory;
	
	@Inject
	GameDataProvider gameDataProvider;
	
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
		Game game = Game.valueOf((String) event.getDetail().get(GameItemDigest.GAME_ATTR_KEY));
		
		switch (detailType) {
		case GAME_OUT_OF_TIME:
			EndGameHandler handler = endGameHandlerFactory.get(game);
			handler.handleGameTimeout(gameId);
			break;
		case ALL_PLAYERS_FINISHED:
			handler = endGameHandlerFactory.get(game);
			handler.handleEndGame(gameId);
			break;
		case WAITING_FOR_PLAYERS_COMPLETE:
			startGameHandler.startGame(gameId);
			break;
		default:
			throw new IllegalArgumentException("Unsupported event detail: " + detailType);
		
		}
		
		return null;
	}
}
