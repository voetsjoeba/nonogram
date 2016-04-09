package com.voetsjoeba.nonogram.exception;

import com.voetsjoeba.nonogram.structure.StandardSquare;

/**
 * Indicates that a single {@link StandardSquare} has been determined to have two or more different runs at the same time.
 * 
 * @author Jeroen De Ridder
 */
public class ConflictingSquareRunException extends Exception {
	
	public ConflictingSquareRunException() {
		super();
	}

	public ConflictingSquareRunException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConflictingSquareRunException(String message) {
		super(message);
	}

	public ConflictingSquareRunException(Throwable cause) {
		super(cause);
	}

}
