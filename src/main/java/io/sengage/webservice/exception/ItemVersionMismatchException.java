package io.sengage.webservice.exception;

public class ItemVersionMismatchException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    public ItemVersionMismatchException(String message) {
        super(message);
    }

    public ItemVersionMismatchException(Throwable throwable) {
        super(throwable);
    }

    public ItemVersionMismatchException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
