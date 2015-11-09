package it.polimi.modaclouds.space4cloud.utils;

public class PalladioRunException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6558354052273204623L;
	
	
	public PalladioRunException(Throwable cause) {
		super(cause);
	}

	public PalladioRunException(String message, Throwable cause) {
		super(message,cause);
	}
}
