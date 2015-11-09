package it.polimi.modaclouds.space4cloud.utils;

import it.polimi.modaclouds.space4cloud.exceptions.InitializationException;

public class ResourceEnvironmentLoadingException extends InitializationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8411035179724271687L;


	public ResourceEnvironmentLoadingException(String message) {
		super(message);
	}


	public ResourceEnvironmentLoadingException(String message, Exception e) {
		super(message,e);
	}

}
