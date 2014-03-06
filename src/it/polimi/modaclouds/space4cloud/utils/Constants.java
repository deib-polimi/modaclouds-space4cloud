/*
 * 
 */
package it.polimi.modaclouds.space4cloud.utils;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;

import de.uka.ipd.sdq.pcmsolver.runconfig.MessageStrings;

// TODO: Auto-generated Javadoc
/**
 * Singleton class providing constants.
 * 
 * @author Davide Franceschelli
 * 
 */
public class Constants {

	public final String FOLDER_PREFIX = "hour_";

	/** The wdir. */
	private static String WORKING_DIR;
	
	
	/** The instance. */
	private static Constants instance;
	
	/** The full working directory. */
	public String ABSOLUTE_WORKING_DIRECTORY;
	
	/** The rel working directory. */
	public String REL_WORKING_DIRECTORY;
	
	/** The project path. */
	public String PROJECT_PATH;
	
	/** The project name. */
	public String PROJECT_NAME;
	
	/** The working directory. */
	public String WORKING_DIRECTORY;
	
	/** The solver name. */	
	public String SOLVER = MessageStrings.LQNS_SOLVER;
	
	/** The allocation models folder. */
	public String ALLOCATION_MODEL = null;
	
	/** The resource models folder. */
	public String RESOURCE_MODEL= null;
	
	/** The repository models */
	public String REPOSITORY_MODEL= null;
	
	/** The usage models folder. */
	public String USAGE_MODEL = null;
	
	/** The performance results folder. */
	public String PERFORMANCE_RESULTS_FOLDER = "performance_results"
			+ File.separator;
	
	/** The launch config name. */
	public String LAUNCH_CONFIG= "launchConfig.launch";
	
	/** The property resourceenvriroment. */
	public final String PROP_RESOURCE = "resource_environment";

	/** The property configuration file. */	
	public final String CONFIG_FILE_NAME = "config.properties";
	
	/** The palladio extension file. */	
	public  String EXTENSION_FILE = "extension.xml";

	public	String LINE_PROPERTIES_FILE = "LINE.properties";

	/**
	 * Instantiates a new constants.
	 */
	private Constants() {

		if (WORKING_DIR == null) {
			String s = "";
			try {
				s = ResourcesPlugin.getWorkspace().getRoot().getProjects()[0]
						.getLocation().toOSString();
			} catch (Exception e) {
				s = System.getProperty("user.dir");
			}
			PROJECT_PATH = s;
		} else
			PROJECT_PATH = WORKING_DIR;
		PROJECT_NAME = PROJECT_PATH.substring(PROJECT_PATH
				.lastIndexOf(File.separator) + 1);				
		WORKING_DIRECTORY = "space4cloud";							
		ABSOLUTE_WORKING_DIRECTORY = PROJECT_PATH + File.separator
				+ WORKING_DIRECTORY + File.separator;
		REL_WORKING_DIRECTORY = PROJECT_NAME + File.separator
				+ WORKING_DIRECTORY;
		System.out.println("Project Name: " + PROJECT_NAME);
		System.out.println("Project Path: " + PROJECT_PATH);
		System.out.println("Working Directory: " + WORKING_DIRECTORY);
		System.out.println("Full Working Directory: " + ABSOLUTE_WORKING_DIRECTORY);
		System.out.println("Relative Working Directory: "
				+ REL_WORKING_DIRECTORY);
	}

	/**
	 * Gets the single instance of Constants.
	 *
	 * @return single instance of Constants
	 */
	public static Constants getInstance() {
		if (instance == null)
			instance = new Constants();
		return instance;
	}

	/**
	 * Inits the.
	 *
	 * @param f the f
	 */
	public static void setWorkingDirectory(String directory) {
		WORKING_DIR = directory;
	}

	public static void clear() {
		instance = null;
	}

	

}
