package com.voetsjoeba.nonogram.exception;

/**
 * Indicates that a puzzle appears to be unsolvable.
 * 
 * @author Jeroen De Ridder
 */
public class UnsolvablePuzzleException extends RuntimeException {

	public UnsolvablePuzzleException() {
		super();
	}

	public UnsolvablePuzzleException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnsolvablePuzzleException(String message) {
		super(message);
	}
	
	public UnsolvablePuzzleException(Throwable cause) {
		super(cause);
	}
	
}
