package io.sengage.webservice.twitch;

import io.sengage.webservice.model.GameItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotifyGameStartedRequest {
	private GameItem gameItem;
	private int totalPlayers;
}
