package it.polimi.modaclouds.space4cloud.exceptions;

import java.io.IOException;

public class InitalFolderCreationException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -443495976766436117L;

	public InitalFolderCreationException(Throwable cause) {
		super(cause);
	}
	
	public InitalFolderCreationException(String message, Throwable cause) {
		super(message, cause);
	}
	

}
