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
import it.polimi.modaclouds.space4cloud.exceptions.InitalFolderCreationException;
import it.polimi.modaclouds.space4cloud.gui.AssesmentWindow;
import it.polimi.modaclouds.space4cloud.gui.OptimizationProgressWindow;
import it.polimi.modaclouds.space4cloud.optimization.OptEngine;
import it.polimi.modaclouds.space4cloud.optimization.PartialEvaluationOptimizationEngine;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class Space4Cloud extends SwingWorker<Object, Object> {

	private static OptimizationProgressWindow progressWindow;
	private static AssesmentWindow assesmentWindow;
	private OptEngine engine = null;	
	private static final Logger programLogger = LoggerFactory.getLogger(Space4Cloud.class);
	private boolean batch;
	private ConstraintHandler constraintHandler;	
	private File initialSolution = null, initialMce = null;

	private List<String> providersInitialSolution = new ArrayList<String>();


	/**
	 * Robustness Variables
	 * */
	private int testFrom, testTo, step;
	private int attempts = 5;

	public Space4Cloud() {
		this(false,100, 10000, 300 );
	}

	public Space4Cloud(boolean batch, int testFrom, int testTo, int step) {
		this.batch = batch;		
		if (batch) {
			this.testFrom = testFrom;
			this.testTo = testTo;
			this.step = step;			
		}
	}

	public Space4Cloud(int testFrom, int testTo, int step) {
		this(true, testFrom, testTo, step);
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

		//load the configuration
		if (!batch) {
			//TODO: show the GUI
		}else{
			//TODO: load the configuration from a file
		}


		//clean the folder used for the evaluation/optimization process
		try{
			cleanFolders(Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY));
		}	catch (NoSuchFileException e) {
			if(e.getMessage().contains("space4cloud"))
				programLogger.debug("Space4Cloud folder not present, nothing to clean");
			else
				programLogger.error("Error cleaning directories",e);
		}
		catch (IOException e) {
			programLogger.error("Error cleaning directories",e);
		}

		//TODO: analizzare da qui


		//initialize the connection to the database
		try {
			InputStream dbConfigurationStream = null;
			//load the configuration file if specified 
			if(Configuration.DB_CONNECTION_FILE != null && Paths.get(Configuration.DB_CONNECTION_FILE).toFile().exists()){				
				dbConfigurationStream = new FileInputStream(Configuration.DB_CONNECTION_FILE);				
			}else{
				//if the file has not been specified or it does not exist use the one with default values embedded in the plugin
				programLogger.info("Could not load the database connection file: "+Configuration.DB_CONNECTION_FILE+" will use the default configuration");
				dbConfigurationStream = this.getClass().getResourceAsStream(Configuration.DEFAULT_DB_CONNECTION_FILE);				
			}
			DatabaseConnector.initConnection(dbConfigurationStream);		
			dbConfigurationStream.close();
		} catch (SQLException | IOException e) {
			programLogger.error("Could not initialize database connection",e);
			dirtyExit();
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
				programLogger.error("Could not init the Initial solution engine",e);
				dirtyExit();
			}
		}

		//If the chosen solver is LINE try to connect to it or launch it locally. 		
		if(Configuration.SOLVER ==Solver.LINE){
			programLogger.info("Looking for LINE server");			
			LineServerHandler lineHandler = LineServerHandlerFactory.getHandler();
			lineHandler.connectToLINEServer();
			lineHandler.closeConnections();
			programLogger.info("succesfully connected to LINE server");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				programLogger.error("Error while waiting for LINE first connection",e);
			}//give time to LINE to close the connection on his side
		}

		// Build the run configuration
		RunConfigurationsHandler runConfigHandler = new RunConfigurationsHandler();
		// launch it
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


		// Parse the constraints and initialize the handler
		constraintHandler = new ConstraintHandler();
		try {
			constraintHandler.loadConstraints();
		} catch (ParserConfigurationException | SAXException | IOException
				| JAXBException e) {
			programLogger.error("Error in loading constraints", e);
		}

		//TODO: check these conditions with a fresh mind
		if (Configuration.RELAXED_INITIAL_SOLUTION  && Configuration.FUNCTIONALITY == Operation.Robustness) {
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
			Configuration.RESOURCE_ENVIRONMENT_EXTENSION = null;
			initialSolution = null;

		} else if (Configuration.RELAXED_INITIAL_SOLUTION && Configuration.FUNCTIONALITY == Operation.Optimization) {
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


		switch (Configuration.FUNCTIONALITY) {
		case Assessment:
			try {
				programLogger.info("Performing Assesment");
				performAssessment();
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

			/*case Robustness:
			try {
				programLogger.info("Performing Robustness Analysis");
				performRobustnessAnalysis();
			} catch (ParserConfigurationException | SAXException | IOException
					| JAXBException e) {
				programLogger.error("Error in the robustness analysis", e);
			}
			break;*/

		default:
			programLogger.info("User exit at functionality choiche");
			break;
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
			programLogger.error("Error in connecting to the database",e);
			dirtyExit();
		}

		// load the initial solution from the PCM specified in the
		// configuration and the extension
		programLogger.info("Parsing The Solution");
		try {
			engine.loadInitialSolution();
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
			programLogger.error("Error! It's impossible to generate the solution! Are you connected?",e);			
			dirtyExit();
		}

		// override values provided with those generated by the initial solution
		Configuration.RESOURCE_ENVIRONMENT_EXTENSION = re.getResourceEnvExt().getAbsolutePath();
		initialSolution = re.getSolution();
		initialMce = re.getMultiCloudExt();

		programLogger.info("Generated resource model extension: "+ Configuration.RESOURCE_ENVIRONMENT_EXTENSION);
		programLogger.info("Generated solution: "+ initialSolution.getAbsolutePath());
		programLogger.info("Generated multi cloud extension: "+ initialMce.getAbsolutePath());
		programLogger.info("Cost: " + re.getCost() + ", computed in: "+ re.getEvaluationTime() + " ms");

		if (SolutionMulti.isEmpty(initialSolution)) {
			Configuration.RESOURCE_ENVIRONMENT_EXTENSION = null;
			programLogger.error("The generated solution is empty!");
			dirtyExit();
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
			engine = new PartialEvaluationOptimizationEngine(constraintHandler, batch);
		} catch (DatabaseConnectionFailureExteption e) {
			programLogger.error("Error in connecting to the database",e);
			dirtyExit();
		}

		// load the initial solution from the PCM specified in the
		// configuration and the extension
		programLogger.info("Parsing The Solution");
		try {
			engine.loadInitialSolution(initialSolution, initialMce);
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
	/*
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
						Operation.Optimization, resourceEnvironmentFile,
						Configuration. + File.separator + testValue + File.separator
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
	 */


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

	public void setRobustnessAttempts(int attempts) {
		this.attempts = attempts;
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
		programLogger.info(g.getAbsolutePath());
		return g;

	}

	private  void cleanFolders(Path path) throws IOException {			
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

		});
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

}
