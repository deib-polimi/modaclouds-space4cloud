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
package it.polimi.modaclouds.space4cloud.optimization;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.time.StopWatch;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.emf.common.util.EList;
import org.jfree.data.xy.XYSeriesCollection;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.ClosedWorkload;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.Loop;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.solver.models.PCMInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import it.polimi.modaclouds.adaptationDesignTime4Cloud.Main.AdaptationModelBuilder;
import it.polimi.modaclouds.space4cloud.chart.GenericChart;
import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.EvaluationException;
import it.polimi.modaclouds.space4cloud.exceptions.InitializationException;
import it.polimi.modaclouds.space4cloud.exceptions.OptimizationException;
import it.polimi.modaclouds.space4cloud.gui.BestSolutionExplorer;
import it.polimi.modaclouds.space4cloud.gui.OptimizationProgressWindow;
import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;
import it.polimi.modaclouds.space4cloud.optimization.bursting.PrivateCloud;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.constraints.MachineTypeConstraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.NumberProvidersConstraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.RamConstraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ReplicasConstraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.WorkloadPercentageConstraint;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationProxy;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationServer;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Database;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.PaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Platform;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Queue;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Cache;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Policy;
import it.polimi.modaclouds.space4cloud.utils.MILPEvaluator;
import it.polimi.modaclouds.space4cloud.utils.ResourceEnvironmentExtensionParser;
import it.polimi.modaclouds.space4cloud.utils.ResourceEnvironmentLoadingException;
import it.polimi.modaclouds.space4cloud.utils.SolutionHelper;
import it.polimi.modaclouds.space4cloud.utils.UsageModelExtensionParser;

/**
 * @author Michele Ciavotta Class defining the optimization engine.
 */
public class OptEngine extends SwingWorker<Void, Void> implements
		PropertyChangeListener {

	protected SolutionMulti initialSolution = null;

	protected SolutionMulti bestSolution = null;

	protected SolutionMulti currentSolution = null;

	protected SolutionMulti localBestSolution = null;

	private List<SolutionMulti> bestSolutions = new ArrayList<SolutionMulti>();

	protected ConstraintHandler constraintHandler;

	protected DataHandler dataHandler;

	protected int numberOfIterations;

	protected int numberOfFeasibilityIterations;

	protected Policy SELECTION_POLICY;

	protected int MAXMEMORYSIZE = 10;

	private static final int MAX_SCRAMBLE_NO_CHANGE = 10;

	protected int MAX_SCRUMBLE_ITERS = 20; /*
											 * 40 Now it is a constant in the
											 * future it might become a
											 * parameter
											 */

	protected int MAXFEASIBILITYITERATIONS = 10; // 20

	/**
	 * Tabu list containing the representation of the solutions recently
	 * evaluated, used for the tabu search (scramble phase).
	 */
	protected Cache<String, String> tabuSolutionList;

	/**
	 * This is the long term memory of the tabu search used in the scramble
	 * process. Each Tier (key of the Map) has its own memory which uses the
	 * resource name as ID. This memory is used to restart the search process
	 * after the full exploration of a local optimum by building a solution out
	 * of components with low frequency
	 */
	protected Map<String, Cache<String, Integer>> longTermFrequencyMemory;

	protected Random random;
	protected int numIterNoImprov = 0;

	protected StopWatch timer = new StopWatch();

	protected EvaluationServer evalServer;

	protected Logger logger = LoggerFactory.getLogger(OptEngine.class);
	protected Logger optimLogger = LoggerFactory.getLogger("optimLogger");
	protected Logger scrambleLogger = LoggerFactory.getLogger("scrambleLogger");

	protected GenericChart<XYSeriesCollection> costLogImage;
	protected GenericChart<XYSeriesCollection> logVm;
	protected GenericChart<XYSeriesCollection> logConstraints;

	protected String bestSolutionSerieHandler;
	protected String localBestSolutionSerieHandler;

	private boolean batch = false;

	public OptEngine(ConstraintHandler handler)
			throws DatabaseConnectionFailureExteption {
		this(handler, false);
	}

	/**
	 * Instantiates a new opt engine.
	 * 
	 * @param handler
	 *            : the constraint handler
	 * @throws DatabaseConnectionFailureExteption
	 */
	public OptEngine(ConstraintHandler handler, boolean batch)
			throws DatabaseConnectionFailureExteption {

		loadConfiguration();

		try {
			costLogImage = GenericChart.createCostLogger();
			logVm = GenericChart.createVmLogger();
			logConstraints = GenericChart.createConstraintsLogger();
		} catch (NumberFormatException | IOException e) {
			logger.error("Unable to create chart loggers", e);
		}

		optimLogger.debug("Random seed: " + Configuration.RANDOM_SEED);
		random = new Random(Configuration.RANDOM_SEED);

		// batch mode

		showConfiguration();

		tabuSolutionList = new Cache<>(MAXMEMORYSIZE);
		constraintHandler = handler;

		/* this handle manage the data loaded from the database */
		dataHandler = DataHandlerFactory.getHandler();

		/* this object is a server needed to evaluate the solutions */
		evalServer = new EvaluationProxy();
		evalServer.setConstraintHandler(handler);
		evalServer.setLog2png(costLogImage);
		evalServer.setMachineLog(logVm);
		evalServer.setConstraintLog(logConstraints);
		evalServer.setTimer(timer);
		evalServer.addPropertyChangeListener(this);

		// this.evalProxy.setEnabled(false);

		this.batch = batch;
	}

	protected void showConfiguration() {
		optimLogger.info("Running the optimization with parameters:");
		optimLogger.info("Max Memory Size: " + MAXMEMORYSIZE);
		optimLogger.info("Max Scrumble Iterations: " + MAX_SCRUMBLE_ITERS);
		optimLogger.info("Max Feasibility Iterations: "
				+ MAXFEASIBILITYITERATIONS);
		optimLogger.info("Selection Policy: " + SELECTION_POLICY);
	}

	/**
	 * This method should allow the change of workload at runtime!
	 * 
	 * @param sol
	 *            the current solution that is going to be modified by the
	 *            method.
	 * @param rates
	 *            the rates by which we'll multiply the actual workload, taken
	 *            from the usage model extension file.
	 */
	protected void changeWorkload(Solution sol, double[] rates) {

		logger.debug("The hourly values of the workload are:");
		String tmp = "";
		for (Instance i : sol.getApplications())
			tmp += i.getWorkload() + " ";
		logger.debug(tmp);
		logger.debug("Trying to change them using those rates:");
		tmp = "";
		for (double rate : rates)
			tmp += rate + " ";
		logger.debug(tmp);

		MoveChangeWorkload move = new MoveChangeWorkload(sol);

		try {
			move.modifyWorkload(rates);

			logger.debug("Done! The new values are:");
			tmp = "";
			for (Instance i : sol.getApplications())
				tmp += i.getWorkload() + " ";
			logger.debug(tmp);

		} catch (ParserConfigurationException | SAXException | IOException
				| JAXBException e) {
			logger.error("Error!", e);

			return;
		}

	}

	/**
	 * This method should allow the change of workload at runtime!
	 * 
	 * @param sol
	 *            the current solution that is going to be modified by the
	 *            method.
	 * @param hour
	 *            the hour for which we are going to modify the workload
	 * @param rate
	 *            the rate by which we'll multiply the actual workload, taken
	 *            from the usage model extension file.
	 */
	protected void changeWorkload(Solution sol, int hour, double rate) {

		logger.debug("The hourly values of the workload are:");
		String tmp = "";
		for (Instance i : sol.getApplications())
			tmp += i.getWorkload() + " ";
		logger.debug(tmp);
		logger.debug("Trying to change the " + hour + " hour using this rate: "
				+ rate);

		MoveChangeWorkload move = new MoveChangeWorkload(sol);

		try {
			move.modifyWorkload(hour, rate);

			logger.debug("Done! The new values are:");
			tmp = "";
			for (Instance i : sol.getApplications())
				tmp += i.getWorkload() + " ";
			logger.debug(tmp);

		} catch (ParserConfigurationException | SAXException | IOException
				| JAXBException e) {
			logger.error("Error!", e);
			return;
		}

	}

	@Override
	protected Void doInBackground() throws Exception {
		if (initialSolution.size() == 1)
			Configuration.REDISTRIBUTE_WORKLOAD = false;
		try {
			optimize();
		} catch (OptimizationException e) {
			logger.error("Optimization raised an exception", e);
		}
		if (!batch)
			BestSolutionExplorer.prepare(this);
		return null;
	}

	protected Tier findResource(Instance application, String id) {
		return findResource(application, id, SELECTION_POLICY);
	}

	/**
	 * Find the tier to scale given the id of the constraint
	 * 
	 * @param id
	 *            of the resource in the constraint
	 * @return a IaaS resource on which to perform a scale operation.
	 */
	protected Tier findResource(Instance application, String id, Policy policy) {

		Tier resource = null;
		IConstrainable constrainedResource = application
				.getConstrainableResources().get(id);

		// if the constraint is on the tier resource we do nothing
		if (constrainedResource instanceof Tier)
			resource = ((Tier) constrainedResource);
		// if the constraint is on a functionality we have to build the list of
		// affected components
		else if (constrainedResource instanceof Functionality) {
			List<Functionality> functionalityChain = ((Functionality) constrainedResource)
					.getExternalCallTrace();
			functionalityChain.add(0, (Functionality) constrainedResource);
			// select the functionality to get the container and the IaaS
			// Resource from
			Functionality selectedFun = null;
			// with random policy
			if (policy == Policy.Random)
				selectedFun = functionalityChain.get(new Random()
						.nextInt(functionalityChain.size()));

			// just the first of the list
			else if (policy == Policy.First)
				selectedFun = functionalityChain.get(0);

			// the functionality that takes more time in the chain
			else if (policy == Policy.Longest) {
				selectedFun = functionalityChain.get(0);
				for (Functionality f : functionalityChain)
					if (f.isEvaluated()
							&& f.getResponseTime() > selectedFun
									.getResponseTime())
						selectedFun = f;
			}

			// the functionality whose resource has higher utilization
			else if (policy == Policy.Utilization) {
				selectedFun = functionalityChain.get(0);
				resource = selectedFun.getContainer().getContainer();
				for (Functionality f : functionalityChain) {
					Tier affectedRes = f.getContainer().getContainer();
					if (affectedRes.getUtilization() > resource
							.getUtilization()) {
						selectedFun = f;
						resource = affectedRes;
					}
				}
			}

			// retreive the IaaS resource from the selected functionality
			resource = selectedFun.getContainer().getContainer();

		}

		// if the constraint is on a component
		// TODO: this could be improved by looking at the trace of all
		// functionalities in the component and selecting the resource that have
		// the highest aggregated utilization.
		if (constrainedResource instanceof Component)
			resource = ((Component) constrainedResource).getContainer();

		return resource;

	}

	protected void findSeffsInScenarioBehavior(
			ScenarioBehaviour scenarioBehaviour, Map<String, String> calls) {

		for (AbstractUserAction action : scenarioBehaviour
				.getActions_ScenarioBehaviour()) {
			if (action instanceof Branch) {
				for (BranchTransition trans : ((Branch) action)
						.getBranchTransitions_Branch()) {
					// recursively look in all branches
					findSeffsInScenarioBehavior(
							trans.getBranchedBehaviour_BranchTransition(),
							calls);
				}
			} else if (action instanceof EntryLevelSystemCall) {
				String callID = action.getId();
				String signatureID = ((EntryLevelSystemCall) action)
						.getOperationSignature__EntryLevelSystemCall().getId();
				calls.put(signatureID, callID);
			} else if (action instanceof Loop) {
				findSeffsInScenarioBehavior(
						((Loop) action).getBodyBehaviour_Loop(), calls);
			}
		}

	}

	/**
	 * @param sol
	 * @return
	 */
	protected MoveOnVM[] generateArrayMoveOnVM(Solution sol) {
		MoveOnVM moveArray[] = new MoveOnVM[24];
		for (int i = 0; i < 24; i++) {
			moveArray[i] = new MoveOnVM(sol, i);
		}
		return moveArray;
	}

	/**
	 * Generates a list for each hour containing all the resources that can be
	 * scaled in
	 * 
	 * @param sol
	 * @return
	 */
	protected List<ArrayList<Tier>> generateVettResTot(Solution sol) {
		List<ArrayList<Tier>> vettResTot = new ArrayList<ArrayList<Tier>>(24);
		// list of lists of Iass

		for (Instance i : sol.getApplications()) {
			ArrayList<Tier> resMemory = new ArrayList<>();
			for (Tier t : i.getTiers())
				// if the cloud service hosting the application tier is a IaaS
				if ((t.getCloudService() instanceof IaaS &&
				// and it has more than one replica
						(t.getCloudService().getReplicas() > 1) || (t
						.getCloudService() instanceof PaaS
						&& ((PaaS) t.getCloudService()).areReplicasChangeable() && (t
						.getCloudService().getReplicas() > 1))))
					// add it to the list of resources that can be scaled in
					resMemory.add(t);

			vettResTot.add(resMemory);
		}
		return vettResTot;
	}

	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}

	public GenericChart<XYSeriesCollection> getConstraintsLogger() {
		return logConstraints;
	}

	public GenericChart<XYSeriesCollection> getCostLogger() {
		return costLogImage;
	}

	public EvaluationServer getEvalServer() {
		return evalServer;
	}

	public SolutionMulti getInitialSolution() {
		return initialSolution;
	}

	public int getMaxIterations() {
		return MAX_SCRUMBLE_ITERS;
	}

	public GenericChart<XYSeriesCollection> getVMLogger() {
		return logVm;
	}

	/**
	 * Internal optimization. The aim of this method is to locally optimize the
	 * current solution by increasing or decreasing the number of replicas of
	 * certain cloud resources taking into account the total cost and the
	 * constraints.
	 * 
	 * @param sol
	 *            is the current solution
	 * 
	 * @throws OptimizationException
	 */
	protected void InternalOptimization(Solution sol)
			throws OptimizationException {

		/*
		 * 
		 * Innanzitutto dobbiamo implementare almeno 2 spazi di ricerca. Ogni
		 * spazio e' definito da una mossa specifica la mossa 1 consiste
		 * nell'aumentare o diminuire in numero di repliche di una certa risorsa
		 * cloud (tipicamente saranno VM) la mossa 2 consiste nel ribilanciare
		 * gli arrival rates tra i vari provider.
		 * 
		 * Dobbiamo chiederci quindi: 1) con quale forza applichiamo una certa
		 * mossa. (es. di quanto aumentiamo o diminuiamo il numero di VM) 2) in
		 * che ordine eseguiamo le mosse 1 e 2. (potremmo per esempio
		 * implementare una roulette e assegnare una certa proprieta' p ad una
		 * mossa e una proprieta' 1-p all'altra)
		 * 
		 * -Si potrebbe cercare di stimare l'impatto delle mosse e decidere
		 * quale attuare. -Si deve cercare di capire dove attuare quando c'e' un
		 * vincolo che non e' soddisfatto.
		 * 
		 * - ragionando alla lavagna con gibbo ci siamo resi conto che una mossa
		 * di bilanciamento ha gli stessi effetti di una di variazione. per
		 * questo non e' necessario far seguire ad una mossa di bilanciamento
		 * una serie di mosse di variazione per far assestare i risultati.
		 */

		// if the solution is unfeasible there is a first phase in which the
		// solution is forced to became feasible

		optimLogger.trace("feasibility phase");
		makeFeasible(sol);
		// optimLogger.trace("feasible solution: "+sol.showStatus());
		// If the current solution is better than the best one it becomes the
		// new best solution.

		updateBestSolution(sol);
		updateLocalBestSolution(sol);

		optimLogger.info("costReduction phase");
		if (sol.isFeasible()) {
			IteratedRandomScaleInLS(sol);
		} else {
			logger.info("Solution not feasible, skipping scale in");
		}
		// optimLogger.trace("optimized solution"+sol.showStatus());

	}

	protected void InternalOptimization(SolutionMulti sol)
			throws OptimizationException {

		for (Solution s : sol.getAll())
			InternalOptimization(s);
	}

	protected boolean isMaxNumberOfFesibilityIterations() {

		if (numberOfFeasibilityIterations <= MAXFEASIBILITYITERATIONS) {
			return false;
		}
		return true;
	}

	protected boolean isMaxNumberOfIterations() {

		if (numberOfIterations <= MAX_SCRUMBLE_ITERS) {
			return false;
		}
		return true;
	}

	protected void IteratedRandomScaleInLS(Solution sol)
			throws OptimizationException {
		for (Constraint constraint : sol.getViolatedConstraints()) {
			if (constraint instanceof RamConstraint) {
				logger.info("Wron type of VM selected, make feasible will not be executed");
				return;
			}
		}
		MoveOnVM[] moveArray = generateArrayMoveOnVM(sol);

		List<ArrayList<Tier>> vettResTot = generateVettResTot(sol);
		/* first phase: overall descent optimization */
		// logger.warn("Descent Optimization first phase - start");
		// logger.warn(sol.isFeasible()+","+sol.numberOfUnfeasibleHours());
		// for(Instance i:sol.getApplications())
		// if(!i.isFeasible())
		// logger.warn("\thour: "+sol.getApplications().indexOf(i)+" violated constraints: "+i.getNumerOfViolatedConstraints());
		boolean done = false;
		logger.info("\t Descent Optimization second phase");
		resetNoImprovementCounter();
		Solution restartSol = sol.clone();
		Tier tier;
		Tier tierArray[] = new Tier[24];
		while (this.numIterNoImprov < 5) {
			done = false;
			while (!done) {
				tier = null;
				boolean noScaleIn = true;
				for (int i = 0; i < 24; i++) {

					for (int j = 0; j < vettResTot.get(i).size(); j++)
						if ((vettResTot.get(i).get(j).getCloudService() instanceof IaaS && vettResTot
								.get(i).get(j).getCloudService().getReplicas() == 1)
								|| (vettResTot.get(i).get(j).getCloudService() instanceof PaaS
										&& ((PaaS) vettResTot.get(i).get(j)
												.getCloudService())
												.areReplicasChangeable() && vettResTot
										.get(i).get(j).getCloudService()
										.getReplicas() == 1))
							vettResTot.get(i).remove(j);

					if (vettResTot.get(i).size() > 0) {
						tier = vettResTot.get(i).get(
								random.nextInt(vettResTot.get(i).size()));
						moveArray[i].scaleIn(tier);
						tierArray[i] = tier;
						noScaleIn = false;
					} else
						tierArray[i] = null;

				}

				// without any scaleIn the solution is stuck
				if (noScaleIn) {
					numIterNoImprov++;
					break;

				}

				try {
					evalServer.EvaluateSolution(sol);
				} catch (EvaluationException e) {
					throw new OptimizationException("", "scaleIn", e);
				}
				updateBestSolution(sol);

				if (!sol.isFeasible()) {
					int totSize = 0;

					for (int i = 0; i < 24; i++) {
						if (!sol.getApplication(i).isFeasible()) {
							moveArray[i].scaleOut(tierArray[i]);
							vettResTot.get(i).remove(tierArray[i]);
						}
						totSize += vettResTot.get(i).size();
					}

					if (totSize == 0)
						done = true;
				}
			}

			// here we have to implement the restart

			// the clone could be avoided if we save the original state of the
			// tiers
			sol = restartSol.clone();
			moveArray = generateArrayMoveOnVM(sol);
			vettResTot = generateVettResTot(sol);

		}// while

	}

	@Override
	protected void done() {
		evalServer.terminateServer();
		try {
			get();
		} catch (InterruptedException e) {
			logger.error("Interrupted ending of optimization engine", e);
		} catch (ExecutionException e) {
			logger.error(
					"Execution exception occurred in the optimization engine",
					e);
		} catch (CancellationException e) {
			logger.debug("Execution was cancelled");
		}

	}

	/**
	 * Load initial solution. The aim of this method is to load the initial
	 * solution from file.
	 * 
	 * @param resourceEnvExtension
	 *            the extension file
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws JAXBException
	 */
	public void loadInitialSolution() throws InitializationException {
		loadInitialSolution(null, null);
	}

	/**
	 * Load the initial solution. The aim of this method is to load the initial
	 * solution from the palladio model files contained in the configuration
	 * 
	 */
	public void loadInitialSolution(File generatedInitialSolution,
			File generatedInitialMce) throws InitializationException {
		boolean closedWorkload = true;

		// initialSolution = new Solution();
		this.initialSolution = new SolutionMulti();

		// parse the extension file

		ResourceEnvironmentExtensionParser resourceEnvParser;
		try {
			resourceEnvParser = new ResourceEnvironmentExtensionParser();
		} catch (ResourceEnvironmentLoadingException e) {
			throw new InitializationException(
					"Error loading the resource environment extension", e);
		}

		UsageModelExtensionParser usageModelParser;
		try {
			usageModelParser = new UsageModelExtensionParser(Paths.get(
					Configuration.USAGE_MODEL_EXTENSION).toFile());
		} catch (ParserConfigurationException | SAXException | IOException
				| JAXBException e) {
			throw new InitializationException(
					"Error loading the usage model extension", e);
		}

		// get the PCM from the launch configuration
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = org.eclipse.core.runtime.Path.fromOSString(Paths.get(
				Configuration.PROJECT_BASE_FOLDER,
				Configuration.WORKING_DIRECTORY, Configuration.LAUNCH_CONFIG)
				.toString());
		@SuppressWarnings("deprecation")
		IFile ifile = workspace.getRoot().findFilesForLocation(location)[0]; // getFileForLocation(location);
		ILaunchConfiguration launchConfig = DebugPlugin.getDefault()
				.getLaunchManager().getLaunchConfiguration(ifile);

		@SuppressWarnings("deprecation")
		PCMInstance pcm = new PCMInstance(launchConfig);
		// Since we are working on a single cloud solution a hourly solution

		// is just an instance (no multi cloud, no load balancer)

		// create a tier for each resource container
		EList<ResourceContainer> resourceContainers = pcm
				.getResourceEnvironment()
				.getResourceContainer_ResourceEnvironment();

		EList<UsageScenario> scenarios = pcm.getUsageModel()
				.getUsageScenario_UsageModel();
		if (scenarios.size() > 1) {
			logger.warn("Multiple user scenarios defined, Space4Cloud currently only support single scenarios");
			throw new InitializationException(
					"Multiple user scenarios defined in the PCM model");
		} else if (scenarios.size() == 0) {
			throw new InitializationException(
					"No usage scenario have been defined in the PCM");
		}
		if (scenarios.get(0).getWorkload_UsageScenario() instanceof ClosedWorkload)
			closedWorkload = true;
		else
			closedWorkload = false;

		if (!closedWorkload) {
			throw new InitializationException(
					"No usage scenario have been defined in the PCM");
		}

		// we need to create a Solution object for each one of the providers

		ArrayList<String> providers = new ArrayList<String>();
		for (String s : resourceEnvParser.getProviders().values()) {
			if (s != null && !providers.contains(s))
				providers.add(s);
		}

		// if no provider has been selected pick one form the database
		boolean defaultProvider = false;

		int providerMin = 1;
		{
			List<Constraint> constraints = constraintHandler
					.getConstraintByResourceId(Configuration.APPLICATION_ID);

			for (Constraint c : constraints)
				if (c instanceof NumberProvidersConstraint)
					providerMin = ((NumberProvidersConstraint) c).getMin();
		}

		if (providers.size() < providerMin) {
			defaultProvider = true;
			Set<String> providerNames = dataHandler.getCloudProviders();

			if (providerNames.size() < providerMin) {
				logger.error("There are not enough providers in the database!");
				throw new InitializationException(
						"There are not enough providers in the database!");
			}

			while (providers.size() < providerMin) {
				String defaultProviderName = null;
				Iterator<String> iter = providerNames.iterator();

				do {
					defaultProviderName = iter.next();
					// skip the generic provider whose data might not be
					// relevant

					// and those that do not offer Compute services. (use only
					// IaaS now)

					if (defaultProviderName.equals("Generic")
							|| dataHandler.getServices(defaultProviderName,
									"Compute").size() == 0
							|| providers.contains(defaultProviderName))
						defaultProviderName = null;
				} while (defaultProviderName == null && iter.hasNext());

				if (defaultProviderName == null) {
					logger.error("No provider with services of type Compute has been found");
					throw new InitializationException(
							"No default provider could be found");
				}
				providers.add(defaultProviderName);
				logger.info("No provider specified in the extension, defaulting on "
						+ defaultProviderName);
			}

		}

		for (String provider : providers) {

			Solution initialSolution = new Solution();

			try {
				initialSolution.buildFolderStructure(provider);
			} catch (IOException e) {
				throw new InitializationException(
						"No default provider could be found");
			}

			// set the region
			initialSolution.setRegion(resourceEnvParser.getRegion(provider));

			for (int i = 0; i < 24; i++) {
				logger.info("Initializing hour " + i);
				Instance application = new Instance();
				initialSolution.addApplication(application);
				File[] models = Paths
						.get(Configuration.PROJECT_BASE_FOLDER,
								Configuration.WORKING_DIRECTORY,
								Configuration.PERFORMANCE_RESULTS_FOLDER,
								provider, Configuration.FOLDER_PREFIX + i)
						.toFile().listFiles(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								return name.endsWith(".xml")
										&& !name.contains("line");
							}
						});
				// suppose there is just 1 model
				Path lqnModelPath = models[0].toPath();
				application.initLqnHandler(lqnModelPath);

				// add population and think time from usage model extension
				int population = -1;
				double thinktime = -1;

				double arrivalRate = -1;

				application.setClosedWorkload(closedWorkload);
				if (closedWorkload && !usageModelParser.isClosedWorkload())
					throw new InitializationException(
							"The PCM model constains an closed workload while the usage model extension does not");

				if (usageModelParser.getPopulations().size() == 1)
					population = usageModelParser.getPopulations().values()
							.iterator().next()[i];
				if (usageModelParser.getThinkTimes().size() == 1)
					thinktime = usageModelParser.getThinkTimes().values()
							.iterator().next()[i];

				if (usageModelParser.getArrivalRates().size() == 1)
					arrivalRate = usageModelParser.getArrivalRates().values()
							.iterator().next()[i];

				double percentage = (double) 1 / providers.size();

				population = (int) Math.ceil(population * percentage);

				initialSolution.setPercentageWorkload(i, percentage);

				if (application.isClosedWorkload()) {
					application.getLqnHandler().setPopulation(population);
					application.getLqnHandler().setThinktime(thinktime);
				} else {
					application.getLqnHandler().setArrivalRate(arrivalRate);
				}

				application.getLqnHandler().saveToFile();
				if (application.isClosedWorkload()) {
					application.setWorkload(population);
				} else {
					application.setWorkload((int) arrivalRate);
				}

				// // create a tier for each resource container
				// EList<ResourceContainer> resourceContainers = pcm
				// .getResourceEnvironment()
				// .getResourceContainer_ResourceEnvironment();

				// STEP 1: load the resource environment
				for (ResourceContainer c : resourceContainers) {
					logger.trace("Tier " + c.getId());

					CloudService service = null;
					// switch over the type of cloud service
					// String cloudProvider =
					// resourceEnvParser.getProviders().get(
					// c.getId()); // provider

					// associated
					// to
					// the
					// resource
					if (!resourceEnvParser.getServiceType().containsKey(
							c.getId() + (defaultProvider ? "" : provider)))
						throw new InitializationException("The tier with ID "
								+ c.getId() + " could not be initialized");
					String serviceType = resourceEnvParser.getServiceType()
							.get(c.getId() + (defaultProvider ? "" : provider)); // Service

					if (!resourceEnvParser.getInstanceSize().containsKey(
							c.getId() + (defaultProvider ? "" : provider)))
						throw new InitializationException("The tier with ID "
								+ c.getId() + " could not be initialized");
					String resourceSize = resourceEnvParser.getInstanceSize()
							.get(c.getId() + (defaultProvider ? "" : provider));

					if (!resourceEnvParser.getServiceName().containsKey(
							c.getId() + (defaultProvider ? "" : provider)))
						throw new InitializationException("The tier with ID "
								+ c.getId() + " could not be initialized");
					String serviceName = resourceEnvParser.getServiceName()
							.get(c.getId() + (defaultProvider ? "" : provider));

					if (!resourceEnvParser.getInstanceReplicas().containsKey(
							c.getId() + (defaultProvider ? "" : provider)))
						throw new InitializationException("The tier with ID "
								+ c.getId() + " could not be initialized");
					int replicas = resourceEnvParser.getInstanceReplicas().get(
							c.getId() + (defaultProvider ? "" : provider))[i];

					// // pick a cloud provider if not specified by the
					// extension
					// if (cloudProvider == null)
					// cloudProvider =
					// dataHandler.getCloudProviders().iterator()
					// .next();

					// pick a service if not specified by the extension
					// if (serviceName == null)
					// logger.info("provider: " + provider + " default: "
					// + defaultProvider);
					logger.trace("serviceType: " + serviceType);

					String actualProvider = provider;
					if (actualProvider.indexOf(PrivateCloud.BASE_PROVIDER_NAME) > -1) {
						boolean keepGoing = true;
						for (int pi = 0; pi < providers.size() && keepGoing; ++pi) {
							String p = providers.get(pi);
							if (p.indexOf(PrivateCloud.BASE_PROVIDER_NAME) > -1)
								continue;
							double speed = dataHandler.getProcessingRate(p,
									serviceName, resourceSize);
							if (speed > -1) {
								actualProvider = p;
								keepGoing = false;
							}
						}
					}

					for (String st : dataHandler.getServices(actualProvider, // cloudProvider,
							serviceType))
						logger.trace("\tService Name: " + st);
					serviceName = dataHandler.getServices(actualProvider, // cloudProvider,
							serviceType).get(0);
					// if the resource size has not been decided pick one
					if (resourceSize == null) {
						logger.trace("Defaulting on resource Size");
						resourceSize = dataHandler
								.getCloudElementSizes(actualProvider,/*
								 * cloudProvider,
								 */serviceName, Configuration.BENCHMARK.toString())
								 .iterator().next();
					}
					logger.trace("default size" + resourceSize);

					// double speed =
					// dataHandler.getProcessingRate(actualProvider, //
					// cloudProvider,
					// serviceName, resourceSize);
					// logger.trace("processing rate "+speed);
					//
					// int ram = dataHandler.getAmountMemory(actualProvider, //
					// cloudProvider,
					// serviceName, resourceSize);
					// logger.trace("ram "+ram);
					//
					//
					// int numberOfCores = dataHandler.getNumberOfReplicas(
					// actualProvider, /* cloudProvider, */serviceName,
					// resourceSize);
					//
					// logger.trace("cores "+numberOfCores);

					/*
					 * each tier has a certain kind of cloud resource and a
					 * number of replicas of that resource
					 */
					Tier t = new Tier(c.getId(), c.getEntityName()
							+ "_CPU_Processor"); // TODO: here for PaaS?

					// set the name
					t.setName(resourceEnvParser.getContainerNames().get(
							c.getId() + (defaultProvider ? "" : provider)));

					/* creation of a Compute type resource */
					// service = new Compute(provider, /* cloudProvider, */
					// serviceType, serviceName, resourceSize, replicas,
					// numberOfCores, speed, ram);

					service = dataHandler.getCloudService(provider,
							serviceType, serviceName, resourceSize, replicas);

					if (service == null)
						throw new InitializationException(
								"The tier with ID "
										+ c.getId()
										+ " could not be initialized, no service of type: "
										+ serviceType + " with name: "
										+ serviceName + " and size: "
										+ resourceSize
										+ " could be found for ptovider: "
										+ provider);
					t.setService(service);

					application.addTier(t);

				}

				// STEP 2: parse the usage model to get the reference between
				// calls
				// and seffs in components
				HashMap<String, String> systemCalls2Signatures = new HashMap<>();
				ScenarioBehaviour scenarioBehaviour = pcm.getUsageModel()
						.getUsageScenario_UsageModel().get(0)
						.getScenarioBehaviour_UsageScenario();
				findSeffsInScenarioBehavior(scenarioBehaviour,
						systemCalls2Signatures);

				// STEP 3: load components from the allocation
				EList<AllocationContext> allocations = pcm.getAllocation()
						.getAllocationContexts_Allocation();

				Map<String, Functionality> functionalities = new HashMap<>();
				Map<String, String> startActionId2FunId = new HashMap<>();

				for (AllocationContext context : allocations) {
					String containerId = context
							.getResourceContainer_AllocationContext().getId();
					RepositoryComponent repositoryComp = context
							.getAssemblyContext_AllocationContext()
							.getEncapsulatedComponent__AssemblyContext();

					// create the component
					Component comp = new Component(repositoryComp.getId(),
							repositoryComp.getEntityName());

					// add the functionalities (from SEFFs)
					EList<ServiceEffectSpecification> seffs = ((BasicComponent) repositoryComp)
							.getServiceEffectSpecifications__BasicComponent();
					for (ServiceEffectSpecification s : seffs) {
						String signatureID = s.getDescribedService__SEFF()
								.getId();
						Functionality function = new Functionality(s
								.getDescribedService__SEFF().getEntityName(),
								((ResourceDemandingSEFF) s).getId(),
								systemCalls2Signatures.get(signatureID));
						EList<AbstractAction> actions = ((ResourceDemandingSEFF) s)
								.getSteps_Behaviour();
						for (AbstractAction a : actions) {
							if (a instanceof ExternalCallAction) {
								// add the id of the called functionality to the
								// list of external calls
								OperationSignature sig = ((ExternalCallAction) a)
										.getCalledService_ExternalService();
								function.addExternalCall(sig
										.getInterface__OperationSignature()
										.getEntityName()
										+ "_" + sig.getEntityName(), null);

							} else if (a instanceof StartAction) {
								startActionId2FunId.put(a.getId(),
										function.getId());

							}
						}
						functionalities.put(
								((Entity) s.getDescribedService__SEFF()
										.eContainer()).getEntityName()
										+ "_"
										+ s.getDescribedService__SEFF()
												.getEntityName(), function);
						comp.addFunctionality(function);
					}

					// add the component to the cloud resource
					for (Tier t : application.getTiers()) {
						if (t.getId().equals(containerId))
							t.addComponent(comp);
					}
				}

				// concurrent modification, use a temporary list
				// link the functionalities toghether using their ids

				for (Functionality f : functionalities.values()) {
					HashMap<String, Functionality> tempCalls = new HashMap<>();
					// fill the temporary hashmap
					for (String s : f.getExternalCalls().keySet())
						tempCalls.put(functionalities.get(s).getId(),
								functionalities.get(s));
					// clear the ids in the hashmap of the functionality
					f.getExternalCalls().clear();

					// add the mappings of the temporary hashmap
					f.getExternalCalls().putAll(tempCalls);
				}

				// initialize the constrinable hashmap
				application.initConstrainableResources();

				// fill the lqnProcessorId in the functionality by using the
				// mapping
				// between the start action and looking in the lqn file
				Map<String, String> processorId2FunId = application
						.getLqnHandler().getProcessorIdMap(startActionId2FunId);
				LqnResultParser.setIdSubstitutionMap(processorId2FunId);

				// use the initial evaluation to initialize parser and
				// structures

				// evalServer.evaluateInstance(application);

				// initialSolution.showStatus();
			}

			this.initialSolution.add(initialSolution);
		}

		this.initialSolution.setFrom(generatedInitialSolution,
				generatedInitialMce);

		try {
			evalServer.StartTimer();
			evalServer.EvaluateSolution(initialSolution);
			evalServer.StopTimer();
		} catch (EvaluationException e) {
			throw new InitializationException(
					"Could not evaulate the initial solution", e);
		}
		logger.info(this.initialSolution.showStatus());

		bestSolutions.add(initialSolution);
		firePropertyChange(OptimizationProgressWindow.FIRST_SOLUTION_AVAILABLE,
				false, true);
	}

	/**
	 * Loads the solution from a serliazed object
	 * 
	 * @throws InitializationException
	 * */
	public void loadInitialSolutionObject(File file)
			throws InitializationException {

		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(file);
			in = new ObjectInputStream(fis);
			initialSolution = (SolutionMulti) in.readObject();
			in.close();
			fis.close();
		} catch (Exception e) {

			throw new InitializationException(
					"Error initializng the solution from: "
							+ file.getAbsolutePath(), e);
		}
		logger.info("Deserialized: " + initialSolution);
	}

	protected void makeFeasible(SolutionMulti sol) throws OptimizationException {
		for (Solution s : sol.getAll())
			makeFeasible(s);
	}

	/**
	 * Make the solution feseable by performing scale out operations
	 * 
	 * @param sol
	 * @throws OptimizationException
	 */
	protected void makeFeasible(Solution sol) throws OptimizationException {

		final double MAX_FACTOR = 5;
		final double MIN_FACTOR = 1.2;
		numberOfFeasibilityIterations = 0;
		while (!sol.isFeasible() && !isMaxNumberOfFesibilityIterations()) {
			boolean evaluateAgain = false;

			for (Constraint constraint : sol.getViolatedConstraints()) {
				if (constraint instanceof RamConstraint) {
					// if the unfeasibility is due to a violated ram constraint
					// this procedure is ineffective and should be terminated
					// since vmType is constant among hours we can just look at
					// the first one
					logger.info("Wrong type of VM selected, make feasible will not be executed");
					return;
				} else if (constraint instanceof ReplicasConstraint) {
					// if the unfeasibility is due to a replicas constraint, we
					// fix it here
					ReplicasConstraint rconstraint = (ReplicasConstraint) constraint;

					for (int h = 0; h < 24; ++h) {
						Tier t = sol.getApplication(h).getTierById(
								constraint.getResourceID());
						CloudService service = t.getCloudService();
						if (rconstraint.hasMaxReplica(service)) {
							if (service instanceof IaaS) {
								((IaaS) service).setReplicas(rconstraint
										.getMax());
								evaluateAgain = true;
							} else if (service instanceof PaaS
									&& ((PaaS) service).areReplicasChangeable()) {
								((PaaS) service).setReplicas(rconstraint
										.getMax());
								evaluateAgain = true;
							}
						} else if (rconstraint.hasMinReplica(service)) {
							if (service instanceof IaaS) {
								((IaaS) service).setReplicas(rconstraint
										.getMin());
								evaluateAgain = true;
							} else if (service instanceof PaaS
									&& ((PaaS) service).areReplicasChangeable()) {
								((PaaS) service).setReplicas(rconstraint
										.getMin());
								evaluateAgain = true;
							}
						}
					}
				} else if (constraint instanceof MachineTypeConstraint) {
					for (int h = 0; h < 24; ++h) {
						Tier t = sol.getApplication(h).getTierById(
								constraint.getResourceID());
						if (scramble(sol, t, 0))
							evaluateAgain = true;
					}
				}
			}

			if (evaluateAgain)
				try {
					evalServer.EvaluateSolution(sol);
				} catch (EvaluationException e) {
					logger.error("Error while evaluating the solution.", e);
				}

			logger.info("\tFeasibility iteration: "
					+ numberOfFeasibilityIterations);

			double factor = MAX_FACTOR - (MAX_FACTOR - MIN_FACTOR)
					* numberOfFeasibilityIterations / MAXFEASIBILITYITERATIONS;
			for (int i = 0; i < 24; i++) {

				if (sol.getHourApplication().get(i).isFeasible())
					continue;

				Set<Tier> affectedTiers = new HashSet<>();

				// for each constraint find the IaaS affected resource and add
				// it to the set. Multiple constraints affecting the same
				// resource are ignored since we use a Set as memory
				for (Constraint c : sol.getApplication(i)
						.getViolatedConstraints())
					affectedTiers.add(findResource(sol.getApplication(i),
							c.getResourceID()));

				// remove resources that have reached the maximum replica
				// constraint
				affectedTiers = constraintHandler
						.filterResourcesForScaleOut(affectedTiers);
				// this is the list of the
				// resouces that doesn't
				// satisfy the constraints
				// now we will scaleout the resources

				if (affectedTiers.size() > 0) {
					MoveOnVM moveVM_i = new MoveOnVM(sol, i);
					for (Tier t : affectedTiers)

						moveVM_i.scaleOut(t, factor);
				}

			}
			// it shouldn't be necessary to set the solution to unEvaluated.

			try {
				evalServer.EvaluateSolution(sol);
			} catch (EvaluationException e) {
				throw new OptimizationException("", "makeFeasible", e);
			}

			numberOfFeasibilityIterations += 1;
		}
		// optimLogger.trace(sol.showStatus());
		if (sol.isFeasible())
			logger.info("Solution made feasible");
		else
			logger.info("Max number of feasibility iterations reached");
	}

	private SolutionMulti considerPrivateCloud(SolutionMulti solutionMulti)
			throws Exception {
		SolutionMulti sol = PrivateCloud.getSolution(solutionMulti);
		if (sol != null) {
			logger.info("A solution using the private cloud has been found!");
			evalServer.EvaluateSolution(sol);
			logger.info(sol.showStatus());
		} else
			logger.info("No solution considering the private cloud!");

		return sol;
	}

	/**
	 * This is the bulk of the optimization process in case of a single provider
	 * problem.
	 * 
	 * @return the integer -1 an error has happened.
	 * @throws OptimizationException
	 */
	public Integer optimize() throws OptimizationException {

		// 1: check if an initial solution has been set
		if (this.initialSolution == null)
			return -1;
		optimLogger.trace("starting the optimization");
		// start the timer
		timer.start();
		try {
			evalServer.EvaluateSolution(initialSolution);
		} catch (EvaluationException e) {
			throw new OptimizationException("", "initialEvaluation", e);
		}// evaluate the current

		// solution
		// initialSolution.showStatus();
		// Debugging constraintHandler

		bestSolution = initialSolution.clone();
		localBestSolution = initialSolution.clone();

		bestSolutionSerieHandler = "Best Solution";
		localBestSolutionSerieHandler = "Local Best Solution";

		timer.split();
		costLogImage.add(localBestSolutionSerieHandler,
				TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
				localBestSolution.getCost());
		costLogImage.add(bestSolutionSerieHandler,
				TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
				bestSolution.getCost());
		logger.warn("" + bestSolution.getCost() + ", 1 " + ", "
				+ bestSolution.isFeasible());
		timer.unsplit();

		numberOfIterations = 1;
		currentSolution = initialSolution.clone(); // the best solution is the

		// evalSvr.EvaluateSolution(currentSolution);

		// this is one possibility, I however prefer using the execution time as
		// a stopping criterion. Mich
		boolean solutionChanged = true;

		if (Configuration.RELAXED_INITIAL_SOLUTION) {
			// make feasible:
			makeFeasible(currentSolution);

			optimLogger.info("Updating best solutions");

			bestSolution = currentSolution.clone();
			localBestSolution = currentSolution.clone();

			// scale in:
			InternalOptimization(currentSolution);
			updateLocalBestSolution(currentSolution, true);
			updateBestSolution(currentSolution, true);
		}

		// if (Configuration.USE_PRIVATE_CLOUD) {
		// // make feasible:
		// makeFeasible(currentSolution);
		//
		// // scale in:
		// InternalOptimization(currentSolution);
		//
		// try {
		// evalServer.EvaluateSolution(currentSolution);
		// currentSolution = considerPrivateCloud(currentSolution);
		// bestSolution = currentSolution.clone();
		// localBestSolution = currentSolution.clone();
		// updateBestSolution(currentSolution, true);
		// updateLocalBestSolution(currentSolution, true);
		//
		// return 0;
		// } catch (Exception e) {
		// logger.error("Error while dealing with the private cloud!", e);
		// return -1;
		// }
		// }

		while (!isMaxNumberOfIterations()) {

			optimLogger.info("Iteration: " + numberOfIterations);
			// optimLogger.trace( currentSolution.showStatus());

			// 2: Internal Optimization process

			if (Configuration.isPaused())
				waitForResume();

			// //////////////////
			if (bestSolutionUpdated && Configuration.REDISTRIBUTE_WORKLOAD) {

				optimLogger
						.info("The best solution did change, so let's redistribuite the workload...");

				currentSolution = bestSolution.clone();

				optimLogger.trace(currentSolution.showWorkloadPercentages());
				setWorkloadPercentagesFromMILP(currentSolution);
				optimLogger.trace("MILP:\n"
						+ currentSolution.showWorkloadPercentages());

				optimLogger.debug(currentSolution.showWorkloadPercentages());
				maximizeWorkloadPercentagesForLeastUsedTier(currentSolution);
				optimLogger.debug(currentSolution.showWorkloadPercentages());

				optimLogger.debug("My method:\n"
						+ currentSolution.showWorkloadPercentages());

				optimLogger.info("Updating best solutions");

				// both sohuld be feasible
				bestSolution = currentSolution.clone();
				localBestSolution = currentSolution.clone();

				bestSolutionUpdated = false;

			}
			// //////////////////

			// 2: Internal Optimization process

			InternalOptimization(currentSolution);

			// 3: check whether the best solution has changed
			// If the current solution is better than the best one it becomes
			// the new best solution.
			optimLogger.info("Updating best solutions");
			updateLocalBestSolution(currentSolution);
			updateBestSolution(currentSolution);

			// 3b: clone the best solution to start the scruble from it
			currentSolution = localBestSolution.clone();
			// ogSolution(bestSolution); // a dire la verit� questo � un po'
			// restrittivo.

			// 4 Scrambling the current solution.
			optimLogger.info("Scramble");
			optimLogger.info("Tabu List Size: " + tabuSolutionList.size());

			if (Configuration.isPaused())
				waitForResume();

			solutionChanged = scramble(currentSolution);
			// if the local optima with respect to the type of machine has been
			// found we need to perform some diversification (using the
			// long-ferm memory)
			if (!solutionChanged) {
				optimLogger
						.info("Stuck in a local optimum, using long term memory");
				currentSolution = longTermMemoryRestart(currentSolution);
				optimLogger.info("Long term memory statistics:");
				for (Tier t : currentSolution.get(0).getApplication(0)
						.getTiers()) {
					optimLogger.info("\tTier: " + t.getPcmName() + " Size: "
							+ longTermFrequencyMemory.get(t.getId()).size());
				}
				localBestSolution = currentSolution.clone();

			}

			setProgress((numberOfIterations * 100 / MAX_SCRUMBLE_ITERS));
			// increment the number of iterations
			numberOfIterations += 1;
			firePropertyChange("iteration", numberOfIterations - 1,
					numberOfIterations);

		}

		// We now consider the private cloud, if we have to
		if (Configuration.USE_PRIVATE_CLOUD) {
			// Debuggin
			// exportSolution();

			try {
				SolutionMulti sol = considerPrivateCloud(bestSolution);
				if (sol != null) {
					currentSolution = sol;
					bestSolution = currentSolution.clone();
					localBestSolution = currentSolution.clone();

					updateBestSolution(currentSolution, true);
					updateLocalBestSolution(currentSolution, true);

					// TODO: fix this solution!
				}
			} catch (Exception e) {
				logger.error("Unable to consider the private cloud", e);
			}
		}

		// dump memory
		scrambleLogger.debug("MemoryDump");
		for (String s : tabuSolutionList.keySet()) {
			scrambleLogger.debug(s);
		}

		try {
			costLogImage.save2png();
			logVm.save2png();
			logConstraints.save2png();
		} catch (IOException e) {
			logger.error("Unable to create charts", e);
		}

		// exportSolution(); TODO: this is always called twice, becaue
		// Space4Cloud calls it at the end of the process! Disabled here.
		evalServer.showStatistics();

		// if (!batch)
		// SolutionWindow.show(bestSolution);

		if (Configuration.CONTRACTOR_TEST)
			bestSolution.generateOptimizedCosts();

		return -1;

	}

	private void waitForResume() throws OptimizationException {
		boolean fired = false;
		while (Configuration.isPaused())
			if (!fired) {
				firePropertyChange("Stopped", false, true);
				fired = true;
			}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new OptimizationException("Error waiting for resume", e);
		}
		firePropertyChange("Stopped", true, false);

	}

	/**
	 * Builds a solution from the less used components in the long term memory
	 * in order to increase diversification
	 * 
	 * @param solMulti
	 *            the corrent solution
	 * @return the changed solution
	 * @throws OptimizationException
	 */
	private SolutionMulti longTermMemoryRestart(SolutionMulti solMulti)
			throws OptimizationException {

		for (Solution s : solMulti.getAll()) {
			Solution sol = longTermMemoryRestart(s);
			solMulti.add(sol);
		}
		return solMulti;
	}

	/**
	 * Builds a solution from the less used components in the long term memory
	 * in order to increase diversification
	 * 
	 * @param solver
	 *            the current solution
	 * @return the changed solution
	 * 
	 * @throws OptimizationException
	 */
	private Solution longTermMemoryRestart(Solution sol)
			throws OptimizationException {

		if (longTermFrequencyMemory == null)
			longTermFrequencyMemory = evalServer.getLongTermFrequencyMemory();
		for (Tier t : sol.getApplication(0).getTiers()) {
			MoveTypeVM moveVM = new MoveTypeVM(sol);
			Cache<String, Integer> tierMemory = longTermFrequencyMemory.get(t
					.getId());
			int lowestValue = Integer.MAX_VALUE;
			String lowestID = null;
			for (String id : tierMemory.keySet()) {
				if (lowestValue > tierMemory.get(id)) {
					lowestValue = tierMemory.get(id);
					lowestID = id;
				}
			}

			CloudService newRes = null;
			List<CloudService> resources = dataHandler.getSameService(
					t.getCloudService(), sol.getRegion());
			String newResType = SolutionHelper
					.getResourceNameFromTierTypeID(lowestID);
			for (CloudService res : resources) {
				if (res.getResourceName().equals(newResType)) {
					newRes = res;
					break;
				}
			}
			if (newRes == null) {
				throw new OptimizationException(
						"longTermMemoryRestart: No resource found!");
			}
			moveVM.changeMachine(t.getId(), newRes);
		}

		try {
			evalServer.EvaluateSolution(sol);
		} catch (EvaluationException e) {
			throw new OptimizationException("", "longTermMemoryRestart", e);
		}
		return sol;

	}

	private SolutionMulti setWorkloadPercentagesFromMILP(
			SolutionMulti currentSolution) {
		MILPEvaluator reval = new MILPEvaluator();

		SolutionMulti tempSolution = currentSolution.clone();

		Path p = null;
		try {
			p = Files.createTempFile("sol", ".xml");
		} catch (IOException e) {
			logger.error("Error while creating a temporary file.", e);
			p = Paths.get(Configuration.PROJECT_BASE_FOLDER,
					Configuration.WORKING_DIRECTORY, "soltmp.xml");
		}
		currentSolution.exportLight(p);

		reval.setStartingSolution(p.toFile());
		try {
			reval.eval();

			currentSolution.setFrom(null, reval.getMultiCloudExt());
		} catch (Exception e) {
			logger.error("Error while using the MILP tool.", e);

			currentSolution = tempSolution.clone();
		}

		return currentSolution;
	}

	private SolutionMulti maximizeWorkloadPercentagesForLeastUsedTier(
			SolutionMulti currentSolution) throws OptimizationException {
		double wpMin = 0.0; // the minimum workload balance constraint
		List<Constraint> constraints = constraintHandler
				.getConstraintByResourceId(Configuration.APPLICATION_ID);
		for (Constraint c : constraints)
			if (c instanceof WorkloadPercentageConstraint)
				wpMin = ((WorkloadPercentageConstraint) c).getMin();

		if (wpMin > 1)
			wpMin /= 100;

		// the increments
		double[] increments = new double[24];
		// the ending conditions
		boolean[] keepGoing = new boolean[24];
		// the or of the previous conditions
		boolean stillWork = true;
		// a single solution memory to rollback
		SolutionMulti lastSolution;

		// initialize variables
		for (int i = 0; i < 24; i++) {
			increments[i] = 0.1;
			keepGoing[i] = true;
		}

		do {
			// ave the current solution in the memory (should always be
			// feasible)
			lastSolution = currentSolution.clone();

			for (int hour = 0; hour < 24; hour++) {
				if (!keepGoing[hour])
					continue;

				// retrieve the least used provider
				Map<Solution, Tier> mostUsedTiers = new HashMap<Solution, Tier>(
						currentSolution.getAll().size());
				// retrieve the most used tier for all the solutions
				for (Solution sol : currentSolution.getAll()) {
					for (Tier t : sol.getApplication(hour).getTiers()) {
						// base case
						if (!mostUsedTiers.containsKey(sol)) {
							mostUsedTiers.put(sol, t);
						} else {
							if (t.getUtilization() > mostUsedTiers.get(sol)
									.getUtilization()) {
								mostUsedTiers.put(sol, t);
							}
						}
					}
				}

				// the least used provider (According to the maximum used tiers
				// only)
				Solution leastUsedProvider = null;

				// get the less used provider (looking only at the most used
				// tier)
				for (Solution sol : mostUsedTiers.keySet()) {
					// base case
					if (leastUsedProvider == null) {
						leastUsedProvider = sol;
					} else {
						if (mostUsedTiers.get(sol).getUtilization() < mostUsedTiers
								.get(leastUsedProvider).getUtilization()) {
							leastUsedProvider = sol;
						}
					}
				}

				// calculate the increment of workload for the minimum used
				// provider
				double wpStar = leastUsedProvider.getPercentageWorkload(hour);
				double diff = 1 - wpStar;
				double rate = increments[hour];
				// keep the remainder of the difference to split it on other
				// proividers if needed
				double remainder = 0.0;
				if (diff < rate) {
					rate = diff;
					if (increments[hour] == 0.1)
						increments[hour] = 0.05;
					else
						keepGoing[hour] = false;
				}

				changeWorkload(leastUsedProvider, hour, wpStar + rate);
				// update the workload percentage with the new value (re-read in
				// order to avoid wrong rounding should be wpStar+rate)
				wpStar = leastUsedProvider.getPercentageWorkload(hour);

				// fix the workload of other providers by removing the one used
				// for the increment and splitting the remainder (if any)
				for (Solution sol : currentSolution.getAll()) {
					if (!sol.equals(leastUsedProvider)) {
						double wp = sol.getPercentageWorkload(hour);
						// avoid breaking theminimum workload constraint, in
						// case it is violated then fix it
						if (wp > 0 && wp < wpMin) {
							remainder += wpMin - wp + rate;
							changeWorkload(sol, hour, wpMin);
						} else {
							// double wpMin = 0.0;
							diff = wp - wpMin;
							double dec = rate / (currentSolution.size() - 1)
									+ remainder;

							if (diff < dec) {
								remainder = dec - diff;
								dec = diff;
							}

							// System.out.println(currentSolution.showWorkloadPercentages());
							changeWorkload(sol, hour, wp - dec);
							// System.out.println(currentSolution.showWorkloadPercentages());
						}
					}
				}

				if (remainder != 0.0) {
					// System.out.println(currentSolution.showWorkloadPercentages());
					changeWorkload(leastUsedProvider, hour, wpStar - remainder);
					// System.out.println(currentSolution.showWorkloadPercentages());

					if (increments[hour] == 0.1)
						increments[hour] = 0.05;
					else
						keepGoing[hour] = false;
				}

			}

			// evaluate the solution only after all the moves have been
			// performed
			try {
				evalServer.EvaluateSolution(currentSolution);
			} catch (EvaluationException e) {
				throw new OptimizationException("", "workloadDistribution", e);
			}

			// a big or of all the ending conditions to check if some hour still
			// has some balancing work to do
			stillWork = false;
			for (int i = 0; i < 24; i++) {
				stillWork = stillWork || keepGoing[i];
			}

		} while (currentSolution.isFeasible() && stillWork);

		if (!currentSolution.isFeasible())
			currentSolution = lastSolution.clone();

		return currentSolution;
	}

	protected void resetNoImprovementCounter() {

		this.numIterNoImprov = 0;
	}

	protected boolean scramble(Solution sol, Tier selectedTier, int iterations)
			throws OptimizationException {
		MoveTypeVM moveVM = new MoveTypeVM(sol);

		scrambleLogger.debug("Selected Tier:" + selectedTier.getPcmName());
		// Phase2: Retrieve resources that can be exchanged with the current
		// one
		CloudService origRes = selectedTier.getCloudService();
		List<CloudService> resList = dataHandler.getSameService(origRes,
				sol.getRegion());
		// filter resources according to architectural constraints

		try {
			constraintHandler.filterResources(resList, selectedTier);
		} catch (ConstraintEvaluationException e) {
			throw new OptimizationException(
					"Error filtering resources for scramble", e);
		}

		// if no resource can substitute the current one increase the number
		// of iterations without a change and try again
		if (resList.size() == 0) {
			scrambleLogger
					.warn("No resource found for scramble after constraint check, iteration: "
							+ iterations);
			return false;
		}

		resList = filterByMemory(resList, sol, selectedTier);
		// if no resource can substitute the current one increase the number
		// of iterations without a change and try again
		if (resList.size() == 0) {
			scrambleLogger.debug("MemoryDump");
			for (String s : tabuSolutionList.keySet()) {
				scrambleLogger.debug(s);
			}
			scrambleLogger
					.warn("No resource found for scramble after memory check, iteration: "
							+ iterations);
			return false;
		}

		// Phase3: choose the new resource. Policy: roulette selection
		// 3.1Calculate the cumulative fitness
		double[] cumulativeFitnesses = new double[resList.size()];
		cumulativeFitnesses[0] = getEfficiency(resList.get(0), sol.getRegion());
		for (int i = 1; i < resList.size(); i++) {
			double fitness = getEfficiency(resList.get(i), sol.getRegion());
			cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + fitness;
		}
		// get a random number weighted by the highest cumulative fitness
		// value
		double randomFitness = random.nextDouble()
				* cumulativeFitnesses[cumulativeFitnesses.length - 1];
		// search for the value
		int index = Arrays.binarySearch(cumulativeFitnesses, randomFitness);
		if (index < 0) {
			// Convert negative insertion point to array index.
			index = Math.abs(index + 1);
		}

		// get the resource.
		CloudService newRes = resList.get(index);

		// debugging
		// scrambleLogger.debug("Sorted Resources");
		// for (IaaS res : resList) {
		// if (newRes.equals(res))
		// scrambleLogger.debug("Index: " + resList.indexOf(res)
		// + " res: " + res.getResourceName() + " value: "
		// + getEfficiency(res, sol.getRegion())
		// + " <--- SELECTED");
		// else
		// scrambleLogger.debug("Index: " + resList.indexOf(res)
		// + " res:" + res.getResourceName() + " value: "
		// + getEfficiency(res, sol.getRegion()));
		// }
		// scrambleLogger.debug("Selected index:" + resList.indexOf(newRes));

		// Phase4: performs the change and evaluate the solution
		// add the new configuration in the memory
		moveVM.changeMachine(selectedTier.getId(), newRes);
		try {
			evalServer.EvaluateSolution(sol);
		} catch (EvaluationException e) {
			throw new OptimizationException("", "scramble", e);
		}

		tabuSolutionList.put(SolutionHelper.buildSolutionTypeID(sol), null);
		scrambleLogger.debug("Memory size " + tabuSolutionList.size());
		return true;
	}

	/**
	 * Changes the type of virtual machines used. This change is more
	 * distruptive than acting on the number of virtual machines and the optimal
	 * type of VM is harder to find This move does not preserve the feasibility
	 * of the solution. If the return value is false it is very likely that the
	 * solutions near the local optima has been evaluated, some difersification
	 * is needed.
	 * 
	 * @param the
	 *            current solution
	 * @return returns true if the scramble has changed the solution, false if
	 *         the maximum number of scramble iterations has been reached
	 * @throws OptimizationException
	 * @throws ConstraintEvaluationException
	 */
	protected boolean scramble(Solution sol) throws OptimizationException {
		/**
		 * 
		 * 1) Choose the candidate tier for the VM change (possible strategies:
		 * RANDOM, cost) 2) Retreive all the VM types that can substitute the
		 * current vm (Same provider/ region / service type and fulfilling
		 * architectural constraints) 3) Choose the new type of VM to use.
		 * (possible strategies: RANDOM, efficiency=cores*processingrate/cost,
		 * roulette selection The proposed strategy uses a random variable whose
		 * probability distribution depends on the efficiency of the machines so
		 * to prefer machines that have a higher core*processingrate/cost 4)
		 * Change the machine and evaluate the new solution.
		 */

		// Phase0: initialization
		// MoveTypeVM moveVM = new MoveTypeVM(sol);
		boolean done = false; // tells if something has changed
		int iterations = 0; // number of iterations without a change in the
		// solution
		scrambleLogger.debug("iteration: " + numberOfIterations);

		// try to performe the change until something has actually changed or we
		// give up
		while (!done && iterations < MAX_SCRAMBLE_NO_CHANGE) {

			// Phase1: Select the resource to change. Policy: RANDOM

			List<Tier> tierList = sol.getApplication(0).getTiers();
			Tier selectedTier = tierList.get(random.nextInt(tierList.size()));
			// scrambleLogger.debug("Selected Tier:" + selectedTier.getName());
			// // Phase2: Retrieve resources that can be exchanged with the
			// current
			// // one
			// CloudService origRes = selectedTier.getCloudService();
			// List<CloudService> resList = dataHandler.getSameService(origRes,
			// sol.getRegion());
			// // filter resources according to architectural constraints
			//
			// try {
			// constraintHandler.filterResources(resList, selectedTier);
			// } catch (ConstraintEvaluationException e) {
			// throw new
			// OptimizationException("Error filtering resources for scramble",e);
			// }
			//
			// // if no resource can substitute the current one increase the
			// number
			// // of iterations without a change and try again
			// if (resList.size() == 0) {
			// scrambleLogger
			// .warn("No resource found for scramble after constraint check, iteration: "
			// + iterations);
			// iterations++;
			// continue;
			// }
			//
			// resList = filterByMemory(resList, sol, selectedTier);
			// // if no resource can substitute the current one increase the
			// number
			// // of iterations without a change and try again
			// if (resList.size() == 0) {
			// scrambleLogger.debug("MemoryDump");
			// for (String s : tabuSolutionList.keySet()) {
			// scrambleLogger.debug(s);
			// }
			// scrambleLogger
			// .warn("No resource found for scramble after memory check, iteration: "
			// + iterations);
			// iterations++;
			// continue;
			// }
			//
			// // Phase3: choose the new resource. Policy: roulette selection
			// // 3.1Calculate the cumulative fitness
			// double[] cumulativeFitnesses = new double[resList.size()];
			// cumulativeFitnesses[0] = getEfficiency(resList.get(0),
			// sol.getRegion());
			// for (int i = 1; i < resList.size(); i++) {
			// double fitness = getEfficiency(resList.get(i), sol.getRegion());
			// cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + fitness;
			// }
			// // get a random number weighted by the highest cumulative fitness
			// // value
			// double randomFitness = random.nextDouble()
			// * cumulativeFitnesses[cumulativeFitnesses.length - 1];
			// // search for the value
			// int index = Arrays.binarySearch(cumulativeFitnesses,
			// randomFitness);
			// if (index < 0) {
			// // Convert negative insertion point to array index.
			// index = Math.abs(index + 1);
			// }
			//
			// // get the resource.
			// CloudService newRes = resList.get(index);
			//
			// // debugging
			// // scrambleLogger.debug("Sorted Resources");
			// // for (IaaS res : resList) {
			// // if (newRes.equals(res))
			// // scrambleLogger.debug("Index: " + resList.indexOf(res)
			// // + " res: " + res.getResourceName() + " value: "
			// // + getEfficiency(res, sol.getRegion())
			// // + " <--- SELECTED");
			// // else
			// // scrambleLogger.debug("Index: " + resList.indexOf(res)
			// // + " res:" + res.getResourceName() + " value: "
			// // + getEfficiency(res, sol.getRegion()));
			// // }
			// // scrambleLogger.debug("Selected index:" +
			// resList.indexOf(newRes));
			//
			//
			// // Phase4: performs the change and evaluate the solution
			// // add the new configuration in the memory
			// moveVM.changeMachine(selectedTier.getId(), newRes);
			// try {
			// evalServer.EvaluateSolution(sol);
			// } catch (EvaluationException e) {
			// throw new OptimizationException("","scramble",e);
			// }
			//
			// tabuSolutionList.put(SolutionHelper.buildSolutionTypeID(sol),
			// null);
			// scrambleLogger.debug("Memory size " + tabuSolutionList.size());
			// done = true;

			done = scramble(sol, selectedTier, iterations);
			iterations++;
		}

		if (iterations >= MAX_SCRAMBLE_NO_CHANGE)
			return false;

		return true;
	}

	/**
	 * Calculates the efficiency of the reousce. The efficiency is calculated by
	 * numberOfCores*speed/averageCost
	 * 
	 * @param resource
	 * @param region
	 * @return
	 */
	private double getEfficiency(CloudService resource, String region) {
		if (Configuration.BENCHMARK != Configuration.Benchmark.None) {
			return dataHandler.getBenchmarkValue(resource,
					Configuration.BENCHMARK.toString());
		}

		Compute computeRes = null;
		if (resource instanceof Compute)
			computeRes = (Compute) resource;
		else if (resource instanceof Database)
			computeRes = ((Database) resource).getCompute();
		else if (resource instanceof Platform)
			computeRes = ((Platform) resource).getCompute();
		else if (resource instanceof it.polimi.modaclouds.space4cloud.optimization.solution.impl.Cache)
			computeRes = ((it.polimi.modaclouds.space4cloud.optimization.solution.impl.Cache) resource)
					.getCompute();
		else if (resource instanceof Queue)
			computeRes = ((Queue) resource).getCompute();

		if (computeRes != null) {
			double cost = evalServer.getCostEvaulator().getResourceAverageCost(
					computeRes, region);
			double efficiency = computeRes.getNumberOfCores()
					* computeRes.getSpeed() / cost;
			return efficiency;
		} else {
			logger.error("Trying to sort non Compute resources");
			return -1;
		}
	}

	/**
	 * Filters the resourceList removing resources that would cause the scramble
	 * process to obtain a configuration that has been already been evaluated
	 * (within the memory size limit) The filtering process is performed by
	 * building a solution type ID which is defined by looking at all tiers and
	 * all type of services that are selected.
	 * 
	 * @param resourceList
	 *            the resource list to be filtered
	 * @param sol
	 *            the solution used to build the id
	 * @param selectedTier
	 *            the selected tier
	 * @return the list of resources filtered by those resources that has
	 *         already been evaluated.
	 */
	private List<CloudService> filterByMemory(List<CloudService> resourceList,
			Solution sol, Tier selectedTier) {
		// Filter resources that have been already used

		final String token = "jfhbvwiuahj038h9nvlbv93vbie";
		String solutionTypeID = SolutionHelper.buildSolutionTypeID(sol,
				selectedTier, token);
		List<CloudService> newList = new ArrayList<CloudService>();
		for (CloudService resource : resourceList) {
			// Complete the id generated before
			String newSolID = solutionTypeID.replace(token,
					resource.getResourceName());
			if (!tabuSolutionList.containsKey(newSolID)) {
				newList.add(resource);
			} else {
				logger.debug("Memory Hit");
			}
		}
		return newList;

	}

	protected boolean scramble(SolutionMulti sol) throws OptimizationException {

		boolean change = false;
		for (Solution s : sol.getAll())
			change = change || scramble(s);
		// TODO: check effecto of this on the diversification
		return change;
	}

	public void SerializeInitialSolution(File file) {
		logger.info("Serializing: " + initialSolution);
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(file);
			out = new ObjectOutputStream(fos);
			out.writeObject(initialSolution);
			out.close();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
		}

		initialSolution = null;

	}

	/**
	 * Sets the initial solution.
	 * 
	 * @param initialSolution
	 *            the initial solution
	 */
	public void setInitialSolution(SolutionMulti initialSolution) {
		this.initialSolution = initialSolution;
	}

	/**
	 * Indicates if the bestSolution did change or not.
	 */
	private boolean bestSolutionUpdated = true;

	/**
	 * Checks wether the solution is "better" with respect to the current
	 * optimal solution. Better here means that the solution is Evaluated,
	 * Feasible and cost less
	 * 
	 * @param the
	 *            new solution
	 * @return true if sol has become the new best solution
	 */
	protected boolean checkBestSolution(Solution sol, boolean force) {
		if (force || sol.greaterThan(bestSolution)) {
			Solution clone = sol.clone();
			bestSolution.add(clone);
			timer.split();
			clone.setGenerationIteration(numberOfIterations);
			clone.setGenerationTime(timer.getSplitTime());
			bestSolution.setGenerationIteration(numberOfIterations);
			bestSolution.setGenerationTime(timer.getSplitTime());
			timer.unsplit();

			if (bestSolutions != null) {
				bestSolutions.add(bestSolution.clone());
				firePropertyChange(BestSolutionExplorer.PROPERTY_ADDED_VALUE,
						false, true);
				// bse.propertyChange(new PropertyChangeEvent(this,
				// "BSEAddedValue", false, true));
			}

			optimLogger.info("updated best solution");
			return true;
		}
		return false;
	}

	protected boolean updateBestSolution(Solution sol) {
		return updateBestSolution(sol, false);
	}

	protected boolean updateBestSolution(Solution sol, boolean force) {
		boolean result = checkBestSolution(sol, force);
		if (result)
			updateCostLogImage(bestSolution, bestSolutionSerieHandler);
		return result;
	}

	protected void updateBestSolution(SolutionMulti sol) {
		updateBestSolution(sol, false);
	}

	protected void updateBestSolution(SolutionMulti sol, boolean force) {
		boolean updated = false;
		for (Solution s : sol.getAll())
			updated = updated || checkBestSolution(s, force);
		if (updated)
			updateCostLogImage(bestSolution, bestSolutionSerieHandler);
	}

	private void updateCostLogImage(SolutionMulti solution, String seriesHandler) {
		logger.warn("" + solution.getCost() + ", "
				+ TimeUnit.MILLISECONDS.toSeconds(solution.getGenerationTime())
				+ ", " + solution.isFeasible());
		costLogImage.add(seriesHandler,
				TimeUnit.MILLISECONDS.toSeconds(solution.getGenerationTime()),
				solution.getCost());
	}

	protected boolean checkLocalBestSolution(Solution sol, boolean force) {
		if (force || sol.greaterThan(localBestSolution)) {

			// updating the best solution
			// bestSolution = sol.clone();
			// String filename = bestTmpSol + bestTmpSolIndex+".xml";
			// sol.exportLight(filename);
			localBestSolution.add(sol.clone());
			this.numIterNoImprov = 0;
			timer.split();
			localBestSolution.setGenerationIteration(numberOfIterations);
			localBestSolution.setGenerationTime(timer.getSplitTime());
			timer.unsplit();
			optimLogger.info("updated local best solution");
			return true;
		}
		return false;
	}

	protected boolean updateLocalBestSolution(Solution sol, boolean force) {
		boolean result = checkLocalBestSolution(sol, force);
		if (result)
			updateCostLogImage(localBestSolution, localBestSolutionSerieHandler);
		return result;
	}

	protected boolean updateLocalBestSolution(Solution sol) {
		return updateLocalBestSolution(sol, false);
	}

	protected void updateLocalBestSolution(SolutionMulti sol) {
		updateLocalBestSolution(sol, false);
	}

	protected void updateLocalBestSolution(SolutionMulti sol, boolean force) {
		boolean updated = false;
		for (Solution s : sol.getAll())
			updated = updated || checkLocalBestSolution(s, force);
		if (updated)
			updateCostLogImage(localBestSolution, localBestSolutionSerieHandler);
	}

	protected void loadConfiguration() {
		MAXFEASIBILITYITERATIONS = Configuration.FEASIBILITY_ITERS;
		MAX_SCRUMBLE_ITERS = Configuration.SCRUMBLE_ITERS;
		MAXMEMORYSIZE = Configuration.TABU_MEMORY_SIZE;
		SELECTION_POLICY = Configuration.SELECTION_POLICY;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource().equals(evalServer)
				&& evt.getPropertyName().equals("EvaluationError")) {
			logger.error("Optimizaiton ending due to evaluation error");
			cancel(true);
		} else if (evt.getPropertyName().equals(
				BestSolutionExplorer.PROPERTY_WINDOW_CLOSED)) {
			firePropertyChange(BestSolutionExplorer.PROPERTY_WINDOW_CLOSED,
					false, true);
		}
	}

	public void exportSolution() {
		if (bestSolution == null) {
			logger.warn("The solution has not been created yet! Trying to export the initial solution...");
			bestSolution = initialSolution;
		}

		if (duration != null) {
			bestSolution.setGenerationTime(duration.getTime());
		}

		logger.info(bestSolution.showStatus());

		String basePath = Paths.get(Configuration.PROJECT_BASE_FOLDER,
				Configuration.WORKING_DIRECTORY).toString();

		bestSolution.exportLight(Paths.get(basePath,
				Configuration.SOLUTION_LIGHT_FILE_NAME
						+ Configuration.SOLUTION_FILE_EXTENSION));

		for (Solution sol : bestSolution.getAll())
			sol.exportAsExtension(Paths.get(Configuration.PROJECT_BASE_FOLDER,
					Configuration.WORKING_DIRECTORY,
					Configuration.SOLUTION_FILE_NAME + sol.getProvider()
							+ Configuration.SOLUTION_FILE_EXTENSION));

		bestSolution.exportAsExtension(Paths.get(basePath,
				Configuration.SOLUTION_FILE_NAME + "Total"
						+ Configuration.SOLUTION_FILE_EXTENSION));

		bestSolution.exportCSV(Paths.get(basePath,
				Configuration.SOLUTION_CSV_FILE_NAME));

		bestSolution.exportCostsAsExtension(Paths.get(basePath, "costs"
				+ Configuration.SOLUTION_FILE_EXTENSION));

		for (Solution s : bestSolution.getAll())
			s.exportPerformancesAsExtension(Paths.get(basePath, "performance"
					+ s.getProvider() + Configuration.SOLUTION_FILE_EXTENSION));

		if (Configuration.GENERATE_DESIGN_TO_RUNTIME_FILES) {
			logger.info("Calling the DesignToRuntimeConnector project...");
			AdaptationModelBuilder amb = new AdaptationModelBuilder(Paths.get(
					Configuration.DB_CONNECTION_FILE).toString());
			for (Solution s : bestSolution.getAll())
				amb.createAdaptationModelAndRules(
						basePath,
						Paths.get(
								basePath,
								Configuration.SOLUTION_FILE_NAME
										+ s.getProvider()
										+ Configuration.SOLUTION_FILE_EXTENSION)
								.toString(),
						Paths.get(Configuration.FUNCTIONALITY_TO_TIER_FILE)
								.toString(),
						Paths.get(
								basePath,
								"performance" + s.getProvider()
										+ Configuration.SOLUTION_FILE_EXTENSION)
								.toString(),
						Configuration.OPTIMIZATION_WINDOW_LENGTH,
						Configuration.TIMESTEP_DURATION, s.getProvider());
		}
	}

	private StopWatch duration = null;

	public void setDuration(StopWatch duration) {
		this.duration = duration;
	}

	public void inspect() {
		if (bestSolutions != null && bestSolutions.size() > 0)
			BestSolutionExplorer.show(this);
		/*
		 * if(bestSolution != null) SolutionWindow.show(bestSolution);
		 */
	}

	public synchronized List<SolutionMulti> getBestSolutions() {
		return bestSolutions;
	}
}
