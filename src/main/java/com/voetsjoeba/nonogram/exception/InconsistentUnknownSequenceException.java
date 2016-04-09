package com.voetsjoeba.nonogram.exception;

public class InconsistentUnknownSequenceException extends RuntimeException {
	
	public InconsistentUnknownSequenceException() {
		super();
	}
	
	public InconsistentUnknownSequenceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InconsistentUnknownSequenceException(String message) {
		super(message);
	}
	
	public InconsistentUnknownSequenceException(Throwable cause) {
		super(cause);
	}
	
}
