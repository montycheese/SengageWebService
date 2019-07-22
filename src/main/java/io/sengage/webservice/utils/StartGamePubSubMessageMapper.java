package io.sengage.webservice.utils;

import io.sengage.webservice.sengames.model.pubsub.FlappyBirdStartGameMessage;
import io.sengage.webservice.sengames.model.pubsub.SingleStrokeStartGameMessage;
import io.sengage.webservice.sengames.model.pubsub.StartGameMessage;
import io.sengage.webservice.twitch.NotifyGameStartedRequest;

public final class StartGamePubSubMessageMapper {
	
	public static StartGameMessage get(NotifyGameStartedRequest request) {
		
		switch (request.getGameItem().getGame()) {
		case SINGLE_STROKE:
			return new SingleStrokeStartGameMessage(request.getGameItem(), request.getTotalPlayers());
		case FLAPPY_BIRD_BR:
			return new FlappyBirdStartGameMessage(request.getGameItem(), request.getTotalPlayers());
		default:
			throw new IllegalArgumentException("Unsupported game: " + request.getGameItem().getGame());
		}
	}
}
