package io.sengage.webservice.sengames.handler.singlestroke;


import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.sengames.handler.EndGameHandler;
import io.sengage.webservice.sf.StepFunctionTaskExecutor;
import io.sengage.webservice.twitch.TwitchClient;

public class SingleStrokeEndGameHandler extends EndGameHandler {

	private final TwitchClient twitchClient;
	private final StepFunctionTaskExecutor sfExecutor;

	public SingleStrokeEndGameHandler(GameDataProvider gameDataProvider,
			TwitchClient twitchClient,
			StepFunctionTaskExecutor sfExecutor) {
		super(gameDataProvider);
		this.twitchClient = twitchClient;
		this.sfExecutor = sfExecutor;
	}
	
	
	@Override
	protected void handleEndGame(GameItem gameItem) {		
		try {		
			twitchClient.notifyChannelGameEnded(gameItem);
			
			try {
				gameDataProvider.updateGame(gameItem);
			} catch (ItemVersionMismatchException e) {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			e.printStackTrace();
			gameItem.setGameStatus(GameStatus.ERROR_STATE);
			try {
				gameDataProvider.updateGame(gameItem);	
			} catch(Exception e2) {
				e2.printStackTrace();
				System.out.println("SingleStrokeEndGameHandler(): Failed to persist moving game to error state");
				throw e;
			}
		} finally {
			sfExecutor.cleanUpGameStateMachineResources(gameItem);
		}
	}
	
}
