package it.polimi.modaclouds.space4cloud.optimization.constraints;

public class ConstraintHandlerFactory {
	
	private static ConstraintHandler handlerInstance;
	
	public static ConstraintHandler getConstraintHandler(){
		if(handlerInstance == null)
			handlerInstance = new ConstraintHandler();
		return handlerInstance;
	}
	
	public static void clearHandler(){
		handlerInstance = null;
	}

}
