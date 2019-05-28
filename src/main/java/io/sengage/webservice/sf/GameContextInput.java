package io.sengage.webservice.sf;

import io.sengage.webservice.events.EventDetail;
import io.sengage.webservice.model.Game;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameContextInput {
	private String gameId;
	private Game game;
	private EventDetail eventDetail;
}
