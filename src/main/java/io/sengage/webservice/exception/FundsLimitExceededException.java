package io.sengage.webservice.exception;

public class FundsLimitExceededException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    public FundsLimitExceededException(String message) {
        super(message);
    }

    public FundsLimitExceededException(Throwable throwable) {
        super(throwable);
    }

    public FundsLimitExceededException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
