package com.voetsjoeba.nonogram.exception;

public class InconsistentDecompositionException extends RuntimeException {
	
	public InconsistentDecompositionException() {
		super();
	}
	
	public InconsistentDecompositionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InconsistentDecompositionException(String message) {
		super(message);
	}
	
	public InconsistentDecompositionException(Throwable cause) {
		super(cause);
	}
	
}
