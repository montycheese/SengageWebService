package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.exception.GameCompletedException;
import io.sengage.webservice.model.GameSpecificState;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.sengames.model.HandleGameUpdateResponse;

public abstract class GameUpdateHandler {

	public abstract HandleGameUpdateResponse handleGameUpdate(String gameId, GameSpecificState state, StreamContext streamContext) 
			throws GameCompletedException;
}
