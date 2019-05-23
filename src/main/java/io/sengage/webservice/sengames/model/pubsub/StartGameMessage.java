package io.sengage.webservice.sengames.model.pubsub;

import java.time.temporal.ChronoUnit;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
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
public class StartGameMessage implements PubSubGameMessage {

	private String gameId;
	private Game game;
	private GameStatus gameStatus;
	private long gameEndTimeEpochMilli;
	
	public static StartGameMessage from(GameItem gameItem) {
		return StartGameMessage.builder()
				.gameId(gameItem.getGameId())
				.game(gameItem.getGame())
				.gameStatus(GameStatus.WAITING_FOR_PLAYERS)
				.gameEndTimeEpochMilli(gameItem.getCreatedAt().plus(gameItem.getDuration(), ChronoUnit.SECONDS).toEpochMilli())
				.build();
	}
}