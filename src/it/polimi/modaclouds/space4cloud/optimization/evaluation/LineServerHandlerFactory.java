package it.polimi.modaclouds.space4cloud.optimization.evaluation;


public class LineServerHandlerFactory {
	
	private static LineServerHandler handler = null;
	
	public static LineServerHandler getHandler(){
		if(handler==null){
			handler = new LineServerHandler();
		}
		return handler;
	}
	
	public static void clearHandler(){
		handler = null;
	}

}
