package com.voetsjoeba.nonogram.exception;

/**
 * Indicates that a row contains no incomplete runs.
 * 
 * @author Jeroen De Ridder
 */
public class NoIncompleteRunsException extends RuntimeException {

	public NoIncompleteRunsException() {
		super();
	}

	public NoIncompleteRunsException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoIncompleteRunsException(String message) {
		super(message);
	}

	public NoIncompleteRunsException(Throwable cause) {
		super(cause);
	}
	
}
