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
			int waitDuration = GameToWaitForPlayersToJoinDurationMapper.get(gameItem.getGame());
			return new SingleStrokeJoinGameMessage(
					gameItem.getGameId(),
					gameItem.getGame(),
					gameItem.getGameStatus(),
					waitDuration,
					gameItem.getDuration(),
					gameItem.getCreatedAt().plus(waitDuration, ChronoUnit.SECONDS).toEpochMilli(),
					gameItem.getCreatedAt().plus(gameItem.getDuration(), ChronoUnit.SECONDS).toEpochMilli(),
					((CreateSingleStrokeGameParameters) gameItem.getGameSpecificParameters()).getImage()
					);
		case FLAPPY_BIRD_BR:
		default:
			return JoinGameMessage.from(gameItem);
		}
	}
}
