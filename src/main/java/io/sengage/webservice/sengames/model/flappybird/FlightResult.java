package io.sengage.webservice.sengames.model.flappybird;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlightResult {
	
	private String character;
	private long distance;
	private String userName;
	private String opaqueId;
	private int attempt;
	
}
