package io.sengage.webservice.persistence;

import java.util.List;

import io.sengage.webservice.exception.ItemAlreadyExistsException;
import io.sengage.webservice.exception.ItemNotFoundException;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.PlayerStatus;

public interface PlayerDataProvider {

	Player getPlayer(String gameId, String opaqueUserId) throws ItemNotFoundException;
	
	Player getPlayer(String gameId, String opaqueUserId, Class<? extends Player> clazz) throws ItemNotFoundException;
	
	public void createPlayer(Player player) throws ItemAlreadyExistsException;
	
	public void updateGamePlayer(Player player) throws ItemNotFoundException;
	
	// todo do pagination
	public List<? extends Player> listPlayers(String gameId, PlayerStatus status, Class<? extends Player> clazz);
	
	public List<? extends Player> listPlayersByScore(String gameId, Class<? extends Player> clazz);
	
	public int getNumberOfPlayersInGame(String gameId, PlayerStatus status);
	
}
