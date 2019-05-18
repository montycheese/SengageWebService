package io.sengage.webservice.exception;

public class UnauthorizedActionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    public UnauthorizedActionException(String message) {
        super(message);
    }

    public UnauthorizedActionException(Throwable throwable) {
        super(throwable);
    }

    public UnauthorizedActionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
