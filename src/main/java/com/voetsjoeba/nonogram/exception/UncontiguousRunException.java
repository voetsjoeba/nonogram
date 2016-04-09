package com.voetsjoeba.nonogram.exception;

import com.voetsjoeba.nonogram.structure.api.Run;

/**
 * Indicates that a {@link Run} is not contiguous.
 * 
 * @author Jeroen De Ridder
 */
public class UncontiguousRunException extends Exception {
	
	public UncontiguousRunException() {
		super();
	}
	
	public UncontiguousRunException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UncontiguousRunException(String message) {
		super(message);
	}
	
	public UncontiguousRunException(Throwable cause) {
		super(cause);
	}
	
}
