package io.sengage.webservice.sengames.model.pubsub;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameStatus;

public interface PubSubGameMessage {

	String getIdempotencyToken();
	String getGameId();
	Game getGame();
	GameStatus getGameStatus();
	
}
