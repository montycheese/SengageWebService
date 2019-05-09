package io.sengage.webservice.model;

import lombok.Data;

@Data
public class UpdateGameStateRequest {
	private String username;
	private String opaqueUserId;
	private String userId;
	private Game game;
	private GameSpecificState gameSpecificState;
}
