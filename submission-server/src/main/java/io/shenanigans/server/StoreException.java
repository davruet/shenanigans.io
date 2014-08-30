package io.shenanigans.server;

public class StoreException extends Exception {

	public StoreException() {
	}

	public StoreException(String message) {
		super(message);
	}

	public StoreException(Throwable cause) {
		super(cause);
	}

	public StoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public StoreException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
