package it.polimi.modaclouds.space4cloud.exceptions;

import java.util.concurrent.ExecutionException;

public class RobustnessException extends ExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1075788425136350734L;

	
	public RobustnessException(String message) {
		super(message);
	}
	
	public RobustnessException(String message, Exception e) {
		super(message,e);
	}
}
