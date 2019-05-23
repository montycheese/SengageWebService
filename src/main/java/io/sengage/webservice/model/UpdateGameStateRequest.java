package io.sengage.webservice.model;

import lombok.Data;

@Data
public class UpdateGameStateRequest {
	private String username;
	private String gameId;
	private Game game;
	private GameSpecificState gameSpecificState;
}
