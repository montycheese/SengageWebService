package io.sengage.webservice.sengames.model.pubsub;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.sengames.model.flappybird.GameCharacter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Data
public class FlappyBirdPlayerCompletePubSubMessage implements PubSubGameMessage {
	private Game game;
	private GameStatus gameStatus;
	private String gameId;
	private String opaqueId;
	private String username;
	private GameCharacter character; 
	@Builder.Default
	private final ViewAction action = ViewAction.KILL_FEED; 
}
