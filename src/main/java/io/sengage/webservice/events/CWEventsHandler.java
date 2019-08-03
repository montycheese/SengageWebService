package io.sengage.webservice.events;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j2;
import io.sengage.webservice.dagger.DaggerTaskComponent;
import io.sengage.webservice.dagger.TaskComponent;
import io.sengage.webservice.function.BaseLambda;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem.GameItemDigest;
import io.sengage.webservice.sengames.handler.EndGameHandler;
import io.sengage.webservice.sengames.handler.EndGameHandlerFactory;
import io.sengage.webservice.sengames.handler.StartGameHandler;
import io.sengage.webservice.sengames.handler.StartGameHandlerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

@Log4j2
public class CWEventsHandler extends BaseLambda<ScheduledEvent, Void> {


	@Inject
	StartGameHandlerFactory startGameHandlerFactory;
	
	@Inject
	EndGameHandlerFactory endGameHandlerFactory;

	
	public CWEventsHandler() {
		TaskComponent component = DaggerTaskComponent.create();
		component.injectCWEventsHandler(this);
	}
	
	@Override
	public Void handleRequest(ScheduledEvent event, Context context) {
		log.info("handleEvent(): input: " + event);
		
		EventDetail detailType = EventDetail.valueOf(event.getDetailType());
		if (EventDetail.PING.equals(detailType)) {
			return null;
		}
		
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
			StartGameHandler startGameHandler = startGameHandlerFactory.get(game);
			startGameHandler.startGame(gameId);
			break;
		case PING:
			return null;
		default:
			throw new IllegalArgumentException("Unsupported event detail: " + detailType);
		
		}
		
		return null;
	}
}
