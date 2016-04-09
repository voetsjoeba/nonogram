package com.voetsjoeba.nonogram.exception;

/**
 * Indicates that no such sequence exists within a particular structure.
 * 
 * @author Jeroen De Ridder
 */
public class NoSuchSequenceException extends Exception {

	public NoSuchSequenceException() {
		super();
	}

	public NoSuchSequenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchSequenceException(String message) {
		super(message);
	}

	public NoSuchSequenceException(Throwable cause) {
		super(cause);
	}
	
}
