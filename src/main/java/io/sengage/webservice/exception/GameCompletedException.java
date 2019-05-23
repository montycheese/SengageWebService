package io.sengage.webservice.exception;

public class GameCompletedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    public GameCompletedException(String message) {
        super(message);
    }

    public GameCompletedException(Throwable throwable) {
        super(throwable);
    }

    public GameCompletedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
