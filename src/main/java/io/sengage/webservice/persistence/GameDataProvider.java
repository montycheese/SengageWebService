package io.sengage.webservice.persistence;

import java.util.Optional;

import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameItem;

public interface GameDataProvider {
	Optional<GameItem> getGame(String gameId);
	String createGame(GameItem gameItem);
	void updateGame(GameItem gameItem) throws ItemVersionMismatchException;
}
