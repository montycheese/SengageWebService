package io.sengage.webservice.exception;

public class ItemNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public ItemNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
