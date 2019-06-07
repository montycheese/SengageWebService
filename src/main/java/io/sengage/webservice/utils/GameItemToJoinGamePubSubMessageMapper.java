package io.sengage.webservice.utils;

import java.time.temporal.ChronoUnit;

import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.sengames.model.pubsub.JoinGameMessage;
import io.sengage.webservice.sengames.model.pubsub.SingleStrokeJoinGameMessage;
import io.sengage.webservice.sengames.model.singlestroke.CreateSingleStrokeGameParameters;

public class GameItemToJoinGamePubSubMessageMapper {

	private GameItemToJoinGamePubSubMessageMapper() {}
	
	public static JoinGameMessage get(GameItem gameItem) {
		switch (gameItem.getGame()) {
		case SINGLE_STROKE:
			return new SingleStrokeJoinGameMessage(
					gameItem.getGameId(),
					gameItem.getGame(),
					gameItem.getGameStatus(),
					GameToWaitForPlayersToJoinDurationMapper.get(gameItem.getGame()),
					gameItem.getDuration(),
					gameItem.getCreatedAt().plus(gameItem.getDuration(), ChronoUnit.SECONDS).toEpochMilli(),
					((CreateSingleStrokeGameParameters) gameItem.getGameSpecificParameters()).getImage()
					);
		case FLAPPY_BIRD_BR:
		default:
			return JoinGameMessage.from(gameItem);
		}
	}
}
