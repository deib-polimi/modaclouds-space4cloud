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
import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.schema.OpenWorkload;
import it.polimi.modaclouds.qos_models.schema.OpenWorkloadElement;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.chart.Logger2JFreeChartImage;
import it.polimi.modaclouds.space4cloud.chart.SeriesHandle;
import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnector;
import it.polimi.modaclouds.space4cloud.exceptions.InitalFolderCreationException;
import it.polimi.modaclouds.space4cloud.gui.AssesmentWindow;
import it.polimi.modaclouds.space4cloud.gui.LoadModel;
import it.polimi.modaclouds.space4cloud.gui.OptimizationProgressWindow;
import it.polimi.modaclouds.space4cloud.gui.RobustnessProgressWindow;
import it.polimi.modaclouds.space4cloud.gui.XMLFileSelection;
import it.polimi.modaclouds.space4cloud.optimization.OptEngine;
import it.polimi.modaclouds.space4cloud.optimization.PartialEvaluationOptimizationEngine;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.ConfigurationHandler;
import it.polimi.modaclouds.space4cloud.utils.Constants;
import it.polimi.modaclouds.space4cloud.utils.LoggerHelper;
import it.polimi.modaclouds.space4cloud.utils.ResourceEnvironmentExtensionParser;
import it.polimi.modaclouds.space4cloud.utils.ResourceEnvironmentExtentionLoader;
import it.polimi.modaclouds.space4cloud.utils.RunConfigurationsHandler;
import it.polimi.modaclouds.space4cloud.utils.RussianEvaluator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.time.StopWatch;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import de.uka.ipd.sdq.pcmsolver.runconfig.MessageStrings;

public class Space4Cloud extends SwingWorker<Object, Object> {

	public static enum Operations {
		Assessment, Optimization, Robustness, Exit;

		public static Operations getById(int id) {
			Operations[] values = Operations.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}

		public static int size() {
			return Operations.values().length;
		}
	}

	private static OptimizationProgressWindow progressWindow;
	private static AssesmentWindow assesmentWindow;
	private OptEngine engine = null;
	private Constants c;

	private static final Logger programLogger = LoggerHelper
			.getLogger(Space4Cloud.class);

	private static File generateModifiedUsageModelExt(File f, double deltaRatio)
			throws JAXBException, IOException, SAXException {
		UsageModelExtensions umes = XMLHelper.deserialize(f.toURI().toURL(),
				UsageModelExtensions.class);

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
		XMLHelper.serialize(umes, UsageModelExtensions.class,
				new FileOutputStream(g));
		programLogger.info(g.getAbsolutePath());
		return g;

	}

	public static int getMaxPopulation(File usageModelExtension) {
		UsageModelExtensions umes = null;
		try {
			umes = XMLHelper.deserialize(usageModelExtension.toURI().toURL(),
					UsageModelExtensions.class);
		} catch (Exception e) {
			e.printStackTrace();
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

	private boolean batch;
	private Operations functionality;
	private File resourceEnvironmentFile, usageFile, allocationFile,
	repositoryFile, lineConfFile, usageModelExtFile,
	resourceEnvExtFile, constraintFile;
	private File optimizationConfigurationFile;

	private String resFolder;

	private String solver;

	private ConstraintHandler constraintHandler;

	private static final String defaultDbConfigurationFile = "/config/DBConnection.properties";
	private String dbConfigurationFile = null;
	private int testFrom, testTo, step;

	private File initialSolution = null, initialMce = null;

	private List<String> providersInitialSolution = new ArrayList<String>();

	private int attempts = 5;


	public Space4Cloud() {
		this(false, null, null, "space4cloud", null, null, null, null, null,
				null, null, null, 100, 10000, 300, null, null);
	}

	public Space4Cloud(boolean batch, Operations functionality,
			File resourceEnvironmentFile, String resFolder, File usageFile,
			File allocationFile, File repositoryFile, String solver,
			File lineConfFile, File usageModelExtFile, File resourceEnvExtFile,
			File constraintFile, int testFrom, int testTo, int step, String databaseConnectionProperties, File optimizationConfigurationFile) {
		this.batch = batch;
		this.resFolder = resFolder;
		if (batch) {
			this.functionality = functionality;
			this.resourceEnvironmentFile = resourceEnvironmentFile;
			this.usageFile = usageFile;
			this.allocationFile = allocationFile;
			this.repositoryFile = repositoryFile;
			this.solver = solver;
			this.lineConfFile = lineConfFile;
			this.usageModelExtFile = usageModelExtFile;
			this.resourceEnvExtFile = resourceEnvExtFile;
			this.constraintFile = constraintFile;
			this.testFrom = testFrom;
			this.testTo = testTo;
			this.step = step;
			this.dbConfigurationFile = databaseConnectionProperties;
			this.optimizationConfigurationFile = optimizationConfigurationFile;
		}
	}

	public Space4Cloud(Operations operation, String basePath,
			File usageModelExtFile, File resourceEnvExtFile,
			File constraintFile, int testFrom, int testTo, int step, String databaseConnectionProperties, File optimizationConfigurationFile) {
		this(true, operation, Paths
				.get(basePath, "default.resourceenvironment").toFile(),
				"space4cloud", Paths.get(basePath, "default.usagemodel")
				.toFile(), Paths.get(basePath, "default.allocation")
				.toFile(), Paths.get(basePath, "default.repository")
				.toFile(), "LQNS (Layered Queueing Network Solver)",
				new File("LINE.properties"), usageModelExtFile,
				resourceEnvExtFile, constraintFile, testFrom, testTo, step,databaseConnectionProperties,optimizationConfigurationFile);
	}

	public Space4Cloud(Operations operation, String basePath,
			File usageModelExtFile, File resourceEnvExtFile,
			File constraintFile, int testFrom, int testTo, int step) {
		this(operation, basePath, usageModelExtFile, resourceEnvExtFile, constraintFile, testFrom, testTo, step, null, null);
	}






	/**
	 * Asks the user to choose the desired functionality
	 * 
	 * 0 -> Assesment 1 -> Optimization 2 -> Robustness 3 -> Exit /
	 */
	private void askForFunctionality() {
		// Object[] options = { Constants.ASSESSMENT, Constants.OPTIMIZATION,
		// Constants.ROBUSTNESS, Constants.CANCEL };

		Object[] options = new Object[Operations.size()];

		int i = 0;
		for (Operations o : Operations.values())
			options[i++] = o.toString();

		int id = JOptionPane.showOptionDialog(null,
				"Choose the desired functionality", "Functionality selection",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[options.length - 1]);

		functionality = Operations.getById(id);
	}

	private void askForSolver() {
		// Choose choice = new Choose("Choose the Solver",
		// "Which Solver do you want to use?");
		// if (!choice.isChosen()){
		// cleanExit();
		// return null;
		// }
		// solver = choice.getSolver();

		Object[] options = { "LQNS", "LINE", "Simucom", "Cancel" };
		int idSolver = JOptionPane.showOptionDialog(null,
				"Which Solver do you want to use?", "Choose the Solver",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[3]);

		programLogger.info("Solver selection: " + idSolver);

		switch (idSolver) {
		case 0:
			solver = MessageStrings.LQNS_SOLVER;
			break;
		case 1:
			solver = MessageStrings.PERFENGINE_SOLVER;
			break;
		case 2:
			solver = "Simucom";
			break;
		default:
			solver = null;
		}
	}

	private void askProvidersForInitialSolution() {
		DataHandler db = null;
		try {
			db = DataHandlerFactory.getHandler();
		} catch (DatabaseConnectionFailureExteption e) {
			programLogger.error("Error in connecting to the database",e);
			dirtyExit();
		}
		Set<String> providers = db.getCloudProviders();

		Object[] possibilities = new Object[providers.size() + 1];
		possibilities[0] = "None";
		int i = 1;
		for (String provider : providers)
			possibilities[i++] = provider;

		String s = (String) possibilities[0];
		do {
			s = (String) JOptionPane
					.showInputDialog(
							null,
							"Select a provider if you want to restrict the search, or none to continue:",
							"Initial Solution", JOptionPane.PLAIN_MESSAGE,
							null, possibilities, possibilities[0]);

			if (!s.equals(possibilities[0])
					&& !providersInitialSolution.contains(s))
				providersInitialSolution.add(s);

		} while (!s.equals(possibilities[0]));
	}

	private File askResourceEnvironmentExtensionFile()
			throws MalformedURLException {

		return loadExtensionFile("Load Resource Environment Extension",ResourceModelExtension.class);

	}


	private File askUsageModelExtensionFile()
			throws MalformedURLException {
		return loadExtensionFile("Load Usage Model Extension",UsageModelExtensions.class);

	}


	private File askConstraintFile()
			throws MalformedURLException {
		return loadExtensionFile("Load Constraints", Constraints.class);
	}

	private <T> File loadExtensionFile(String message, Class<T> clazz)
			throws MalformedURLException {
		File providedFile;
		XMLFileSelection extensionSelector = new XMLFileSelection(message);
		// keep asking the file until a valid file is provided or the user
		// pressed cancel
		do {
			extensionSelector.askFile();


			providedFile = extensionSelector.getFile();
			try {
				if (providedFile != null)
					XMLHelper.deserialize(providedFile.toURI().toURL(), clazz);
			} catch (JAXBException | SAXException e) {
				programLogger.error("The specified file (" + providedFile
						+ ") is not valid ", e);
				e.printStackTrace();
				providedFile = null;
			}
		} while (!extensionSelector.isCanceled() && providedFile == null);
		if (extensionSelector.isCanceled()) {
			cleanExit();
		}
		return providedFile;
	}





	private void buildFolderStructure(Path lqnModelPath, Path resultModelPath)
			throws IOException {
		return;

		// for (int i = 0; i < 24; i++) {
		// Path tmpFolderPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
		// c.PERFORMANCE_RESULTS_FOLDER, c.FOLDER_PREFIX + i);
		// Files.createDirectory(tmpFolderPath);
		// Path tmpLqnPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
		// c.PERFORMANCE_RESULTS_FOLDER, c.FOLDER_PREFIX + i,
		// lqnModelPath.getFileName().toString());
		// Files.copy(lqnModelPath, tmpLqnPath);
		// Path tmpResultPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
		// c.PERFORMANCE_RESULTS_FOLDER, c.FOLDER_PREFIX + i,
		// resultModelPath.getFileName().toString());
		// Files.copy(resultModelPath, tmpResultPath);
		// }
	}

	private void cleanExit() {
		programLogger.info("Exiting SPACE4Cloud");		
		//close the connection with the database
		try {
			if(DatabaseConnector.getConnection() != null)
				DatabaseConnector.getConnection().close();
		} catch (SQLException e) {
			programLogger.error("Error in closing the connection with the database",e);
		}
		if(engine != null){
			engine.cancel(true);
		}
	}

	private void dirtyExit(){
		programLogger.info("Exiting SPACE4Cloud because of an error");
		cleanExit();
		this.cancel(true);
	}

	@Override

	protected Object doInBackground() throws CoreException, InitalFolderCreationException {




		LoadModel lm;
		if (!batch) {

			functionality = Operations.Exit;

			askForFunctionality();

			// in case the user cancelled exit
			if (functionality == Operations.Exit) {
				cleanExit();
				return null;
			}
			programLogger.info("Chosen functionality: " + functionality);

			/* Load the resourceEnvironment */
			/* LoadModel */lm = new LoadModel(null, "Resource Model",
					".resourceenvironment");
			if (!lm.isChosen()) {
				cleanExit();
				return null;
			}
			/* File */resourceEnvironmentFile = lm.getModelFile();

		}

		/* Initialize constants */
		String modelsDirectory = resourceEnvironmentFile.getAbsolutePath();
		modelsDirectory = modelsDirectory.substring(0,
				modelsDirectory.lastIndexOf(File.separator));
		Constants.clear();
		Constants.setWorkingDirectory(modelsDirectory);
		c = Constants.getInstance();

		c.changeWorkingDirectory(resFolder);

		c.RESOURCE_MODEL = resourceEnvironmentFile.getAbsolutePath();

		/*
		 * Look for the config files generated by previous runs that stores
		 * other models
		 */
		Path confFilePath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY
				+ c.CONFIG_FILE_NAME);
		ConfigurationHandler confHandler = new ConfigurationHandler(
				confFilePath);

		// if there exists a configuration file
		if (confFilePath.toFile().exists()) {

			// clean all the files from previous run
			Path workingFolder = confFilePath.getParent();
			// get all the files and folders but the configuration file and log
			// files
			File[] filesToRemove = workingFolder.toFile().listFiles(
					new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return !(name.equals(c.CONFIG_FILE_NAME) || name
									.contains(".log"));
						}
					});

			for (File file : filesToRemove)
				if (file.isDirectory()) {
					try {
						ConfigurationHandler.cleanFolders(file.getAbsolutePath());
					} catch (IOException e) {
						programLogger.error("Error cleaning directories",e);
					}
				} else {
					file.delete();
				}

			refreshProject();
			// load the new configuration
			try {
				confHandler.loadConfiguration();
			} catch (IOException e) {
				programLogger.error("Error in loading the configuration", e);
			}

			usageFile = new File(c.USAGE_MODEL);
			allocationFile = new File(c.ALLOCATION_MODEL);
			repositoryFile = new File(c.REPOSITORY_MODEL);
			solver = c.SOLVER;
			lineConfFile = new File(c.LINE_PROPERTIES_FILE);
			dbConfigurationFile = c.DB_CONNECTION_SETTINGS;

		} else {
			// if there is no configuration file then generate a valid
			// configuration
			// delete any old space4cloud folder
			try {
				ConfigurationHandler.cleanFolders(c.ABSOLUTE_WORKING_DIRECTORY);
			}	catch (NoSuchFileException e) {
				if(e.getMessage().contains("space4cloud"))
					programLogger.info("Space4Cloud folder not present");
				else
					programLogger.error("Error cleaning directories",e);
			}
			catch (IOException e) {
				programLogger.error("Error cleaning directories",e);
			}

			if (!batch) {
				// load the usage model
				lm = new LoadModel(null, "Usage Model", ".usagemodel");
				if (!lm.isChosen()) {
					cleanExit();
					return null;
				}
				usageFile = lm.getModelFile();

				// load the allocation model
				lm = new LoadModel(null, "Allocation Model", ".allocation");
				if (!lm.isChosen()) {
					cleanExit();
					return null;
				}
				allocationFile = lm.getModelFile();

				// load the repository model
				lm = new LoadModel(null, "Repository Model", ".repository");
				if (!lm.isChosen()) {
					cleanExit();
					return null;
				}
				repositoryFile = lm.getModelFile();

				// load the solver
				askForSolver();

				if (solver == null) {
					cleanExit();
					return null;
				}

				// if the solver is LINE it needs the path to its configuration
				// file
				if (solver.equals(MessageStrings.PERFENGINE_SOLVER)) {
					lm = new LoadModel(null, "Performance Engine Config",
							".properties");
					if (!lm.isChosen()) {
						cleanExit();
						return null;
					}
					lineConfFile = lm.getModelFile();
				}
			}
			c.USAGE_MODEL = usageFile.getAbsolutePath();
			c.ALLOCATION_MODEL = allocationFile.getAbsolutePath();
			c.REPOSITORY_MODEL = repositoryFile.getAbsolutePath();
			c.SOLVER = solver;
			if (lineConfFile != null) {
				c.LINE_PROPERTIES_FILE = lineConfFile.getAbsolutePath();
			}

			//loads the database connection file
			if(!batch)
				dbConfigurationFile = askDatabaseConnectionFile();

			// save the configuration, it will be run later in the optimization
			// process
			try {
				confHandler.saveConfiguration();
			} catch (IOException e) {
				programLogger.error("Error in saving the configuration", e);
			}
			// refresh the project in the workspace
			refreshProject();

		}

		//initialize the connection to the database
		try {
			InputStream dbConfigurationStream = null;
			//load the configuration file if specified 
			if(dbConfigurationFile != null && Paths.get(dbConfigurationFile).toFile().exists())
				dbConfigurationStream = new FileInputStream(Paths.get(dbConfigurationFile).toFile());
			//if the file file has not been specified or it does not exist use the one with default values embedded in the plugin
			else
				dbConfigurationStream = this.getClass().getResourceAsStream(defaultDbConfigurationFile);
			DatabaseConnector.initConnection(dbConfigurationStream);	
			RussianEvaluator.setDatabaseInformation(DatabaseConnector.url,
													DatabaseConnector.dbName,
													DatabaseConnector.driver,
													DatabaseConnector.userName,
													DatabaseConnector.password);
			dbConfigurationStream.close();
		} catch (SQLException e1) {
			programLogger.error("Could not initialize database connection",e1);
			dirtyExit();
		} catch (IOException e1) {
			programLogger.error("Could not load the Database configuration, will use the default one");
		}

		// refresh the project in the workspace
		refreshProject();

		// Build the run configuration
		RunConfigurationsHandler runConfigHandler = new RunConfigurationsHandler();
		// launch it
		runConfigHandler.launch();

		// refresh the project in the workspace
		refreshProject();
		// Build the folder structure to host results and copy the LQN model in
		// those folders
		File resultDirPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
				c.PERFORMANCE_RESULTS_FOLDER).toFile();
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
		if (modelFiles.length != 1 || resultFiles.length != 1) {
			programLogger
			.error("The first initialization run has encounter some problem during the generation of the first solution");
			programLogger.error("SPACE4CLOUD will now exit.");
			dirtyExit();
			return null;
		}

		// there should be just 1 palladio model
		Path lqnModelPath = modelFiles[0].toPath();
		// with the corresponding evaluation
		Path resultModelPath = resultFiles[0].toPath();

		// build the folder structure
		try {
			buildFolderStructure(lqnModelPath, resultModelPath);
		} catch (IOException e) {
			throw new InitalFolderCreationException(e);
		}
		// refresh the workspace
		refreshProject();

		if (!batch) {

			/* File */try {
				usageModelExtFile = askUsageModelExtensionFile();
			} catch (MalformedURLException e) {
				programLogger.error(
						"Error in loading the usage model extension", e);
			}
		}
		if (usageModelExtFile == null) {
			programLogger
			.info("No usage model extension selected. Quitting SPACE4CLOUD");
			dirtyExit();
			return null;
		}
		// c.RESOURCE_ENV_EXT_FILE = usageModelExtFile.getAbsolutePath();
		c.USAGE_MODEL_EXT_FILE = usageModelExtFile.getAbsolutePath();

		/* Load the Constraint file */
		if (!batch) {
			try {
				constraintFile = askConstraintFile();
			} catch (MalformedURLException e) {
				programLogger.error("Error in loading the constraint file", e);
			}

		}

		// Parse the constraints and initialize the handler
		constraintHandler = new ConstraintHandler();
		try {
			constraintHandler.loadConstraints(constraintFile);
		} catch (ParserConfigurationException | SAXException | IOException
				| JAXBException e) {
			programLogger.error("Error in loading constraints", e);
		}

		// load the extension file
		if (!batch) {
			try {

				resourceEnvExtFile = askResourceEnvironmentExtensionFile();
			} catch (MalformedURLException e) {
				programLogger.error(
						"Error in loading the resource environment", e);
			}
		}

		int n = 1; // 0 = generate the solution by default, 1 otherwise
		if (!batch && functionality != Operations.Assessment) {
			/*
			 * Here I (Riccardo B. Desantis) am implementing the generation of
			 * the first solution using cplex and the tool made by Alexander
			 * Lavrentev.
			 */

			/* Ask if you want to produce the "intelligent" first solution */
			Object[] options = { "Yes", "No" };
			n = JOptionPane
					.showOptionDialog(
							null,
							"Do you want to generate a first optimal solution? (It might take some time)",
							"First solution", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options,
							options[1]);

			programLogger.debug("Initial Solution generation: " + n);
		}
		if (n == 0 && !batch && functionality == Operations.Robustness) {
			try {
				getProvidersFromExtension();
			} catch (ParserConfigurationException | SAXException | IOException
					| JAXBException e) {
				programLogger
				.error("Error in loading the selected providers from the resource environemnt extension file",
						e);
			}
			if (providersInitialSolution.size() == 0)
				askProvidersForInitialSolution();

			// override any other value specified with the ones obtained by the
			// initial optimization
			resourceEnvExtFile = new File("none");
			initialSolution = new File("none");

		} else if (n == 0) {
			try {
				getProvidersFromExtension();
			} catch (ParserConfigurationException | SAXException | IOException
					| JAXBException e) {
				programLogger
				.error("Error in loading the selected providers from the resource environemnt extension file",
						e);
			}
			if (providersInitialSolution.size() == 0)
				askProvidersForInitialSolution();
			performGenerateInitialSolution();
		}

		if (resourceEnvExtFile == null) {
			programLogger
			.warn("No resource model extension selected. Quitting SPACE4CLOUD");
			dirtyExit();
			return null;
		}

		// put the provided or generated extension in the constants
		c.RESOURCE_ENV_EXT_FILE = resourceEnvExtFile.getAbsolutePath();

		switch (functionality) {
		case Assessment:
			try {
				programLogger.info("Performing Assesment");
				performAssessment(confHandler);
			} catch (NumberFormatException | IOException
					| ParserConfigurationException | SAXException e) {
				programLogger.error("Error in performing the assesment", e);
			}
			break;

		case Optimization:
			try {
				programLogger.info("Performing Optimization");
				performOptimization();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				programLogger.error("Error in the optimization", e);
			}
			break;

		case Robustness:
			try {
				programLogger.info("Performing Robustness Analysis");
				performRobustnessAnalysis();
			} catch (ParserConfigurationException | SAXException | IOException
					| JAXBException e) {
				programLogger.error("Error in the robustness analysis", e);
			}
			break;

		default:
			programLogger.info("User exit at functionality choiche");
			break;
		}
		return null;
	}

	private String askDatabaseConnectionFile() {
		JFileChooser fileChooser = new JFileChooser(c.ABSOLUTE_WORKING_DIRECTORY);
		fileChooser.setDialogTitle("Load Database Connection Settings");
		int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			c.DB_CONNECTION_SETTINGS = fileChooser.getSelectedFile().getAbsolutePath();
			return c.DB_CONNECTION_SETTINGS;
		}
		else if(returnVal == JFileChooser.CANCEL_OPTION){
			programLogger.info("No database connection file has been chosen, default settings will be used");
			return null;
		}
		return null;
	}

	@Override
	protected void done() {
		try {
			get();
		} catch (ExecutionException e) {
			programLogger
			.error("Execution error while running space4cloud ", e);
		} catch (InterruptedException e) {
			programLogger.error("Interrupted execution of space4cloud ", e);
		}
	}

	private void getProvidersFromExtension()
			throws ParserConfigurationException, SAXException, IOException,
			JAXBException {
		// parse the extension file
		ResourceEnvironmentExtensionParser resourceEnvParser = new ResourceEnvironmentExtentionLoader(
				resourceEnvExtFile);
		providersInitialSolution = new ArrayList<String>();
		for (String s : resourceEnvParser.getProviders().values()) {
			if (!providersInitialSolution.contains(s))
				providersInitialSolution.add(s);
		}
	}

	public String[] getProvidersInitialSolution() {
		String[] res = new String[providersInitialSolution.size()];
		int i = 0;
		for (String s : providersInitialSolution)
			res[i++] = s;
		return res;
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
	private void performAssessment(ConfigurationHandler confHandler)
			throws NumberFormatException, IOException,
			ParserConfigurationException, SAXException {

		try {
			engine = new PartialEvaluationOptimizationEngine(
					constraintHandler, true);
		} catch (DatabaseConnectionFailureExteption e) {
			programLogger.error("Error in connecting to the database",e);
			dirtyExit();
		}

		// load the initial solution from the PCM specified in the
		// configuration and the extension
		programLogger.info("Parsing The Solution");
		try {
			engine.loadInitialSolution(resourceEnvExtFile, usageModelExtFile);
		} catch (JAXBException e) {
			programLogger.error("Error in loading the initial solution", e);
			dirtyExit();
		}

		// evaluate the solution
		programLogger.info("Evaluating the solution");
		engine.evaluate();

		// print the results
		SolutionMulti providedSolutions = engine.getInitialSolution();
		Solution providedSolution = providedSolutions.get(0);
		// TODO: we should consider the multi-provider solution here!

		assesmentWindow = new AssesmentWindow();
		// plotting the number of VMs
		Logger2JFreeChartImage vmLogger = new Logger2JFreeChartImage(
				"vmCount.properties");
		Map<String, SeriesHandle> vmSeriesHandlers = new HashMap<>();
		for (Tier t : providedSolution.getApplication(0).getTiers()) {
			vmSeriesHandlers.put(t.getId(), vmLogger.newSeries(t.getName()));
		}
		for (int i = 0; i < 24; i++) {
			for (Tier t : providedSolution.getApplication(i).getTiers()) {
				vmLogger.addPoint2Series(vmSeriesHandlers.get(t.getId()), i,
						((IaaS) t.getCloudService()).getReplicas());
			}
		}
		assesmentWindow.setVMLogger(vmLogger);

		// plotting the response Times
		Logger2JFreeChartImage rtLogger = new Logger2JFreeChartImage(
				"responseTime.properties");
		Map<String, SeriesHandle> rtSeriesHandlers = new HashMap<>();
		for (Tier t : providedSolution.getApplication(0).getTiers())
			for (Component c : t.getComponents())
				for (Functionality f : c.getFunctionalities())
					rtSeriesHandlers.put(f.getName(),
							rtLogger.newSeries(f.getName()));

		for (int i = 0; i < 24; i++)
			for (Tier t : providedSolution.getApplication(i).getTiers())
				for (Component c : t.getComponents())
					for (Functionality f : c.getFunctionalities()){
						if(f.isEvaluated())
							rtLogger.addPoint2Series(
									rtSeriesHandlers.get(f.getName()), i,
									f.getResponseTime());
					}
		assesmentWindow.setResponseTimeLogger(rtLogger);

		// plotting the utilization
		Logger2JFreeChartImage utilLogger = new Logger2JFreeChartImage(
				"utilization.properties");
		Map<String, SeriesHandle> utilSeriesHandlers = new HashMap<>();
		for (Tier t : providedSolution.getApplication(0).getTiers())
			utilSeriesHandlers.put(t.getId(), utilLogger.newSeries(t.getName()));

		for (int i = 0; i < 24; i++)
			for (Tier t : providedSolution.getApplication(i).getTiers())
				utilLogger.addPoint2Series(utilSeriesHandlers.get(t.getId()),
						i, ((Compute) t.getCloudService()).getUtilization());
		assesmentWindow.setUtilizationLogger(utilLogger);
		assesmentWindow.show();
		assesmentWindow.updateImages();

		// export the solution
		providedSolution.exportLight(c.ABSOLUTE_WORKING_DIRECTORY
				+ "solution.xml");
	}

	private void performGenerateInitialSolution() {
		// resourceEnvExtFile = null;
		// programLogger.warn("Generation of the first solution disabled at the moment!");

		// ///////////////////////////
		RussianEvaluator re = new RussianEvaluator(usageModelExtFile,
				constraintFile);

		if (providersInitialSolution.size() > 0)
			re.setProviders(getProvidersInitialSolution());

		try {
			re.eval();
		} catch (Exception e) {
			programLogger
			.error("Error! It's impossible to generate the solution! Are you connected?");
			e.printStackTrace();
			return;
		}

		// override values provided with those generated by the initial solution
		resourceEnvExtFile = re.getResourceEnvExt();
		initialSolution = re.getSolution();
		initialMce = re.getMultiCloudExt();

		programLogger.info("Generated resource model extension: "
				+ resourceEnvExtFile.getAbsolutePath());
		programLogger.info("Generated solution: "
				+ initialSolution.getAbsolutePath());
		programLogger.info("Generated multi cloud extension: "
				+ initialMce.getAbsolutePath());
		programLogger.info("Cost: " + re.getCost() + ", computed in: "
				+ re.getEvaluationTime() + " ms");

		if (SolutionMulti.isEmpty(initialSolution)) {
			resourceEnvExtFile = null;
			programLogger.error("The generated solution is empty!");
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
	private void performOptimization() throws ParserConfigurationException,
	SAXException, IOException {

		// Build a new Optimization Engine engine and an empty initial
		// solution
		programLogger
		.info("Loading the optimization enging and perparing the solver");


		try {
			engine = new PartialEvaluationOptimizationEngine(
					constraintHandler,optimizationConfigurationFile, batch);
		} catch (DatabaseConnectionFailureExteption e) {
			programLogger.error("Error in connecting to the database",e);
			dirtyExit();
		}

		// load the initial solution from the PCM specified in the
		// configuration and the extension
		programLogger.info("Parsing The Solution");
		try {
			engine.loadInitialSolution(resourceEnvExtFile, usageModelExtFile,
					initialSolution, initialMce);
		} catch (JAXBException e) {
			programLogger.error("Error in loading the initial solution", e);
		}

		// create the progress window
		//if (!batch) {
		progressWindow = new OptimizationProgressWindow();
		progressWindow.setMax(100);
		progressWindow.setCostLogger(engine.getCostLogger());
		progressWindow.setVMLogger(engine.getVMLogger());
		progressWindow.setConstraintsLogger(engine.getConstraintsLogger());
		engine.addPropertyChangeListener(progressWindow);
		engine.getEvalServer().addPropertyChangeListener(progressWindow);
		//}

		// start the optimization
		programLogger.info("Starting the optimization");
		engine.execute();
		while(!engine.isDone()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				programLogger.error("Error while waiting for optimization completion",e);
			}
		}
		//if(batch)
		progressWindow.signalCompletion();
		programLogger.info("Optimization ended");
		cleanExit();

	}

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
	private void performRobustnessAnalysis()
			throws ParserConfigurationException, SAXException, IOException,
			JAXBException {

		// if we want to check the robustness of the solution, a number of
		// modifications of the usage model file must be created.
		ArrayList<File> usageModelExtFiles = new ArrayList<File>();

		String tmp = null;
		boolean error;

		int highestPeak = getMaxPopulation(usageModelExtFile);

		if (!batch) {
			error = true;
			while (error) {
				tmp = (String) JOptionPane.showInputDialog(null,
						"Robustness test with a peak high from...:",
						"Robustness test configuration (1/4)",
						JOptionPane.PLAIN_MESSAGE, null, null, "100");

				try {
					if (tmp != null && tmp.length() > 0) {
						testFrom = Integer.parseInt(tmp);
						if (testFrom > 0)
							error = false;
					}
				} catch (Exception e) {
					error = true;
				}
			}

			error = true;
			while (error) {
				tmp = (String) JOptionPane.showInputDialog(null, "...to:",
						"Robustness test configuration (2/4)",
						JOptionPane.PLAIN_MESSAGE, null, null, "10000");

				try {
					if (tmp != null && tmp.length() > 0) {
						testTo = Integer.parseInt(tmp);
						if (testTo > testFrom)
							error = false;
					}
				} catch (Exception e) {
					error = true;
				}
			}

			error = true;
			while (error) {
				tmp = (String) JOptionPane.showInputDialog(null, "Step size?",
						"Robustness test configuration (3/4)",
						JOptionPane.PLAIN_MESSAGE, null, null, "300");

				try {
					if (tmp != null && tmp.length() > 0) {
						step = Integer.parseInt(tmp);
						if (step > 0)
							error = false;
					}
				} catch (Exception e) {
					error = true;
				}
			}

			error = true;
			while (error) {
				tmp = (String) JOptionPane.showInputDialog(null, "Attempts?",
						"Robustness test configuration (4/4)",
						JOptionPane.PLAIN_MESSAGE, null, null, "5");

				try {
					if (tmp != null && tmp.length() > 0) {
						attempts = Integer.parseInt(tmp);
						if (attempts > 0)
							error = false;
					}
				} catch (Exception e) {
					error = true;
				}
			}
		}

		double x = testFrom, basex = x;

		usageModelExtFile = generateModifiedUsageModelExt(usageModelExtFile, x
				/ highestPeak);
		c.USAGE_MODEL_EXT_FILE = usageModelExtFile.getAbsolutePath();

		for (x += step; x <= testTo; x += step) {
			usageModelExtFiles.add(generateModifiedUsageModelExt(
					usageModelExtFile, x / basex));
		}

		// We're checking the robustness of the solution here!

		// We use a pool of executors for checking a number of modification
		// of the problem.
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(1);

		RobustnessProgressWindow rpw = new RobustnessProgressWindow(
				usageModelExtFiles.size() + 1);

		// /////////////////////////////
		// // Build a new Optimization Engine engine and an empty initial
		// // solution
		// programLogger.info("Loading the optimization engine and preparing the solver");
		//
		// OptEngine engine = new PartialEvaluationOptimizationEngine(
		// constraintHandler, batch);
		//
		// // load the initial solution from the PCM specified in the
		// // configuration and the extension
		// programLogger.info("Parsing The Solution");
		// try {
		// engine.loadInitialSolution(resourceEnvExtFile, usageModelExtFile,
		// initialSolution, initialMce);
		// } catch (JAXBException e) {
		// programLogger.error("Error in loading the initial solution", e);
		// }
		//
		// // start the optimization
		// programLogger.info("Starting the optimization");
		// executor.execute(engine);
		//
		// List<File> solutions = new ArrayList<File>();
		// int terminated = 0;
		// {
		// File f = Paths.get(c.PROJECT_PATH, resFolder,
		// "solution.xml").toFile();
		// solutions.add(f);
		//
		// boolean found = false;
		// while (!found) {
		// if (f.exists()) {
		// terminated++;
		// found = true;
		//
		// // to save space on hd I remove the results as soon as i get
		// // the solution.xml file, because that's all I need
		// ConfigurationHandler.cleanFolders(f.getParent()
		// + File.separator + "performance_results");
		// }
		//
		// if (!found) {
		// try {
		// Thread.sleep(1000);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// }
		//
		// rpw.add(usageModelExtFile, solutions.get(0));
		// rpw.setValue(terminated);
		// rpw.save2png(solutions.get(0).getParent());
		//
		// int testValue = testFrom;
		//
		// Path p = Paths.get(solutions.get(0).getParent(), "results");
		// System.out.println(p.toString());
		// int i = 0;
		// while (Files.exists(p)) {
		// p = Paths.get(solutions.get(0).getParent(), "results-" + i);
		// ++i;
		// }
		// Files.createDirectory(p);
		//
		// try {
		// Files.copy(Paths.get(usageModelExtFile.getAbsolutePath()),
		// Paths.get(p.toString(), "ume-" + testValue + ".xml"));
		// Files.copy(Paths.get(solutions.get(0).getAbsolutePath()),
		// Paths.get(p.toString(), "solution-" + testValue + ".xml"));
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// testValue += step;
		//
		// int el = 0;
		// for (File f : usageModelExtFiles) {
		// Space4Cloud s4c = new Space4Cloud(true, Operations.Optimization,
		// resourceEnvironmentFile, resFolder + File.separator + el,
		// usageFile, allocationFile, repositoryFile, solver, lineConfFile,
		// f, initialSolution != null ? null : resourceEnvExtFile,
		// constraintFile, testFrom, testTo, step);
		// // if initialSolution isn't null, it was because we generated it! so
		// we must keep generating them!
		//
		// Future<?> fut = executor.submit(s4c);
		// try {
		// fut.get();
		// } catch (InterruptedException | ExecutionException e) {
		// e.printStackTrace();
		// }
		//
		// {
		// File g = Paths.get(c.PROJECT_PATH, resFolder, "" + el,
		// "solution.xml").toFile();
		// solutions.add(g);
		//
		// boolean found = false;
		// while (!found) {
		// if (g.exists()) {
		// terminated++;
		// found = true;
		//
		// // to save space on hd I remove the results as soon as i get the
		// solution.xml file, because that's all I need
		// ConfigurationHandler.cleanFolders(g.getParent() + File.separator +
		// "performance_results");
		//
		// try {
		// Files.copy(Paths.get(f.getAbsolutePath()), Paths.get(p.toString(),
		// "ume-" + testValue + ".xml"));
		// Files.copy(Paths.get(g.getAbsolutePath()), Paths.get(p.toString(),
		// "solution-" + testValue + ".xml"));
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// testValue += step;
		// }
		//
		// if (!found) {
		// try {
		// Thread.sleep(1000);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// }
		//
		// rpw.add(usageModelExtFiles.get(el), solutions.get(el+1));
		// rpw.setValue(terminated);
		// rpw.save2png(solutions.get(0).getParent());
		//
		// el++;
		// }
		// /////////////////////////////

		String duration = "";
		{
			// an average of 5 minutes per attempt... maybe it's too little, but
			// whatever...
			int res = (attempts * (int) Math.ceil(((testTo - testFrom) / step))) * 5 * 60;
			if (res > 60 * 60) {
				duration += (res / (60 * 60)) + " h ";
				res = res % (60 * 60);
			}
			if (res > 60) {
				duration += (res / 60) + " m ";
				res = res % 60;
			}
			duration += res + " s";
		}
		programLogger
		.info("Starting the robustness test, considering each problem "
				+ attempts + " times (it could take up to " + duration
				+ ")...");

		StopWatch timer = new StopWatch();
		timer.start();
		timer.split();

		usageModelExtFiles.add(0, usageModelExtFile);

		List<File> solutions = new ArrayList<File>();

		int testValue = testFrom;
		int terminated = 0;

		Path p = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY, "results");
		programLogger.info(p.toString());
		int i = 0;
		while (Files.exists(p)) {
			p = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY, "results-" + i);
			++i;
		}
		Files.createDirectory(p);
		int step=0;
		int el = 0;
		for (File f : usageModelExtFiles) {
			step++;
			File bestSolution = null;
			int bestCost = Integer.MAX_VALUE;

			for (int attempt = 1; attempt <= attempts; ++attempt) {
				Space4Cloud s4c = new Space4Cloud(true,
						Operations.Optimization, resourceEnvironmentFile,
						resFolder + File.separator + testValue + File.separator
						+ attempt, usageFile, allocationFile,
						repositoryFile, solver, lineConfFile, f,
						initialSolution != null ? null : resourceEnvExtFile,
								constraintFile, testFrom, testTo, step, dbConfigurationFile, optimizationConfigurationFile);
				// if initialSolution isn't null, it was because we generated
				// it! so we must keep generating them!

				s4c.setProvidersInitialSolution(getProvidersInitialSolution());

				Future<?> fut = executor.submit(s4c);
				try {
					fut.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}

				{
					File g = Paths.get(c.PROJECT_PATH, resFolder,
							"" + testValue + File.separator + attempt,
							"solution.xml").toFile();

					boolean found = false;
					while (!found) {
						if (g.exists()) {
							found = true;

							//save the solution
							Files.createDirectories(Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,"attempts","step"+step));
							Files.copy(Paths.get(g.getAbsolutePath()), Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,"attempts","step"+step,"solution"+attempt+".xml"));
							// to save space on hd I remove the results as soon
							// as i get the solution.xml file, because that's
							// all I need
							ConfigurationHandler.cleanFolders(g.getParent()
									+ File.separator + "performance_results");

							int cost = SolutionMulti.getCost(g);
							if (bestSolution == null || cost < bestCost) {
								// System.out.println("DEBUG: Best cost from " +
								// bestCost + " to " + cost);
								bestCost = cost;
								bestSolution = g;
							}
						}

						if (!found) {
							try {
								Thread.sleep(1000);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			try {
				Files.copy(Paths.get(f.getAbsolutePath()),
						Paths.get(p.toString(), "ume-" + testValue + ".xml"));
				Files.copy(
						Paths.get(bestSolution.getAbsolutePath()),
						Paths.get(p.toString(), "solution-" + testValue
								+ ".xml"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			terminated++;
			solutions.add(bestSolution);

			rpw.add(usageModelExtFiles.get(el), solutions.get(el));
			rpw.setValue(terminated);
			rpw.save2png(c.ABSOLUTE_WORKING_DIRECTORY);

			el++;

			ConfigurationHandler.cleanFolders(Paths.get(
					c.ABSOLUTE_WORKING_DIRECTORY, testValue + "").toString());
			testValue += step;

		}

		executor.shutdown();
		timer.stop();

		programLogger.info("Check ended!");

		String actualDuration = "";
		{
			int res = (int) timer.getTime() / 1000;
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

		programLogger.info("Expected time of execution: " + duration
				+ ", actual time of execution: " + actualDuration);

		executor.shutdown();

		// ArrayList<File> solutions = new ArrayList<File>();
		// {
		// File sol = new File(c.PROJECT_PATH + File.separator + resFolder +
		// File.separator + "solution.xml");
		// solutions.add(sol);
		// for (int i = 0; i < usageModelExtFiles.size(); i++) {
		// File f = new File(c.PROJECT_PATH + File.separator + resFolder +
		// File.separator + i + File.separator + "solution.xml");
		// solutions.add(f);
		// }
		// }
		//
		// int terminated = 0;
		// while (terminated != usageModelExtFiles.size() + 1) {
		// terminated = 0;
		//
		// for (File f : solutions) {
		// if (f.exists()) {
		// terminated++;
		//
		// // to save space on hd I remove the results as soon as i get the
		// solution.xml file, because that's all I need
		// ConfigurationHandler.cleanFolders(f.getParent() + File.separator +
		// "performance_results");
		// }
		// }
		// rpw.setValue(terminated);
		// if (terminated != usageModelExtFiles.size() + 1) {
		// try {
		// Thread.sleep(1000);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }
		//
		// try {
		//
		// rpw.add("Initial", usageModelExtFile, solutions.get(0));
		//
		// for (int i = 0; i < usageModelExtFiles.size(); ++i)
		// rpw.add("Var " + i, usageModelExtFiles.get(i), solutions.get(i+1));
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		programLogger.info("Check ended!");
		// rpw.save2png(solutions.get(0).getParent());

	}

	private void refreshProject() throws CoreException {
		ResourcesPlugin
		.getWorkspace()
		.getRoot()
		.getProject(c.PROJECT_NAME)
		.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());

	}

	public void setProvidersInitialSolution(String... providers) {
		providersInitialSolution.clear();

		for (String s : providers)
			providersInitialSolution.add(s);
	}

	public void setRobustnessAttempts(int attempts) {
		this.attempts = attempts;
	}

}
