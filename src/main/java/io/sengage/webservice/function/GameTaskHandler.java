package io.sengage.webservice.function;

import io.sengage.webservice.dagger.DaggerTaskComponent;
import io.sengage.webservice.dagger.TaskComponent;
import io.sengage.webservice.events.EventDetail;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.sengames.handler.EndGameHandler;
import io.sengage.webservice.sengames.handler.EndGameHandlerFactory;
import io.sengage.webservice.sengames.handler.StartGameHandler;
import io.sengage.webservice.sf.GameContextInput;

import javax.inject.Inject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class GameTaskHandler extends BaseLambda<GameContextInput, Void> {


	@Inject
	StartGameHandler startGameHandler;
	
	@Inject
	EndGameHandlerFactory endGameHandlerFactory;
	
	private LambdaLogger logger;
	
	public GameTaskHandler() {
		TaskComponent component = DaggerTaskComponent.create();
		component.injectGameTaskHandler(this);
	}
	
	@Override
	public Void handleRequest(GameContextInput input, Context context) {
		logger = context.getLogger();
		logger.log("handleEvent(): input: " + input);
		
		EventDetail detailType = input.getEventDetail();
		String gameId = input.getGameId();
		Game game = input.getGame();
		
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