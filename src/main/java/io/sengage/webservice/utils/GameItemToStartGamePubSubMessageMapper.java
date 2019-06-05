package io.sengage.webservice.utils;

import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.sengames.model.pubsub.FlappyBirdStartGameMessage;
import io.sengage.webservice.sengames.model.pubsub.SingleStrokeStartGameMessage;
import io.sengage.webservice.sengames.model.pubsub.StartGameMessage;

public final class GameItemToStartGamePubSubMessageMapper {
	
	public static StartGameMessage get(GameItem gameItem) {
		
		switch (gameItem.getGame()) {
		case SINGLE_STROKE:
			return new SingleStrokeStartGameMessage(gameItem);
		case FLAPPY_BIRD_BR:
			return new FlappyBirdStartGameMessage(gameItem);
		default:
			throw new IllegalArgumentException("Unsupported game: " + gameItem.getGame());
		}
	}
}
