package it.polimi.modaclouds.space4cloud.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnector;
import it.polimi.modaclouds.space4cloud.gui.CloudBurstingPanel;

public class Configuration {

	private static final Logger logger = LoggerFactory
			.getLogger(Configuration.class);

	// Configuration constant among all runs
	public static String WORKING_DIRECTORY = "space4cloud";
	public static final String FOLDER_PREFIX = "hour_";
	public static final String PERFORMANCE_RESULTS_FOLDER = "performance_results";
	public static final String LAUNCH_CONFIG = "launchConfig.launch";
	public static final String SOLUTION_FILE_NAME = "solution";
	public static final String SOLUTION_FILE_EXTENSION = ".xml";

	public static final String SOLUTION_LIGHT_FILE_NAME = "statistics";
	public static final String SOLUTION_CSV_FILE_NAME = "solution.csv";
	public static final String DEFAULT_DB_CONNECTION_FILE = "/config/DBConnection.properties";
	public static final String RANDOM_ENVIRONMENT_SOLUTION_TAG = "+RandomEnvironments";
	public static final String LINE_SOLUTION_TAG = "_line";
	public static final String APPLICATION_ID = "Application";

	// Configuration for the current run of space4cloud
	public static String PALLADIO_REPOSITORY_MODEL;
	public static String PALLADIO_SYSTEM_MODEL;
	public static String PALLADIO_ALLOCATION_MODEL;
	public static String PALLADIO_USAGE_MODEL;
	public static String PALLADIO_RESOURCE_MODEL;
	public static String USAGE_MODEL_EXTENSION;
	public static String RESOURCE_ENVIRONMENT_EXTENSION;
	public static String MULTI_CLOUD_EXTENSION;
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
	public static String RANDOM_ENV_FILE = "";

	// For the robustness test:
	public static int ROBUSTNESS_PEAK_FROM = 100;
	public static int ROBUSTNESS_PEAK_TO = 10000;
	public static int ROBUSTNESS_STEP_SIZE = 300;
	public static int ROBUSTNESS_ATTEMPTS = 1;

	public static boolean REDISTRIBUTE_WORKLOAD = false;

	public static boolean CONTRACTOR_TEST = false;

	// For the Private Cloud part:
	public static boolean USE_PRIVATE_CLOUD = false;
	public static String PRIVATE_CLOUD_HOSTS = "";
	// public static String PRIVATE_CLOUD_HOSTS_TMP= "";
	// Used to start or stop the optimization process
	public static boolean run = true;

	// public static int ROBUSTNESS_VARIABILITY= 0;
	public static double ROBUSTNESS_Q = 0.15;
	// public static int ROBUSTNESS_G = 24;
	public static int ROBUSTNESS_H = 1095;

	public static int[] ROBUSTNESS_VARIABILITIES = { 0 };
	public static int[] ROBUSTNESS_GS = { 24 };

	public static boolean GENERATE_DESIGN_TO_RUNTIME_FILES = false;
	public static String FUNCTIONALITY_TO_TIER_FILE = "";
	public static int OPTIMIZATION_WINDOW_LENGTH = 5;
	public static int TIMESTEP_DURATION = 5;
	
	public static Benchmark BENCHMARK = Benchmark.None;

	// Operations
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

	// Solvers
	public static enum Solver {
		LINE, LQNS;

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

	// Selection Policies
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

	// Benchmarks
	public static enum Benchmark {
		None, DaCapo, Filebench;

		public static Benchmark getById(int id) {
			Benchmark[] values = Benchmark.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}

		public static int size() {
			return Benchmark.values().length;
		}

	}

	public static void saveConfiguration(String filePath) throws IOException {
		FileOutputStream fos = new FileOutputStream(filePath);
		Properties prop = new Properties();
		prop.put("PALLADIO_REPOSITORY_MODEL", PALLADIO_REPOSITORY_MODEL);
		prop.put("PALLADIO_SYSTEM_MODEL", PALLADIO_SYSTEM_MODEL);
		prop.put("PALLADIO_ALLOCATION_MODEL", PALLADIO_ALLOCATION_MODEL);
		prop.put("PALLADIO_USAGE_MODEL", PALLADIO_USAGE_MODEL);
		prop.put("PALLADIO_RESOURCE_MODEL", PALLADIO_RESOURCE_MODEL);
		prop.put("USAGE_MODEL_EXTENSION", USAGE_MODEL_EXTENSION);
		prop.put("RESOURCE_ENVIRONMENT_EXTENSION",
				RESOURCE_ENVIRONMENT_EXTENSION);
		prop.put("MULTI_CLOUD_EXTENSION", MULTI_CLOUD_EXTENSION);
		prop.put("CONSTRAINTS", CONSTRAINTS);
		prop.put("PROJECT_BASE_FOLDER", PROJECT_BASE_FOLDER);
		prop.put("DB_CONNECTION_FILE", DB_CONNECTION_FILE);
		prop.put("FUNCTIONALITY", FUNCTIONALITY.toString());
		prop.put("SOLVER", SOLVER.toString());
		prop.put("LINE_PROP_FILE", LINE_PROP_FILE);
		prop.put("RANDOM_ENV_FILE", RANDOM_ENV_FILE);
		prop.put("WORKING_DIRECTORY", WORKING_DIRECTORY);
		prop.put("TABU_MEMORY_SIZE", Integer.toString(TABU_MEMORY_SIZE));
		prop.put("SCRUMBLE_ITERS", Integer.toString(SCRUMBLE_ITERS));
		prop.put("FEASIBILITY_ITERS", Integer.toString(FEASIBILITY_ITERS));
		prop.put("SCALE_IN_CONV_ITERS", Integer.toString(SCALE_IN_CONV_ITERS));
		prop.put("SCALE_IN_FACTOR", Double.toString(SCALE_IN_FACTOR));
		prop.put("SCALE_IN_ITERS", Integer.toString(SCALE_IN_ITERS));
		prop.put("SELECTION_POLICY", SELECTION_POLICY.toString());
		prop.put("RELAXED_INITIAL_SOLUTION",
				Boolean.toString(RELAXED_INITIAL_SOLUTION));
		prop.put("SSH_HOST", SSH_HOST);
		prop.put("SSH_USER_NAME", SSH_USER_NAME);
		prop.put("SSH_PASSWORD", SSH_PASSWORD);
		prop.put("RANDOM_SEED", Integer.toString(RANDOM_SEED));
		prop.put("ROBUSTNESS_PEAK_FROM", Integer.toString(ROBUSTNESS_PEAK_FROM));
		prop.put("ROBUSTNESS_PEAK_TO", Integer.toString(ROBUSTNESS_PEAK_TO));
		prop.put("ROBUSTNESS_STEP_SIZE", Integer.toString(ROBUSTNESS_STEP_SIZE));
		prop.put("ROBUSTNESS_ATTEMPTS", Integer.toString(ROBUSTNESS_ATTEMPTS));

		prop.put("REDISTRIBUTE_WORKLOAD",
				Boolean.toString(REDISTRIBUTE_WORKLOAD));

		prop.put("USE_PRIVATE_CLOUD", Boolean.toString(USE_PRIVATE_CLOUD));
		prop.put("PRIVATE_CLOUD_HOSTS", PRIVATE_CLOUD_HOSTS);
		// prop.put("ROBUSTNESS_VARIABILITY",
		// Integer.toString(ROBUSTNESS_VARIABILITY));
		String tmp = "";
		for (int i = 0; i < ROBUSTNESS_VARIABILITIES.length; ++i)
			tmp += ROBUSTNESS_VARIABILITIES[i] + ";";
		prop.put("ROBUSTNESS_VARIABILITY", tmp.substring(0, tmp.length() - 1));
		prop.put("ROBUSTNESS_Q", Double.toString(ROBUSTNESS_Q));
		// prop.put("ROBUSTNESS_G", Integer.toString(ROBUSTNESS_G));
		tmp = "";
		for (int i = 0; i < ROBUSTNESS_GS.length; ++i)
			tmp += ROBUSTNESS_GS[i] + ";";
		prop.put("ROBUSTNESS_G", tmp.substring(0, tmp.length() - 1));

		prop.put("ROBUSTNESS_H", Integer.toString(ROBUSTNESS_H));

		prop.put("CONTRACTOR_TEST", Boolean.toString(CONTRACTOR_TEST));

		prop.put("GENERATE_DESIGN_TO_RUNTIME_FILES",
				Boolean.toString(GENERATE_DESIGN_TO_RUNTIME_FILES));
		prop.put("FUNCTIONALITY_TO_TIER_FILE", FUNCTIONALITY_TO_TIER_FILE);
		prop.put("TIMESTEP_DURATION", Integer.toString(TIMESTEP_DURATION));
		prop.put("OPTIMIZATION_WINDOW_LENGTH",
				Integer.toString(OPTIMIZATION_WINDOW_LENGTH));
		
		prop.put("BENCHMARK", BENCHMARK.toString());

		it.polimi.modaclouds.space4cloud.milp.Configuration
				.addToConfiguration(prop);

		prop.store(fos, "SPACE4Clouds configuration properties");
		fos.flush();
	}

	public static void loadConfiguration(String filePath) throws IOException {
		Properties prop = new Properties();
		FileInputStream fis = new FileInputStream(filePath);
		prop.load(fis);
		PALLADIO_REPOSITORY_MODEL = prop.getProperty(
				"PALLADIO_REPOSITORY_MODEL", PALLADIO_REPOSITORY_MODEL);
		PALLADIO_SYSTEM_MODEL = prop.getProperty("PALLADIO_SYSTEM_MODEL",
				PALLADIO_SYSTEM_MODEL);
		PALLADIO_ALLOCATION_MODEL = prop.getProperty(
				"PALLADIO_ALLOCATION_MODEL", PALLADIO_ALLOCATION_MODEL);
		PALLADIO_USAGE_MODEL = prop.getProperty("PALLADIO_USAGE_MODEL",
				PALLADIO_USAGE_MODEL);
		PALLADIO_RESOURCE_MODEL = prop.getProperty("PALLADIO_RESOURCE_MODEL",
				PALLADIO_RESOURCE_MODEL);
		USAGE_MODEL_EXTENSION = prop.getProperty("USAGE_MODEL_EXTENSION",
				USAGE_MODEL_EXTENSION);
		RESOURCE_ENVIRONMENT_EXTENSION = prop.getProperty(
				"RESOURCE_ENVIRONMENT_EXTENSION",
				RESOURCE_ENVIRONMENT_EXTENSION);
		MULTI_CLOUD_EXTENSION = prop.getProperty("MULTI_CLOUD_EXTENSION",
				MULTI_CLOUD_EXTENSION);
		CONSTRAINTS = prop.getProperty("CONSTRAINTS", CONSTRAINTS);
		PROJECT_BASE_FOLDER = prop.getProperty("PROJECT_BASE_FOLDER",
				PROJECT_BASE_FOLDER);

		WORKING_DIRECTORY = prop.getProperty("WORKING_DIRECTORY",
				WORKING_DIRECTORY);

		DB_CONNECTION_FILE = prop.getProperty("DB_CONNECTION_FILE",
				DB_CONNECTION_FILE);
		FUNCTIONALITY = Operation.valueOf(prop.getProperty("FUNCTIONALITY",
				FUNCTIONALITY.toString()));
		SOLVER = Solver.valueOf(prop.getProperty("SOLVER", SOLVER.toString()));
		LINE_PROP_FILE = prop.getProperty("LINE_PROP_FILE", LINE_PROP_FILE);
		RANDOM_ENV_FILE = prop.getProperty("RANDOM_ENV_FILE", RANDOM_ENV_FILE);
		SSH_PASSWORD = prop.getProperty("SSH_PASSWORD", SSH_PASSWORD);
		SSH_USER_NAME = prop.getProperty("SSH_USER_NAME", SSH_USER_NAME);
		SSH_HOST = prop.getProperty("SSH_HOST", SSH_HOST);
		PRIVATE_CLOUD_HOSTS = prop.getProperty("PRIVATE_CLOUD_HOSTS",
				PRIVATE_CLOUD_HOSTS);

		FUNCTIONALITY_TO_TIER_FILE = prop.getProperty(
				"FUNCTIONALITY_TO_TIER_FILE", FUNCTIONALITY_TO_TIER_FILE);

		BENCHMARK = Benchmark.valueOf(prop.getProperty("BENCHMARK",
				BENCHMARK.toString()));
		
		try {
			TABU_MEMORY_SIZE = Integer.parseInt(prop
					.getProperty("TABU_MEMORY_SIZE"));
			SCRUMBLE_ITERS = Integer.parseInt(prop
					.getProperty("SCRUMBLE_ITERS"));
			FEASIBILITY_ITERS = Integer.parseInt(prop
					.getProperty("FEASIBILITY_ITERS"));
			SCALE_IN_CONV_ITERS = Integer.parseInt(prop
					.getProperty("SCALE_IN_CONV_ITERS"));
			SCALE_IN_FACTOR = Double.parseDouble(prop
					.getProperty("SCALE_IN_FACTOR"));
			SCALE_IN_ITERS = Integer.parseInt(prop
					.getProperty("SCALE_IN_ITERS"));
			SELECTION_POLICY = Policy.valueOf(prop
					.getProperty("SELECTION_POLICY"));
			RELAXED_INITIAL_SOLUTION = Boolean.parseBoolean(prop
					.getProperty("RELAXED_INITIAL_SOLUTION"));
			RANDOM_SEED = Integer.parseInt(prop.getProperty("RANDOM_SEED"));
			ROBUSTNESS_PEAK_FROM = Integer.parseInt(prop.getProperty(
					"ROBUSTNESS_PEAK_FROM",
					String.valueOf(ROBUSTNESS_PEAK_FROM)));
			ROBUSTNESS_PEAK_TO = Integer
					.parseInt(prop.getProperty("ROBUSTNESS_PEAK_TO",
							String.valueOf(ROBUSTNESS_PEAK_FROM)));
			ROBUSTNESS_STEP_SIZE = Integer.parseInt(prop.getProperty(
					"ROBUSTNESS_STEP_SIZE",
					String.valueOf(ROBUSTNESS_STEP_SIZE)));
			ROBUSTNESS_ATTEMPTS = Integer
					.parseInt(prop.getProperty("ROBUSTNESS_ATTEMPTS",
							String.valueOf(ROBUSTNESS_ATTEMPTS)));
			// ROBUSTNESS_VARIABILITY =
			// Integer.parseInt(prop.getProperty("ROBUSTNESS_VARIABILITY",
			// String.valueOf(ROBUSTNESS_VARIABILITY)));

			String read = prop.getProperty("ROBUSTNESS_VARIABILITY");
			if (read != null) {
				String[] tmp = read.split(";");
				if (tmp.length > 0) {
					ROBUSTNESS_VARIABILITIES = new int[tmp.length];
					for (int i = 0; i < ROBUSTNESS_VARIABILITIES.length; ++i)
						ROBUSTNESS_VARIABILITIES[i] = Integer.parseInt(tmp[i]);
				}

			}

			ROBUSTNESS_Q = Double.parseDouble(prop.getProperty("ROBUSTNESS_Q",
					String.valueOf(ROBUSTNESS_Q)));
			// ROBUSTNESS_G= Integer.parseInt(prop.getProperty("ROBUSTNESS_G",
			// String.valueOf(ROBUSTNESS_G)));

			read = prop.getProperty("ROBUSTNESS_G");
			if (read != null) {
				String[] tmp = read.split(";");
				if (tmp.length > 0) {
					ROBUSTNESS_GS = new int[tmp.length];
					for (int i = 0; i < ROBUSTNESS_GS.length; ++i)
						ROBUSTNESS_GS[i] = Integer.parseInt(tmp[i]);
				}

			}

			ROBUSTNESS_H = Integer.parseInt(prop.getProperty("ROBUSTNESS_H",
					String.valueOf(ROBUSTNESS_H)));
			REDISTRIBUTE_WORKLOAD = Boolean.parseBoolean(prop.getProperty(
					"REDISTRIBUTE_WORKLOAD",
					String.valueOf(REDISTRIBUTE_WORKLOAD)));
			USE_PRIVATE_CLOUD = Boolean.parseBoolean(prop.getProperty(
					"USE_PRIVATE_CLOUD", String.valueOf(USE_PRIVATE_CLOUD)));

			CONTRACTOR_TEST = Boolean.parseBoolean(prop.getProperty(
					"CONTRACTOR_TEST", String.valueOf(CONTRACTOR_TEST)));

			GENERATE_DESIGN_TO_RUNTIME_FILES = Boolean.parseBoolean(prop
					.getProperty("GENERATE_DESIGN_TO_RUNTIME_FILES",
							String.valueOf(GENERATE_DESIGN_TO_RUNTIME_FILES)));
			TIMESTEP_DURATION = Integer.parseInt(prop.getProperty(
					"TIMESTEP_DURATION", String.valueOf(TIMESTEP_DURATION)));
			OPTIMIZATION_WINDOW_LENGTH = Integer.parseInt(prop.getProperty(
					"OPTIMIZATION_WINDOW_LENGTH",
					String.valueOf(OPTIMIZATION_WINDOW_LENGTH)));
		} catch (NumberFormatException e) {
			logger.warn(
					"Part of the configuration was invalid, reverted the invalid value to the default one.",
					e);
		}

		it.polimi.modaclouds.space4cloud.milp.Configuration
				.loadConfiguration(filePath);

	}

	/**
	 * Retrieves the name of the project. This method assumes that the project
	 * has the same name of the folder in which it is contained.
	 * 
	 * @return the name of the eclipse project
	 */
	public static String getProjectName() {
		if (PROJECT_BASE_FOLDER != null)
			return Paths.get(PROJECT_BASE_FOLDER).getFileName().toString();
		return null;
	}

	public static boolean isRunningLocally() {
		return (SSH_HOST.equals("localhost") || SSH_HOST.equals("127.0.0.1"));
	}

	/**
	 * Checks if the configuration is valid returning a list of errors
	 * 
	 * @return
	 */
	public static List<String> checkValidity() {
		ArrayList<String> errors = new ArrayList<String>();

		// check Palladio Model Files
		if (fileNotSpecifiedORNotExist(PALLADIO_REPOSITORY_MODEL))
			errors.add("The palladio repository model has not been specified");
		if (fileNotSpecifiedORNotExist(PALLADIO_SYSTEM_MODEL))
			errors.add("The palladio system model has not been specified");
		if (fileNotSpecifiedORNotExist(PALLADIO_RESOURCE_MODEL))
			errors.add("The palladio resource environment model has not been specified");
		if (fileNotSpecifiedORNotExist(PALLADIO_ALLOCATION_MODEL))
			errors.add("The palladio allocation model has not been specified");
		if (fileNotSpecifiedORNotExist(PALLADIO_USAGE_MODEL))
			errors.add("The palladio usage model has not been specified");
		// check extensions
		if (fileNotSpecifiedORNotExist(USAGE_MODEL_EXTENSION))
			errors.add("The usage model extension has not been specified");
		if (fileNotSpecifiedORNotExist(RESOURCE_ENVIRONMENT_EXTENSION))
			errors.add("The resource environment extension has not been specified");
		if (fileNotSpecifiedORNotExist(CONSTRAINTS))
			errors.add("The constraint file has not been specified");
		// check functionality and the solver
		if (fileNotSpecifiedORNotExist(DB_CONNECTION_FILE))
			errors.add("The database connection file has not been specified");
		if (SOLVER == Solver.LINE && fileNotSpecifiedORNotExist(LINE_PROP_FILE))
			errors.add("The LINE configuration file has not been specified");
		// check the optimization if it has been selected
		if (FUNCTIONALITY == Operation.Optimization
				|| FUNCTIONALITY == Operation.Robustness) {

//			if (TABU_MEMORY_SIZE < 1)
//				errors.add("The tabu memory size must be a positive number");
//			if (SCRUMBLE_ITERS < 1)
//				errors.add("The number of scrumble iterations must be positive");
//			if (FEASIBILITY_ITERS < 1)
//				errors.add("The number of feasibility iterations must be positive");
//			if (SCALE_IN_FACTOR < 1)
//				errors.add("The scale in factor must be positive");
//			if (SCALE_IN_ITERS < 1)
//				errors.add("The number of scale in iterations must be positive");
//			if (SCALE_IN_CONV_ITERS < 1)
//				errors.add("The number of scale in convergence iterations must be positive");

			// check the initial solution generation
			if ((RELAXED_INITIAL_SOLUTION || REDISTRIBUTE_WORKLOAD
					|| USE_PRIVATE_CLOUD || CONTRACTOR_TEST)
					&& !isRunningLocally()) {
				if (SSH_USER_NAME == null || SSH_USER_NAME.isEmpty())
					errors.add("The user name for SSH connection has to be provided to perform the initial solution generation");
				if (SSH_PASSWORD == null || SSH_PASSWORD.isEmpty())
					errors.add("The password for SSH connection has to be provided to perform the initial solution generation");
				if (SSH_HOST == null || SSH_HOST.isEmpty())
					errors.add("The host for SSH connection has to be provided to perform the initial solution generation");
			}

		}
		if (USE_PRIVATE_CLOUD) {
			if (!CloudBurstingPanel.hasHosts())
				errors.add("You need to specify the file describing the private hosts.");
		}
		if (FUNCTIONALITY == Operation.Robustness) {
			if (ROBUSTNESS_ATTEMPTS < 1)
				errors.add("The number of robustness attempts must be at least 1");
			if (ROBUSTNESS_PEAK_FROM < 1)
				errors.add("The minimum peak for the robustness test should be at least 1");
			if (ROBUSTNESS_PEAK_TO < ROBUSTNESS_PEAK_FROM)
				errors.add("The maximum peak for the robustness test has to be higher than the minimum peak");
			if (ROBUSTNESS_STEP_SIZE < 1)
				errors.add("The step size for the robustness test should be at least 1");
			for (int ROBUSTNESS_VARIABILITY : ROBUSTNESS_VARIABILITIES)
				if (ROBUSTNESS_VARIABILITY < 0 || ROBUSTNESS_VARIABILITY > 100)
					errors.add("The variability for the robustness test should be a number from 0 to 100");
		}

		if (GENERATE_DESIGN_TO_RUNTIME_FILES) {
			if (fileNotSpecifiedORNotExist(FUNCTIONALITY_TO_TIER_FILE))
				errors.add("File functionality2tier not valid.");
			if (TIMESTEP_DURATION < 0)
				errors.add("Negative timestep provided.");
			if (OPTIMIZATION_WINDOW_LENGTH < 0)
				errors.add("Negative optimization window provided.");
		}

		return errors;
	}

	private static boolean fileNotSpecifiedORNotExist(String filePath) {
		return filePath == null || filePath.isEmpty()
				|| !Paths.get(filePath).toFile().exists();
	}

	public static void flushLog() {
		logger.debug("PALLADIO_REPOSITORY_MODEL: {}", PALLADIO_REPOSITORY_MODEL);
		logger.debug("PALLADIO_SYSTEM_MODEL: {}", PALLADIO_SYSTEM_MODEL);
		logger.debug("PALLADIO_ALLOCATION_MODEL: {}", PALLADIO_ALLOCATION_MODEL);
		logger.debug("PALLADIO_USAGE_MODEL: {}", PALLADIO_USAGE_MODEL);
		logger.debug("PALLADIO_RESOURCE_MODEL: {}", PALLADIO_RESOURCE_MODEL);
		logger.debug("USAGE_MODEL_EXTENSION: {}", USAGE_MODEL_EXTENSION);
		logger.debug("RESOURCE_ENVIRONMENT_EXTENSION: {}",
				RESOURCE_ENVIRONMENT_EXTENSION);
		logger.debug("MULTI_CLOUD_EXTENSION: {}", MULTI_CLOUD_EXTENSION);
		logger.debug("CONSTRAINTS: {}", CONSTRAINTS);
		logger.debug("PROJECT_BASE_FOLDER: {}", PROJECT_BASE_FOLDER);

		logger.debug("DB_CONNECTION_FILE: {}", DB_CONNECTION_FILE);
		logger.debug("FUNCTIONALITY: {}", FUNCTIONALITY.toString());
		logger.debug("SOLVER: {}", SOLVER.toString());
		logger.debug("LINE_PROP_FILE: {}", LINE_PROP_FILE);
		logger.debug("RANDOM_ENV_FILE: {}", RANDOM_ENV_FILE);
		logger.debug("WORKING_DIRECTORY: {}", WORKING_DIRECTORY);
		logger.debug("TABU_MEMORY_SIZE: {}", Integer.toString(TABU_MEMORY_SIZE));
		logger.debug("SCRUMBLE_ITERS: {}", Integer.toString(SCRUMBLE_ITERS));
		logger.debug("FEASIBILITY_ITERS: {}",
				Integer.toString(FEASIBILITY_ITERS));
		logger.debug("SCALE_IN_CONV_ITERS: {}",
				Integer.toString(SCALE_IN_CONV_ITERS));
		logger.debug("SCALE_IN_FACTOR: {}", Double.toString(SCALE_IN_FACTOR));
		logger.debug("SCALE_IN_ITERS: {}", Integer.toString(SCALE_IN_ITERS));
		logger.debug("SELECTION_POLICY: {}", SELECTION_POLICY.toString());
		logger.debug("RELAXED_INITIAL_SOLUTION: {}",
				Boolean.toString(RELAXED_INITIAL_SOLUTION));
		logger.debug("SSH_HOST: {}", SSH_HOST);
		logger.debug("SSH_USER_NAME: {}", SSH_USER_NAME);
		logger.debug("SSH_PASSWORD: {}", SSH_PASSWORD);
		logger.debug("RANDOM_SEED: {}", Integer.toString(RANDOM_SEED));
		logger.debug("ROBUSTNESS_PEAK_FROM: {}",
				Integer.toString(ROBUSTNESS_PEAK_FROM));
		logger.debug("ROBUSTNESS_PEAK_TO: {}",
				Integer.toString(ROBUSTNESS_PEAK_TO));
		logger.debug("ROBUSTNESS_STEP_SIZE: {}",
				Integer.toString(ROBUSTNESS_STEP_SIZE));
		logger.debug("ROBUSTNESS_ATTEMPTS: {}",
				Integer.toString(ROBUSTNESS_ATTEMPTS));
		for (int ROBUSTNESS_VARIABILITY : ROBUSTNESS_VARIABILITIES)
			logger.debug("ROBUSTNESS_VARIABILITY: {}",
					Integer.toString(ROBUSTNESS_VARIABILITY));
		logger.debug("ROBUSTNESS_Q: {}", Double.toString(ROBUSTNESS_Q));
		for (int ROBUSTNESS_G : ROBUSTNESS_GS)
			logger.debug("ROBUSTNESS_G: {}", Integer.toString(ROBUSTNESS_G));
		logger.debug("ROBUSTNESS_H: {}", Integer.toString(ROBUSTNESS_H));
		logger.debug("REDISTRIBUTE_WORKLOAD: {}",
				Boolean.toString(REDISTRIBUTE_WORKLOAD));
		logger.debug("USE_PRIVATE_CLOUD: {}",
				Boolean.toString(USE_PRIVATE_CLOUD));
		logger.debug("PRIVATE_CLOUD_HOSTS: {}", PRIVATE_CLOUD_HOSTS);
		// logger.debug("PRIVATE_CLOUD_HOSTS_TMP: " + PRIVATE_CLOUD_HOSTS_TMP);

		logger.debug("CONTRACTOR_TEST: {}", Boolean.toString(CONTRACTOR_TEST));

		logger.debug("GENERATE_DESIGN_TO_RUNTIME_FILES: {}",
				Boolean.toString(GENERATE_DESIGN_TO_RUNTIME_FILES));
		logger.debug("FUNCTIONALITY_TO_TIER_FILE: {}",
				FUNCTIONALITY_TO_TIER_FILE);
		logger.debug("OPTIMIZATION_WINDOW_LENGTH: {}",
				Integer.toString(OPTIMIZATION_WINDOW_LENGTH));
		logger.debug("TIMESTEP_DURATION: {}",
				Integer.toString(TIMESTEP_DURATION));
		logger.debug("BENCHMARK: {}", BENCHMARK.toString());
	}

	/**
	 * Checks if the configuration in the given properties file is the same as
	 * the actual configuration.
	 * 
	 * @param filePath
	 *            The path to the properties file
	 * @return true if the configuration are identical
	 */
	public static boolean sameConfiguration(String filePath) {
		// Notice that the method only checks the values in the properties file,
		// thus if only one property is set
		// with the same value, the result is true. This is because I suppose
		// that the properties file was
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
				if (!(String.valueOf(Configuration.class.getField(key)
						.get(null))).equals(prop.getProperty(key)))
					return false;
			} catch (Exception e) {
				return false;
			}

		}

		return true;
	}

	public static synchronized boolean isPaused() {
		return !run;
	}

	public static synchronized void pause() {
		run = false;
	}

	public static synchronized void resume() {
		run = true;
	}

	private static Path replaceAllOccurrencies(Path p, String suffix,
			Map<String, String> subs) throws Exception {
		if (!p.toFile().exists())
			return null;

		if (subs.size() == 0)
			return p;

		String baseFile = new String(Files.readAllBytes(p)); // ,
																// Charset.defaultCharset());
																// //
																// StandardCharsets.UTF_8);

		boolean done = false;

		for (String orig : subs.keySet()) {
			Matcher m = java.util.regex.Pattern.compile(orig).matcher(baseFile);
			while (m.find() && !done) {
				if (!m.group().equals(subs.get(orig)))
					done = true;
			}
			baseFile = m.replaceAll(subs.get(orig));

			// baseFile = baseFile.replaceAll(orig, subs.get(orig));
		}

		if (!done)
			return p;

		String fullName = p.toFile().getName();
		int i = fullName.lastIndexOf('.');
		String name = fullName.substring(0, i);
		String ext = fullName.substring(i);
		// Path newp = Files.createTempFile(name, ext);
		Path newp = Paths.get(p.getParent().toString(), name + suffix + ext);

		try (PrintWriter out = new PrintWriter(new FileWriter(newp.toFile()))) {
			out.printf(baseFile);
			out.flush();
		}

		return newp;
	}

	public static String getDate() {
		Calendar c = Calendar.getInstance();

		DecimalFormat f = new DecimalFormat("00");

		return String.format("%d%s%s%s%s%s", c.get(Calendar.YEAR),
				f.format(c.get(Calendar.MONTH) + 1),
				f.format(c.get(Calendar.DAY_OF_MONTH)),
				f.format(c.get(Calendar.HOUR_OF_DAY)),
				f.format(c.get(Calendar.MINUTE)),
				f.format(c.get(Calendar.SECOND)));
	}

	public static void fixPalladioFiles() throws Exception {
		Map<String, String> subs = new HashMap<String, String>();
		subs.put("href=\"[a-z]+.repository",
				"href=\""
						+ Paths.get(Configuration.PALLADIO_REPOSITORY_MODEL)
								.toFile().getName());

		String suffix = getDate();

		Configuration.PALLADIO_SYSTEM_MODEL = replaceAllOccurrencies(
				Paths.get(Configuration.PALLADIO_SYSTEM_MODEL), suffix, subs)
				.toString();

		subs.put("href=\"[a-z]+.resourceenvironment",
				"href=\""
						+ Paths.get(Configuration.PALLADIO_RESOURCE_MODEL)
								.toFile().getName());
		subs.put("href=\"[a-z]+.system",
				"href=\""
						+ Paths.get(Configuration.PALLADIO_SYSTEM_MODEL)
								.toFile().getName());

		Configuration.PALLADIO_ALLOCATION_MODEL = replaceAllOccurrencies(
				Paths.get(Configuration.PALLADIO_ALLOCATION_MODEL), suffix,
				subs).toString();
		Configuration.PALLADIO_USAGE_MODEL = replaceAllOccurrencies(
				Paths.get(Configuration.PALLADIO_USAGE_MODEL), suffix, subs)
				.toString();

	}

	public static void initDatabaseConfiguration() throws Exception {
		InputStream dbConfigurationStream = null;
		// load the configuration file if specified

		if (Configuration.DB_CONNECTION_FILE != null
				&& Paths.get(Configuration.DB_CONNECTION_FILE).toFile()
						.exists()) {
			try {
				dbConfigurationStream = new FileInputStream(
						Configuration.DB_CONNECTION_FILE);
			} catch (FileNotFoundException e) {
				logger.warn("Could not load the dabase configuration from: "
						+ Configuration.DB_CONNECTION_FILE
						+ ". Will try to use the default one");
				dbConfigurationStream = Configuration.class
						.getResourceAsStream(Configuration.DEFAULT_DB_CONNECTION_FILE);
			}
		} else {
			// if the file has not been specified or it does not exist use the
			// one with default values embedded in the plugin
			dbConfigurationStream = Configuration.class
					.getResourceAsStream(Configuration.DEFAULT_DB_CONNECTION_FILE);
		}

		try {
			DatabaseConnector.initConnection(dbConfigurationStream);
			DataHandlerFactory.getHandler();
		} catch (SQLException | IOException
				| DatabaseConnectionFailureExteption e) {
			throw new Exception("Error connecting to the Database", e);
		}

		try {
			dbConfigurationStream.close();
		} catch (IOException e) {
			logger.error("Error closing the dabase configuration");
		}
	}

}
