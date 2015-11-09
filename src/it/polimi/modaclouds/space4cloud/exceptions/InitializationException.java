package it.polimi.modaclouds.space4cloud.exceptions;

import java.util.concurrent.ExecutionException;

public class InitializationException extends ExecutionException {

	public InitializationException(String string, Exception e) {
		super(string,e);
	}

	public InitializationException(String string) {
		super(string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2542099744541842340L;

}
