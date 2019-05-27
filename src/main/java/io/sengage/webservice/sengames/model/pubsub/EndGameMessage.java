package io.sengage.webservice.sengames.model.pubsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameStatus;

@EqualsAndHashCode(callSuper=false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndGameMessage implements PubSubGameMessage {
	
	private String gameId;
	private Game game;
	private GameStatus gameStatus;
	//private List<? extends EndGameResult> results;

}
