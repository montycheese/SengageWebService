package io.sengage.webservice.model.flappybird;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.sengage.webservice.model.EndGameResult;
import io.sengage.webservice.sengames.model.flappybird.FlightResult;

@EqualsAndHashCode(callSuper=false)
@Data
@AllArgsConstructor
public class FlappyBirdEndGameResult extends EndGameResult {
	public static final String type = "FlappyBirdEndGameResult";
	private FlightResult flightResult;
}
