package it.polimi.modaclouds.space4cloud.db;

public class DatabaseConnectionFailureExteption extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5669445080977675482L;
	
	public DatabaseConnectionFailureExteption() { super(); }
	  public DatabaseConnectionFailureExteption(String message) { super(message); }
	  public DatabaseConnectionFailureExteption(String message, Throwable cause) { super(message, cause); }
	  public DatabaseConnectionFailureExteption(Throwable cause) { super(cause); }

}
