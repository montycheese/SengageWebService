package io.sengage.webservice.sengames.handler;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameSpecificParameters;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.twitch.TwitchClient;

public class SingleStrokeCreateGameHandler extends CreateGameHandler {
	
	private final GameDataProvider gameDataProvider;
	private final TwitchClient twitchClient;
	
	public SingleStrokeCreateGameHandler(GameDataProvider gameDataProvider, TwitchClient twitchClient) {
		this.gameDataProvider = gameDataProvider;
		this.twitchClient = twitchClient;
	}

	@Override
	public String handleCreateGame(Game game,
			GameSpecificParameters parameters, int duration,
			StreamContext streamContext) {
		
		// create game persistence object
		GameItem item = GameItem.from(game,  duration, streamContext);
		
		gameDataProvider.createGame(item);
		// send pubsub message
		
		item = item.toBuilder().gameStatus(GameStatus.WAITING_FOR_PLAYERS).build();
		
		twitchClient.notifyChannelGameStarted(item);
		gameDataProvider.updateGame(item);
		
		// send CWE to notify of next event.
		
		
		return item.getGameId();
	}

}
