package it.polimi.modaclouds.space4cloud.exceptions;

import java.util.concurrent.ExecutionException;

public class ConstraintEvaluationException extends ExecutionException {

	public ConstraintEvaluationException(String message) {
		super(message);
	}
	
	public ConstraintEvaluationException(String message, Exception e) {
		super(message,e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5529975089219598445L;

}
