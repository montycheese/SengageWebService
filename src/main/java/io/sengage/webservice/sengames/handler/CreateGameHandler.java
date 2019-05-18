package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameSpecificParameters;
import io.sengage.webservice.model.StreamContext;

public abstract class CreateGameHandler {

	public abstract String handleCreateGame(Game game, GameSpecificParameters parameters, int duration, StreamContext streamContext);
}
