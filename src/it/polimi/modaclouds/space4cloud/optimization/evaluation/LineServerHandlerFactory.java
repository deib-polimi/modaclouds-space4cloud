package it.polimi.modaclouds.space4cloud.optimization.evaluation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class LineServerHandlerFactory {
	
	private static LineServerHandler handler = null;
	private static String linePropFilePath = null;
	private static File linePropFile = null;
	private static final Logger logger = LoggerFactory.getLogger(LineServerHandlerFactory.class);
	
	public static LineServerHandler getHandler(){
		if(handler==null){
			handler = new LineServerHandler(linePropFile);
		}
		return handler;
	}
	
	public static void setLinePropFilePath(String propFilePath){
		linePropFilePath = propFilePath;
		URL linePropFileURL = null;		
		try {
			//try to load it from workspace
			linePropFileURL = FileLocator.toFileURL(new URL(linePropFilePath));
			linePropFile = new File(linePropFileURL.getFile());
		} catch (MalformedURLException e2) {
			// if it is not a workspace url but a path to a file
			linePropFile = new File(linePropFilePath);
		} catch (IOException e) {
			logger.error("Error loading line handler",e);
		}
	}

}
