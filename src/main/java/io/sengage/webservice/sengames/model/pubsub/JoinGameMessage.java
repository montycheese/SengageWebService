package io.sengage.webservice.sengames.model.pubsub;

import java.time.temporal.ChronoUnit;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.utils.GameToWaitForPlayersToJoinDurationMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper=false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinGameMessage implements PubSubGameMessage {

	private String gameId;
	private Game game;
	private GameStatus gameStatus;
	private int waitDuration;
	private int gameDuration;
	private long gameStartTimeEpochMilli;
	private long gameEndTimeEpochMilli;
	
	public static JoinGameMessage from(GameItem gameItem) {
		int waitDuration = GameToWaitForPlayersToJoinDurationMapper.get(gameItem.getGame());
		return JoinGameMessage.builder()
				.gameId(gameItem.getGameId())
				.game(gameItem.getGame())
				.gameStatus(GameStatus.WAITING_FOR_PLAYERS)
				.gameDuration(gameItem.getDuration())
				.gameEndTimeEpochMilli(gameItem.getCreatedAt().plus(gameItem.getDuration(), ChronoUnit.SECONDS).toEpochMilli())
				.gameStartTimeEpochMilli(gameItem.getCreatedAt().plus(waitDuration, ChronoUnit.SECONDS).toEpochMilli())
				.waitDuration(waitDuration)
				.build();
	}
}
