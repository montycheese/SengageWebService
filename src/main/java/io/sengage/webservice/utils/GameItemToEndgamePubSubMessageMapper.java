package io.sengage.webservice.utils;

import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.sengames.model.pubsub.EndGameMessage;
import io.sengage.webservice.sengames.model.pubsub.SingleStrokeEndGameMessage;
import io.sengage.webservice.sengames.model.singlestroke.CreateSingleStrokeGameParameters;

public final class GameItemToEndgamePubSubMessageMapper {

	public static EndGameMessage get(GameItem gameItem) {
		
		switch (gameItem.getGame()) {
			case SINGLE_STROKE:
				
				CreateSingleStrokeGameParameters params = (CreateSingleStrokeGameParameters)
					gameItem.getGameSpecificParameters();
				
				return new SingleStrokeEndGameMessage(gameItem.getGameId(), 
						gameItem.getGame(), 
						gameItem.getGameStatus(),
						params.getImage());
			case FLAPPY_BIRD_BR:
			default:
				throw new IllegalArgumentException("Unsupported game: " + gameItem.getGame());
		}
		
	}
}
