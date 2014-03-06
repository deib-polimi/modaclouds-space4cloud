package it.polimi.modaclouds.space4cloud.db;

public class DataHandlerFactory {
	
	
	private static DataHandler instance = null;
	
	public static DataHandler getHandler(){
		if(instance == null)
 			instance = new DataHandler();
		return instance;
	}

}
