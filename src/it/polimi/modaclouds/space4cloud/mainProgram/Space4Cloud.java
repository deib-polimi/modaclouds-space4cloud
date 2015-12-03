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
package it.polimi.modaclouds.space4cloud.mainProgram;

import it.polimi.modaclouds.qos_models.schema.ClosedWorkload;
import it.polimi.modaclouds.qos_models.schema.ClosedWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.OpenWorkload;
import it.polimi.modaclouds.qos_models.schema.OpenWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnector;
import it.polimi.modaclouds.space4cloud.exceptions.AssesmentException;
import it.polimi.modaclouds.space4cloud.exceptions.InitializationException;
import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.exceptions.RobustnessException;
import it.polimi.modaclouds.space4cloud.gui.AssessmentWindow;
import it.polimi.modaclouds.space4cloud.gui.BestSolutionExplorer;
import it.polimi.modaclouds.space4cloud.gui.ConfigurationWindow;
import it.polimi.modaclouds.space4cloud.gui.OptimizationProgressWindow;
import it.polimi.modaclouds.space4cloud.gui.RobustnessProgressWindow;
import it.polimi.modaclouds.space4cloud.optimization.OptimizationEngine;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandlerFactory;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintLoadingException;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.LineServerHandler;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.LineServerHandlerFactory;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Operation;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Solver;
import it.polimi.modaclouds.space4cloud.utils.DataExporter;
import it.polimi.modaclouds.space4cloud.utils.MILPEvaluator;
import it.polimi.modaclouds.space4cloud.utils.PalladioRunException;
import it.polimi.modaclouds.space4cloud.utils.PluginConsoleAppender;
import it.polimi.modaclouds.space4cloud.utils.RunConfigurationsHandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker.StateValue;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class Space4Cloud extends Thread implements PropertyChangeListener {

	private static OptimizationProgressWindow progressWindow;

	private static AssessmentWindow assesmentWindow;
	private static RobustnessProgressWindow robustnessWindow;
	private OptimizationEngine engine = null;
	private static final Logger logger = LoggerFactory.getLogger(Space4Cloud.class);
	public static final Logger consoleLogger = LoggerFactory.getLogger("ConsoleLogger");
	private boolean batch = false;
	private ConstraintHandler constraintHandler;
	private File initialSolution = null, initialMce = null;
	private String batchConfigurationFile;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	// Properties fired
	public static final String INITIALIZATION_COMPLEATED = "Inizialied";
	public static final String TRANSFORMATION_COMPLEATED = "PCM2LQNCompleated";
	public static final String RELAXED_SOLUTION_GENERATED = "RelaxedSolutionGenerated";
	public static final String SOLUTION_INITIALIZED = "Solution Initialized";
	public static final String OPTIMIZATION_ENDED = "optimizationEnded";
	public static final String ROBUSTNESS_CLOSED = "robustnessClosed";
	public static final String ASSESSMENT_ENDED = "assessmentEnded";
	public static final String ASSESSMENT_CLOSED = "assessmentClosed";
	public static final String ROBUSTNESS_ENDED = "robustnessEnded";
	public static final String BEST_SOLUTION_UPDATED = "bestSolutionUpdated";

	private boolean compleated = false;
	private StopWatch duration;

	public Space4Cloud() {

	}

	/**
	 * Initialize space4cloud in batch mode in order to run the configuration
	 * specified in the provided file
	 * 
	 * @param batchConfFile
	 *            the path to the file containing the configuration
	 */
	public Space4Cloud(String batchConfFile) {
		this(batchConfFile, null);
	}
	
	/**
	 * Initialize space4cloud in batch mode in order to run the configuration
	 * specified in the provided file
	 * 
	 * @param batchConfFile
	 *            the path to the file containing the configuration
	 */
	public Space4Cloud(String batchConfFile, StopWatch timer) {
		batchConfigurationFile = batchConfFile;
		batch = true;		
		duration = timer;
	}

	@Override
	public void run() {
		if (duration == null) {
			duration = new StopWatch();
			duration.start();
		}
		duration.split();

		// clear singleton instances from previous runs
		LineServerHandlerFactory.clearHandler();
		ConstraintHandlerFactory.clearHandler();

		// load the configuration
		if (!batch) {
			if (Configuration.PROJECT_BASE_FOLDER == null)
				Configuration.PROJECT_BASE_FOLDER = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
			ConfigurationWindow configGui = new ConfigurationWindow();
			configGui.setVisible(true);
			logger.trace("Witing for GUI disposal");

			while (!configGui.hasBeenDisposed()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error("Error waiting for GUI to be closed", e);
					signalError("An error occured in the GUI: " + e.getLocalizedMessage());
					return;

				}
			}

			if (configGui.isCancelled()) {
				consoleLogger.error("Execution cancelled by the user");
				cleanResources();
				return;
			}
			// release all resources, this should not be necessary
			configGui = null;
		} else {
			try {
				Configuration.loadConfiguration(batchConfigurationFile);
			} catch (IOException e) {
				logger.error("Could not load the configuration from: " + batchConfigurationFile, e);
				signalError("Could not load the configuration from: " + batchConfigurationFile);
				return;
			}
		}

		try {
			consoleLogger.debug("Fixing the Palladio files...");
			Configuration.fixPalladioFiles();
		} catch (Exception e) {
			logger.error("Error while fixing the Palladio files.", e);
		}

		consoleLogger.info("Cleaning the project from previous runs");
		// clean the folder used for the evaluation/optimization process
		try {
			FileUtils.deleteDirectory(
					Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY).toFile());
		} catch (NoSuchFileException e) {

			if (e.getMessage().contains("space4cloud"))
				logger.debug("Space4Cloud folder not present, nothing to clean");
			else
				logger.error("Error cleaning directories", e);
		} catch (IOException e) {
			if (e.getMessage().contains("cannot access the file")) {
				logger.error(
						"An error occurred while accessing a temporary file from a previous run, space4cloud will try to ignore it and proceed");
			} else {
				logger.error("Error cleaning directories", e);
			}
		}

		refreshProject();

		// initialize the connection to the database

		consoleLogger.info("Connecting to the resource model database");
		try {
			Configuration.initDatabaseConfiguration();
		} catch (Exception e) {
			consoleLogger.error("Error while initializing the connection to the database.", e);
			return;
		}

		// If the chosen solver is LINE try to connect to it or launch it
		// locally.
		if (Configuration.SOLVER == Solver.LINE) {
			consoleLogger.info("Looking for LINE server");
			LineServerHandler lineHandler = LineServerHandlerFactory.getHandler();
			lineHandler.connectToLINEServer();
			lineHandler.closeConnections();
			consoleLogger.info("Succesfully connected to LINE server");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Error while waiting for LINE first connection, will try to proceed", e);
			} // give time to LINE to close the connection on his side
		}

		// Build the temporary space4cloud folder
		try {
			consoleLogger.info("Creating directories");
			Files.createDirectories(Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY));
		} catch (IOException e) {
			logger.error("Error creating the " + Configuration.PERFORMANCE_RESULTS_FOLDER + " folder", e);
			signalError("An error occurred creating the " + Configuration.PERFORMANCE_RESULTS_FOLDER + " folder. "
					+ e.getLocalizedMessage());
			return;
		}

		// Build the run configuration
		RunConfigurationsHandler runConfigHandler = new RunConfigurationsHandler();

		refreshProject();

		pcs.firePropertyChange(INITIALIZATION_COMPLEATED, false, true);
		// launch it
		consoleLogger.info("Launching Palladio transformation.");

		try {
			runConfigHandler.launch();
		} catch (PalladioRunException e) {
			logger.error("Error running Palladio", e);
			signalError("An error occurred running Palladio" + e.getLocalizedMessage());
			return;
		}

		// bring up the Space4Cloud console
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (IConsole c : consoles) {
			if (c.getName().equals(PluginConsoleAppender.CONSOLE_NAME))
				ConsolePlugin.getDefault().getConsoleManager().showConsoleView(c);
		}
		consoleLogger.info("Transformation terminated");
		pcs.firePropertyChange(TRANSFORMATION_COMPLEATED, false, true);

		// Build the folder structure to host results and copy the LQN model in
		// those folders
		File resultDirPath = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,
				Configuration.PERFORMANCE_RESULTS_FOLDER).toFile();
		// list files excluding the result file generated by the solver
		File[] modelFiles = resultDirPath.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml") && !name.contains("_line");
			}
		});
		File[] resultFiles = resultDirPath.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("_line.xml") || name.endsWith(".lqxo");
			}
		});

		// if the palladio run has not produced a lqn model exit

		if (modelFiles.length != 1) {
			signalError("The Palladio transformation did not produce a valid LQN model");
			return;
		}
		if (resultFiles.length != 1) {
			signalError("An error occured in the evaluation od the initial LQN model");
			return;
		}

		consoleLogger.info("Parsing the constraints");

		// Parse the constraints and initialize the handler
		constraintHandler = ConstraintHandlerFactory.getConstraintHandler();
		try {
			constraintHandler.loadConstraints();
		} catch (ConstraintLoadingException e) {
			logger.error("Error in loading constraints", e);
			signalError("An error occured loading the constraints.\n" + e.getLocalizedMessage());
			return;
		}

		if (Configuration.RELAXED_INITIAL_SOLUTION && Configuration.FUNCTIONALITY == Operation.Optimization) {
			performGenerateInitialSolution();
			pcs.firePropertyChange(RELAXED_SOLUTION_GENERATED, false, true);
		}

		processEnded = false;

		switch (Configuration.FUNCTIONALITY) {
		case Assessment:
			try {
				consoleLogger.info("Performing Assesment");
				performAssessment();
				// consoleLogger.info("Assesment finished");
			} catch (AssesmentException e) {
				logger.error("Error in performing the assesment", e);
				signalError("An error occured performing the assesment.\n" + e.getLocalizedMessage());
				return;
			}
			break;

		case Optimization:
			try {

				consoleLogger.info("Performing Optimization");
				performOptimization();
				// consoleLogger.info("Optimization finished");
			} catch (OptimizationException e) {
				logger.error("Error in the optimization", e);
				signalError("An error occured performing the optimization.\n" + e.getLocalizedMessage());
				return;
			}
			break;

		case Robustness:
			try {
				consoleLogger.info("Performing Robustness Analysis");
				performRobustnessAnalysis();
				// consoleLogger.info("Robustness Analysis finished");
			} catch (RobustnessException e) {
				logger.error("Error in the robustness analysis", e);
				signalError("An error occured performing the robustness analysis.\n" + e.getLocalizedMessage());
				return;
			}
			break;

		default:
			logger.info("User exit at functionality choiche");
			break;
		}

		refreshProject();
		logger.debug("Space4Cloud sleeping while the engine works..");
		while (!compleated) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error("Waiting for space4cloud completion error", e);
			}
		}

		duration.stop();
		return;
	}

	private void signalError(String message) {
		consoleLogger.error(message);
		OptimizationProgressWindow.signalError(message);
		cleanResources();
	}

	/**
	 * Performs the assessment of a solution evaluating utilization and response
	 * times.
	 * 
	 * @param confHandler
	 *            the handler of the loaded configuration
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private void performAssessment() throws AssesmentException {

		try {
			engine = new OptimizationEngine(constraintHandler, true, duration);
		} catch (DatabaseConnectionFailureExteption e) {
			throw new AssesmentException("", e);
		}

		// load the initial solution from the PCM specified in the
		// configuration and the extension
		logger.info("Parsing The Solution");
		try {
			File mce = Paths.get(Configuration.MULTI_CLOUD_EXTENSION).toFile();
			if (!mce.exists())
				mce = null;
			engine.loadInitialSolution(null, mce);

		} catch (InitializationException e) {
			throw new AssesmentException("", e);
		}

		pcs.firePropertyChange(SOLUTION_INITIALIZED, false, true);

		SolutionMulti providedSolution = engine.getInitialSolution();

		assesmentWindow = new AssessmentWindow(constraintHandler);

		assesmentWindow.addPropertyChangeListener(this);

		try {
			assesmentWindow.considerSolution(providedSolution);
		} catch (NumberFormatException | IOException e) {
			throw new AssesmentException("Could not load the solution in the assesment window", e);
		}

	}

	private void cleanResources() {
		cleanResources(false);
	}

	private void cleanResources(boolean keepThingsOpened) {
		if (engine != null) {
			engine.exportSolution();
			engine.cancel(true);
		}

		if (!keepThingsOpened) {
			BestSolutionExplorer.show();
			engine = null;
			logger.info("Exiting SPACE4Cloud");
		
			if (Configuration.SOLVER == Solver.LINE) {
				if (LineServerHandlerFactory.getHandler() != null) {
					LineServerHandlerFactory.getHandler().closeConnections();
				}
			}

			
			LineServerHandlerFactory.clearHandler();
			ConstraintHandlerFactory.clearHandler();
			DataHandlerFactory.resetHandler();
			DatabaseConnector.closeConnection();
			refreshProject();
			// FileUtils.deleteQuietly(Paths.get(Configuration.PRIVATE_CLOUD_HOSTS_TMP).toFile());
			compleated = true;
		}
	}

	private void performGenerateInitialSolution() {
		consoleLogger.info("Generating the initial solution with the MILP engine");
		MILPEvaluator re = new MILPEvaluator();

		try {
			re.eval();
		} catch (Exception e) {
			logger.error("Error! It's impossible to generate the solution! Are you connected?", e);
			cleanResources();

			return;
		}

		// override values provided with those generated by the initial solution
 		Configuration.RESOURCE_ENVIRONMENT_EXTENSION = re.getResourceEnvExt().getAbsolutePath();
		initialSolution = re.getSolution();
		initialMce = re.getMultiCloudExt();

		logger.info("Generated resource model extension: " + Configuration.RESOURCE_ENVIRONMENT_EXTENSION);
		logger.info("Generated solution: " + initialSolution.getAbsolutePath());
		logger.info("Generated multi cloud extension: " + initialMce.getAbsolutePath());
		logger.info("Cost: " + re.getCost() + ", computed in: " + re.getEvaluationTime() + " ms");

		if (SolutionMulti.isEmpty(initialSolution)) {
			Configuration.RESOURCE_ENVIRONMENT_EXTENSION = null;
			logger.error("The generated solution is empty!");
			cleanResources();

			return;
		}

	}

	/**
	 * Performs the optimization process to find a feasible solution that
	 * minimizes costs
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */

	private void performOptimization() throws OptimizationException {

		// Build a new Optimization Engine engine and an empty initial
		// solution
		logger.info("Loading the optimization engine and perparing the solver");

		try {
			engine = new OptimizationEngine(constraintHandler, batch,duration);
		} catch (DatabaseConnectionFailureExteption e) {
			throw new OptimizationException("Optinization error. ", e);
		}

		engine.addPropertyChangeListener(this);

		// create the progress window
		if (!batch) {
			progressWindow = new OptimizationProgressWindow();
			progressWindow.setCostLogger(engine.getCostLogger());
			progressWindow.setVMLogger(engine.getVMLogger());
			progressWindow.setConstraintsLogger(engine.getConstraintsLogger());
			progressWindow.setMax(100);
			progressWindow.addPropertyChangeListener(this);
			engine.addPropertyChangeListener(progressWindow);
			engine.getEvalServer().addPropertyChangeListener(progressWindow);
		}

		// load the initial solution from the PCM specified in the
		// configuration and the extension
		logger.info("Parsing The Solution");
		try {
			engine.loadInitialSolution(initialSolution, initialMce);

		} catch (InitializationException e) {
			throw new OptimizationException("Error in loading the initial solution", e);
		}

		// // create the progress window
		// if (!batch) {
		// progressWindow = new OptimizationProgressWindow();
		// progressWindow.setCostLogger(engine.getCostLogger());
		// progressWindow.setVMLogger(engine.getVMLogger());
		// progressWindow.setConstraintsLogger(engine.getConstraintsLogger());
		// progressWindow.setMax(100);
		// progressWindow.addPropertyChangeListener(this);
		// engine.addPropertyChangeListener(progressWindow);
		// engine.getEvalServer().addPropertyChangeListener(progressWindow);
		//
		// }

		// start the optimization
		pcs.firePropertyChange(SOLUTION_INITIALIZED, false, true);

		logger.info("Starting the optimization");
		engine.execute();
	}

	private void performVariability() throws OptimizationException {

		if (Configuration.ROBUSTNESS_VARIABILITIES.length == 0 || Configuration.ROBUSTNESS_GS.length == 0
				|| Configuration.USE_PRIVATE_CLOUD)
			return;
		for (int variability : Configuration.ROBUSTNESS_VARIABILITIES)
			if (variability <= 0)
				return;

		logger.info("Considering the variability");

		if (executor == null)
			executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

		File results = Paths
				.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY, DataExporter.RESULT_CSV)
				.toFile();

		for (int variability : Configuration.ROBUSTNESS_VARIABILITIES)
			performVariability(variability, Configuration.ROBUSTNESS_GS, results);

		try {
			Files.createFile(Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,
					"variability-ended.xml"));
		} catch (IOException e) {
			logger.error("Error while creating the file for ending the variability test.", e);
		}

	}

	private void performVariability(int consideredVariability, int[] consideredG, File append)
			throws OptimizationException {

		if (consideredVariability <= 0) {
			logger.warn("The variability of {}% is too low, exiting.", consideredVariability);
			return;
		}
		if (consideredVariability >= 100) {
			logger.warn("It's impossible to consider a variability of {}%. Considering a variability of 99% instead.",
					consideredVariability);
			consideredVariability = 99;
		}
		for (int i = 0; i < consideredG.length; ++i) {
			if (consideredG[i] < 1)
				consideredG[i] = 1;
			else if (consideredG[i] > DataExporter.DEFAULT_T)
				consideredG[i] = DataExporter.DEFAULT_T;
		}

		String tmpConf = null;
		try {
			tmpConf = Files.createTempFile("space4cloud", ".properties").toString();
		} catch (IOException e) {
			throw new OptimizationException("Error creating a temporary file", e);
		}
		try {
			Configuration.saveConfiguration(tmpConf);
		} catch (IOException e) {
			throw new OptimizationException("Error saving the configuration", e);
		}

		String baseWorkingDirectory = Paths.get(Configuration.WORKING_DIRECTORY).toString();

		Path resultsFolder = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY);
		if (!resultsFolder.toFile().exists()) {
			logger.error("The results folder (" + resultsFolder.toString() + ") doesn't exists!");
			return;
		}

		File initialSolution = Paths.get(resultsFolder.toString(),
				Configuration.SOLUTION_LIGHT_FILE_NAME + Configuration.SOLUTION_FILE_EXTENSION).toFile();

		List<String> providers = SolutionMulti.getAllProviders(initialSolution);
		if (providers.size() != 1 || !providers.get(0).equals("Amazon")) {
			logger.error("It only works when considering solutions single-provider on Amazon.");
			return;
		}

		Configuration.SCRUMBLE_ITERS = 2;
		Configuration.REDISTRIBUTE_WORKLOAD = false;
		Configuration.USE_PRIVATE_CLOUD = false;
		Configuration.RESOURCE_ENVIRONMENT_EXTENSION = Paths.get(resultsFolder.toString(),
				Configuration.SOLUTION_FILE_NAME + "Total" + Configuration.SOLUTION_FILE_EXTENSION).toString();
		// Configuration.CONTRACTOR_TEST = false;
		boolean contractorTest = Configuration.CONTRACTOR_TEST;

		TreeMap<Integer, File> usageModelExts = new TreeMap<Integer, File>();
		File usageModelExt = new File(Configuration.USAGE_MODEL_EXTENSION);
		int highestPeak = getMaxPopulation(usageModelExt);
		try {
			Files.copy(
					Paths.get(resultsFolder.toString(),
							Configuration.SOLUTION_FILE_NAME + "Total" + Configuration.SOLUTION_FILE_EXTENSION),
					Paths.get(resultsFolder.toString(), Configuration.SOLUTION_FILE_NAME + "-" + highestPeak
							+ Configuration.SOLUTION_FILE_EXTENSION),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error("Error when copying and moving the files", e);
		}

		try {
			double key = 1.0 - (consideredVariability / 100.0);
			usageModelExts.put((int) Math.round(key * highestPeak), generateModifiedUsageModelExt(usageModelExt, key));
			key = 1.0 + (consideredVariability / 100.0);
			usageModelExts.put((int) Math.round(key * highestPeak), generateModifiedUsageModelExt(usageModelExt, key));
		} catch (JAXBException | IOException | SAXException e) {
			throw new OptimizationException("Error creating a modified usage model extension", e);
		}

		Configuration.ROBUSTNESS_VARIABILITIES = new int[] { 0 };
		Configuration.FUNCTIONALITY = Operation.Optimization;

		try {
			Configuration.CONSTRAINTS = ConstraintHandler
					.generateConstraintsForVariabilityTest(new File(Configuration.CONSTRAINTS), initialSolution)
					.getAbsolutePath();
		} catch (Exception e) {
			throw new OptimizationException("Error while getting the modified constraints file.", e);
		}

		// Build a new Optimization Engine engine and an empty initial
		// solution
		logger.info("Loading the optimization engine and perparing the solver for the variability test");

		for (Integer key : usageModelExts.keySet()) {
			File ume = usageModelExts.get(key);
			Configuration.USAGE_MODEL_EXTENSION = ume.getAbsolutePath();

			Configuration.WORKING_DIRECTORY = Paths.get(baseWorkingDirectory, "" + key).toString();
			String tmpConfRun = null;
			try {
				tmpConfRun = Files.createTempFile("space4cloud", ".properties").toString();
			} catch (IOException e) {
				throw new OptimizationException("Error creating a temporary file", e);
			}
			try {
				Configuration.saveConfiguration(tmpConfRun);
			} catch (IOException e) {
				throw new OptimizationException("Error saving the configuration", e);
			}

			Space4Cloud s4c = new Space4Cloud(tmpConfRun);

			Future<?> fut = executor.submit(s4c);
			try {
				fut.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new OptimizationException("Error getting the result from the Future", e);
			}

			File solution = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,
					Configuration.SOLUTION_LIGHT_FILE_NAME + Configuration.SOLUTION_FILE_EXTENSION).toFile();

			boolean found = false;
			while (!found) {
				if (solution.exists()) {
					found = true;

					Path performanceResults = Paths.get(solution.getParent(), Configuration.PERFORMANCE_RESULTS_FOLDER);
					FileUtils.deleteQuietly(performanceResults.toFile());

					// TODO: here

					try {
						Files.copy(Paths.get(solution.getAbsolutePath()),
								Paths.get(resultsFolder.toString(),
										Configuration.SOLUTION_FILE_NAME + "-" + highestPeak + "-" + key
												+ Configuration.SOLUTION_FILE_EXTENSION),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						throw new OptimizationException("Error copying: " + solution.getAbsolutePath() + " to: "
								+ resultsFolder.toString() + Configuration.SOLUTION_FILE_NAME + "-" + highestPeak + "-"
								+ key + Configuration.SOLUTION_FILE_EXTENSION, e);
					}

					try {
						Files.copy(Paths.get(solution.getParent(), "costs.xml"),
								Paths.get(resultsFolder.toString(), "costs-" + highestPeak + "-" + key + ".xml"),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						throw new OptimizationException("Error copying: " + solution.getAbsolutePath() + " to: "
								+ resultsFolder.toString() + Configuration.SOLUTION_FILE_NAME + "-" + highestPeak + "-"
								+ key + Configuration.SOLUTION_FILE_EXTENSION, e);
					}

					if (contractorTest)
						try {
							Files.copy(
									Paths.get(solution.getParent(),
											it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_NAME
													+ it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_EXTENSION),
									Paths.get(resultsFolder.toString(),
											it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_NAME
													+ "-" + highestPeak + "-" + key
													+ it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_EXTENSION),
									StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							throw new OptimizationException("Error copying: " + solution.getAbsolutePath() + " to: "
									+ resultsFolder.toString() + Configuration.SOLUTION_FILE_NAME + "-" + highestPeak
									+ "-" + key + Configuration.SOLUTION_FILE_EXTENSION, e);
						}
				}

				if (!found) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						logger.error("Error while waiting.", e);
					}
				}
			}
		}

		try {
			Configuration.loadConfiguration(tmpConf);
		} catch (IOException e) {
			throw new OptimizationException("Error loading the configuration", e);
		}

		logger.info("Exporting the data in the simplified output format...");

		List<File> generatedFiles = new ArrayList<File>();

		for (int g : consideredG) {
			logger.info("Evaluating with a variability of {} and a g of {}...", consideredVariability, g);
			generatedFiles.addAll(DataExporter.evaluate(resultsFolder, highestPeak, consideredVariability, g)); // TODO:
																												// here
																												// add
																												// the
																												// sigma
																												// value
		}

		try {
			DataExporter.robustnessTest(Paths.get(resultsFolder.toString(),
					Configuration.SOLUTION_FILE_NAME + "-" + highestPeak + Configuration.SOLUTION_FILE_EXTENSION)
					.toFile(), generatedFiles, append);
		} catch (Exception e) {
			logger.error("Error while exporting the results of the robustness test.", e);
		}

	}

	private ThreadPoolExecutor executor;

	/**
	 * Performs the analysis of the robustness of the solution by running the
	 * optimization against different workloads
	 * 
	 * @param attempts
	 *            The number of tests to perform for each problem
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws JAXBException
	 */
	private void performRobustnessAnalysis() throws RobustnessException {

		// if we want to check the robustness of the solution, a number of
		// modifications of the usage model file must be created.
		// ArrayList<File> usageModelExtFiles = new ArrayList<File>();
		TreeMap<Integer, File> usageModelExtFiles = new TreeMap<Integer, File>();
		File usageModelExtFile = new File(Configuration.USAGE_MODEL_EXTENSION);

		int highestPeak = getMaxPopulation(usageModelExtFile);
		int testFrom = Configuration.ROBUSTNESS_PEAK_FROM;
		int testTo = Configuration.ROBUSTNESS_PEAK_TO;
		int stepSize = Configuration.ROBUSTNESS_STEP_SIZE;
		int attempts = Configuration.ROBUSTNESS_ATTEMPTS;

		double x = testFrom, basex = x;

		try {
			usageModelExtFile = generateModifiedUsageModelExt(usageModelExtFile, x / highestPeak);
		} catch (JAXBException | IOException | SAXException e) {
			throw new RobustnessException("Error generating the mofigied usage model extension", e);
		}
		Configuration.USAGE_MODEL_EXTENSION = usageModelExtFile.getAbsolutePath();

		try {
			for (; x <= testTo; x += stepSize) {
				usageModelExtFiles.put((int) x, generateModifiedUsageModelExt(usageModelExtFile, x / basex));
			}
		} catch (JAXBException | IOException | SAXException e) {
			throw new RobustnessException("Error generating the mofigied usage model extension", e);
		}
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

		robustnessWindow = new RobustnessProgressWindow(usageModelExtFiles.size());

		robustnessWindow.addPropertyChangeListener(this);

		int tests = (int) Math.ceil(((testTo - testFrom) / stepSize));
		if (tests == 0)
			tests = 1;
		String duration = durationToString(1000 * (attempts * tests) * 5 * 60);

		logger.info("Starting the robustness test, considering each problem " + attempts
				+ " times (it could take up to " + duration + ")...");

		StopWatch timer = new StopWatch();
		timer.start();
		timer.split();

		List<File> solutions = new ArrayList<File>();

		int terminated = 0;

		Path resultsFolder = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY, "results");
		int i = 0;
		while (Files.exists(resultsFolder) && !Configuration
				.sameConfiguration(Paths.get(resultsFolder.toString(), "space4cloud-bak.properties").toString())) {
			resultsFolder = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,
					"results-" + i);
			++i;
		}
		try {
			Files.createDirectory(resultsFolder);
		} catch (IOException e) {
			throw new RobustnessException("Error creating directories", e);
		}
		logger.info("The results will be put in this folder: " + resultsFolder.toString());

		// int step=0;
		int el = 0;

		String bakConf = Paths.get(resultsFolder.toString(), "space4cloud-bak.properties").toString(); // Files.createTempFile("space4cloud",
																										// "-bak.properties").toString();
		String baseWorkingDirectory = Paths.get(Configuration.WORKING_DIRECTORY).toString();
		try {
			Configuration.saveConfiguration(bakConf);
		} catch (IOException e) {
			throw new RobustnessException("Error saving the configuration", e);
		}
		try {
			FileUtils.deleteDirectory(Paths.get(Configuration.PROJECT_BASE_FOLDER, baseWorkingDirectory,
					Configuration.PERFORMANCE_RESULTS_FOLDER).toFile());
		} catch (IOException e) {
			throw new RobustnessException("Error cleaning directory: " + Paths.get(Configuration.PROJECT_BASE_FOLDER,
					baseWorkingDirectory, Configuration.PERFORMANCE_RESULTS_FOLDER), e);
		}
		// try {
		// cleanFolders(Paths.get(Configuration.PROJECT_BASE_FOLDER,
		// baseWorkingDirectory, "attempts"));
		// } catch (Exception e) { }

		for (int key : usageModelExtFiles.keySet()) {
			File f = usageModelExtFiles.get(key);

			// step++;
			File bestSolution = null;
			double bestCost = Double.MAX_VALUE;

			Configuration.USAGE_MODEL_EXTENSION = f.getAbsolutePath();
			Configuration.FUNCTIONALITY = Operation.Optimization;

			try {
				Files.copy(Paths.get(f.getAbsolutePath()), Paths.get(resultsFolder.toString(), "ume-" + key + ".xml"),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
			}

			int robustnessVariability = Configuration.ROBUSTNESS_VARIABILITIES[0];
			boolean relaxedInitialSolution = Configuration.RELAXED_INITIAL_SOLUTION;
			boolean contractorTest = Configuration.CONTRACTOR_TEST;

			for (int attempt = 1; attempt <= attempts; ++attempt) {

				Configuration.WORKING_DIRECTORY = Paths.get(baseWorkingDirectory, "" + key, "" + attempt).toString();
				Configuration.RANDOM_SEED = attempt;
				String tmpConf = null;
				try {
					tmpConf = Files.createTempFile("space4cloud", ".properties").toString();
				} catch (IOException e) {
					throw new RobustnessException("Error creating a temporary file", e);
				}
				try {
					Configuration.saveConfiguration(tmpConf);
				} catch (IOException e) {
					throw new RobustnessException("Error saving the configuration", e);
				}

				Space4Cloud s4c = new Space4Cloud(tmpConf);

				// if initialSolution isn't null, it was because we generated
				// it! so we must keep generating them!

				Future<?> fut = executor.submit(s4c);
				try {
					fut.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new RobustnessException("Error getting the result from the Future", e);
				}

				{

					// File g = Paths.get(c.PROJECT_PATH, resFolder,
					// "" + testValue + File.separator + attempt,
					// "solution.xml").toFile();

					File g = Paths
							.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,
									Configuration.SOLUTION_LIGHT_FILE_NAME + Configuration.SOLUTION_FILE_EXTENSION)
							.toFile();

					boolean found = false;
					while (!found) {
						if (g.exists()) {
							found = true;

							// save the solution

							/////////
							// Or don't, because we only need to save the best
							///////// one.

							// Files.createDirectories(Paths.get(Configuration.PROJECT_BASE_FOLDER,
							// baseWorkingDirectory, "attempts", "step"+step));
							// Files.copy(
							// Paths.get(g.getAbsolutePath()),
							// Paths.get(Configuration.PROJECT_BASE_FOLDER,
							// baseWorkingDirectory, "attempts", "step"+step,
							// Configuration.SOLUTION_LIGHT_FILE_NAME + attempt
							// + Configuration.SOLUTION_FILE_EXTENSION)
							// );

							// to save space on hd I remove the results as soon
							// as i get the solution.xml file, because that's
							// all I need

							{
								Path performanceResults = Paths.get(g.getParent(),
										Configuration.PERFORMANCE_RESULTS_FOLDER);

								FileUtils.deleteQuietly(performanceResults.toFile());

							}

							double cost = SolutionMulti.getCost(g);

							if (bestSolution == null || cost < bestCost) {
								// System.out.println("DEBUG: Best cost from " +
								// bestCost + " to " + cost);
								bestCost = cost;
								bestSolution = g;

								try {
									Files.copy(Paths.get(bestSolution.getAbsolutePath()),
											Paths.get(resultsFolder.toString(),
													Configuration.SOLUTION_FILE_NAME + "-" + key
															+ Configuration.SOLUTION_FILE_EXTENSION),
											StandardCopyOption.REPLACE_EXISTING);
								} catch (IOException e) {
									throw new RobustnessException("Error copying: " + bestSolution.getAbsolutePath()
											+ " to: " + resultsFolder.toString() + File.separator
											+ Configuration.SOLUTION_FILE_NAME + "-" + key
											+ Configuration.SOLUTION_FILE_EXTENSION, e);
								}

							}

						}

						if (!found) {
							try {
								Thread.sleep(1000);
							} catch (Exception e) {
								logger.error("Error while waiting.", e);
							}
						}
					}
				}
			}

			Configuration.WORKING_DIRECTORY = bestSolution.getParent()
					.substring(Configuration.PROJECT_BASE_FOLDER.length() + 1);

			if (relaxedInitialSolution)
				try {
					Files.copy(Paths.get(bestSolution.getParent(), "generated-solution.xml"),
							Paths.get(resultsFolder.toString(), "generated-solution-" + key + ".xml"),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new RobustnessException("Error copying generated solution: " + bestSolution.getAbsolutePath()
							+ " to: " + resultsFolder.toString() + File.separator + Configuration.SOLUTION_FILE_NAME
							+ "-" + key + Configuration.SOLUTION_FILE_EXTENSION, e);
				}

			try {
				Files.copy(Paths.get(bestSolution.getParent(), "costs.xml"),
						Paths.get(resultsFolder.toString(), "costs-" + key + ".xml"),
						StandardCopyOption.REPLACE_EXISTING);
				Files.copy(Paths.get(bestSolution.getParent(), "costs.xml"),
						Paths.get(bestSolution.getParent(), "costs-" + key + ".xml"),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new RobustnessException("Error copying costs: " + bestSolution.getAbsolutePath() + " to: "
						+ resultsFolder.toString() + File.separator + Configuration.SOLUTION_FILE_NAME + "-" + key
						+ Configuration.SOLUTION_FILE_EXTENSION, e);
			}

			if (contractorTest)
				try {
					Files.copy(
							Paths.get(bestSolution.getParent(),
									it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_NAME
											+ it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_EXTENSION),
							Paths.get(resultsFolder.toString(),
									it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_NAME
											+ "-" + key
											+ it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_EXTENSION),
							StandardCopyOption.REPLACE_EXISTING);
					Files.copy(
							Paths.get(bestSolution.getParent(),
									it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_NAME
											+ it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_EXTENSION),
							Paths.get(bestSolution.getParent(),
									it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_NAME
											+ "-" + key
											+ it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_EXTENSION),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new RobustnessException("Error copying generated costs: " + bestSolution.getAbsolutePath()
							+ " to: " + resultsFolder.toString() + File.separator + Configuration.SOLUTION_FILE_NAME
							+ "-" + key + Configuration.SOLUTION_FILE_EXTENSION, e);
				}

			if (robustnessVariability > 0) {
				try {
					Files.copy(Paths.get(resultsFolder.toString(), "ume-" + key + ".xml"),
							Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,
									"ume-" + key + ".xml"),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					throw new RobustnessException("Error while copying the usage model extensions.", e);
				}

				try {
					performVariability();
				} catch (OptimizationException e) {
					throw new RobustnessException("Error while performing the variability test.", e);
				}

				File useless = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,
						"variability-ended.xml").toFile();

				while (!useless.exists()) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						logger.error("Error while waiting.", e);
					}

					useless = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,
							"variability-ended.xml").toFile();
				}

				logger.info("Saving the data generated with the variability test...");
				List<File> generatedFiles = DataExporter.getAllGeneratedFiles(Paths.get(bestSolution.getParent()));

				for (File file : generatedFiles)
					try {
						Files.copy(file.toPath(), Paths.get(resultsFolder.toString(), file.getName()),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						throw new RobustnessException("Error copying generated evaluation: " + file.getAbsolutePath()
								+ " to: " + resultsFolder.toString() + File.separator + file.getName(), e);
					}

				File parent = Paths.get(bestSolution.getParent()).toFile();
				final int keyValue = key;
				File[] files = parent.listFiles(new FileFilter() {

					@Override
					public boolean accept(File pathname) {
						return (pathname.getName().startsWith(Configuration.SOLUTION_FILE_NAME + "-" + keyValue + "-"))
								|| (pathname.getName().startsWith("costs-" + keyValue + "-"))
								|| (pathname.getName().startsWith(
										it.polimi.modaclouds.space4cloud.contractor4cloud.Configuration.COSTS_FILE_NAME
												+ "-" + keyValue + "-"));
					}
				});

				for (File file : files)
					try {
						Files.copy(file.toPath(), Paths.get(resultsFolder.toString(), file.getName()),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						throw new RobustnessException(
								"Error copying: " + file.getAbsolutePath() + " to: " + resultsFolder.toString(), e);
					}

				File robustnessTest = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,
						DataExporter.RESULT_CSV).toFile();
				try {
					Files.copy(robustnessTest.toPath(), Paths.get(resultsFolder.toString(), robustnessTest.getName()),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new RobustnessException(
							"Error copying: " + robustnessTest.getAbsolutePath() + " to: " + resultsFolder.toString(),
							e);
				}

			}

			try {
				Configuration.loadConfiguration(bakConf);
			} catch (IOException e) {
				throw new RobustnessException("Error loading the configuration", e);
			}

			terminated++;
			solutions.add(bestSolution);

			try {
				robustnessWindow.add(usageModelExtFiles.get(key), solutions.get(el));
				robustnessWindow.setValue(terminated);
				robustnessWindow
						.save2png(Paths.get(Configuration.PROJECT_BASE_FOLDER, baseWorkingDirectory).toString());
			} catch (JAXBException | SAXException | IOException e) {
				logger.error("Error showing the results", e);
			}
			el++;

			FileUtils.deleteQuietly(
					Paths.get(Configuration.PROJECT_BASE_FOLDER, baseWorkingDirectory, key + "").toFile());

		}

		executor.shutdown();
		timer.stop();

		logger.info("Check ended!");

		String actualDuration = durationToString(timer.getTime());

		logger.info("Expected time of execution: " + duration + ", actual time of execution: " + actualDuration);

		robustnessWindow.testEnded();

		logger.info("Check ended!");

	}

	public static String durationToString(long duration) {
		String actualDuration = "";
		{
			int res = (int) TimeUnit.MILLISECONDS.toSeconds(duration);
			if (res > 60 * 60) {
				actualDuration += (res / (60 * 60)) + " h ";
				res = res % (60 * 60);
			}
			if (res > 60) {
				actualDuration += (res / 60) + " m ";
				res = res % 60;
			}
			actualDuration += res + " s";
		}

		return actualDuration;
	}

	private void refreshProject() {
		try {
			ResourcesPlugin.getWorkspace().getRoot().getProject(Configuration.getProjectName())
					.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			logger.error("Could not refresh the project", e);
		}

	}

	private static File generateModifiedUsageModelExt(File f, double deltaRatio)
			throws JAXBException, IOException, SAXException {
		UsageModelExtensions umes = XMLHelper.deserialize(f.toURI().toURL(), UsageModelExtensions.class);

		ClosedWorkload cw = umes.getUsageModelExtension().getClosedWorkload();
		if (cw != null)
			for (ClosedWorkloadElement we : cw.getWorkloadElement()) {
				we.setPopulation((int) (we.getPopulation() * deltaRatio));
			}

		OpenWorkload ow = umes.getUsageModelExtension().getOpenWorkload();
		if (ow != null)
			for (OpenWorkloadElement we : ow.getWorkloadElement()) {
				we.setPopulation((int) (we.getPopulation() * deltaRatio));
			}

		String s = Double.toString(deltaRatio);
		s = s.replace('.', '-');

		File g;

		g = File.createTempFile("ume" + s + "-", ".xml");
		XMLHelper.serialize(umes, UsageModelExtensions.class, new FileOutputStream(g));
		logger.info(g.getAbsolutePath());
		return g;

	}

	@SuppressWarnings("unused")
	private static File generateModifiedUsageModelExtAddingTheAugmentedPeak(File f, double deltaRatio)
			throws JAXBException, IOException, SAXException {

		int addendum = (int) (getMaxPopulation(f) * (deltaRatio - 1));

		UsageModelExtensions umes = XMLHelper.deserialize(f.toURI().toURL(), UsageModelExtensions.class);

		ClosedWorkload cw = umes.getUsageModelExtension().getClosedWorkload();
		if (cw != null)
			for (ClosedWorkloadElement we : cw.getWorkloadElement()) {
				we.setPopulation(Math.max(we.getPopulation() + addendum, 1));
			}

		OpenWorkload ow = umes.getUsageModelExtension().getOpenWorkload();
		if (ow != null)
			for (OpenWorkloadElement we : ow.getWorkloadElement()) {
				we.setPopulation(Math.max(we.getPopulation() + addendum, 1));
			}

		String s = Double.toString(deltaRatio);
		s = s.replace('.', '-');

		File g;

		g = File.createTempFile("ume" + s + "-", ".xml");
		XMLHelper.serialize(umes, UsageModelExtensions.class, new FileOutputStream(g));
		logger.info(g.getAbsolutePath());
		return g;

	}

	public static int getMaxPopulation(File usageModelExtension) {
		UsageModelExtensions umes = null;
		try {
			umes = XMLHelper.deserialize(usageModelExtension.toURI().toURL(), UsageModelExtensions.class);
		} catch (Exception e) {
			logger.error("Error while opening the file for getting the maximum population.", e);
			return -1;
		}

		int maxPopulation = -1;

		ClosedWorkload cw = umes.getUsageModelExtension().getClosedWorkload();
		if (cw != null) {
			for (ClosedWorkloadElement we : cw.getWorkloadElement()) {
				if (maxPopulation < we.getPopulation())
					maxPopulation = we.getPopulation();
			}
		} else {

			OpenWorkload ow = umes.getUsageModelExtension().getOpenWorkload();
			if (ow != null) {
				for (OpenWorkloadElement we : ow.getWorkloadElement()) {
					if (maxPopulation < we.getPopulation())
						maxPopulation = we.getPopulation();
				}
			} else {
				return -1;
			}
		}

		return maxPopulation;
	}

	private boolean processEnded = false;

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// when the worker with the engine has done
		if (evt.getSource().equals(engine) && ((evt.getPropertyName().equals("state")
				&& evt.getNewValue() == StateValue.DONE) || evt.getPropertyName().equals("finished")) && !engine.isCancelled()) {
			consoleLogger.info("Optimization ended");
			processEnded = true;
			pcs.firePropertyChange(OPTIMIZATION_ENDED, false, true);

			if (!batch)
				progressWindow.signalCompletion();

			cleanResources();
		}
		if (evt.getSource().equals(engine) && evt.getPropertyName().equals(OptimizationEngine.BEST_SOLUTION_UPDATED)) {
			pcs.firePropertyChange(Space4Cloud.BEST_SOLUTION_UPDATED, false, true);
		}
		// forward progress to window
		else if (evt.getSource().equals(engine) && evt.getPropertyName().equals("progress")) {
			consoleLogger.info("Progress: " + (int) evt.getNewValue());
		}
		// stop the optimization process if the user closes the window
		else if (evt.getSource().equals(progressWindow) && evt.getPropertyName().equals("WindowClosed")
				&& !processEnded) {
			if (engine != null) {
				engine.exportSolution();
				engine.cancel(true);
			}
			consoleLogger.info("Optimization Process cancelled by the user");
			pcs.firePropertyChange(OPTIMIZATION_ENDED, false, true);
			cleanResources();
		} else if (evt.getSource().equals(progressWindow) && evt.getPropertyName().equals("InspectSolution")) {
			if (engine != null)
				engine.inspect();

			// stop the optimization process if the user closes the window
		} else if (evt.getSource().equals(robustnessWindow) && evt.getPropertyName().equals("WindowClosed")
				&& !processEnded) {
			consoleLogger.info("Robustness Process cancelled by the user");
			pcs.firePropertyChange(ROBUSTNESS_CLOSED, false, true);
			try {
				executor.shutdownNow();
			} catch (Exception e) {
			}
			cleanResources();
		} else if (evt.getSource().equals(robustnessWindow) && evt.getPropertyName().equals("RobustnessEnded")) {
			consoleLogger.info("Robustness ended");
			processEnded = true;
			pcs.firePropertyChange(ROBUSTNESS_ENDED, false, true);
			if (!batch)
				robustnessWindow.signalCompletion();
			cleanResources();
		} else if (evt.getSource().equals(assesmentWindow) && evt.getPropertyName().equals("WindowClosed")
				&& !processEnded) {
			consoleLogger.info("Assessment window closed");
			pcs.firePropertyChange(ASSESSMENT_CLOSED, false, true);
			cleanResources();
		} else if (evt.getSource().equals(assesmentWindow) && evt.getPropertyName().equals("AssessmentEnded")) {
			consoleLogger.info("Assessment ended");
			processEnded = true;
			pcs.firePropertyChange(ASSESSMENT_ENDED, false, true);
			cleanResources();
		} else if (progressWindow != null && (evt.getPropertyName().equals(BestSolutionExplorer.PROPERTY_WINDOW_CLOSED)
				|| evt.getPropertyName().equals(BestSolutionExplorer.PROPERTY_ADDED_VALUE))) {
			progressWindow.propertyChange(evt);
		}

	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

}
