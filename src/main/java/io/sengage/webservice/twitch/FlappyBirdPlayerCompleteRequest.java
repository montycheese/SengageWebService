package io.sengage.webservice.twitch;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.flappybird.FlappyBirdPlayer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class FlappyBirdPlayerCompleteRequest implements PlayerCompleteRequest {
	private FlappyBirdPlayer player;
	private GameItem gameItem;
	private int playersRemaining;
	private String channelId;
	
	public Game getGame() {
		return gameItem.getGame();
	}
	
}
