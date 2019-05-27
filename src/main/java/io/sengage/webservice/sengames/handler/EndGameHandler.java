package io.sengage.webservice.sengames.handler;

public interface EndGameHandler {

	/**
	 * Called to gracefully end a game once all players have made their moves.
	 */
	public void handleEndGame(String gameId);
	
	/**
	 * Called to gracefully end an ongoing game if the time is exhausted and not all players
	 * are finished.
	 */
	public void handleGameTimeout(String gameId);

}
