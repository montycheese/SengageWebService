package io.sengage.webservice.exception;

public class GameInProgressException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    public GameInProgressException(String message) {
        super(message);
    }

    public GameInProgressException(Throwable throwable) {
        super(throwable);
    }

    public GameInProgressException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
