/*******************************************************************************
 * Copyright 2014 Giovanni Paolo Gibilisco
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

	/** Label for the Optimization Functionality*/
	public static final String OPTIMIZATION = "Optimization";

	/** Label for the Assesment Functionality*/
	public static final String ASSESSMENT = "Assessment";
	
	/** Label for the Robustness Functionality*/
	public static final String ROBUSTNESS = "Robustness";
	
	/** Label for the Cancel Functionality*/
	public static final String CANCEL = "Cancel";

	public static boolean CONFIGURATIONGUI = false;
	
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
	public  String RESOURCE_ENV_EXT_FILE = "extension.xml";
	
	public  String USAGE_MODEL_EXT_FILE = "usagemodel.xml";

	public	String LINE_PROPERTIES_FILE = "LINE.properties";

	public static String LOG4J_PROP_FILE;

	/**
	 * Instantiates a new constants.
	 */
	private Constants() {
		changeWorkingDirectory("space4cloud");

//		if (WORKING_DIR == null) {
//			String s = "";
//
//			s = ResourcesPlugin.getWorkspace().getRoot().getProjects()[0]
//					.getLocation().toOSString();
//
//
//			PROJECT_PATH = s;
//		} else
//			PROJECT_PATH = WORKING_DIR;
//		PROJECT_NAME = PROJECT_PATH.substring(PROJECT_PATH
//				.lastIndexOf(File.separator) + 1);				
//		WORKING_DIRECTORY = "space4cloud";							
//		ABSOLUTE_WORKING_DIRECTORY = PROJECT_PATH + File.separator
//				+ WORKING_DIRECTORY + File.separator;
//		REL_WORKING_DIRECTORY = PROJECT_NAME + File.separator
//				+ WORKING_DIRECTORY;
//
//		LOG4J_PROP_FILE = this.getClass().getResource("/log/log4j.properties").toString();
//		System.out.println("Project Name: " + PROJECT_NAME);
//		System.out.println("Project Path: " + PROJECT_PATH);
//		System.out.println("Working Directory: " + WORKING_DIRECTORY);
//		System.out.println("Full Working Directory: " + ABSOLUTE_WORKING_DIRECTORY);
//		System.out.println("Relative Working Directory: "
//				+ REL_WORKING_DIRECTORY);
	}
	
	public void changeWorkingDirectory(String workingDirectory) {
		if (WORKING_DIR == null) {
			String s = "";

			s = ResourcesPlugin.getWorkspace().getRoot().getProjects()[0]
					.getLocation().toOSString();


			PROJECT_PATH = s;
		} else
			PROJECT_PATH = WORKING_DIR;
		PROJECT_NAME = PROJECT_PATH.substring(PROJECT_PATH
				.lastIndexOf(File.separator) + 1);				
		WORKING_DIRECTORY = workingDirectory;							
		ABSOLUTE_WORKING_DIRECTORY = PROJECT_PATH + File.separator
				+ WORKING_DIRECTORY + File.separator;
		REL_WORKING_DIRECTORY = PROJECT_NAME + File.separator
				+ WORKING_DIRECTORY;

		LOG4J_PROP_FILE = this.getClass().getResource("/log/log4j.properties").toString();
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
