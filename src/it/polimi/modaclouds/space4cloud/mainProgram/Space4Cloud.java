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
import it.polimi.modaclouds.space4cloud.chart.Logger2JFreeChartImage;
import it.polimi.modaclouds.space4cloud.chart.SeriesHandle;
import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnector;
import it.polimi.modaclouds.space4cloud.gui.AssesmentWindow;
import it.polimi.modaclouds.space4cloud.gui.ConfigurationWindow;
import it.polimi.modaclouds.space4cloud.gui.OptimizationProgressWindow;
import it.polimi.modaclouds.space4cloud.gui.RobustnessProgressWindow;
import it.polimi.modaclouds.space4cloud.optimization.OptEngine;
import it.polimi.modaclouds.space4cloud.optimization.PartialEvaluationOptimizationEngine;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandlerFactory;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.LineServerHandler;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.LineServerHandlerFactory;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Operation;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Solver;
import it.polimi.modaclouds.space4cloud.utils.ResourceEnvironmentExtensionParser;
import it.polimi.modaclouds.space4cloud.utils.ResourceEnvironmentExtentionLoader;
import it.polimi.modaclouds.space4cloud.utils.RunConfigurationsHandler;
import it.polimi.modaclouds.space4cloud.utils.RussianEvaluator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

import javax.swing.JOptionPane;
import javax.swing.SwingWorker.StateValue;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class Space4Cloud extends Thread implements PropertyChangeListener{

	private static OptimizationProgressWindow progressWindow;
	private static AssesmentWindow assesmentWindow;
	private OptEngine engine = null;	
	private static final Logger logger = LoggerFactory.getLogger(Space4Cloud.class);
	private boolean batch;
	private ConstraintHandler constraintHandler;	
	private File initialSolution = null, initialMce = null;
	private List<String> providersInitialSolution = new ArrayList<String>();
	private String batchConfigurationFile;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);



//	/**
//	 * Robustness Variables
//	 * */
//	private int testFrom, testTo, step;
//	private int attempts = 5;
	
	private boolean compleated = false;

	public Space4Cloud() {
		this(false); //,100, 10000, 300 );
	}

	public Space4Cloud(boolean batch) { //, int testFrom, int testTo, int step) {
		this.batch = batch;		
//		if (batch) {
//			this.testFrom = testFrom;
//			this.testTo = testTo;
//			this.step = step;			
//		}
	}



	/**
	 * Initialize space4cloud in batch mode in order to run the configuration specified in the provided file
	 * @param batchConfFile the path to the file containing the configuration
	 */
	public Space4Cloud(String batchConfFile){
		batchConfigurationFile = batchConfFile;
		batch = true;
	}



	private void cleanExit() {
		logger.info("Exiting SPACE4Cloud");		
		//close the connection with the database
		try {
			if(DatabaseConnector.getConnection() != null)
				DatabaseConnector.getConnection().close();
		} catch (SQLException e) {
			logger.error("Error in closing the connection with the database",e);
		}
		if(Configuration.SOLVER == Solver.LINE){
			if(LineServerHandlerFactory.getHandler() != null){
				LineServerHandlerFactory.getHandler().closeConnections();
			}
		}
		if(engine != null){
			engine.exportSolution();
			engine.cancel(true);
			engine = null;
		}
		DatabaseConnector.closeConnection();
		//this.interrupt();
		refreshProject();
		compleated = true;
	}

	@Override

	public void  run(){

		//clear singleton instances from previous runs
		LineServerHandlerFactory.clearHandler();
		ConstraintHandlerFactory.clearHandler();
		//load the configuration
		if (!batch) {
			if(Configuration.PROJECT_BASE_FOLDER == null)
				Configuration.PROJECT_BASE_FOLDER = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();						
			ConfigurationWindow configGui = new ConfigurationWindow();
			configGui.show();			
			while(!configGui.hasBeenDisposed()){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error("Error waiting for GUI to be closed",e);
				}				
			}

			if(configGui.isCancelled()){
				logger.error("Execution cancelled by the user");
				cleanExit();
				return;
			}

			//release all resources, this should not be necessary
			configGui = null;


		}else{
			try {
				Configuration.loadConfiguration(batchConfigurationFile);
			} catch (IOException e) {
				logger.error("Could not load the configuration from: "+batchConfigurationFile);
			}
		}


		//clean the folder used for the evaluation/optimization process
		try{
			FileUtils.deleteDirectory(Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY).toFile());
		}	catch (NoSuchFileException e) {
			if(e.getMessage().contains("space4cloud"))
				logger.debug("Space4Cloud folder not present, nothing to clean");
			else
				logger.error("Error cleaning directories",e);
		}
		catch (IOException e) {
			if(e.getMessage().contains("cannot access the file")){
				logger.error("An error occurred while accessing a temporary file from a previous run, space4cloud will try to ignore it and proceed");
			}else{
				logger.error("Error cleaning directories",e);
			}
		}


		refreshProject();

		//initialize the connection to the database
		try {
			InputStream dbConfigurationStream = null;
			//load the configuration file if specified 
			if(Configuration.DB_CONNECTION_FILE != null && Paths.get(Configuration.DB_CONNECTION_FILE).toFile().exists()){				
				dbConfigurationStream = new FileInputStream(Configuration.DB_CONNECTION_FILE);				
			}else{
				//if the file has not been specified or it does not exist use the one with default values embedded in the plugin
				logger.info("Could not load the database connection file: "+Configuration.DB_CONNECTION_FILE+" will use the default configuration");
				dbConfigurationStream = this.getClass().getResourceAsStream(Configuration.DEFAULT_DB_CONNECTION_FILE);				
			}
			DatabaseConnector.initConnection(dbConfigurationStream);		
			DataHandlerFactory.getHandler();
			dbConfigurationStream.close();
		} catch (SQLException | IOException | DatabaseConnectionFailureExteption e) {
			logger.error("Could not initialize database connection",e);
			cleanExit();
			return;
		}

		//if the initial solution has to be computed then initialize the solver with the database connection values
		if(Configuration.RELAXED_INITIAL_SOLUTION){
			try {
				RussianEvaluator.setDatabaseInformation(DatabaseConnector.url,
						DatabaseConnector.dbName,
						DatabaseConnector.driver,
						DatabaseConnector.userName,
						DatabaseConnector.password);
			} catch (IOException e) {
				logger.error("Could not init the Initial solution engine",e);
				cleanExit();
				return;
			}
		}

		//If the chosen solver is LINE try to connect to it or launch it locally. 		
		if(Configuration.SOLVER ==Solver.LINE){
			logger.info("Looking for LINE server");			
			LineServerHandler lineHandler = LineServerHandlerFactory.getHandler();
			lineHandler.connectToLINEServer();
			lineHandler.closeConnections();
			logger.info("succesfully connected to LINE server");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Error while waiting for LINE first connection",e);
			}//give time to LINE to close the connection on his side
		}

		//Build the temporary space4cloud folder
		try {
			Files.createDirectories(Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY));
		} catch (IOException e) {
			logger.error("Error creating the "+Configuration.PERFORMANCE_RESULTS_FOLDER+" folder",e);
		}


		// Build the run configuration
		RunConfigurationsHandler runConfigHandler = new RunConfigurationsHandler();

		refreshProject();
		// launch it
		logger.info("Launching Palladio transformation..");
		runConfigHandler.launch();

		// Build the folder structure to host results and copy the LQN model in
		// those folders
		File resultDirPath = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,Configuration.PERFORMANCE_RESULTS_FOLDER).toFile();
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
			logger
			.error("The first initialization run has encounter some problem during the generation of the first solution");
			logger.error("SPACE4CLOUD will now exit.");
			cleanExit();
			return;
		}
		
		logger.info("Transformation succesfully performed");
//		
//		//if the solver is LINE we need to initialize the id mapping between SEFFs and processors in the LQN using the generated LQN model
//		if(Configuration.SOLVER == Solver.LINE){
//			logger.info("Initializing LINE result parser");
//			LINEResultParser.initIds(modelFiles[0]);
//		}

		// Parse the constraints and initialize the handler
		constraintHandler = ConstraintHandlerFactory.getConstraintHandler();
		try {
			constraintHandler.loadConstraints();
		} catch (ParserConfigurationException | SAXException | IOException
				| JAXBException e) {
			logger.error("Error in loading constraints", e);
		}

		//TODO: check these conditions with a fresh mind
		if (Configuration.RELAXED_INITIAL_SOLUTION  && Configuration.FUNCTIONALITY == Operation.Robustness) {
			try {
				getProvidersFromExtension();
			} catch (ParserConfigurationException | SAXException | IOException
					| JAXBException e) {
				logger
				.error("Error in loading the selected providers from the resource environemnt extension file",
						e);
			}
			if (providersInitialSolution.size() == 0)
				askProvidersForInitialSolution();

//			// override any other value specified with the ones obtained by the
//			// initial optimization
//			Configuration.RESOURCE_ENVIRONMENT_EXTENSION = null;
//			initialSolution = null;

		} else if (Configuration.RELAXED_INITIAL_SOLUTION && Configuration.FUNCTIONALITY == Operation.Optimization) {
			try {
				getProvidersFromExtension();
			} catch (ParserConfigurationException | SAXException | IOException
					| JAXBException e) {
				logger
				.error("Error in loading the selected providers from the resource environemnt extension file",
						e);
			}
			if (providersInitialSolution.size() == 0)
				askProvidersForInitialSolution();
			performGenerateInitialSolution();
		}


		switch (Configuration.FUNCTIONALITY) {
		case Assessment:
			try {
				logger.info("Performing Assesment");
				performAssessment();
			} catch (NumberFormatException | IOException
					| ParserConfigurationException | SAXException e) {
				logger.error("Error in performing the assesment", e);
			}
			break;

		case Optimization:
			try {
				logger.info("Performing Optimization");
				performOptimization();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				logger.error("Error in the optimization", e);
			}
			break;

		case Robustness:
			try {
				logger.info("Performing Robustness Analysis");
				performRobustnessAnalysis();
			} catch (ParserConfigurationException | SAXException | IOException
					| JAXBException e) {
				logger.error("Error in the robustness analysis", e);
			}
			break;

		default:
			logger.info("User exit at functionality choiche");
			break;
		}
		refreshProject();


		while(!compleated){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error("Waiting for space4cloud completion error",e);
			}
		}
		return;
	}

	private void getProvidersFromExtension()
			throws ParserConfigurationException, SAXException, IOException,
			JAXBException {
		// parse the extension file
		ResourceEnvironmentExtensionParser resourceEnvParser = new ResourceEnvironmentExtentionLoader(Paths.get(Configuration.RESOURCE_ENVIRONMENT_EXTENSION).toFile());
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
	private void performAssessment()
			throws NumberFormatException, IOException,
			ParserConfigurationException, SAXException {

		try {
			engine = new PartialEvaluationOptimizationEngine(
					constraintHandler, true);
		} catch (DatabaseConnectionFailureExteption e) {
			logger.error("Error in connecting to the database",e);
			cleanExit();
			return;
		}		

		// load the initial solution from the PCM specified in the
		// configuration and the extension
		logger.info("Parsing The Solution");
		try {
			engine.loadInitialSolution();
		} catch (JAXBException e) {
			logger.error("Error in loading the initial solution", e);
			cleanExit();
			return;
		}

		// evaluate the solution
		logger.info("Evaluating the solution");
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
		providedSolution.exportLight(Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.SOLUTION_FILE_NAME));
	}

	private void performGenerateInitialSolution() {
		// resourceEnvExtFile = null;
		// programLogger.warn("Generation of the first solution disabled at the moment!");

		// ///////////////////////////
		RussianEvaluator re = new RussianEvaluator();

		if (providersInitialSolution.size() > 0)
			re.setProviders(getProvidersInitialSolution());

		try {
			re.eval();
		} catch (Exception e) {
			logger.error("Error! It's impossible to generate the solution! Are you connected?",e);			
			cleanExit();
			return;
		}

		// override values provided with those generated by the initial solution
		Configuration.RESOURCE_ENVIRONMENT_EXTENSION = re.getResourceEnvExt().getAbsolutePath();
		initialSolution = re.getSolution();
		initialMce = re.getMultiCloudExt();

		logger.info("Generated resource model extension: "+ Configuration.RESOURCE_ENVIRONMENT_EXTENSION);
		logger.info("Generated solution: "+ initialSolution.getAbsolutePath());
		logger.info("Generated multi cloud extension: "+ initialMce.getAbsolutePath());
		logger.info("Cost: " + re.getCost() + ", computed in: "+ re.getEvaluationTime() + " ms");

		if (SolutionMulti.isEmpty(initialSolution)) {
			Configuration.RESOURCE_ENVIRONMENT_EXTENSION = null;
			logger.error("The generated solution is empty!");
			cleanExit();
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
	private void performOptimization() throws ParserConfigurationException,
	SAXException, IOException {

		// Build a new Optimization Engine engine and an empty initial
		// solution
		logger
		.info("Loading the optimization engine and perparing the solver");


		try {
			engine = new PartialEvaluationOptimizationEngine(constraintHandler, batch);
		} catch (DatabaseConnectionFailureExteption e) {
			logger.error("Error in connecting to the database",e);
			cleanExit();
			return;
		}
	
		engine.addPropertyChangeListener(this);

		// load the initial solution from the PCM specified in the
		// configuration and the extension
		logger.info("Parsing The Solution");
		try {
			engine.loadInitialSolution(initialSolution, initialMce);
		} catch (JAXBException e) {
			logger.error("Error in loading the initial solution", e);
			cleanExit();
			return;

		}

		// create the progress window
		if (!batch) {
			progressWindow = new OptimizationProgressWindow();
			progressWindow.setMax(100);
			progressWindow.setCostLogger(engine.getCostLogger());
			progressWindow.setVMLogger(engine.getVMLogger());
			progressWindow.setConstraintsLogger(engine.getConstraintsLogger());
			progressWindow.addPropertyChangeListener(this);
			engine.addPropertyChangeListener(progressWindow);
			engine.getEvalServer().addPropertyChangeListener(progressWindow);

		}

		// start the optimization
		logger.info("Starting the optimization");
		engine.execute();


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
		File usageModelExtFile = new File(Configuration.USAGE_MODEL_EXTENSION);

		int highestPeak = getMaxPopulation(usageModelExtFile);
		int testFrom = Configuration.ROBUSTNESS_PEAK_FROM;
		int testTo = Configuration.ROBUSTNESS_PEAK_TO;
		int stepSize = Configuration.ROBUSTNESS_STEP_SIZE;
		int attempts = Configuration.ROBUSTNESS_ATTEMPTS;

		double x = testFrom, basex = x;

		usageModelExtFile = generateModifiedUsageModelExt(usageModelExtFile, x
				/ highestPeak);
		Configuration.USAGE_MODEL_EXTENSION = usageModelExtFile.getAbsolutePath();

		for (x += stepSize; x <= testTo; x += stepSize) {
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

		String duration = "";
		{
			// an average of 5 minutes per attempt... maybe it's too little, but
			// whatever...
			int res = (attempts * (int) Math.ceil(((testTo - testFrom) / stepSize))) * 5 * 60;
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
		logger
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

		Path resultsFolder = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY, "results");
		int i = 0;
		while (Files.exists(resultsFolder) && !Configuration.sameConfiguration(Paths.get(resultsFolder.toString(), "space4cloud-bak.properties").toString())) {
			resultsFolder = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY, "results-" + i);
			++i;
		}
		Files.createDirectory(resultsFolder);
		logger.info("The results will be put in this folder: " + resultsFolder.toString());
		
//		int step=0;
		int el = 0;
		
		String bakConf = Paths.get(resultsFolder.toString(), "space4cloud-bak.properties").toString();  //Files.createTempFile("space4cloud", "-bak.properties").toString();
		String baseWorkingDirectory = Paths.get(Configuration.WORKING_DIRECTORY).toString();
		Configuration.saveConfiguration(bakConf);
		FileUtils.deleteDirectory(Paths.get(Configuration.PROJECT_BASE_FOLDER, baseWorkingDirectory, "performance_results").toFile());	
//		try {
//			cleanFolders(Paths.get(Configuration.PROJECT_BASE_FOLDER, baseWorkingDirectory, "attempts"));
//		} catch (Exception e) { }
		
		for (File f : usageModelExtFiles) {
//			step++;
			File bestSolution = null;
			int bestCost = Integer.MAX_VALUE;
			
			Configuration.USAGE_MODEL_EXTENSION = f.getAbsolutePath();
			Configuration.FUNCTIONALITY = Operation.Optimization;
			
			try {
				Files.copy(Paths.get(f.getAbsolutePath()),
						Paths.get(resultsFolder.toString(), "ume-" + testValue + ".xml"),
						StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) { }
			
			if (initialSolution != null)
				Configuration.RESOURCE_ENVIRONMENT_EXTENSION = null;

			boolean alreadyThere = false;
			{
				Path p = Paths.get(resultsFolder.toString(), "solution-" + testValue + ".xml");
				if (Files.exists(p)) {
					alreadyThere = true;
					bestSolution = p.toFile();
				}
			}
			
			for (int attempt = 1; attempt <= attempts && !alreadyThere; ++attempt) {
				
				Configuration.WORKING_DIRECTORY = Paths.get(baseWorkingDirectory, ""+testValue, ""+attempt).toString();
				String tmpConf = Files.createTempFile("space4cloud", ".properties").toString();
				Configuration.saveConfiguration(tmpConf);
				
				Space4Cloud s4c = new Space4Cloud(tmpConf);
				
//				Space4Cloud s4c = new Space4Cloud(true,
//						Operation.Optimization, resourceEnvironmentFile,
//						resFolder + File.separator + testValue + File.separator
//						+ attempt, usageFile, allocationFile,
//						repositoryFile, solver, lineConfFile, f,
//						initialSolution != null ? null : resourceEnvExtFile,
//								constraintFile, testFrom, testTo, step, dbConfigurationFile, optimizationConfigurationFile);
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
//					File g = Paths.get(c.PROJECT_PATH, resFolder,
//							"" + testValue + File.separator + attempt,
//							"solution.xml").toFile();
					
					File g = Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY,
							Configuration.SOLUTION_LIGHT_FILE_NAME + Configuration.SOLUTION_FILE_EXTENSION).toFile();

					boolean found = false;
					while (!found) {
						if (g.exists()) {
							found = true;

							//save the solution
							
							/////////
							// Or don't, because we only need to save the best one.
							
//							Files.createDirectories(Paths.get(Configuration.PROJECT_BASE_FOLDER, baseWorkingDirectory, "attempts", "step"+step));
//							Files.copy(
//									Paths.get(g.getAbsolutePath()),
//									Paths.get(Configuration.PROJECT_BASE_FOLDER, baseWorkingDirectory, "attempts", "step"+step, Configuration.SOLUTION_LIGHT_FILE_NAME + attempt + Configuration.SOLUTION_FILE_EXTENSION)
//									);
							
							
							// to save space on hd I remove the results as soon
							// as i get the solution.xml file, because that's
							// all I need
							
							{
								Path performanceResults = Paths.get(g.getParent(), "performance_results");
							
								while (performanceResults.toFile().exists()) {
									try {
										FileUtils.deleteDirectory(performanceResults.toFile());
//										cleanFolders(Paths.get(g.getParent(), "performance_results"));
									} catch (Exception e) { }
								}
							
							}

							int cost = SolutionMulti.getCost(g);
							if (bestSolution == null || cost < bestCost) {
								// System.out.println("DEBUG: Best cost from " +
								// bestCost + " to " + cost);
								bestCost = cost;
								bestSolution = g;
								
								Files.copy(
										Paths.get(bestSolution.getAbsolutePath()),
										Paths.get(resultsFolder.toString(), "solution-" + testValue
												+ ".xml"), StandardCopyOption.REPLACE_EXISTING);
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
			
			Configuration.loadConfiguration(bakConf);

			terminated++;
			solutions.add(bestSolution);

			rpw.add(usageModelExtFiles.get(el), solutions.get(el));
			rpw.setValue(terminated);
			rpw.save2png(Paths.get(Configuration.PROJECT_BASE_FOLDER, baseWorkingDirectory).toString());

			el++;
			FileUtils.deleteDirectory(Paths.get(Configuration.PROJECT_BASE_FOLDER, baseWorkingDirectory, testValue + "").toFile());		
			testValue += stepSize;

		}

		executor.shutdown();
		timer.stop();

		logger.info("Check ended!");

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

		logger.info("Expected time of execution: " + duration
				+ ", actual time of execution: " + actualDuration);

		logger.info("Check ended!");

	}
	


	//	private void refreshProject() throws CoreException {
	//		ResourcesPlugin
	//		.getWorkspace()
	//		.getRoot()
	//		.getProject(c.PROJECT_NAME)
	//		.refreshLocal(IResource.DEPTH_INFINITE,
	//				new NullProgressMonitor());
	//
	//	}

	public void setProvidersInitialSolution(String... providers) {
		providersInitialSolution.clear();

		for (String s : providers)
			providersInitialSolution.add(s);
	}

//	public void setRobustnessAttempts(int attempts) {
//		this.attempts = attempts;
//	}



	private void refreshProject() {
		try {
			ResourcesPlugin
			.getWorkspace()
			.getRoot()
			.getProject(Configuration.getProjectName())
			.refreshLocal(IResource.DEPTH_INFINITE,
					new NullProgressMonitor());
		} catch (CoreException e) {
			logger.error("Could not refresh the project",e);
		}

	}

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
		logger.info(g.getAbsolutePath());
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

	private void askProvidersForInitialSolution() {
		DataHandler db = null;
		try {
			db = DataHandlerFactory.getHandler();
		} catch (DatabaseConnectionFailureExteption e) {
			logger.error("Error in connecting to the database",e);
			cleanExit();
			return;			
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//when the worker with the engine has done
		if(evt.getSource().equals(engine) && evt.getPropertyName().equals("state") && evt.getNewValue()== StateValue.DONE && !engine.isCancelled()){
			logger.info("Optimization ended");										
			pcs.firePropertyChange("optimizationEnded", false, true);
			if(!batch)
				progressWindow.signalCompletion();
			cleanExit();
		}
		//stop the optimization process if the user closes the window
		else if(evt.getSource().equals(progressWindow) && evt.getPropertyName().equals("WindowClosed")){
			if(engine != null){
				engine.exportSolution();
				engine.cancel(true);
			}
			logger.info("Optimization Process cancelled by the user");
			pcs.firePropertyChange("optimizationEnded", false, true);
			cleanExit();
		}else if (evt.getSource().equals(engine) && evt.getPropertyName().equals("progress")) {
			logger.info("Progress: "+(int) evt.getNewValue());			
		}

	}

	public void addPropertyChangeListener(PropertyChangeListener listener){
		pcs.addPropertyChangeListener(listener);
	}

}
