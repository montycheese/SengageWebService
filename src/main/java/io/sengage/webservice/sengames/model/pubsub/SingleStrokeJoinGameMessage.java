package io.sengage.webservice.sengames.model.pubsub;

import lombok.Getter;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameStatus;

public class SingleStrokeJoinGameMessage extends JoinGameMessage {
	@Getter
	private String image;
	
	public SingleStrokeJoinGameMessage(String gameId, Game game,
			GameStatus gameStatus, int waitDuration, int gameDuration, long gameEndTimeEpochMilli,
			String image) {
		super(gameId, game, gameStatus, waitDuration, gameDuration, gameEndTimeEpochMilli);
		this.image = image;
	}
}
