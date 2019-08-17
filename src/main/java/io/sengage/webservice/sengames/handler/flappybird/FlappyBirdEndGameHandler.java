package io.sengage.webservice.sengames.handler.flappybird;

import java.util.List;

import lombok.extern.log4j.Log4j2;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.sengames.handler.EndGameHandler;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;
import io.sengage.webservice.utils.Constants;
import io.sengage.webservice.utils.GameToPlayerClassMapper;

@Log4j2
public class FlappyBirdEndGameHandler extends EndGameHandler {
	private final TwitchClient twitchClient;
	private final StepFunctionTaskExecutor sfExecutor;
	private final PlayerDataProvider playerDataProvider;

	public FlappyBirdEndGameHandler(GameDataProvider gameDataProvider,
			PlayerDataProvider playerDataProvider,
			TwitchClient twitchClient,
			StepFunctionTaskExecutor sfExecutor) {
		super(gameDataProvider);
		this.twitchClient = twitchClient;
		this.sfExecutor = sfExecutor;
		this.playerDataProvider = playerDataProvider;
	}
	
	@Override
	protected void handleEndGame(GameItem gameItem) {		
		try {		
			twitchClient.notifyChannelGameEnded(gameItem);
			gameDataProvider.updateGame(gameItem);
			
			try {
				sendExtensionChatMessage(gameItem);
			} catch (Exception e) {
				log.warn("Failed to send extension chat message.", e);
				// It's OK to swallow this.
			}
			
		} catch (Exception e) {
			log.warn("Caught unexpected exception while ending game", e);
			gameItem.setGameStatus(GameStatus.ERROR_STATE);
			try {
				gameDataProvider.updateGame(gameItem);	
			} catch(Exception e2) {
				log.warn("FlappyBirdEndGameHandler(): Failed to persist moving game to error state", e2);
				throw new RuntimeException(e);
			}
		} finally {
			sfExecutor.cleanUpGameStateMachineResources(gameItem);
		}
	}
	
	// TODO: handle case where people can tie for #1
	private void sendExtensionChatMessage(GameItem gameItem) {
		List<? extends Player> players =
				playerDataProvider.listPlayersByScore(gameItem.getGameId(), GameToPlayerClassMapper.get(gameItem.getGame()));
		Player topPlayer = players.get(0);
		String message = null;
		if (topPlayer.getUserName() == null) {
			message = String.format(Constants.SINGLE_WINNER_FLAPPY_BIRD_NO_ID_MESSAGE_FORMAT, topPlayer.getScore());
		}
		else {
			message = String.format(Constants.SINGLE_WINNER_FLAPPY_BIRD_MESSAGE_FORMAT, topPlayer.getUserName(), topPlayer.getScore());	
		}
		log.debug(String.format("FlappyBirdEndGameHandler(): sending message %s to channel %s", message, gameItem.getChannelId()));
		twitchClient.sendExtensionChatMessage(gameItem.getChannelId(), message);
		
	}
}
