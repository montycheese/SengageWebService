package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.model.GameSpecificState;

public abstract class GameUpdateHandler {

	public abstract void handleGameUpdate(GameSpecificState state);
}
