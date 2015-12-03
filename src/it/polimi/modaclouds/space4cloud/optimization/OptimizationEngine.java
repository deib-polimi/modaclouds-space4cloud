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

import it.polimi.modaclouds.adaptationDesignTime4Cloud.AdaptationModelBuilder;
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
import it.polimi.modaclouds.space4cloud.optimization.constraints.UsageConstraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.WorkloadPercentageConstraint;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.CostEvaluationException;
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
import it.polimi.modaclouds.space4cloud.utils.Rounder;
import it.polimi.modaclouds.space4cloud.utils.SolutionHelper;
import it.polimi.modaclouds.space4cloud.utils.UsageModelExtensionParser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.uka.ipd.sdq.pcm.allocation.AllocationContext;
import de.uka.ipd.sdq.pcm.core.entity.Entity;
import de.uka.ipd.sdq.pcm.repository.BasicComponent;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.RepositoryComponent;
import de.uka.ipd.sdq.pcm.resourceenvironment.ResourceContainer;
import de.uka.ipd.sdq.pcm.seff.AbstractAction;
import de.uka.ipd.sdq.pcm.seff.ExternalCallAction;
import de.uka.ipd.sdq.pcm.seff.InternalAction;
import de.uka.ipd.sdq.pcm.seff.ResourceDemandingSEFF;
import de.uka.ipd.sdq.pcm.seff.ServiceEffectSpecification;
import de.uka.ipd.sdq.pcm.seff.StartAction;
import de.uka.ipd.sdq.pcm.usagemodel.AbstractUserAction;
import de.uka.ipd.sdq.pcm.usagemodel.Branch;
import de.uka.ipd.sdq.pcm.usagemodel.BranchTransition;
import de.uka.ipd.sdq.pcm.usagemodel.ClosedWorkload;
import de.uka.ipd.sdq.pcm.usagemodel.EntryLevelSystemCall;
import de.uka.ipd.sdq.pcm.usagemodel.Loop;
import de.uka.ipd.sdq.pcm.usagemodel.ScenarioBehaviour;
import de.uka.ipd.sdq.pcm.usagemodel.UsageScenario;
import de.uka.ipd.sdq.pcmsolver.models.PCMInstance;

/**
 * @author Michele Ciavotta Class defining the optimization engine.
 */
public class OptimizationEngine extends SwingWorker<Void, Void> implements
		PropertyChangeListener {

	private SolutionMulti initialSolution = null;

	private SolutionMulti bestSolution = null;

	private SolutionMulti currentSolution = null;

	private SolutionMulti localBestSolution = null;

	private List<SolutionMulti> bestSolutions = new ArrayList<SolutionMulti>();

	private ConstraintHandler constraintHandler;

	private DataHandler dataHandler;

	private int scrambleIteration;

	private int numberOfFeasibilityIterations;

	private Policy SELECTION_POLICY;

	private int MAXMEMORYSIZE = 10;

	private static final int MAX_SCRAMBLE_NO_CHANGE = 10;

	private int MAX_SCRUMBLE_ITERS = 20;

	private int MAXFEASIBILITYITERATIONS = 10;

	private double DEFAULT_SCALE_IN_FACTOR = 2;
	private int MAX_OUT_OF_BOUND_ITERATIONS = 5;
	private int MAX_SCALE_IN_CONV_ITERATIONS = 3;

	public static final String BEST_SOLUTION_UPDATED = "bestSolutionUpdated";

	private static final double WL_INCREMENT = 0.01;

	/**
	 * Tabu list containing the representation of the solutions recently
	 * evaluated, used for the tabu search (scramble phase).
	 */
	private Cache<String, String> tabuSolutionList;

	/**
	 * This is the long term memory of the tabu search used in the scramble
	 * process. Each Tier (key of the Map) has its own memory which uses the
	 * resource name as ID. This memory is used to restart the search process
	 * after the full exploration of a local optimum by building a solution out
	 * of components with low frequency
	 */
	private Map<String, Cache<String, Integer>> longTermFrequencyMemory;

	private Random random;

	private StopWatch timer = new StopWatch();

	private EvaluationServer evalServer;

	private Logger logger = LoggerFactory.getLogger(OptimizationEngine.class);

	private GenericChart<XYSeriesCollection> costLogImage;
	private GenericChart<XYSeriesCollection> logVm;
	private GenericChart<XYSeriesCollection> logConstraints;

	private String bestSolutionSerieHandler;
	private String localBestSolutionSerieHandler;

	private boolean batch = false;

	private boolean providedtimer = false;

	/**
	 * Instantiates a new opt engine using as timer the provided one. the
	 * provided timer should already be started
	 * 
	 * @param handler
	 *            : the constraint handler
	 * @throws DatabaseConnectionFailureExteption
	 */
	public OptimizationEngine(ConstraintHandler handler, boolean batch,
			StopWatch timer) throws DatabaseConnectionFailureExteption {
		this.timer = timer;
		providedtimer = true;
		init(handler, batch);

	}

	private void init(ConstraintHandler handler, boolean batch)
			throws DatabaseConnectionFailureExteption {
		loadConfiguration();

		try {
			costLogImage = GenericChart.createCostLogger();
			logVm = GenericChart.createVmLogger();
			logConstraints = GenericChart.createConstraintsLogger();
		} catch (NumberFormatException | IOException e) {
			logger.error("Unable to create chart loggers", e);
		}

		logger.debug("Random seed: " + Configuration.RANDOM_SEED);
		random = new Random(Configuration.RANDOM_SEED);

		// batch mode

		showConfiguration();

		tabuSolutionList = new Cache<>(MAXMEMORYSIZE);
		constraintHandler = handler;

		/* this handle manage the data loaded from the database */
		dataHandler = DataHandlerFactory.getHandler();

		/* this object is a server needed to evaluate the solutions */
		evalServer = new EvaluationProxy();
		evalServer.setTimer(timer);
		evalServer.setConstraintHandler(handler);
		evalServer.setLog2png(costLogImage);
		evalServer.setMachineLog(logVm);
		evalServer.setConstraintLog(logConstraints);
		evalServer.addPropertyChangeListener(this);

		// this.evalProxy.setEnabled(false);

		this.batch = batch;
	}

	private void showConfiguration() {
		logger.info("Running the optimization with parameters:");
		logger.info("Max Memory Size: " + MAXMEMORYSIZE);
		logger.info("Max Scrumble Iterations: " + MAX_SCRUMBLE_ITERS);
		logger.info("Max Feasibility Iterations: " + MAXFEASIBILITYITERATIONS);
		logger.info("Selection Policy: " + SELECTION_POLICY);
		logger.info("Default Scale in Factor: " + DEFAULT_SCALE_IN_FACTOR);
		logger.info("Max Scale In Out of Bound Iterations: "
				+ MAX_OUT_OF_BOUND_ITERATIONS);
		logger.info("Max Scale In Convergence Iterations: "
				+ MAX_SCALE_IN_CONV_ITERATIONS);
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
	private void changeWorkload(Solution sol, double[] rates) {

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
	private void changeWorkload(Solution sol, int hour, double rate) {

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
		else
			firePropertyChange("finished", false, true);
		return null;
	}

	private Tier findResource(Instance application, String id) {
		return findResource(application, id, SELECTION_POLICY);
	}

	/**
	 * Find the tier to scale given the id of the constraint
	 * 
	 * @param id
	 *            of the resource in the constraint
	 * @return a IaaS resource on which to perform a scale operation.
	 */
	private Tier findResource(Instance application, String id, Policy policy) {

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
				selectedFun = functionalityChain.get(random
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

	private void findSeffsInScenarioBehavior(
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
	private MoveOnVM[] generateArrayMoveOnVM(Solution sol) {
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
	private List<ArrayList<Tier>> generateVettResTot(Solution sol) {
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
	private void InternalOptimization(SolutionMulti solution, String provider)
			throws OptimizationException {

		// if the solution is unfeasible there is a first phase in which the
		// solution is forced to became feasible
		logger.debug("Executing: Make Feasible");
		makeFeasible(solution, provider);
		// logger.trace("feasible solution: "+sol.showStatus());
		// If the current solution is better than the best one it becomes the
		// new best solution.

		updateBestSolution(solution);
		updateLocalBestSolution(solution);

		logger.info("Executing Scale In");
		if (solution.isFeasible()) {
			ScaleLS(solution, provider);
		} else {
			logger.info("Solution not feasible, skipping scale in");
		}
		// logger.trace("optimized solution"+sol.showStatus());

	}

	private void InternalOptimization(SolutionMulti sol)
			throws OptimizationException {

		for (Solution s : sol.getAll())
			InternalOptimization(sol, s.getProvider());
	}

	private boolean isMaxNumberOfFesibilityIterations() {

		if (numberOfFeasibilityIterations <= MAXFEASIBILITYITERATIONS) {
			return false;
		}
		return true;
	}

	private boolean isMaxNumberOfIterations() {

		if (scrambleIteration <= MAX_SCRUMBLE_ITERS) {
			return false;
		}
		return true;
	}

	private void ScaleLS(SolutionMulti solution, String provider)
			throws OptimizationException {

		// Select the provider we are working on
		Solution sol = solution.get(provider);

		for (Constraint constraint : sol.getViolatedConstraints()) {
			if (constraint instanceof RamConstraint) {
				logger.info("Wrong type of VM selected, scale in will not be executed");
				return;
			}
		}
		logger.info("initializing scale in phase");

		// initialize the factors (one for each hour) to the default value
		// we should use a factor for each hour and for each tier but if the
		// number
		// of tiers is significantly smaller than the number of iterations then
		// it
		// is likely that each tier will be scaled using just 1 factor for each
		// hour
		double[] scaleInFactors = new double[24];
		int[] unfeasibleCounters = new int[24];
		for (int i = 0; i < 24; i++) {
			scaleInFactors[i] = DEFAULT_SCALE_IN_FACTOR;
			unfeasibleCounters[i] = 0;
		}
		// This counter measure how much iterations we spend far from the
		// optimal solution, if this grows then probably we want to stop this
		// process since it is not likely to generate a solution better than the
		// optimal one
		int outOfBoundIterations = 0;

		// This counter measure how many scale in operations we try to perform
		// but we get an unfeasible solution. If this grows then probably we are
		// very close to the optimal number of machines
		int numIterNoImprov = 0;

		double boundFactor = 2.0;
		int iteration = 0;

		int numberOfTiers = sol.getHourApplication().get(0).getTiers().size();

		while (numIterNoImprov < MAX_SCALE_IN_CONV_ITERATIONS * numberOfTiers
				&& outOfBoundIterations < MAX_OUT_OF_BOUND_ITERATIONS) {

			iteration++;
			// Log some statistics
			String scalingFactors = "";
			for (int i = 0; i < 24; i++)
				scalingFactors += " h: " + i + " val: " + scaleInFactors[i];
			logger.debug("Scale In iteration: " + iteration
					+ " Out of bound iterations: " + outOfBoundIterations
					+ " No improvement iterations: " + numIterNoImprov
					+ " local optimal cost: " + localBestSolution.getCost()
					+ " Bound Factor: " + boundFactor + " Bound: "
					+ boundFactor * localBestSolution.getCost());
			logger.debug("Scaling factors: " + scalingFactors);

			// Select the tiers to scale and apply the scaling action
			boolean noScaleIn = true;
			Solution previousSol = sol.clone();
			List<ArrayList<Tier>> vettResTot = generateVettResTot(sol);
			MoveOnVM[] moveArray = generateArrayMoveOnVM(sol);
			// scale in each hour
			for (int hour = 0; hour < 24; hour++) {
				Tier tier = null;
				// remove resources with minimum number of replicas
				vettResTot = constraintHandler.filterResourcesForScaleDown(
						vettResTot, hour);

				// if no resource can be scaled in in this hour then jump to the
				// next one
				if (vettResTot.get(hour).size() == 0)
					continue;

				// if there are tiers that can be scaled then chose one
				// randomly
				// TODO: better policy then random?
				tier = vettResTot.get(hour).get(
						random.nextInt(vettResTot.get(hour).size()));

				// scale the resource by the factor
				moveArray[hour].scaleIn(tier, scaleInFactors[hour]);
				noScaleIn = false;
			}

			// evaluate the solution
			try {
				evalServer.EvaluateSolution(solution);
			} catch (EvaluationException e) {
				throw new OptimizationException("", "scaleIn", e);
			}

			// if an application has become infeasible, revert it and try again
			// with a smaller factor
			for (int i = 0; i < 24; i++) {
				if (!sol.getApplication(i).isFeasible()) {
					sol.copyApplication(previousSol.getApplication(i), i);
					unfeasibleCounters[i]++;
					scaleInFactors[i] = 1 + (DEFAULT_SCALE_IN_FACTOR - 1)
							/ (double) unfeasibleCounters[i];
				}
			}

			// re-evaluate the solution (every solution here should already be
			// cached) this just re-establish the evaluation, the feasibility
			// and the cost in the solution
			try {
				evalServer.EvaluateSolution(solution);
			} catch (EvaluationException e) {
				throw new OptimizationException("", "scaleIn", e);
			}

			logger.debug("Current Solution Cost:" + sol.getCost());

			// If we couldn't perform the scale in action or if we performed it
			// but the cost of the new solution is not smaller than the cost of
			// the previous solutions then increase the number of iterations
			// without an improvement
			if (noScaleIn || previousSol.getCost() <= sol.getCost()) {
				logger.debug("No improvement. Scaled " + !noScaleIn
						+ " Previous Cost: " + previousSol.getCost()
						+ " Current Cost: " + sol.getCost());
				numIterNoImprov++;
			}

			updateLocalBestSolution(solution);
			updateBestSolution(solution);

			// if the cost of the current solution is far from the best one this
			// iteration is considered "out of bound"
			if (sol.getCost() - localBestSolution.getCost() > boundFactor
					* localBestSolution.getCost()) {
				logger.debug("Out of Bound. Scaled " + !noScaleIn
						+ " Previous Cost: " + previousSol.getCost()
						+ " Current Cost: " + sol.getCost() + " Bound: "
						+ boundFactor * localBestSolution.getCost()
						+ " Bound factor: " + boundFactor);
				outOfBoundIterations++;
			}
			// if the solution is close to the optimal one then reduce the bound
			// linearly with the amount of iterations in which we were within
			// the bounds.
			else {
				boundFactor /= (double) (iteration - outOfBoundIterations);
			}

		}

		logger.debug("Scale In finished");
		if (numIterNoImprov >= MAX_SCALE_IN_CONV_ITERATIONS * numberOfTiers)
			logger.debug("The optimal number of replicas has been found. Iterations without any further improvement: "
					+ numIterNoImprov);
		if (outOfBoundIterations >= MAX_OUT_OF_BOUND_ITERATIONS)
			logger.debug("The solution is too far from the optimal one. Iterations out of bounr: "
					+ outOfBoundIterations + " Bound Factor: " + boundFactor);

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
			logger.info("Execution was cancelled");
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
								.getCloudElementSizes(actualProvider, /*
																	 * cloudProvider
																	 * ,
																	 */
								serviceName, Configuration.BENCHMARK.toString())
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
							+ "_CPU_Processor"); // TODO:
													// here
													// for
													// PaaS?

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
						double demand = getDemandFromSeff(((ResourceDemandingSEFF) s));// TODO
																						// .getSteps_Behaviour());

						String signatureID = s.getDescribedService__SEFF()
								.getId();
						Functionality function = new Functionality(s
								.getDescribedService__SEFF().getEntityName(),
								((ResourceDemandingSEFF) s).getId(),
								systemCalls2Signatures.get(signatureID), demand);
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
			evalServer.EvaluateSolution(initialSolution);
		} catch (EvaluationException e) {
			throw new InitializationException(
					"Could not evaulate the initial solution", e);
		}
		logger.info(this.initialSolution.showStatus());

		bestSolutions.add(initialSolution);
		firePropertyChange(OptimizationProgressWindow.FIRST_SOLUTION_AVAILABLE,
				false, true);

		String basePath = Paths.get(Configuration.PROJECT_BASE_FOLDER,
				Configuration.WORKING_DIRECTORY, "milp_solution").toString();
		
		try {
			Files.createDirectories(Paths.get(Configuration.PROJECT_BASE_FOLDER,
					Configuration.WORKING_DIRECTORY, "milp_solution"));
		} catch (IOException e) {
			throw new InitializationException(
					"Could not create initial solution log folders", e);
		}

		initialSolution.exportAsExtension(Paths.get(basePath,
				Configuration.SOLUTION_FILE_NAME + "Total"
						+ Configuration.SOLUTION_FILE_EXTENSION));

		initialSolution.exportStatisticCSV(Paths.get(basePath,
				Configuration.SOLUTION_CSV_FILE_NAME));

		initialSolution.exportCostsAsExtension(Paths.get(basePath, "costs"
				+ Configuration.SOLUTION_FILE_EXTENSION));

		for (Solution s : initialSolution.getAll())
			s.exportPerformancesAsExtension(Paths.get(basePath, "performance"
					+ s.getProvider() + Configuration.SOLUTION_FILE_EXTENSION));
	}

	private double getDemandFromSeff(ResourceDemandingSEFF s) {
		double demand = 0.0;

		EList<AbstractAction> actions = s.getSteps_Behaviour();

		for (int i = 0; i < actions.size(); ++i) {
			AbstractAction a = actions.get(i);

			if (a instanceof InternalAction) {
				InternalAction ia = ((InternalAction) a);

				for (de.uka.ipd.sdq.pcm.seff.seff_performance.ParametricResourceDemand d : ia
						.getResourceDemand_Action()) {
					try {
						demand += Double.parseDouble(d
								.getSpecification_ParametericResourceDemand()
								.getSpecification());
					} catch (Exception e) {
					}
				}
			} else if (a instanceof ExternalCallAction) {
				ExternalCallAction ea = ((ExternalCallAction) a);

				actions.addAll(ea
						.getResourceDemandingBehaviour_AbstractAction()
						.getSteps_Behaviour());
			}
		}

		return demand;
	}

	/**
	 * Loads the solution from a serliazed object
	 * 
	 * @throws InitializationException
	 */
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

	private void makeFeasible(SolutionMulti sol) throws OptimizationException {
		for (Solution s : sol.getAll())
			makeFeasible(sol, s.getProvider());
	}

	/**
	 * Make the solution feseable by performing scale out operations
	 * 
	 * @param sol
	 * @throws OptimizationException
	 */
	private void makeFeasible(SolutionMulti solution, String provider)
			throws OptimizationException {

		Solution sol = solution.get(provider);

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
						if (tsMoveInternal(solution, provider, t, 0))
							evaluateAgain = true;
					}
				}
			}

			if (evaluateAgain)
				try {
					evalServer.EvaluateSolution(solution);
				} catch (EvaluationException e) {
					logger.error("Error while evaluating the solution.", e);
				}

			// This line makes the factor decrease linearly for high nintial
			// impact
			// double factor = MAX_FACTOR - (MAX_FACTOR - MIN_FACTOR)
			// * numberOfFeasibilityIterations / MAXFEASIBILITYITERATIONS;

			// this line makes the factor increase linearly for more smooth
			// initial impact (overall seems to be a better strategy since the
			// growth is still exponential)
			double factor = MIN_FACTOR + (MAX_FACTOR - MIN_FACTOR)
					* numberOfFeasibilityIterations / MAXFEASIBILITYITERATIONS;
			logger.info("\tFeasibility iteration: "
					+ numberOfFeasibilityIterations + "factor: " + factor);
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
				evalServer.EvaluateSolution(solution);
			} catch (EvaluationException e) {
				throw new OptimizationException("", "makeFeasible", e);
			}

			numberOfFeasibilityIterations += 1;
		}
		// logger.trace(sol.showStatus());
		if (sol.isFeasible())
			logger.info("Solution is feasible");
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
	 * @throws ConstraintEvaluationException
	 * @throws IOException 
	 */
	public Integer optimize() throws OptimizationException,
			ConstraintEvaluationException, IOException {

		// 1: check if an initial solution has been set
		if (this.initialSolution == null)
			return -1;
		logger.info("starting the optimization");
		// start the timer
		if (!providedtimer)
			timer.start();
		try {
			evalServer.EvaluateSolution(initialSolution);
		} catch (EvaluationException e) {
			throw new OptimizationException("", "initialEvaluation", e);
		} // evaluate the current

		// solution
		// initialSolution.showStatus();
		// Debugging constraintHandler

		// timer.split();
		// costLogImage.add(localBestSolutionSerieHandler,
		// TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
		// localBestSolution.getCost());
		// costLogImage.add(bestSolutionSerieHandler,
		// TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
		// bestSolution.getCost());
		// logger.warn("" + bestSolution.getCost() + ", 1 " + ", "
		// + bestSolution.isFeasible());

		scrambleIteration = 1;
		bestSolutionSerieHandler = "Best Solution";
		localBestSolutionSerieHandler = "Local Best Solution";
		resetBestSolution(initialSolution);
		resetLocalBestSolution(initialSolution);
		currentSolution = initialSolution.clone();

		boolean solutionChanged = true;
		
		if(MAX_SCRUMBLE_ITERS ==0)
			return -1;
		
		if (Configuration.RELAXED_INITIAL_SOLUTION) {

			logger.info("Assessing Feasibility of relaxed solution");
			// make feasible:
			makeFeasible(currentSolution);

			resetBestSolution(currentSolution);
			resetLocalBestSolution(currentSolution);

			// scale in:
			logger.info("Optimizing relaxed initial solution");
			InternalOptimization(currentSolution);
			updateLocalBestSolution(currentSolution);
			updateBestSolution(currentSolution);
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

			logger.info("Scramble Iteration: " + scrambleIteration);
			// logger.trace( currentSolution.showStatus());

			// 2: Internal Optimization process

			if (Configuration.isPaused())
				waitForResume();

			// //////////////////
			if (bestSolutionUpdated && Configuration.REDISTRIBUTE_WORKLOAD) {

				logger.debug("The best solution did change, so let's redistribuite the workload...");

				currentSolution = bestSolution.clone();

				logger.trace(currentSolution.showWorkloadPercentages());
				// setWorkloadPercentagesFromMILP(currentSolution);
				logger.trace("MILP:\n"
						+ currentSolution.showWorkloadPercentages());

				logger.debug(currentSolution.showWorkloadPercentages());
				currentSolution = maximizeWorkloadPercentagesForLeastUsedTier(currentSolution);
				logger.debug(currentSolution.showWorkloadPercentages());

				logger.debug("My method:\n"
						+ currentSolution.showWorkloadPercentages());

				logger.info("Updating best solutions");

				// both should be feasible
				resetBestSolution(currentSolution);
				resetLocalBestSolution(currentSolution);

				bestSolutionUpdated = false;

			}
			// //////////////////

			// 2: Internal Optimization process

			InternalOptimization(currentSolution);

			// 3: check whether the best solution has changed
			// If the current solution is better than the best one it becomes
			// the new best solution.
			logger.info("Updating best solutions");
			updateLocalBestSolution(currentSolution);
			updateBestSolution(currentSolution);

			// 3b: clone the best solution to start the scramble from it
			currentSolution = localBestSolution.clone();

			// 4 Scrambling the current solution.
			logger.info("Executing: Scramble");
			logger.info("Tabu List Size: " + tabuSolutionList.size());

			if (Configuration.isPaused())
				waitForResume();

			solutionChanged = tsMove(currentSolution);
			// if the local optimum with respect to the type of machine has been
			// found we need to perform some diversification (using the
			// long-term memory)
			if (!solutionChanged) {
				logger.info("Stuck in a local optimum, using long term memory");
				currentSolution = longTermMemoryRestart(currentSolution);
				logger.info("Long term memory statistics:");
				for (Tier t : currentSolution.get(0).getApplication(0)
						.getTiers()) {
					logger.info("\tTier: " + t.getPcmName() + " Size: "
							+ longTermFrequencyMemory.get(t.getId()).size());
				}
				resetLocalBestSolution(currentSolution);

			}

			setProgress((scrambleIteration * 100 / MAX_SCRUMBLE_ITERS));
			// increment the number of iterations
			scrambleIteration += 1;
			firePropertyChange("iteration", scrambleIteration - 1,
					scrambleIteration);

		}

		// We now consider the private cloud, if we have to
		if (Configuration.USE_PRIVATE_CLOUD) {
			buildPrivatecloudSolution();
		}

		// dump memory
		logger.debug("MemoryDump");
		for (String s : tabuSolutionList.keySet()) {
			logger.debug(s);
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

		exportBestSolutionsTrace();

		return -1;

	}

	private void exportBestSolutionsTrace() throws IOException {

		Path base = Paths.get(Configuration.PROJECT_BASE_FOLDER,
				Configuration.WORKING_DIRECTORY, "Solutions");
		String basePath = base.toString();

		Files.createDirectories(base);
		for (SolutionMulti multisol : bestSolutions) {
			
			for (Solution sol : multisol.getAll())
				sol.exportAsExtension(Paths.get(basePath,
						Configuration.SOLUTION_FILE_NAME
								+ bestSolutions.indexOf(multisol)
								+ sol.getProvider()
								+ Configuration.SOLUTION_FILE_EXTENSION));

			multisol.exportAsExtension(Paths.get(
					basePath,
					Configuration.SOLUTION_FILE_NAME
							+ bestSolutions.indexOf(multisol) + "Total"
							+ Configuration.SOLUTION_FILE_EXTENSION));

			multisol.exportStatisticCSV(Paths.get(
					basePath,
					Configuration.SOLUTION_CSV_FILE_NAME
							+ bestSolutions.indexOf(multisol)));

			multisol.exportCostsAsExtension(Paths.get(
					basePath,
					Configuration.SOLUTION_FILE_EXTENSION
							+ bestSolutions.indexOf(multisol) + "costs"));

			for (Solution s : bestSolution.getAll())
				s.exportPerformancesAsExtension(Paths.get(
						basePath,
						Configuration.SOLUTION_FILE_EXTENSION
								+ bestSolutions.indexOf(multisol)
								+ "performance" + s.getProvider()));
			
			
		}
		
		exportCostsStatisticCSV(Paths.get(basePath,"optimizationTrace.csv" ),bestSolutions);
	}
	
	private void exportCostsStatisticCSV(Path filePath, List<SolutionMulti> solutions) {
		String text = "Generation Iteration,Generation Time, Cost, Feasibility\n";
		
		for(SolutionMulti sol:solutions){
			text += sol.getGenerationIteration()+ ",";
			text  += sol.getGenerationTime()+ ",";
			text += sol.getCost() + ",";
			text += sol.isFeasible()+"\n";
		}
		
		
		try {
			PrintWriter outFile = new PrintWriter(filePath.toFile());
			outFile.println(text);
			outFile.close();
		} catch (FileNotFoundException e) {
			logger.error("Error while exporting the data via CSV.", e);
		}
	}

	/**
	 * Discards the local best solution and forces the selected one
	 * 
	 * @param solution
	 */
	private void resetLocalBestSolution(SolutionMulti solution) {
		localBestSolution = null;
		updateLocalBestSolution(solution);

	}

	/**
	 * Discards the best solution and forces the selected one
	 * 
	 * @param solution
	 */
	private void resetBestSolution(SolutionMulti solution) {
		bestSolution = null;
		updateBestSolution(solution);
	}

	/**
	 * Builds a private cloud solution starting from the best solution
	 */
	private void buildPrivatecloudSolution() {
		// Debugging
		// exportSolution();

		try {
			SolutionMulti sol = considerPrivateCloud(bestSolution);
			if (sol != null) {
				currentSolution = sol;

				resetBestSolution(currentSolution);

				resetLocalBestSolution(currentSolution);

			}
		} catch (Exception e) {
			logger.error("Unable to consider the private cloud", e);
		}
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
			Solution sol = longTermMemoryRestart(solMulti, s.getProvider());
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
	private Solution longTermMemoryRestart(SolutionMulti solution,
			String provider) throws OptimizationException {

		Solution sol = solution.get(provider);
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
			evalServer.EvaluateSolution(solution);
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
			SolutionMulti currentSolution) throws OptimizationException,
			ConstraintEvaluationException {
		double wpMin = 0.0; // the minimum workload balance constraint
		List<Constraint> constraints = constraintHandler
				.getConstraintByResourceId(Configuration.APPLICATION_ID);
		for (Constraint c : constraints)
			if (c instanceof WorkloadPercentageConstraint) {
				wpMin = Rounder.round(((WorkloadPercentageConstraint) c)
						.getMin());
			}

		if (wpMin > 1) {
			wpMin /= 100;
			wpMin = Rounder.round(wpMin);
		}

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
			increments[i] = Rounder.round(WL_INCREMENT);
			keepGoing[i] = true;
		}

		// chose which provider receives the new workload for each hour
		List<Solution> leastUsedProviders = new ArrayList<Solution>(24);
		for (int hour = 0; hour < 24; hour++) {
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
			double distance = 1;
			for (Solution sol : mostUsedTiers.keySet()) {
				double tmpDistance = getTierUtilizaionDistance(mostUsedTiers
						.get(sol));
				if (tmpDistance < distance) {
					distance = tmpDistance;
					leastUsedProvider = sol;
				}
			}
			leastUsedProviders.add(leastUsedProvider);
		}

		do {
			// ave the current solution in the memory (should always be
			// feasible)
			lastSolution = currentSolution.clone();

			for (int hour = 0; hour < 24; hour++) {
				if (!keepGoing[hour])
					continue;

				Solution leastUsedProvider = leastUsedProviders.get(hour);
				double distance = 1;

				for (Tier t : leastUsedProvider.getApplication(hour).getTiers()) {
					double tmpDistance = getTierUtilizaionDistance(t);
					if (tmpDistance < distance) {
						distance = tmpDistance;
					}
				}

				if (distance < 0.1) {
					keepGoing[hour] = false;
					continue;
				}

				// calculate the increment of workload for the minimum used
				// provider
				double wpStar = leastUsedProvider.getPercentageWorkload(hour);
				double diff = Rounder.round(1 - wpStar);

				double rate = increments[hour];
				// keep the remainder of the difference to split it on other
				// providers if needed
				double remainder = 0.0;
				if (diff < rate) {
					rate = diff;
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
						double wp = Rounder.round(sol
								.getPercentageWorkload(hour));
						// avoid breaking theminimum workload constraint, in
						// case it is violated then fix it
						if (wp > 0 && wp < wpMin) {
							remainder += Rounder.round(wpMin - wp + rate);
							changeWorkload(sol, hour, wpMin);
						} else {
							// double wpMin = 0.0;
							diff = Rounder.round(wp - wpMin);
							double dec = Rounder.round(rate
									/ (currentSolution.size() - 1) + remainder);

							if (diff < dec) {
								remainder = Rounder.round(dec - diff);
								dec = diff;
							}

							// System.out.println(currentSolution.showWorkloadPercentages());
							changeWorkload(sol, hour, Rounder.round(wp - dec));
							// System.out.println(currentSolution.showWorkloadPercentages());
						}
					}
				}

				if (remainder != 0.0) {
					// System.out.println(currentSolution.showWorkloadPercentages());
					changeWorkload(leastUsedProvider, hour,
							Rounder.round(wpStar - remainder));
					// System.out.println(currentSolution.showWorkloadPercentages());
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

	private double getTierUtilizaionDistance(Tier tier)
			throws ConstraintEvaluationException {

		List<Constraint> utilizationConstraints = constraintHandler
				.getConstraintByResourceId(tier.getId(), UsageConstraint.class);
		double distance = 100;
		for (Constraint c : utilizationConstraints) {
			double constraintDistance = ((UsageConstraint) c)
					.checkConstraintDistance(tier);
			if (constraintDistance < distance)
				distance = constraintDistance;
		}

		if (distance > 0)
			distance = tier.getUtilization() - 1;

		return Rounder.round(-distance);
	}

	private boolean tsMoveInternal(SolutionMulti solution, String provider,
			Tier selectedTier, int iterations) throws OptimizationException {

		Solution sol = solution.get(provider);
		MoveTypeVM moveVM = new MoveTypeVM(sol);

		logger.debug("Selected Tier:" + selectedTier.getPcmName());
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
			logger.warn("No resource found for scramble after constraint check, iteration: "
					+ iterations);
			return false;
		}

		resList = filterByMemory(resList, sol, selectedTier);
		// if no resource can substitute the current one increase the number
		// of iterations without a change and try again
		if (resList.size() == 0) {
			logger.debug("MemoryDump");
			for (String s : tabuSolutionList.keySet()) {
				logger.debug(s);
			}
			logger.warn("No resource found for scramble after memory check, iteration: "
					+ iterations);
			return false;
		}

		// Phase3: choose the new resource. Policy: roulette selection
		// 3.1Calculate the cumulative fitness
		double[] cumulativeFitnesses = new double[resList.size()];
		try {
			cumulativeFitnesses[0] = getEfficiency(resList.get(0), sol.getRegion());		
		for (int i = 1; i < resList.size(); i++) {
			double fitness = getEfficiency(resList.get(i), sol.getRegion());
			cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + fitness;
		}
		
		} catch (CostEvaluationException e) {
			throw new OptimizationException("Error calculating fitness",e);
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
		// logger.debug("Sorted Resources");
		// for (IaaS res : resList) {
		// if (newRes.equals(res))
		// logger.debug("Index: " + resList.indexOf(res)
		// + " res: " + res.getResourceName() + " value: "
		// + getEfficiency(res, sol.getRegion())
		// + " <--- SELECTED");
		// else
		// logger.debug("Index: " + resList.indexOf(res)
		// + " res:" + res.getResourceName() + " value: "
		// + getEfficiency(res, sol.getRegion()));
		// }
		// logger.debug("Selected index:" + resList.indexOf(newRes));

		// Phase4: performs the change and evaluate the solution
		// add the new configuration in the memory
		moveVM.changeMachine(selectedTier.getId(), newRes);
		try {
			evalServer.EvaluateSolution(solution);
		} catch (EvaluationException e) {
			throw new OptimizationException("", "scramble", e);
		}

		tabuSolutionList.put(SolutionHelper.buildSolutionTypeID(sol), null);
		logger.debug("Memory size " + tabuSolutionList.size());
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
	private boolean tsMove(SolutionMulti solution, String provider)
			throws OptimizationException {
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

		Solution sol = solution.get(provider);
		// Phase0: initialization
		// MoveTypeVM moveVM = new MoveTypeVM(sol);
		boolean done = false; // tells if something has changed
		int iterations = 0; // number of iterations without a change in the
		// solution
		logger.debug("iteration: " + scrambleIteration);

		// try to performe the change until something has actually changed or we
		// give up
		while (!done && iterations < MAX_SCRAMBLE_NO_CHANGE) {

			List<Tier> tierList = sol.getApplication(0).getTiers();
			Tier selectedTier = tierList.get(random.nextInt(tierList.size()));

			done = tsMoveInternal(solution, provider, selectedTier, iterations);
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
	 * @throws CostEvaluationException 
	 */
	private double getEfficiency(CloudService resource, String region) throws CostEvaluationException {
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

	private boolean tsMove(SolutionMulti sol) throws OptimizationException {

		boolean change = false;
		for (Solution s : sol.getAll())
			change = change || tsMove(sol, s.getProvider());
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

	// /**
	// * Checks whether the solution is "better" with respect to the current
	// * optimal solution. Better here means that the solution is Evaluated,
	// * Feasible and cost less
	// *
	// * This methods check the quality only of the solution of a single
	// provider.
	// *
	// * @param the
	// * new solution
	// * @return true if sol has become the new best solution
	// */
	// private boolean updateBestSolution(Solution sol) {
	// if (sol.greaterThan(bestSolution)) {
	// Solution clone = sol.clone();
	// bestSolution.add(clone);
	// timer.split();
	// long time = timer.getSplitTime();
	// clone.setGenerationIteration(scrambleIteration);
	// clone.setGenerationTime(time);
	// bestSolution.setGenerationIteration(scrambleIteration);
	// bestSolution.setGenerationTime(time);
	//
	// //Add to the list of best solutions for logging
	// if (bestSolutions != null) {
	// bestSolutions.add(bestSolution.clone());
	// firePropertyChange(BestSolutionExplorer.PROPERTY_ADDED_VALUE, false,
	// true);
	// updateCostLogImage(bestSolution, bestSolutionSerieHandler);
	// firePropertyChange(OptimizationEngine.BEST_SOLUTION_UPDATED, false,
	// true);
	// // bse.propertyChange(new PropertyChangeEvent(this,
	// // "BSEAddedValue", false, true));
	// }
	//
	// logger.info("updated best solution");
	// return true;
	// }
	// return false;
	// }

	/**
	 * Checks whether the solution is "better" with respect to the current
	 * optimal solution. Better here means that the solution is Evaluated,
	 * Feasible and cost less
	 * 
	 * This methods check the quality only of the solution of a single provider.
	 * 
	 * @param the
	 *            new solution
	 * @return true if sol has become the new best solution
	 */
	private void updateBestSolution(SolutionMulti sol) {
		if (sol.greaterThan(bestSolution)) {
			bestSolution = sol.clone();
			timer.split();
			long time = timer.getSplitTime();
			sol.setGenerationTime(time);
			sol.setGenerationIteration(scrambleIteration);
			bestSolution.setGenerationTime(time);
			bestSolution.setGenerationIteration(scrambleIteration);
			updateCostLogImage(bestSolution, bestSolutionSerieHandler);
			firePropertyChange(OptimizationEngine.BEST_SOLUTION_UPDATED, false,
					true);

			// Add to the list of best solutions for logging
			if (bestSolutions != null) {
				bestSolutions.add(bestSolution.clone());
				firePropertyChange(BestSolutionExplorer.PROPERTY_ADDED_VALUE,
						false, true);
			}
			logger.info("updated best solution");
		}
	}

	private void updateLocalBestSolution(SolutionMulti sol) {

		if (sol.greaterThan(localBestSolution)) {
			localBestSolution = sol.clone();
			timer.split();
			long time = timer.getSplitTime();
			sol.setGenerationTime(time);
			sol.setGenerationIteration(scrambleIteration);
			localBestSolution.setGenerationTime(time);
			localBestSolution.setGenerationIteration(scrambleIteration);
			updateCostLogImage(localBestSolution, localBestSolutionSerieHandler);
			logger.info("updated local best solution");
		}
	}

	private void updateCostLogImage(SolutionMulti solution, String seriesHandler) {
		// logger.warn("" + solution.getCost() + ", "
		// + TimeUnit.MILLISECONDS.toSeconds(solution.getGenerationTime())
		// + ", " + solution.isFeasible());
		costLogImage.add(seriesHandler,
				TimeUnit.MILLISECONDS.toSeconds(solution.getGenerationTime()),
				solution.getCost());
	}

	private void loadConfiguration() {
		MAXFEASIBILITYITERATIONS = Configuration.FEASIBILITY_ITERS;
		MAX_SCRUMBLE_ITERS = Configuration.SCRUMBLE_ITERS;
		MAXMEMORYSIZE = Configuration.TABU_MEMORY_SIZE;
		SELECTION_POLICY = Configuration.SELECTION_POLICY;
		DEFAULT_SCALE_IN_FACTOR = Configuration.SCALE_IN_FACTOR;
		MAX_OUT_OF_BOUND_ITERATIONS = Configuration.SCALE_IN_ITERS;
		MAX_SCALE_IN_CONV_ITERATIONS = Configuration.SCALE_IN_CONV_ITERS;
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

		bestSolution.exportStatisticCSV(Paths.get(basePath,
				Configuration.SOLUTION_CSV_FILE_NAME));

		bestSolution.exportCostsAsExtension(Paths.get(basePath, "costs"
				+ Configuration.SOLUTION_FILE_EXTENSION));

		for (Solution s : bestSolution.getAll())
			s.exportPerformancesAsExtension(Paths.get(basePath, "performance"
					+ s.getProvider() + Configuration.SOLUTION_FILE_EXTENSION));

		if (Configuration.GENERATE_DESIGN_TO_RUNTIME_FILES) {
			for (File f : generateDesignToRuntimeFiles())
				logger.debug("Generated {}", f.toString());
		}
	}

	private List<File> generateDesignToRuntimeFiles() {
		logger.info("Calling the DesignToRuntimeConnector project...");

		String basePath = Paths.get(Configuration.PROJECT_BASE_FOLDER,
				Configuration.WORKING_DIRECTORY).toString();

		List<File> res = new ArrayList<>();

		AdaptationModelBuilder amb = new AdaptationModelBuilder(Paths.get(
				Configuration.DB_CONNECTION_FILE).toString());
		for (Solution s : bestSolution.getAll()) {
			try {
				res.addAll(amb.createAdaptationModelAndRules(
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
						Configuration.TIMESTEP_DURATION, s.getProvider(),
						getDemandMap()));
			} catch (Exception e) {
				logger.error(
						"Error while exporting the runtime files for "
								+ s.getProvider() + ".", e);
			}
		}

		return res;
	}

	private Map<String, Double> getDemandMap() {
		Map<String, Double> res = new HashMap<>();

		for (Solution s : bestSolution) {
			Instance app = s.getApplication(0);
			for (Tier t : app.getTiers()) {
				for (Functionality f : t.getFunctionalities()) {
					res.put(f.getId(), f.getDemand());
				}
			}
		}

		return res;
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
