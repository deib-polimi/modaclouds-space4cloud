package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.space4cloud.exceptions.InitializationException;


public class ConstraintLoadingException extends InitializationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5034037830085147128L;
	
	public ConstraintLoadingException(String message) {
		super(message);
	}
	
	public ConstraintLoadingException(String message, Exception e) {
		super(message,e);
	}

}
