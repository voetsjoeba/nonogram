package com.voetsjoeba.nonogram.exception;

/**
 * Indicates that the run configuration of a particular row is invalid (e.g. if there are insufficient squares).
 * 
 * @author Jeroen De Ridder
 */
public class InvalidRunConfigurationException extends RuntimeException {
	
	public InvalidRunConfigurationException() {
		super();
	}
	
	public InvalidRunConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvalidRunConfigurationException(String message) {
		super(message);
	}
	
	public InvalidRunConfigurationException(Throwable cause) {
		super(cause);
	}
	
}
