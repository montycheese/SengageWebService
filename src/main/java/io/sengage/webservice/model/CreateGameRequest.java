package io.sengage.webservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGameRequest {
	private Game game;
	private GameSpecificParameters gameSpecificParameters;
	private int duration; // seconds
}
