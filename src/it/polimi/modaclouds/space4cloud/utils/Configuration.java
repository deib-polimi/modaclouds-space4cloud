package it.polimi.modaclouds.space4cloud.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
	
	
	//Configuration constant among all runs
	public static final String WORKING_DIRECTORY = "space4cloud";
	public static final String FOLDER_PREFIX = "hour_";
	public static final String PERFORMANCE_RESULTS_FOLDER = "performance_results";
	public static final String LAUNCH_CONFIG = "launchConfig.launch";
	public static final String SOLUTION_FILE_NAME = "solution.xml";
	public static final String SOLUTION_CSV_FILE_NAME = "solution.csv";
	public static final String DEFAULT_DB_CONNECTION_FILE = "/config/DBConnection.properties";

	
	//Configuration for the current run of space4cloud	
	public static String PALLADIO_REPOSITORY_MODEL;
	public static String PALLADIO_SYSTEM_MODEL;
	public static String PALLADIO_ALLOCATION_MODEL;
	public static String PALLADIO_USAGE_MODEL;
	public static String PALLADIO_RESOURCE_MODEL;
	public static String USAGE_MODEL_EXTENSION;
	public static String RESOURCE_ENVIRONMENT_EXTENSION;
	public static String CONSTRAINTS;
	public static String PROJECT_BASE_FOLDER;
	public static String DB_CONNECTION_FILE;
	public static Operation FUNCTIONALITY;
	public static Solver SOLVER;
	public static String LINE_PROP_FILE;	
	public static int TABU_MEMORY_SIZE;
	public static int SCRUMBLE_ITERS;
	public static int FEASIBILITY_ITERS;
	public static double SCALE_IN_FACTOR;
	public static int SCALE_IN_ITERS;
	public static int SCALE_IN_CONV_ITERS;
	public static Policy SELECTION_POLICY;
	public static boolean RELAXED_INITIAL_SOLUTION;
	public static String SSH_USER_NAME;
	public static String SSH_PASSWORD;	
	
	
	//Operations
	public static enum Operation {
		Assessment, Optimization, Robustness, Exit;

		public static Operation getById(int id) {
			Operation[] values = Operation.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}

		public static int size() {
			return Operation.values().length;			
		}
			
	}
	
	//solvers
	public static enum Solver {
		LQNS, LINE;

		public static Operation getById(int id) {
			Operation[] values = Operation.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}

		public static int size() {
			return Operation.values().length;			
		}
			
	}

	//Selection Policies
	public static enum Policy {
		Random, First, Longest, Utilization;

		public static Operation getById(int id) {
			Operation[] values = Operation.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}

		public static int size() {
			return Operation.values().length;			
		}
			
	}
	
	public static void saveConfiguration(String filePath) throws IOException{
		FileOutputStream fos = new FileOutputStream(filePath);
		Properties prop = new Properties();
		prop.put("PALLADIO_REPOSITORY_MODEL", PALLADIO_REPOSITORY_MODEL);
		prop.put("PALLADIO_SYSTEM_MODEL", PALLADIO_SYSTEM_MODEL);
		prop.put("PALLADIO_ALLOCATION_MODEL", PALLADIO_ALLOCATION_MODEL);
		prop.put("PALLADIO_USAGE_MODEL", PALLADIO_USAGE_MODEL);
		prop.put("PALLADIO_RESOURCE_MODEL", PALLADIO_RESOURCE_MODEL);
		prop.put("USAGE_MODEL_EXTENSION", USAGE_MODEL_EXTENSION);
		prop.put("RESOURCE_ENVIRONMENT_EXTENSION", RESOURCE_ENVIRONMENT_EXTENSION);
		prop.put("CONSTRAINTS", CONSTRAINTS);
		prop.put("PROJECT_BASE_FOLDER", PROJECT_BASE_FOLDER);
		prop.put("DB_CONNECTION_FILE", DB_CONNECTION_FILE);
		prop.put("FUNCTIONALITY", FUNCTIONALITY.toString());
		prop.put("SOLVER", SOLVER.toString() );
		prop.put("LINE_PROP_FILE", LINE_PROP_FILE);		
		prop.put("TABU_MEMORY_SIZE", Integer.toString(TABU_MEMORY_SIZE));
		prop.put("SCRUMBLE_ITERS", Integer.toString(SCRUMBLE_ITERS));
		prop.put("FEASIBILITY_ITERS", Integer.toString(FEASIBILITY_ITERS));
		prop.put("SCALE_IN_CONV_ITERS", Integer.toString(SCALE_IN_CONV_ITERS));
		prop.put("SCALE_IN_FACTOR", Double.toString(SCALE_IN_FACTOR));
		prop.put("SCALE_IN_ITERS", Integer.toString(SCALE_IN_ITERS));
		prop.put("SELECTION_POLICY", SELECTION_POLICY.toString());
		prop.put("RELAXED_INITIAL_SOLUTION", Boolean.toString(RELAXED_INITIAL_SOLUTION));
		prop.put("SSH_USER_NAME", SSH_USER_NAME);
		prop.put("SSH_PASSWORD", SSH_PASSWORD);
		
		
		prop.store(fos, "SPACE4Clouds configuration properties");
		fos.flush();
	}
	
	public static void loadConfiguration(String filePath) throws IOException {
		Properties prop = new Properties();
		FileInputStream fis = new FileInputStream(filePath);
		prop.load(fis);
		PALLADIO_REPOSITORY_MODEL = prop.getProperty("PALLADIO_REPOSITORY_MODEL");
		PALLADIO_SYSTEM_MODEL = prop.getProperty("PALLADIO_SYSTEM_MODEL");
		PALLADIO_ALLOCATION_MODEL = prop.getProperty("PALLADIO_ALLOCATION_MODEL");
		PALLADIO_USAGE_MODEL = prop.getProperty("PALLADIO_USAGE_MODEL");
		PALLADIO_RESOURCE_MODEL = prop.getProperty("PALLADIO_RESOURCE_MODEL");
		USAGE_MODEL_EXTENSION = prop.getProperty("USAGE_MODEL_EXTENSION");
		RESOURCE_ENVIRONMENT_EXTENSION = prop.getProperty("RESOURCE_ENVIRONMENT_EXTENSION");
		CONSTRAINTS = prop.getProperty("CONSTRAINTS");
		PROJECT_BASE_FOLDER = prop.getProperty("PROJECT_BASE_FOLDER");
		DB_CONNECTION_FILE= prop.getProperty("DB_CONNECTION_FILE");
		FUNCTIONALITY = Operation.valueOf(prop.getProperty("FUNCTIONALITY"));
		SOLVER = Solver.valueOf(prop.getProperty("SOLVER"));
		LINE_PROP_FILE= prop.getProperty("LINE_PROP_FILE");		
		TABU_MEMORY_SIZE= Integer.parseInt(prop.getProperty("TABU_MEMORY_SIZE"));
		SCRUMBLE_ITERS= Integer.parseInt(prop.getProperty("SCRUMBLE_ITERS"));
		FEASIBILITY_ITERS= Integer.parseInt(prop.getProperty("FEASIBILITY_ITERS"));
		SCALE_IN_CONV_ITERS= Integer.parseInt(prop.getProperty("SCALE_IN_CONV_ITERS"));
		SCALE_IN_FACTOR= Double.parseDouble(prop.getProperty("SCALE_IN_FACTOR"));
		SCALE_IN_ITERS= Integer.parseInt(prop.getProperty("SCALE_IN_ITERS"));
		SELECTION_POLICY= Policy.valueOf(prop.getProperty("SELECTION_POLICY"));
		RELAXED_INITIAL_SOLUTION= Boolean.parseBoolean(prop.getProperty("RELAXED_INITIAL_SOLUTION"));
		SSH_PASSWORD = prop.getProperty("SSH_PASSWORD");
		SSH_USER_NAME = prop.getProperty("SSH_USER_NAME");
	}
	

}
