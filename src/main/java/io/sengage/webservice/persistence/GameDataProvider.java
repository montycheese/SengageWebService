package io.sengage.webservice.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;

public interface GameDataProvider {
	Optional<GameItem> getGame(String gameId);
	String createGame(GameItem gameItem);
	void updateGame(GameItem gameItem) throws ItemVersionMismatchException;
	int getNumberOfGamesWithStatuses(String channelId, Set<GameStatus> status, Instant timeWindow);
}
