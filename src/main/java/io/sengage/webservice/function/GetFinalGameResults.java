package io.sengage.webservice.function;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;

import io.sengage.webservice.auth.AuthorizationHelper;
import io.sengage.webservice.dagger.DaggerExtensionComponent;
import io.sengage.webservice.dagger.ExtensionComponent;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.GetFinalGameResultsResponse;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.flappybird.FlappyBirdEndGameResult;
import io.sengage.webservice.model.flappybird.FlappyBirdFinalGameResults;
import io.sengage.webservice.model.flappybird.FlappyBirdPlayer;
import io.sengage.webservice.model.singlestroke.SingleStrokeEndGameResult;
import io.sengage.webservice.model.singlestroke.SingleStrokeFinalGameResults;
import io.sengage.webservice.model.singlestroke.SingleStrokePlayer;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.model.flappybird.FlightResult;
import io.sengage.webservice.sengames.model.singlestroke.Stroke;
import io.sengage.webservice.sengames.model.singlestroke.StrokeType;
import io.sengage.webservice.utils.GameToPlayerClassMapper;

public class GetFinalGameResults extends BaseLambda<ServerlessInput, ServerlessOutput>{

	
	private static final double MULTIPLIER = 250.0;
	
	@Inject
	Gson gson;
	
	@Inject
	GameDataProvider gameDataProvider;
	
	@Inject
	PlayerDataProvider playerDataProvider;
	
	@Inject
	AuthorizationHelper authHelper;
	
	private LambdaLogger logger;
	
	public GetFinalGameResults() {
		ExtensionComponent component = DaggerExtensionComponent.create();
		component.injectGetFinalGameResults(this);
	}
	
	
	@Override
	public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
		logger = context.getLogger();
		logger.log("GetFinalGameResults() Request: " + serverlessInput);
		
		authHelper.authenticateRequestAndVerifyToken(parseAuthTokenFromHeaders(serverlessInput.getHeaders()));
		
		String gameId = getPathParameter(serverlessInput.getPath(), PathParameter.GAME_ID);
		
		GameItem gameItem = gameDataProvider.getGame(gameId).get();
		
		if (gameItem.getGameStatus().isBefore(GameStatus.COMPLETED)) {
			throw new IllegalStateException("Game is not yet complete: " + gameId);
		}
		
		//TODO: ensure player belongs to game.
		
		List<? extends Player> playerDatum = playerDataProvider.listPlayersByScore(gameId, GameToPlayerClassMapper.get(gameItem.getGame()));
		
		GetFinalGameResultsResponse response = buildResponse(gameItem.getGame(), playerDatum);

		return ServerlessOutput.builder()
			.headers(getOutputHeaders())
			.statusCode(HttpStatus.SC_OK)
			.body(gson.toJson(response, response.getClass()))
			.build();
	}
	
	private GetFinalGameResultsResponse buildResponse(Game game, List<? extends Player> playerDatum) {
		
		GetFinalGameResultsResponse response;
		switch (game) {
		case SINGLE_STROKE:
			List<SingleStrokeEndGameResult> results = playerDatum.stream()
			.map(data -> (SingleStrokePlayer) data)
			.map(ssPlayerData -> new SingleStrokeEndGameResult(
					Stroke.builder()
					.colorHex(ssPlayerData.getColorHex())
					.opaqueId(ssPlayerData.getOpaqueId())
					.pointA(ssPlayerData.getPointA())
					.pointB(ssPlayerData.getPointB())
					.strokeType(ssPlayerData.getStrokeType())
					.userName(ssPlayerData.getUserName())
					.width(ssPlayerData.getWidth())
					.build()
					)
			)
			.collect(Collectors.toList());
			double distanceCovered = calculateDistanceCovered(results);
			response = new SingleStrokeFinalGameResults(results, distanceCovered, playerDatum.size());
			break;
		case FLAPPY_BIRD_BR:
			int distanceCoveredFB = 0;
			List<FlappyBirdEndGameResult> fbResults = new ArrayList<FlappyBirdEndGameResult>(playerDatum.size());
					
			for (Player playerData: playerDatum) {
				FlappyBirdPlayer fbPlayerData = (FlappyBirdPlayer) playerData;
				distanceCoveredFB += fbPlayerData.getDistance();
				fbResults.add(new FlappyBirdEndGameResult(
						FlightResult.builder()
							.attempt(fbPlayerData.getAttempt())
							.character(fbPlayerData.getCharacter())
							.distance(fbPlayerData.getDistance())
							.userName(fbPlayerData.getUserName())
							.opaqueId(fbPlayerData.getOpaqueId())
							.build()
				));
			}
			response = new FlappyBirdFinalGameResults(fbResults, distanceCoveredFB, playerDatum.size());
			break;
		default:
			throw new IllegalArgumentException("Not supported: " + game);
		}
		return response;
	}
	
	private double calculateDistanceCovered(List<SingleStrokeEndGameResult> results) {
		double distance = 0.0;
		
		for (SingleStrokeEndGameResult result : results) {
			Stroke stroke = result.getStroke();
			double deltaX = stroke.getPointA().get(0) - stroke.getPointB().get(0);
			double deltaY = stroke.getPointA().get(1) - stroke.getPointB().get(1);
			
			if (StrokeType.LINE.equals(stroke.getStrokeType())) {
				distance += Math.floor(
						Math.sqrt(
							Math.pow(deltaX, 2) + Math.pow(deltaY, 2)
								) * MULTIPLIER);
			} else if (StrokeType.CIRCLE.equals(stroke.getStrokeType())) {
				distance += Math.floor(
						Math.sqrt(
							Math.pow((deltaX / 2), 2) + Math.pow((deltaY / 2), 2)
						) * 2 * Math.PI * MULTIPLIER
				);
			}
		}
		
		return distance;
	}

}
