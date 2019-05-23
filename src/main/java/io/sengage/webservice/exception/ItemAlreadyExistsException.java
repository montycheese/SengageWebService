package io.sengage.webservice.exception;

public class ItemAlreadyExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    public ItemAlreadyExistsException(String message) {
        super(message);
    }

    public ItemAlreadyExistsException(Throwable throwable) {
        super(throwable);
    }

    public ItemAlreadyExistsException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
