package io.sengage.webservice.sengames.model.pubsub;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameCancellationReason;
import io.sengage.webservice.model.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelGameMessage implements PubSubGameMessage {
	
	private String gameId;
	private Game game;
	private GameStatus gameStatus;
	private GameCancellationReason cancellationReason;
	
}
