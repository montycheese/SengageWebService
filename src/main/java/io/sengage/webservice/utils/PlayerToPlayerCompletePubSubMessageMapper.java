package io.sengage.webservice.utils;

import java.util.UUID;

import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.flappybird.FlappyBirdPlayer;
import io.sengage.webservice.sengames.model.pubsub.FlappyBirdPlayerCompletePubSubMessage;
import io.sengage.webservice.sengames.model.pubsub.PubSubGameMessage;
import io.sengage.webservice.twitch.FlappyBirdPlayerCompleteRequest;
import io.sengage.webservice.twitch.PlayerCompleteRequest;

public class PlayerToPlayerCompletePubSubMessageMapper {
	
	public static PubSubGameMessage get(PlayerCompleteRequest request) {
		
		switch (request.getGame()) {
		case FLAPPY_BIRD_BR:
			FlappyBirdPlayerCompleteRequest fbRequest = (FlappyBirdPlayerCompleteRequest) request;
			FlappyBirdPlayer fbPlayer = fbRequest.getPlayer();
			GameItem gameItem = fbRequest.getGameItem();
			return FlappyBirdPlayerCompletePubSubMessage.builder()
					.character(fbPlayer.getCharacter())
					.game(gameItem.getGame())
					.gameId(gameItem.getGameId())
					.gameStatus(gameItem.getGameStatus())
					.opaqueId(fbPlayer.getOpaqueId())
					.userName(fbPlayer.getUserName())
					.playersRemaining(fbRequest.getPlayersRemaining())
					.action(fbRequest.getViewAction())
					.idempotencyToken(UUID.randomUUID().toString())
					.build();
		case SINGLE_STROKE:
		default:
			throw new IllegalArgumentException("Unsupported type: " + request.getGame());
		}
	}
}
