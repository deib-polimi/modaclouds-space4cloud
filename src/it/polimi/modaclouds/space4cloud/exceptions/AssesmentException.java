package it.polimi.modaclouds.space4cloud.exceptions;

import java.util.concurrent.ExecutionException;

public class AssesmentException extends ExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1825411732191236662L;
	
	public AssesmentException(String message) {
		super(message);
	}
	
	public AssesmentException(String message, Exception e) {
		super(message,e);
	}

}
