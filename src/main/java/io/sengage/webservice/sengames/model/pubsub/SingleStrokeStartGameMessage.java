package io.sengage.webservice.sengames.model.pubsub;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.sengames.model.singlestroke.CreateSingleStrokeGameParameters;

@EqualsAndHashCode(callSuper=false)
@Data
public class SingleStrokeStartGameMessage extends StartGameMessage {

	private String image;
	
	public SingleStrokeStartGameMessage(GameItem gameItem) {
		super(gameItem);
		this.image = ((CreateSingleStrokeGameParameters)
				gameItem.getGameSpecificParameters()).getImage();
	}
}
