package it.polimi.modaclouds.space4cloud.mainProgram;

import java.util.concurrent.ExecutionException;

public class OptimizationException extends ExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4756732121003323950L;

	public OptimizationException(String message) {
		super(message);
	}
	
	public OptimizationException(String message, Exception e) {
		super(message,e);
	}
	
}
