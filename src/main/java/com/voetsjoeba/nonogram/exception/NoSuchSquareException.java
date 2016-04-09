package com.voetsjoeba.nonogram.exception;

/**
 * Indicates that no such square exists within a particular structure.
 * 
 * @author Jeroen De Ridder
 */
public class NoSuchSquareException extends Exception {
	
	public NoSuchSquareException() {
		super();
	}
	
	public NoSuchSquareException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NoSuchSquareException(String message) {
		super(message);
	}
	
	public NoSuchSquareException(Throwable cause) {
		super(cause);
	}
	
}
