package io.sengage.webservice.sengames.model.pubsub;

import java.util.UUID;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class SingleStrokeEndGameMessage extends EndGameMessage {
	
	private String image;
	
	public SingleStrokeEndGameMessage(String gameId, Game game, GameStatus gameStatus, String image) {
		super(gameId, game, gameStatus, UUID.randomUUID().toString());
		this.image = image;
	}
}
