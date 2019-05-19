package io.sengage.webservice.sengames.model;

import io.sengage.webservice.model.Game;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class GameToWaitForPlayersToJoinDurationMapper {

	// in milliseconds
	private static final Map<Game, Integer> gameToWaitForPlayersToJoinDuration;
	
	static {
		gameToWaitForPlayersToJoinDuration = ImmutableMap.<Game, Integer>builder()
				.put(Game.SINGLE_STROKE, 5000)
				.put(Game.FLAPPY_BIRD_BR, 30000)
				.build();
	}
	
	public static int get(Game game) {
		return gameToWaitForPlayersToJoinDuration.get(game);
	}
	
}
