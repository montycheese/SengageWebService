package io.sengage.webservice.sengames.model.pubsub;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.sengames.model.flappybird.CreateFlappyBirdGameParameters;

@EqualsAndHashCode(callSuper=false)
@Data
public class FlappyBirdStartGameMessage extends StartGameMessage {

	private int difficulty;
	
	public FlappyBirdStartGameMessage(GameItem gameItem) {
		super(gameItem);
		this.difficulty = ((CreateFlappyBirdGameParameters)
				gameItem.getGameSpecificParameters()).getDifficulty();
	}
}
