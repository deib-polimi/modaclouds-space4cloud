package it.polimi.modaclouds.space4cloud.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
	
	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
	
	//Configuration constant among all runs
	public static /*final*/ String WORKING_DIRECTORY = "space4cloud";
	public static final String FOLDER_PREFIX = "hour_";
	public static final String PERFORMANCE_RESULTS_FOLDER = "performance_results";
	public static final String LAUNCH_CONFIG = "launchConfig.launch";
	public static final String SOLUTION_FILE_NAME = "solution";
	public static final String SOLUTION_FILE_EXTENSION= ".xml";
	public static final String SOLUTION_LIGHT_FILE_NAME = "solution_light";
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
	public static Operation FUNCTIONALITY = Operation.getById(0);
	public static Solver SOLVER = Solver.getById(0);
	public static String LINE_PROP_FILE = "";	
	public static int TABU_MEMORY_SIZE = 10;
	public static int SCRUMBLE_ITERS = 25;
	public static int FEASIBILITY_ITERS = 10;
	public static double SCALE_IN_FACTOR = 2.0;
	public static int SCALE_IN_ITERS = 15;
	public static int SCALE_IN_CONV_ITERS = 5;
	public static Policy SELECTION_POLICY = Policy.getById(0);
	public static boolean RELAXED_INITIAL_SOLUTION = false;
	public static String SSH_USER_NAME = "";
	public static String SSH_PASSWORD = "";
	public static String SSH_HOST = "specclient1.dei.polimi.it";
	public static int RANDOM_SEED = 1;
	
	// For the robustness test:
	public static int ROBUSTNESS_PEAK_FROM 	= 100;
	public static int ROBUSTNESS_PEAK_TO 	= 10000;
	public static int ROBUSTNESS_STEP_SIZE 	= 300;
	public static int ROBUSTNESS_ATTEMPTS 	= 5;
	
	public static boolean REDISTRIBUTE_WORKLOAD = false;
	
	
	// For the Private Cloud part:
	public static boolean USE_PRIVATE_CLOUD = false;
	public static int PRIVATE_CPUS 			= 10; 			// number
	public static double PRIVATE_CPUPOWER 	= 3.0 * 1000; 	// MHz
	public static int PRIVATE_RAM 			= 16 * 1024; 	// MB
	public static int PRIVATE_STORAGE		= 1 * 1024; 	// GB
	
	//Operations
	public static enum Operation {
		Assessment, Optimization, Robustness;

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

		public static Solver getById(int id) {
			Solver[] values = Solver.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}

		public static int size() {
			return Solver.values().length;			
		}
			
	}

	//Selection Policies
	public static enum Policy {
		Random, First, Longest, Utilization;

		public static Policy getById(int id) {
			Policy[] values = Policy.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}

		public static int size() {
			return Policy.values().length;			
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
		prop.put("SSH_HOST", SSH_HOST);
		prop.put("SSH_USER_NAME", SSH_USER_NAME);
		prop.put("SSH_PASSWORD", SSH_PASSWORD);
		prop.put("RANDOM_SEED", Integer.toString(RANDOM_SEED));
		
		prop.put("ROBUSTNESS_PEAK_FROM", Integer.toString(ROBUSTNESS_PEAK_FROM));
		prop.put("ROBUSTNESS_PEAK_TO", Integer.toString(ROBUSTNESS_PEAK_TO));
		prop.put("ROBUSTNESS_STEP_SIZE", Integer.toString(ROBUSTNESS_STEP_SIZE));
		prop.put("ROBUSTNESS_ATTEMPTS", Integer.toString(ROBUSTNESS_ATTEMPTS));
		
		prop.put("REDISTRIBUTE_WORKLOAD", Boolean.toString(REDISTRIBUTE_WORKLOAD));
		
		prop.put("USE_PRIVATE_CLOUD", Boolean.toString(USE_PRIVATE_CLOUD));
		prop.put("PRIVATE_CPUS", Integer.toString(PRIVATE_CPUS));
		prop.put("PRIVATE_CPUPOWER", Double.toString(PRIVATE_CPUPOWER));
		prop.put("PRIVATE_RAM", Integer.toString(PRIVATE_RAM));
		prop.put("PRIVATE_STORAGE", Integer.toString(PRIVATE_STORAGE));
		
		prop.store(fos, "SPACE4Clouds configuration properties");
		fos.flush();
	}
	
//	public static void loadConfiguration(String filePath) throws IOException {
//		Properties prop = new Properties();
//		FileInputStream fis = new FileInputStream(filePath);
//		prop.load(fis);
//		PALLADIO_REPOSITORY_MODEL = prop.getProperty("PALLADIO_REPOSITORY_MODEL");
//		PALLADIO_SYSTEM_MODEL = prop.getProperty("PALLADIO_SYSTEM_MODEL");
//		PALLADIO_ALLOCATION_MODEL = prop.getProperty("PALLADIO_ALLOCATION_MODEL");
//		PALLADIO_USAGE_MODEL = prop.getProperty("PALLADIO_USAGE_MODEL");
//		PALLADIO_RESOURCE_MODEL = prop.getProperty("PALLADIO_RESOURCE_MODEL");
//		USAGE_MODEL_EXTENSION = prop.getProperty("USAGE_MODEL_EXTENSION");
//		RESOURCE_ENVIRONMENT_EXTENSION = prop.getProperty("RESOURCE_ENVIRONMENT_EXTENSION");
//		CONSTRAINTS = prop.getProperty("CONSTRAINTS");
//		PROJECT_BASE_FOLDER = prop.getProperty("PROJECT_BASE_FOLDER");
//		DB_CONNECTION_FILE= prop.getProperty("DB_CONNECTION_FILE");
//		FUNCTIONALITY = Operation.valueOf(prop.getProperty("FUNCTIONALITY"));
//		SOLVER = Solver.valueOf(prop.getProperty("SOLVER"));
//		LINE_PROP_FILE= prop.getProperty("LINE_PROP_FILE");		
//		TABU_MEMORY_SIZE= Integer.parseInt(prop.getProperty("TABU_MEMORY_SIZE"));
//		SCRUMBLE_ITERS= Integer.parseInt(prop.getProperty("SCRUMBLE_ITERS"));
//		FEASIBILITY_ITERS= Integer.parseInt(prop.getProperty("FEASIBILITY_ITERS"));
//		SCALE_IN_CONV_ITERS= Integer.parseInt(prop.getProperty("SCALE_IN_CONV_ITERS"));
//		SCALE_IN_FACTOR= Double.parseDouble(prop.getProperty("SCALE_IN_FACTOR"));
//		SCALE_IN_ITERS= Integer.parseInt(prop.getProperty("SCALE_IN_ITERS"));
//		SELECTION_POLICY= Policy.valueOf(prop.getProperty("SELECTION_POLICY"));
//		RELAXED_INITIAL_SOLUTION= Boolean.parseBoolean(prop.getProperty("RELAXED_INITIAL_SOLUTION"));
//		SSH_PASSWORD = prop.getProperty("SSH_PASSWORD");
//		SSH_USER_NAME = prop.getProperty("SSH_USER_NAME");
//		RANDOM_SEED = Integer.parseInt(prop.getProperty("RANDOM_SEED"));
//		
//		ROBUSTNESS_PEAK_FROM = Integer.parseInt(prop.getProperty("ROBUSTNESS_PEAK_FROM"));
//		ROBUSTNESS_PEAK_TO = Integer.parseInt(prop.getProperty("ROBUSTNESS_PEAK_TO"));
//		ROBUSTNESS_STEP_SIZE = Integer.parseInt(prop.getProperty("ROBUSTNESS_STEP_SIZE"));
//		ROBUSTNESS_ATTEMPTS = Integer.parseInt(prop.getProperty("ROBUSTNESS_ATTEMPTS"));
//	}
	
	public static void loadConfiguration(String filePath) throws IOException {
		Properties prop = new Properties();
		FileInputStream fis = new FileInputStream(filePath);
		prop.load(fis);
		PALLADIO_REPOSITORY_MODEL = prop.getProperty("PALLADIO_REPOSITORY_MODEL", PALLADIO_REPOSITORY_MODEL);
		PALLADIO_SYSTEM_MODEL = prop.getProperty("PALLADIO_SYSTEM_MODEL", PALLADIO_SYSTEM_MODEL);
		PALLADIO_ALLOCATION_MODEL = prop.getProperty("PALLADIO_ALLOCATION_MODEL", PALLADIO_ALLOCATION_MODEL);
		PALLADIO_USAGE_MODEL = prop.getProperty("PALLADIO_USAGE_MODEL", PALLADIO_USAGE_MODEL);
		PALLADIO_RESOURCE_MODEL = prop.getProperty("PALLADIO_RESOURCE_MODEL", PALLADIO_RESOURCE_MODEL);
		USAGE_MODEL_EXTENSION = prop.getProperty("USAGE_MODEL_EXTENSION", USAGE_MODEL_EXTENSION);
		RESOURCE_ENVIRONMENT_EXTENSION = prop.getProperty("RESOURCE_ENVIRONMENT_EXTENSION", RESOURCE_ENVIRONMENT_EXTENSION);
		CONSTRAINTS = prop.getProperty("CONSTRAINTS", CONSTRAINTS);
		PROJECT_BASE_FOLDER = prop.getProperty("PROJECT_BASE_FOLDER", PROJECT_BASE_FOLDER);
		DB_CONNECTION_FILE= prop.getProperty("DB_CONNECTION_FILE", DB_CONNECTION_FILE);
		FUNCTIONALITY = Operation.valueOf(prop.getProperty("FUNCTIONALITY", FUNCTIONALITY.toString()));
		SOLVER = Solver.valueOf(prop.getProperty("SOLVER", SOLVER.toString()));
		LINE_PROP_FILE= prop.getProperty("LINE_PROP_FILE", LINE_PROP_FILE);		
		TABU_MEMORY_SIZE= Integer.parseInt(prop.getProperty("TABU_MEMORY_SIZE", String.valueOf(TABU_MEMORY_SIZE)));
		SCRUMBLE_ITERS= Integer.parseInt(prop.getProperty("SCRUMBLE_ITERS", String.valueOf(SCRUMBLE_ITERS)));
		FEASIBILITY_ITERS= Integer.parseInt(prop.getProperty("FEASIBILITY_ITERS", String.valueOf(FEASIBILITY_ITERS)));
		SCALE_IN_CONV_ITERS= Integer.parseInt(prop.getProperty("SCALE_IN_CONV_ITERS", String.valueOf(SCALE_IN_CONV_ITERS)));
		SCALE_IN_FACTOR= Double.parseDouble(prop.getProperty("SCALE_IN_FACTOR", String.valueOf(SCALE_IN_FACTOR)));
		SCALE_IN_ITERS= Integer.parseInt(prop.getProperty("SCALE_IN_ITERS", String.valueOf(SCALE_IN_ITERS)));
		SELECTION_POLICY= Policy.valueOf(prop.getProperty("SELECTION_POLICY", SELECTION_POLICY.toString()));
		RELAXED_INITIAL_SOLUTION= Boolean.parseBoolean(prop.getProperty("RELAXED_INITIAL_SOLUTION", String.valueOf(RELAXED_INITIAL_SOLUTION)));
		SSH_PASSWORD = prop.getProperty("SSH_PASSWORD", SSH_PASSWORD);
		SSH_USER_NAME = prop.getProperty("SSH_USER_NAME", SSH_USER_NAME);
		SSH_HOST = prop.getProperty("SSH_HOST", SSH_HOST);
		RANDOM_SEED = Integer.parseInt(prop.getProperty("RANDOM_SEED", String.valueOf(RANDOM_SEED)));
		
		ROBUSTNESS_PEAK_FROM = Integer.parseInt(prop.getProperty("ROBUSTNESS_PEAK_FROM", String.valueOf(ROBUSTNESS_PEAK_FROM)));
		ROBUSTNESS_PEAK_TO = Integer.parseInt(prop.getProperty("ROBUSTNESS_PEAK_TO", String.valueOf(ROBUSTNESS_PEAK_FROM)));
		ROBUSTNESS_STEP_SIZE = Integer.parseInt(prop.getProperty("ROBUSTNESS_STEP_SIZE", String.valueOf(ROBUSTNESS_STEP_SIZE)));
		ROBUSTNESS_ATTEMPTS = Integer.parseInt(prop.getProperty("ROBUSTNESS_ATTEMPTS", String.valueOf(ROBUSTNESS_ATTEMPTS)));
		
		REDISTRIBUTE_WORKLOAD = Boolean.parseBoolean(prop.getProperty("REDISTRIBUTE_WORKLOAD", String.valueOf(REDISTRIBUTE_WORKLOAD)));
		
		USE_PRIVATE_CLOUD = Boolean.parseBoolean(prop.getProperty("USE_PRIVATE_CLOUD", String.valueOf(USE_PRIVATE_CLOUD)));
		PRIVATE_CPUS = Integer.parseInt(prop.getProperty("PRIVATE_CPUS", String.valueOf(PRIVATE_CPUS)));
		PRIVATE_CPUPOWER = Double.parseDouble(prop.getProperty("PRIVATE_CPUPOWER", String.valueOf(PRIVATE_CPUPOWER)));
		PRIVATE_RAM = Integer.parseInt(prop.getProperty("PRIVATE_RAM", String.valueOf(PRIVATE_RAM)));
		PRIVATE_STORAGE = Integer.parseInt(prop.getProperty("PRIVATE_STORAGE", String.valueOf(PRIVATE_STORAGE)));
	}

	/**
	 * Retrieves the name of the project. 
	 * This method assumes that the project has the same name of the folder in which it is contained.
	 * @return the name of the eclipse project
	 */
	public static String getProjectName() {
		if(PROJECT_BASE_FOLDER != null)
			return Paths.get(PROJECT_BASE_FOLDER).getFileName().toString(); 
		return null;
	}

	/**
	 * Checks if the configuration is valid returning a list of errors
	 * @return
	 */
	public static List<String> checkValidity() {
		ArrayList<String> errors = new ArrayList<String>();
		
		//check Palladio Model Files
		if(PALLADIO_REPOSITORY_MODEL == null || PALLADIO_REPOSITORY_MODEL.isEmpty())
			errors.add("The palladio repository model has not been specified");
		if(PALLADIO_SYSTEM_MODEL== null|| PALLADIO_SYSTEM_MODEL.isEmpty())
			errors.add("The palladio system model has not been specified");
		if(PALLADIO_RESOURCE_MODEL== null|| PALLADIO_RESOURCE_MODEL.isEmpty())
			errors.add("The palladio resource environment model has not been specified");
		if(PALLADIO_ALLOCATION_MODEL== null|| PALLADIO_ALLOCATION_MODEL.isEmpty())
			errors.add("The palladio allocation model has not been specified");
		if(PALLADIO_USAGE_MODEL== null|| PALLADIO_USAGE_MODEL.isEmpty())
			errors.add("The palladio usage model has not been specified");
		
		//check extensions
		if(USAGE_MODEL_EXTENSION==null|| USAGE_MODEL_EXTENSION.isEmpty())
			errors.add("The usage model extension has not been specified");
		if(RESOURCE_ENVIRONMENT_EXTENSION==null|| RESOURCE_ENVIRONMENT_EXTENSION.isEmpty())
			errors.add("The resource environment extension has not been specified");
		if(CONSTRAINTS==null|| CONSTRAINTS.isEmpty())
			errors.add("The constraint file has not been specified");
		
		//check functionality and the solver
		if(DB_CONNECTION_FILE==null|| DB_CONNECTION_FILE.isEmpty())
			errors.add("The database connection file has not been specified");		
		if(SOLVER== Solver.LINE && (LINE_PROP_FILE == null || LINE_PROP_FILE.isEmpty()))
			errors.add("The LINE configuration file has not been specified");
		
		//check the optimization if it has been selected
		if(FUNCTIONALITY==Operation.Optimization){
			if(TABU_MEMORY_SIZE < 1)
				errors.add("The tabu memory size must be a positive number");
			if(SCRUMBLE_ITERS < 1)
				errors.add("The number of scrumble iterations must be positive");
			if(FEASIBILITY_ITERS < 1)
				errors.add("The number of feasibility iterations must be positive");
			if(SCALE_IN_FACTOR < 1)
				errors.add("The scale in factor must be positive");
			if(SCALE_IN_ITERS < 1)
				errors.add("The number of scale in iterations must be positive");
			if(SCALE_IN_CONV_ITERS < 1)
				errors.add("The number of scale in convergence iterations must be positive");
			
			//check the initial solution generation
			if(RELAXED_INITIAL_SOLUTION || REDISTRIBUTE_WORKLOAD){
				if(SSH_USER_NAME==null|| SSH_USER_NAME.isEmpty())
					errors.add("The user name for SSH connection has to be provided to perform the initial solution generation");
				if(SSH_PASSWORD==null|| SSH_PASSWORD.isEmpty())
					errors.add("The password for SSH connection has to be provided to perform the initial solution generation");
				if(SSH_HOST==null|| SSH_HOST.isEmpty())
					errors.add("The host for SSH connection has to be provided to perform the initial solution generation");
			}
			
			if (USE_PRIVATE_CLOUD) {
				if (PRIVATE_CPUS < 1)
					errors.add("The CPU number for the private cloud solution should be at least 1.");
				if (PRIVATE_CPUPOWER < 1)
					errors.add("The CPU power for the private cloud solution should be at least 1 MHz.");
				if (PRIVATE_RAM < 1)
					errors.add("The RAM for the private cloud solution should be at least 1 MB.");
				if (PRIVATE_STORAGE < 1)
					errors.add("The storage for the private cloud solution should be at least 1 GB.");
			}
		}

		return errors;
	}

	public static void flushLog() {
		logger.debug("PALLADIO_REPOSITORY_MODEL: "+ PALLADIO_REPOSITORY_MODEL);
		logger.debug("PALLADIO_SYSTEM_MODEL: "+ PALLADIO_SYSTEM_MODEL);
		logger.debug("PALLADIO_ALLOCATION_MODEL: "+ PALLADIO_ALLOCATION_MODEL);
		logger.debug("PALLADIO_USAGE_MODEL: "+ PALLADIO_USAGE_MODEL);
		logger.debug("PALLADIO_RESOURCE_MODEL: "+ PALLADIO_RESOURCE_MODEL);
		logger.debug("USAGE_MODEL_EXTENSION: "+ USAGE_MODEL_EXTENSION);
		logger.debug("RESOURCE_ENVIRONMENT_EXTENSION: "+ RESOURCE_ENVIRONMENT_EXTENSION);
		logger.debug("CONSTRAINTS: "+ CONSTRAINTS);
		logger.debug("PROJECT_BASE_FOLDER: "+ PROJECT_BASE_FOLDER);
		logger.debug("DB_CONNECTION_FILE: "+ DB_CONNECTION_FILE);
		logger.debug("FUNCTIONALITY: "+ FUNCTIONALITY.toString());
		logger.debug("SOLVER: "+ SOLVER.toString() );
		logger.debug("LINE_PROP_FILE: "+ LINE_PROP_FILE);		
		logger.debug("TABU_MEMORY_SIZE: "+ Integer.toString(TABU_MEMORY_SIZE));
		logger.debug("SCRUMBLE_ITERS: "+ Integer.toString(SCRUMBLE_ITERS));
		logger.debug("FEASIBILITY_ITERS: "+ Integer.toString(FEASIBILITY_ITERS));
		logger.debug("SCALE_IN_CONV_ITERS: "+ Integer.toString(SCALE_IN_CONV_ITERS));
		logger.debug("SCALE_IN_FACTOR: "+ Double.toString(SCALE_IN_FACTOR));
		logger.debug("SCALE_IN_ITERS: "+ Integer.toString(SCALE_IN_ITERS));
		logger.debug("SELECTION_POLICY: "+ SELECTION_POLICY.toString());
		logger.debug("RELAXED_INITIAL_SOLUTION: "+ Boolean.toString(RELAXED_INITIAL_SOLUTION));
		logger.debug("SSH_HOST: "+ SSH_HOST);
		logger.debug("SSH_USER_NAME: "+ SSH_USER_NAME);
		logger.debug("SSH_PASSWORD: "+ SSH_PASSWORD);
		logger.debug("RANDOM_SEED: "+ Integer.toString(RANDOM_SEED));
		
		logger.debug("ROBUSTNESS_PEAK_FROM: " + Integer.toString(ROBUSTNESS_PEAK_FROM));
		logger.debug("ROBUSTNESS_PEAK_TO: " + Integer.toString(ROBUSTNESS_PEAK_TO));
		logger.debug("ROBUSTNESS_STEP_SIZE: " + Integer.toString(ROBUSTNESS_STEP_SIZE));
		logger.debug("ROBUSTNESS_ATTEMPTS: " + Integer.toString(ROBUSTNESS_ATTEMPTS));
		
		logger.debug("REDISTRIBUTE_WORKLOAD: " + Boolean.toString(REDISTRIBUTE_WORKLOAD));
		
		logger.debug("USE_PRIVATE_CLOUD: " + Boolean.toString(USE_PRIVATE_CLOUD));
		logger.debug("PRIVATE_CPUS: " + Integer.toString(PRIVATE_CPUS));
		logger.debug("PRIVATE_CPUPOWER: " + Double.toString(PRIVATE_CPUPOWER));
		logger.debug("PRIVATE_RAM: " + Integer.toString(PRIVATE_RAM));
		logger.debug("PRIVATE_STORAGE: " + Integer.toString(PRIVATE_STORAGE));
	}
	
	
	/**
	 * Checks if the configuration in the given properties file is the same as the actual configuration.
	 * 
	 * @param filePath The path to the properties file
	 * @return true if the configuration are identical
	 */
	public static boolean sameConfiguration(String filePath) {
		// Notice that the method only checks the values in the properties file, thus if only one property is set
		// with the same value, the result is true. This is because I suppose that the properties file was
		// first generated using our tool.
		
		Properties prop = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream(filePath);
			prop.load(fis);
		} catch (Exception e) {
			return false;
		}
		

		for (Object o : prop.keySet()) {
			String key = (String) o;
			
			try {
				if (!(String.valueOf(Configuration.class.getField(key).get(null))).equals(prop.getProperty(key)))
					return false;
			} catch (Exception e) {
				return false;
			}
			
		}
		
		return true;
	}

}
