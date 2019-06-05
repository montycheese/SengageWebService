package io.sengage.webservice.utils;

import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.flappybird.FlappyBirdPlayer;
import io.sengage.webservice.sengames.model.pubsub.FlappyBirdPlayerCompletePubSubMessage;
import io.sengage.webservice.sengames.model.pubsub.PubSubGameMessage;

public class PlayerToPlayerCompletePubSubMessageMapper {
	
	public static PubSubGameMessage get(GameItem gameItem, Player player) {
		
		switch (gameItem.getGame()) {
		case FLAPPY_BIRD_BR:
			FlappyBirdPlayer fbPlayer = (FlappyBirdPlayer) player;
			return FlappyBirdPlayerCompletePubSubMessage.builder()
					.character(fbPlayer.getCharacter())
					.game(gameItem.getGame())
					.gameId(gameItem.getGameId())
					.gameStatus(gameItem.getGameStatus())
					.build();
		case SINGLE_STROKE:
		default:
			throw new IllegalArgumentException("Unsupported type: " + gameItem.getGame());
		}
	}
}
