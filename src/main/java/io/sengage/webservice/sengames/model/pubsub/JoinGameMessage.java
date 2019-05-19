package io.sengage.webservice.sengames.model.pubsub;

import java.time.temporal.ChronoUnit;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.sengames.model.GameToWaitForPlayersToJoinDurationMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinGameMessage {

	private String gameId;
	private Game game;
	private GameStatus gameStatus;
	private int waitDuration;
	private long gameEndTimeEpochMilli;
	
	public static JoinGameMessage from(GameItem gameItem) {
		int waitDuration = GameToWaitForPlayersToJoinDurationMapper.get(gameItem.getGame());
		return JoinGameMessage.builder()
				.gameId(gameItem.getGameId())
				.game(gameItem.getGame())
				.gameStatus(GameStatus.WAITING_FOR_PLAYERS)
				.gameEndTimeEpochMilli(gameItem.getCreatedAt().plus(gameItem.getDuration(), ChronoUnit.SECONDS).toEpochMilli())
				.waitDuration(waitDuration)
				.build();
	}
}
