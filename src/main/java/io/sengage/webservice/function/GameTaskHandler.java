package io.sengage.webservice.function;

import io.sengage.webservice.dagger.DaggerTaskComponent;
import io.sengage.webservice.dagger.TaskComponent;
import io.sengage.webservice.events.EventDetail;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.sengames.handler.EndGameHandler;
import io.sengage.webservice.sengames.handler.EndGameHandlerFactory;
import io.sengage.webservice.sengames.handler.StartGameHandler;
import io.sengage.webservice.sengames.handler.StartGameHandlerFactory;
import io.sengage.webservice.sf.GameContextInput;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j2;

import com.amazonaws.services.lambda.runtime.Context;

@Log4j2
public class GameTaskHandler extends BaseLambda<GameContextInput, Void> {


	@Inject
	StartGameHandlerFactory startGameHandlerFactory;
	
	@Inject
	EndGameHandlerFactory endGameHandlerFactory;
	
	public GameTaskHandler() {
		TaskComponent component = DaggerTaskComponent.create();
		component.injectGameTaskHandler(this);
	}
	
	@Override
	public Void handleRequest(GameContextInput input, Context context) {
		log.info("handleEvent(): input: " + input);
		
		EventDetail detailType = input.getEventDetail();
		if (EventDetail.PING.equals(detailType)) {
			return null;
		}
		
		String gameId = input.getGameId();
		Game game = input.getGame();
		
		switch (detailType) {
		case GAME_OUT_OF_TIME:
			EndGameHandler endGameHandler = endGameHandlerFactory.get(game);
			endGameHandler.handleGameTimeout(gameId);
			break;
		case ALL_PLAYERS_FINISHED:
			endGameHandler = endGameHandlerFactory.get(game);
			endGameHandler.handleEndGame(gameId);
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