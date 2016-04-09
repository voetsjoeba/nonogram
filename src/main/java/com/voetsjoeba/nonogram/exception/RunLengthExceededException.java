package com.voetsjoeba.nonogram.exception;

import com.voetsjoeba.nonogram.structure.api.Run;

/**
 * Indicates that too many squares have been added to a {@link Run}.
 * 
 * @author Jeroen De Ridder
 */
public class RunLengthExceededException extends Exception {

	public RunLengthExceededException() {
		super();
	}

	public RunLengthExceededException(String message, Throwable cause) {
		super(message, cause);
	}

	public RunLengthExceededException(String message) {
		super(message);
	}

	public RunLengthExceededException(Throwable cause) {
		super(cause);
	}
	
}
