package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameSpecificParameters;
import io.sengage.webservice.model.StreamInfo;
import io.sengage.webservice.persistence.GameDataProvider;

public class SingleStrokeCreateGameHandler extends CreateGameHandler {
	
	private final GameDataProvider gameDataProvider;
	
	public SingleStrokeCreateGameHandler(GameDataProvider gameDataProvider) {
		this.gameDataProvider = gameDataProvider;
		
	}

	@Override
	public String handleCreateGame(Game game,
			GameSpecificParameters parameters, int duration,
			StreamInfo streamInfo) {
		
		// create game persistence object
		GameItem item = GameItem.from(game,  duration, streamInfo);
		
		gameDataProvider.createGame(item);
		
		
		// send pubsub message
		
		// send CWE to notify of next event.
		return item.getGameId();
	}

}
