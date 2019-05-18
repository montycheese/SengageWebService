package io.sengage.webservice.persistence;

import io.sengage.webservice.model.GameItem;

public interface GameDataProvider {
	String createGame(GameItem gameItem);
	void updateGame(GameItem gameItem);
}
