package it.polimi.modaclouds.space4cloud.utils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerHelper {

	private static final String DEFAULT_LOG_FILE = "log4j.appender.file.File";
	private static final String LINEPERF_LOG_FILE = "log4j.appender.linePerf.File";

	public static Logger getLogger(Class<?> clazz) {

		Properties props = new Properties();
		// if the project has not been chosen provide a logger with the default
		// initialization
		try {
			if (Constants.LOG4J_PROP_FILE == null) {
				FileLocator.toFileURL(clazz
						.getResource("/log/log4j.properties"));
				props.load(clazz.getResource("/log/log4j.properties")
						.openStream());

			} else {

				props.load(new URL(Constants.LOG4J_PROP_FILE).openStream());
				Path logFilePath = Paths.get(
						Constants.getInstance().ABSOLUTE_WORKING_DIRECTORY,
						props.getProperty(DEFAULT_LOG_FILE)).toAbsolutePath();
				props.setProperty(DEFAULT_LOG_FILE, logFilePath.toString());
				Path lineLogFilePath = Paths.get(
						Constants.getInstance().ABSOLUTE_WORKING_DIRECTORY,
						props.getProperty(LINEPERF_LOG_FILE)).toAbsolutePath();
				props.setProperty(LINEPERF_LOG_FILE, lineLogFilePath.toString());

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertyConfigurator.configure(props);
		return LoggerFactory.getLogger(clazz);
	}

}
