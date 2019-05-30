package io.sengage.webservice.sengames.model.pubsub;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class SingleStrokeEndGameMessage extends EndGameMessage {
	
	private String image;
	
	public SingleStrokeEndGameMessage(String gameId, Game game, GameStatus gameStatus, String image) {
		super(gameId, game, gameStatus);
		this.image = image;
	}
}
