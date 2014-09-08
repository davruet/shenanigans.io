package io.shenanigans.server;

public class BatchProcessorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4523633690799759194L;

	public BatchProcessorException() {
	}

	public BatchProcessorException(String message) {
		super(message);
	}

	public BatchProcessorException(Throwable cause) {
		super(cause);
	}

	public BatchProcessorException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchProcessorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
