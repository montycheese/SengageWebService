package io.sengage.webservice.utils;

import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.flappybird.FlappyBirdPlayer;
import io.sengage.webservice.model.singlestroke.SingleStrokePlayer;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class GameToPlayerClassMapper {
	private static final Map<Game, Class<? extends Player>> gameToPlayerMap;
	
	static {
		gameToPlayerMap = ImmutableMap.of(
				Game.SINGLE_STROKE, SingleStrokePlayer.class,
				Game.FLAPPY_BIRD_BR, FlappyBirdPlayer.class);
	}
	
	public static Class<? extends Player> get(Game game) {
		return gameToPlayerMap.get(game);
	}
	
}
