package io.sengage.webservice.cache;

import io.sengage.webservice.model.GameItem;

/**
 * High throughput Cache for game details
 *
 */
public interface GameCache {
	void putGameDetails(String channelId, GameItem game);
	void clearGameDetails(String channelId);
	
	/**
	 * Return details for joining an active game
	 * @param channelId
	 * @return serialized details for joining a game, empty string otherwise.
	 */
	Object getGameDetails(String channelId);
}
