package io.sengage.webservice.twitch;

import io.sengage.webservice.model.Game;

public interface PlayerCompleteRequest {
	Game getGame();
	String getChannelId();
}
