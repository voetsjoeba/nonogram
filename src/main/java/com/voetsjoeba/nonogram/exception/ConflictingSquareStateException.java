package com.voetsjoeba.nonogram.exception;

import com.voetsjoeba.nonogram.structure.StandardSquare;

/**
 * Indicates that a single {@link StandardSquare} has been determined to have two or more different states at the same time.
 * 
 * @author Jeroen De Ridder
 */
public class ConflictingSquareStateException extends Exception {

	public ConflictingSquareStateException() {
		super();
	}

	public ConflictingSquareStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConflictingSquareStateException(String message) {
		super(message);
	}

	public ConflictingSquareStateException(Throwable cause) {
		super(cause);
	}
	
}
