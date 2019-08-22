package io.sengage.webservice.sengames.model.pubsub;

import java.util.UUID;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameStatus;
import lombok.Getter;

public class FlappyBirdJoinGameMessage extends JoinGameMessage {

	@Getter
	private int difficulty;
	
	public FlappyBirdJoinGameMessage(String gameId, Game game,
			GameStatus gameStatus, int waitDuration, int gameDuration, long gameStartTimeEpochMilli, long gameEndTimeEpochMilli,
			int difficulty) {
		super(gameId, game, gameStatus, waitDuration, gameDuration, gameStartTimeEpochMilli, gameEndTimeEpochMilli, UUID.randomUUID().toString());
		this.difficulty = difficulty;
	}
}
