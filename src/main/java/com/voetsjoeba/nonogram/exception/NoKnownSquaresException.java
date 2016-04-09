package com.voetsjoeba.nonogram.exception;

/**
 * Indicates that a certain amount of known squares were expected, but none are present.
 * 
 * @author Jeroen De Ridder
 */
public class NoKnownSquaresException extends RuntimeException {
	
	public NoKnownSquaresException() {
		super();
	}
	
	public NoKnownSquaresException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NoKnownSquaresException(String message) {
		super(message);
	}
	
	public NoKnownSquaresException(Throwable cause) {
		super(cause);
	}
	
}
