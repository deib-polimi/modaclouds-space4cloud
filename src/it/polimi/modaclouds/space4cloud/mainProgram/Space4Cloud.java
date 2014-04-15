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
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.schema.UsageModelExtensions;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.exceptions.InitalFolderCreationException;
import it.polimi.modaclouds.space4cloud.gui.AssesmentWindow;
import it.polimi.modaclouds.space4cloud.gui.Choose;
import it.polimi.modaclouds.space4cloud.gui.LoadModel;
import it.polimi.modaclouds.space4cloud.gui.OptimizationProgressWindow;
import it.polimi.modaclouds.space4cloud.gui.RobustnessProgressWindow;
import it.polimi.modaclouds.space4cloud.gui.XMLFileSelection;
import it.polimi.modaclouds.space4cloud.optimization.OptEngine;
import it.polimi.modaclouds.space4cloud.optimization.PartialEvaluationOptimizationEngine;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.utils.ConfigurationHandler;
import it.polimi.modaclouds.space4cloud.utils.Constants;
import it.polimi.modaclouds.space4cloud.utils.LoggerHelper;
import it.polimi.modaclouds.space4cloud.utils.RunConfigurationsHandler;
import it.polimi.modaclouds.space4cloud.utils.SimpleEvaluator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import de.uka.ipd.sdq.pcmsolver.runconfig.MessageStrings;

public class Space4Cloud extends SwingWorker<Object, Object> {

	private static OptimizationProgressWindow progressWindow;
	private static AssesmentWindow assesmentWindow;
	private Constants c;
	private static Logger logger;
	
	private File generateModifiedUsageModelExt(File f, double deltaRatio) throws JAXBException, MalformedURLException {
		UsageModelExtensions umes = XMLHelper.deserialize(f.toURI().toURL(),
				UsageModelExtensions.class);
		
		ClosedWorkload cw = umes.getUsageModelExtension().getClosedWorkload();
		if (cw != null)
			for (ClosedWorkloadElement we : cw.getWorkloadElement()) {
				we.setPopulation((int)(we.getPopulation() * deltaRatio));
			}
		
		OpenWorkload ow = umes.getUsageModelExtension().getOpenWorkload();
		if (ow != null)
			for (OpenWorkloadElement we : ow.getWorkloadElement()) {
				we.setPopulation((int)(we.getPopulation() * deltaRatio));
			}
		
		String s = Double.toString(deltaRatio);
		s = s.replace('.', '-');
		
		File g;
		try {
			g = File.createTempFile("ume" + s + "-", ".xml");
			XMLHelper.serialize(umes, UsageModelExtensions.class, new FileOutputStream(g));
			
			System.out.println(g.getAbsolutePath());
			return g;
		} catch (Exception e) {
			e.printStackTrace();
			return f;
		}
	}
	
	private boolean batch;
	private int functionality;
	private File resourceEnvironmentFile, usageFile, allocationFile, repositoryFile, lineConfFile, usageModelExtFile, resourceEnvExtFile, constraintFile;
	private String resFolder;
	private String solver;
	
	public Space4Cloud(boolean batch, int functionality, File resourceEnvironmentFile, String resFolder,
			File usageFile, File allocationFile, File repositoryFile, String solver, File lineConfFile,
			File usageModelExtFile, File resourceEnvExtFile, File constraintFile) {
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
		}
	}
	public Space4Cloud() {
		this(false, -1, null, "space4cloud", null, null, null, null, null, null, null, null);
	}

	@Override
	protected Object doInBackground() throws CoreException,
			ParserConfigurationException, SAXException, IOException, JAXBException, MalformedURLException {

		
		
//		/* Ask for the functionality */
//		Choose assesmentChoiche = new Choose("Functionality selection",
//				"Choose the desired functionality", "Assesment",
//				"Optimization", false);
//		if (!assesmentChoiche.isChosen())
//			return null;
		
		/*
		 * Non sopporto la classe Choose. Questo blocco di codice fa le stesse cose e più.
		 * TODO: eliminare la classe Choose.
		 */
		
		LoadModel lm;
		if (!batch) {
			/*int*/ functionality = 3;
			{
				Object[] options = { "Assessment", "Optimization", "Robustness", "Cancel" };
				functionality = JOptionPane
						.showOptionDialog(
								null,
								"Choose the desired functionality",
								"Functionality selection", JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE, null, options, options[3]);
	
				System.out.println("Functionality selection: " + functionality);
				if (functionality == 3)
					return null;
			}
		

			/* Load the resourceEnvironment */
			/*LoadModel*/ lm = new LoadModel(null, "Resource Model",
					".resourceenvironment");
			if (!lm.isChosen())
				return null;
			/*File*/ resourceEnvironmentFile = lm.getModelFile();
		
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
		logger = LoggerHelper.getLogger(Space4Cloud.class);

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
					ConfigurationHandler.cleanFolders(file.getAbsolutePath());
				} else {
					file.delete();
				}

			refreshProject();
			// load the new configuration
			confHandler.loadConfiguration();
			
			usageFile = new File(c.USAGE_MODEL);
			allocationFile = new File(c.ALLOCATION_MODEL);
			repositoryFile = new File(c.REPOSITORY_MODEL);
			solver = c.SOLVER;
			lineConfFile = new File(c.LINE_PROPERTIES_FILE);
			
		} else // if there is no configuration file then generate a valid
				// configuration
		{
			// delete any old space4cloud folder
			ConfigurationHandler.cleanFolders(c.ABSOLUTE_WORKING_DIRECTORY);

			
			if (!batch) {
				// load the usage model
				lm = new LoadModel(null, "Usage Model", ".usagemodel");
				if (!lm.isChosen())
					return null;
	//			c.USAGE_MODEL = lm.getModelFile().getAbsolutePath();
				usageFile = lm.getModelFile();
	
				// load the allocation model
				lm = new LoadModel(null, "Allocation Model", ".allocation");
				if (!lm.isChosen())
					return null;
	//			c.ALLOCATION_MODEL = lm.getModelFile().getAbsolutePath();
				allocationFile = lm.getModelFile();
				
				// load the repository model
				lm = new LoadModel(null, "Repository Model", ".repository");
				if (!lm.isChosen())
					return null;
	//			c.REPOSITORY_MODEL = lm.getModelFile().getAbsolutePath();
				repositoryFile = lm.getModelFile();
				
				// load the solver
				Choose choice = new Choose("Choose the Solver",
						"Which Solver do you want to use?");
				if (!choice.isChosen())
					return null;
				solver = choice.getSolver();
				
				// if the solver is LINE it needs the path to its configuration file
				if (solver.equals(MessageStrings.PERFENGINE_SOLVER)) {
					lm = new LoadModel(null, "Performance Engine Config",
							".properties");
					if (!lm.isChosen())
						return null;
					lineConfFile = lm.getModelFile();
//					c.LINE_PROPERTIES_FILE = lm.getModelFile().getAbsolutePath();
				}
			}
			c.USAGE_MODEL = usageFile.getAbsolutePath();
			c.ALLOCATION_MODEL = allocationFile.getAbsolutePath();
			c.REPOSITORY_MODEL = repositoryFile.getAbsolutePath();
			c.SOLVER = solver;
			if (lineConfFile != null) {
				c.LINE_PROPERTIES_FILE = lineConfFile.getAbsolutePath();
			}
			

			// save the configuration, it will be run later in the optimization
			// process
			confHandler.saveConfiguration();
			// refresh the project in the workspace
			refreshProject();

		}

		// Build the run configuration
		RunConfigurationsHandler runConfigHandler = new RunConfigurationsHandler();
		// launch it
		runConfigHandler.launch();

		// Build the folder structure to host results and copy the LQN model in
		// those folders
		File resultDirPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
				c.PERFORMANCE_RESULTS_FOLDER).toFile();
		// list files excluding the result file generated by the solver
		File[] modelFiles = resultDirPath.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml") && !name.contains("_res");
			}
		});
		File[] resultFiles = resultDirPath.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("_res.xml") || name.endsWith(".lqxo");
			}
		});

		// if the palladio run has not produced a lqn model exit
		if (modelFiles.length != 1 || resultFiles.length != 1) {
			logger.error("The first initialization run has encounter some problem during the generation of the first solution");
			logger.error("SPACE4CLOUD will now exit.");
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
			/*File*/ usageModelExtFile = null;
			XMLFileSelection usageModelExtSelector = new XMLFileSelection(
					"Load Usage Model Extension");
			// keep asking the file until a valid file is provided or the user
			// pressed cancel
			do {
				usageModelExtSelector.askFile();
				usageModelExtFile = usageModelExtSelector.getFile();
				try {
					if (usageModelExtFile != null)
						XMLHelper.deserialize(usageModelExtFile.toURI().toURL(),
								UsageModelExtensions.class);
				} catch (JAXBException e) {
					logger.warn("The usage Model extension file specified ("
							+ usageModelExtFile + ") is not valid ", e);
					usageModelExtFile = null;
				}
			} while (!usageModelExtSelector.isCanceled()
					&& usageModelExtFile == null);
			if (usageModelExtSelector.isCanceled()) {
				logger.info("No usage model extension selected. Quitting SPACE4CLOUD");
				return null;
			}
		}
		c.RESOURCE_ENV_EXT_FILE = usageModelExtFile.getAbsolutePath();
		
		// if we want to check the robustness of the solution, a number of
		// modifications of the usage model file must be created.
		ArrayList<File> usageModelExtFiles = new ArrayList<File>();
		if (functionality == 2) {
//			usageModelExtFiles.add(generateModifiedUsageModelExt(usageModelExtFile, 0.3));
//			usageModelExtFiles.add(generateModifiedUsageModelExt(usageModelExtFile, 0.5));
//			usageModelExtFiles.add(generateModifiedUsageModelExt(usageModelExtFile, 0.7)); // questo
//			usageModelExtFiles.add(generateModifiedUsageModelExt(usageModelExtFile, 0.9));
//			usageModelExtFiles.add(generateModifiedUsageModelExt(usageModelExtFile, 1.1));
//			usageModelExtFiles.add(generateModifiedUsageModelExt(usageModelExtFile, 1.3)); // questo
//			usageModelExtFiles.add(generateModifiedUsageModelExt(usageModelExtFile, 1.5));
//			usageModelExtFiles.add(generateModifiedUsageModelExt(usageModelExtFile, 1.7));
			
			double x = 8500, basex = 8500;
			double max = 1000;
			
			usageModelExtFile = generateModifiedUsageModelExt(usageModelExtFile, x/max);
			c.RESOURCE_ENV_EXT_FILE = usageModelExtFile.getAbsolutePath();
			
			for (x += 300; x <= 10000; x += 300) {
				usageModelExtFiles.add(generateModifiedUsageModelExt(usageModelExtFile, x/basex));
			}
			
		}

		
		if (!batch) {
			/*
			 * Here I (Riccardo B. Desantis) am implementing the generation of the
			 * first solution using cplex and the tool made by Alexander Lavrentev.
			 */
	
			/* Ask if you want to produce the "intelligent" first solution */
			Object[] options = { "Yes", "No" };
			int n = JOptionPane
					.showOptionDialog(
							null,
							"Do you want to generate a first optimal solution? (It might take some time)",
							"First solution", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
	
			logger.debug("Initial Solution generation: " + n);
	
			/*File*/ resourceEnvExtFile = null;
			if (n == 1) {
				XMLFileSelection extensionSelector = new XMLFileSelection(
						"Load Resource Environment Extension");
				// keep asking the file until a valid file is provided or the user
				// pressed cancel
				do {
					extensionSelector.askFile();
					resourceEnvExtFile = extensionSelector.getFile();
					try {
						if (resourceEnvExtFile != null)
							XMLHelper.deserialize(resourceEnvExtFile.toURI()
									.toURL(), ResourceModelExtension.class);
					} catch (JAXBException e) {
						logger.error(
								"The resource Model extension file specified ("
										+ resourceEnvExtFile + ") is not valid ", e);
						resourceEnvExtFile = null;
					}
				} while (!extensionSelector.isCanceled()
						&& resourceEnvExtFile == null);
				if (extensionSelector.isCanceled()) {
					logger.warn("No resource model extension selected. Quitting SPACE4CLOUD");
					return null;
				}
//				c.RESOURCE_ENV_EXT_FILE = resourceEnvExtFile.getAbsolutePath();
			} else {
				logger.info("Solution generation not supported yet");
				return null;
				// resourceEnvExtFile = informationparser.MainTest.generateSolution(
				// c.ABSOLUTE_WORKING_DIRECTORY, c.RESOURCE_MODEL, c.USAGE_MODEL,
				// c.ALLOCATION_MODEL, c.REPOSITORY_MODEL);
				// c.RESOURCE_ENV_EXT_FILE = resourceEnvExtFile.getAbsolutePath();
			}
		
		}
		
		c.RESOURCE_ENV_EXT_FILE = resourceEnvExtFile.getAbsolutePath();

		/*
		 * Load the Extension file XMLFileSelection extensionSelector = new
		 * XMLFileSelection("Load Extension"); File extensionFile =
		 * extensionSelector.getFile(); if(extensionFile == null) return null;
		 * c.EXTENSION_FILE = extensionFile.getAbsolutePath();
		 */

		if (functionality == 1) { //if (!assesmentChoiche.isTrue()) {

			/* Load the Constraint file */
			logger.info("Parsing Constraints");
			if (!batch) {
				XMLFileSelection constraintSelector = new XMLFileSelection(
						"Load Constraints");
				constraintSelector.askFile();
				/*File*/ constraintFile = constraintSelector.getFile();
				if (constraintFile == null)
					return null;
			}

			// Parse the constraints and initialize the handler
			ConstraintHandler constraintHandler = new ConstraintHandler();
			constraintHandler.loadConstraints(constraintFile);

			// Build a new Optimization Engine engine and an empty initial
			// solution
			logger.info("Loading the optimization enging and perparing the solver");

			OptEngine engine = new PartialEvaluationOptimizationEngine(
					constraintHandler);

			// load the initial solution from the PCM specified in the
			// configuration and the extension
			logger.info("Parsing The Solution");
			try {
				engine.loadInitialSolution(resourceEnvExtFile,
						usageModelExtFile);
			} catch (JAXBException e) {
				logger.error("Error in loading the initial solution", e);
			}

			// create the progress window
			if (!batch) {
				progressWindow = new OptimizationProgressWindow();
				progressWindow.setMax(engine.getMaxIterations());
				progressWindow.setCostLogger(engine.getCostLogger());
				progressWindow.setVMLogger(engine.getVMLogger());
				progressWindow.setConstraintsLogger(engine.getConstraintsLogger());
				engine.addPropertyChangeListener(progressWindow);
				engine.getEvalProxy().addPropertyChangeListener(progressWindow);
			}

			// start the optimization
			logger.info("Starting the optimization");
			engine.execute();

		}
		// otherwise just evaluate the extended solution
		// TODO: fix this, it is based on old implementation
		else if (functionality == 0) { // else {
			confHandler.removeOldLQNFiles();
			SimpleEvaluator evaluator = new SimpleEvaluator();
			evaluator.eval();
			evaluator.parseResults();
			assesmentWindow = new AssesmentWindow();
			assesmentWindow.setVMLogger(evaluator.getVMLogger());
			assesmentWindow.setResponseTimeLogger(evaluator.getRTLogger());
			assesmentWindow.setUtilizationLogger(evaluator.getUtilLogger());
			assesmentWindow.show();
			assesmentWindow.updateImages();

		}
		else {
			// We're checking the robustness of the solution here!
			
			/* Load the Constraint file */
			logger.info("Parsing Constraints");
			if (!batch) {
				XMLFileSelection constraintSelector = new XMLFileSelection(
						"Load Constraints");
				constraintSelector.askFile();
				/*File*/ constraintFile = constraintSelector.getFile();
				if (constraintFile == null)
					return null;
			}

			// Parse the constraints and initialize the handler
			ConstraintHandler constraintHandler = new ConstraintHandler();
			constraintHandler.loadConstraints(constraintFile);
			
			// We use a pool of executors for checking a number of modification
			// of the problem.
			ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(1);
//			ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
			
			RobustnessProgressWindow rpw = new RobustnessProgressWindow(usageModelExtFiles.size() + 1);
			
			// Build a new Optimization Engine engine and an empty initial
			// solution
			logger.info("Loading the optimization engine and preparing the solver");

			
			OptEngine engine = new PartialEvaluationOptimizationEngine(
					constraintHandler);

			// load the initial solution from the PCM specified in the
			// configuration and the extension
			logger.info("Parsing The Solution");
			try {
				engine.loadInitialSolution(resourceEnvExtFile,
						usageModelExtFile);
			} catch (JAXBException e) {
				logger.error("Error in loading the initial solution", e);
			}

			// start the optimization
			logger.info("Starting the optimization");
			executor.execute(engine);
			
//			Future<?> fut = executor.submit(engine);
//			try {
//				fut.get();
//				rpw.setValue(1);
//			} catch (InterruptedException | ExecutionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			ArrayList<File> solutions = new ArrayList<File>();
			int terminated = 0;
			{
				File f = new File(c.PROJECT_PATH + File.separator + resFolder + File.separator + "solution.xml");
				solutions.add(f);
				
				boolean found = false;
				while (!found) {
					if (f.exists()) {
						terminated++;
						found = true;
						
						// to save space on hd I remove the results as soon as i get the solution.xml file, because that's all I need
						ConfigurationHandler.cleanFolders(f.getParent() + File.separator + "performance_results");
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
			
			rpw.add("Initial", usageModelExtFile, solutions.get(0));
			rpw.setValue(terminated);
			rpw.save2png(solutions.get(0).getParent());
			
			int el = 0;
			for (File f : usageModelExtFiles) {
				Space4Cloud s4c = new Space4Cloud(true, 1, resourceEnvironmentFile, resFolder + File.separator + el,
						usageFile, allocationFile, repositoryFile, solver, lineConfFile,
						f, resourceEnvExtFile, constraintFile);
				
				Future<?> fut = executor.submit(s4c);
				try {
					fut.get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//				executor.execute(s4c);
				
				{
					File g = new File(c.PROJECT_PATH + File.separator + resFolder + File.separator + el + File.separator + "solution.xml");
					solutions.add(g);
					
					boolean found = false;
						while (!found) {
						if (g.exists()) {
							terminated++;
							found = true;
							
							// to save space on hd I remove the results as soon as i get the solution.xml file, because that's all I need
							ConfigurationHandler.cleanFolders(g.getParent() + File.separator + "performance_results");
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
				
				rpw.add("Var " + el, usageModelExtFiles.get(el), solutions.get(el+1));
				rpw.setValue(terminated);
				rpw.save2png(solutions.get(0).getParent());
				
				el++;
			}
			
			executor.shutdown();
			
//			ArrayList<File> solutions = new ArrayList<File>();
//			{
//				File sol = new File(c.PROJECT_PATH + File.separator + resFolder + File.separator + "solution.xml");
//				solutions.add(sol);
//				for (int i = 0; i < usageModelExtFiles.size(); i++) {
//					File f = new File(c.PROJECT_PATH + File.separator + resFolder + File.separator + i + File.separator + "solution.xml");
//					solutions.add(f);
//				}
//			}
//			
//			int terminated = 0;
//			while (terminated != usageModelExtFiles.size() + 1) {
//				terminated = 0;
//				
//				for (File f : solutions) {
//					if (f.exists()) {
//						terminated++;
//						
//						// to save space on hd I remove the results as soon as i get the solution.xml file, because that's all I need
//						ConfigurationHandler.cleanFolders(f.getParent() + File.separator + "performance_results");
//					}
//				}
//				rpw.setValue(terminated);
//				if (terminated != usageModelExtFiles.size() + 1) {
//					try {
//						Thread.sleep(1000);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			
//			try {
//				
//				rpw.add("Initial", usageModelExtFile, solutions.get(0));
//				
//				for (int i = 0; i < usageModelExtFiles.size(); ++i)
//					rpw.add("Var " + i, usageModelExtFiles.get(i), solutions.get(i+1));
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			logger.info("Check ended!");
//			rpw.save2png(solutions.get(0).getParent());
			
		}
		return null;
	}

	private void buildFolderStructure(Path lqnModelPath, Path resultModelPath)
			throws IOException {
		for (int i = 0; i < 24; i++) {
			Path tmpFolderPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
					c.PERFORMANCE_RESULTS_FOLDER, c.FOLDER_PREFIX + i);
			Files.createDirectory(tmpFolderPath);
			Path tmpLqnPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
					c.PERFORMANCE_RESULTS_FOLDER, c.FOLDER_PREFIX + i,
					lqnModelPath.getFileName().toString());
			Files.copy(lqnModelPath, tmpLqnPath);
			Path tmpResultPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
					c.PERFORMANCE_RESULTS_FOLDER, c.FOLDER_PREFIX + i,
					resultModelPath.getFileName().toString());
			Files.copy(resultModelPath, tmpResultPath);
		}
	}

	private void refreshProject() throws CoreException {
		ResourcesPlugin
				.getWorkspace()
				.getRoot()
				.getProject(c.PROJECT_NAME)
				.refreshLocal(IResource.DEPTH_INFINITE,
						new NullProgressMonitor());

	}

	protected void done() {
		try {
			get();
		} catch (ExecutionException e) {
			logger.error("Execution error while running space4cloud ", e);
		} catch (InterruptedException e) {
			logger.error("Interrupted execution of space4cloud ", e);
		}
	}

}
