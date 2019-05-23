package io.sengage.webservice.persistence;

import java.util.Optional;

import io.sengage.webservice.exception.ItemAlreadyExistsException;
import io.sengage.webservice.model.Player;

public interface PlayerDataProvider {

	Optional<Player> getPlayer(String gameId, String opaqueUserId);
	
	public void createPlayer(Player player) throws ItemAlreadyExistsException;
	
}
