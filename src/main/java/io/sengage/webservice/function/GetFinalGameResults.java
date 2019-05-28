package io.sengage.webservice.function;

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
import io.sengage.webservice.model.EndGameResult;
import io.sengage.webservice.model.Game;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.GetFinalGameResultsResponse;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;
import io.sengage.webservice.model.SingleStrokeEndGameResult;
import io.sengage.webservice.model.SingleStrokePlayer;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.model.Stroke;
import io.sengage.webservice.utils.GameToPlayerClassMapper;

public class GetFinalGameResults extends BaseLambda<ServerlessInput, ServerlessOutput>{

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
		
		//todo ensure player belongs to game.
		
		List<? extends Player> playerDatum = playerDataProvider
				.listPlayers(gameId, 
						PlayerStatus.COMPLETED,
						GameToPlayerClassMapper.get(gameItem.getGame()));
		
		
		GetFinalGameResultsResponse response = buildResponse(gameItem.getGame(), playerDatum);

		return ServerlessOutput.builder()
			.headers(getOutputHeaders())
			.statusCode(HttpStatus.SC_OK)
			.body(gson.toJson(response, GetFinalGameResultsResponse.class))
			.build();
	}
	
	private GetFinalGameResultsResponse buildResponse(Game game, List<? extends Player> playerDatum) {
		
		List<? extends EndGameResult> results;
		switch (game) {
		case SINGLE_STROKE:
			results = playerDatum.stream()
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
					))
			.collect(Collectors.toList());
			
			break;
		case FLAPPY_BIRD_BR:
		default:
			throw new IllegalArgumentException("Not supported: " + game);
		}
		
		return GetFinalGameResultsResponse.builder()
				.results(results)
				.build();
	}

}
