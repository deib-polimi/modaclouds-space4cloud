package it.polimi.modaclouds.space4cloud.exceptions;

import java.util.concurrent.ExecutionException;

public class OptimizationException extends ExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4756732121003323950L;

	String phase=null;
	
	public OptimizationException(String message) {
		super(message);
	}
	
	public OptimizationException(String message, String phase) {
		super(message);
		message = "Optimization error occurred during phase "+phase+". "+message;
	}
	
	public OptimizationException(String message, Exception e) {
		super(message,e);
	}
	
	
	public OptimizationException(String message, String phase, Exception e) {
		super(message,e);
		message = "Optimization error occurred during phase "+phase+". "+message;
	}

}
