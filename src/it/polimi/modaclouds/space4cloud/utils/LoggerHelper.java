package it.polimi.modaclouds.space4cloud.utils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerHelper {
	
	public static Logger getLogger(Class clazz){
		Properties props = new Properties();
		try {						
			props.load(new URL(Constants.LOG4J_PROP_FILE).openStream());
			Path logFilePath = Paths.get(Constants.getInstance().ABSOLUTE_WORKING_DIRECTORY,"space4cloud.log").toAbsolutePath();
			props.setProperty("log4j.appender.file.File", logFilePath.toString());
			PropertyConfigurator.configure(props);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return LoggerFactory.getLogger(clazz);
	}
	

}
