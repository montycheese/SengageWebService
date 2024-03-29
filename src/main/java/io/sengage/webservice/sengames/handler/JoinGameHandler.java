package io.sengage.webservice.sengames.handler;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.sengage.webservice.exception.ItemAlreadyExistsException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.model.StreamContext;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;

@Singleton
public class JoinGameHandler {
	
	private final PlayerDataProvider playerDataProvider;
	private final GameDataProvider gameDataProvider;
	
	@Inject
	public JoinGameHandler(PlayerDataProvider playerDataProvider, GameDataProvider gameDataProvider) {
		this.playerDataProvider = playerDataProvider;
		this.gameDataProvider = gameDataProvider;
	}
	
	/**
	 * 
	 * @return true if player joined game successfully, false otherwise
	 */
	public void handleJoinGame(String gameId, StreamContext streamContext) {		
		Optional<GameItem> optionalGame = gameDataProvider.getGame(gameId);
		
		if (!optionalGame.isPresent()) {
			throw new IllegalStateException("Could not find game with id: " + gameId);
		} else if (optionalGame.get().getGameStatus().isAfter(GameStatus.WAITING_FOR_PLAYERS)) {
			throw new IllegalStateException("Could not join game because it has already started.");
		}
		// todo also check if game is full
		
		Player player = Player.builder()
				.gameId(gameId)
				.opaqueId(streamContext.getOpaqueId())
				.userId(streamContext.getUserId())
				.userName(streamContext.getUserName())
				.playerStatus(PlayerStatus.PLAYING)
				.build();
		
		try {
			playerDataProvider.createPlayer(player);
		} catch (ItemAlreadyExistsException e) {
			e.printStackTrace();
			// TODO throw
		}
	}

}
