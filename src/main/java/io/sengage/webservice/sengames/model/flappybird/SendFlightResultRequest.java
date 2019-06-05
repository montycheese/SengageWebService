package io.sengage.webservice.sengames.model.flappybird;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.sengage.webservice.model.GameSpecificState;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendFlightResultRequest implements GameSpecificState {
	public static final String type = "SendFlightResultRequest"; // Required for GSON to deserialze
	private FlightResult flightResult;
}
