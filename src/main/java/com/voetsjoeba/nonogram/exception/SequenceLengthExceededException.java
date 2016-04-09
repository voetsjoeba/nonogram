package com.voetsjoeba.nonogram.exception;

/**
 * Indicates that a sequence length has been exceeded in some regard. For example, trying to assign a run of length 7
 * to the last square of a sequence may cause this exception, as the run length would exceed the sequence length.
 * 
 * @author Jeroen De Ridder
 */
public class SequenceLengthExceededException extends RuntimeException {
	
	public SequenceLengthExceededException() {
		super();
	}
	
	public SequenceLengthExceededException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SequenceLengthExceededException(String message) {
		super(message);
	}
	
	public SequenceLengthExceededException(Throwable cause) {
		super(cause);
	}
	
}
