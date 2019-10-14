package io.sengage.webservice.exception;

public class InsufficientFundsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(Throwable throwable) {
        super(throwable);
    }

    public InsufficientFundsException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
