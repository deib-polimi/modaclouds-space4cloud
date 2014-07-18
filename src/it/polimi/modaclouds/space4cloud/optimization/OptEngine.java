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

import it.polimi.modaclouds.space4cloud.chart.Logger2JFreeChartImage;
import it.polimi.modaclouds.space4cloud.chart.SeriesHandle;
import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.AnalysisFailureException;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationProxy;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationServer;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Cache;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Configuration.Policy;
import it.polimi.modaclouds.space4cloud.utils.ResourceEnvironmentExtensionParser;
import it.polimi.modaclouds.space4cloud.utils.ResourceEnvironmentExtentionLoader;
import it.polimi.modaclouds.space4cloud.utils.SolutionHelper;
import it.polimi.modaclouds.space4cloud.utils.UsageModelExtensionParser;

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
import de.uka.ipd.sdq.pcm.seff.ResourceDemandingSEFF;
import de.uka.ipd.sdq.pcm.seff.ServiceEffectSpecification;
import de.uka.ipd.sdq.pcm.usagemodel.AbstractUserAction;
import de.uka.ipd.sdq.pcm.usagemodel.Branch;
import de.uka.ipd.sdq.pcm.usagemodel.BranchTransition;
import de.uka.ipd.sdq.pcm.usagemodel.EntryLevelSystemCall;
import de.uka.ipd.sdq.pcm.usagemodel.Loop;
import de.uka.ipd.sdq.pcm.usagemodel.ScenarioBehaviour;
import de.uka.ipd.sdq.pcmsolver.models.PCMInstance;

/**
 * @author Michele Ciavotta Class defining the optimization engine.
 */
public class OptEngine extends SwingWorker<Void, Void> implements PropertyChangeListener{

	protected SolutionMulti initialSolution = null;
	
	protected SolutionMulti bestSolution = null;

	protected SolutionMulti currentSolution = null;

	protected SolutionMulti localBestSolution = null;

	protected ConstraintHandler constraintHandler;

	protected DataHandler dataHandler;

	protected int numberOfIterations;

	protected int numberOfFeasibilityIterations;

	protected Policy SELECTION_POLICY;

	protected int MAXMEMORYSIZE = 10;

	private static final int MAX_SCRAMBLE_NO_CHANGE = 10;

	protected int MAX_SCRUMBLE_ITERS = 20; /*
	 * 40 Now it is a constant in the future it
	 * might become a parameter
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

	protected SeriesHandle bestSolutionSerieHandler;
	protected SeriesHandle localBestSolutionSerieHandler;

	protected Random randomSeed = new Random();
	protected Random random;
	protected int numIterNoImprov = 0;

	protected StopWatch timer = new StopWatch();

	protected EvaluationServer evalServer;

	protected Logger logger = LoggerFactory.getLogger(OptEngine.class);
	protected Logger optimLogger = LoggerFactory.getLogger("optimLogger");
	protected Logger scrambleLogger = LoggerFactory.getLogger("scrambleLogger");

	protected Logger2JFreeChartImage costLogImage;

	protected Logger2JFreeChartImage logVm;
	protected Logger2JFreeChartImage logConstraints;

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
	public OptEngine(ConstraintHandler handler,
			boolean batch) throws DatabaseConnectionFailureExteption {

		loadConfiguration();

		try {
			costLogImage = new Logger2JFreeChartImage();
			logVm = new Logger2JFreeChartImage("vmCount.properties");
			logConstraints = new Logger2JFreeChartImage(
					"constraints.properties");

		} catch (NumberFormatException | IOException e) {
			logger.error("Unable to create chart loggers", e);

		}
		//int seed = randomSeed.nextInt();
		int seed = 1280220077;
		optimLogger.debug("Random seed: " + seed);
		random = new Random(seed);

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
	 * @param rate
	 *            the rate by which we'll multiply the actual workload, taken
	 *            from the usage model extension file.
	 */
	protected void changeWorkload(Solution sol, double rate) {
		logger.debug("The hourly values of the workload are: ");
		for (Instance i : sol.getApplications())
			logger.debug("%d ", i.getWorkload());
		logger.debug("\nTrying the change them using a rate of %f...",
				rate);

		MoveChangeWorkload move = new MoveChangeWorkload(sol);

		try {
			move.modifyWorkload(new File(Configuration.USAGE_MODEL_EXTENSION), rate);
			logger.debug("done!\nThe new values are: ");
			for (Instance i : sol.getApplications())
				logger.debug("%d ", i.getWorkload());

		} catch (ParserConfigurationException | SAXException | IOException
				| JAXBException e) {
			logger.debug("error!\n");
			e.printStackTrace();
			logger.error("Error performing the change of the workload.\n"
					+ e.getMessage());
			return;
		}

		InternalOptimization(currentSolution);

	}

	protected void changeWorkload(SolutionMulti sols) {
		if (sols.size() < 2)
			return;

		HashMap<String, Double> processingRatesMap = new HashMap<String, Double>();
		HashMap<String, Integer> memoriesMap = new HashMap<String, Integer>();
		HashMap<String, double[]> ratesMap = new HashMap<String, double[]>();
		HashMap<String, ArrayList<Tier>> tierMap = new HashMap<String, ArrayList<Tier>>();

		for (Solution sol : sols.getAll()) {
			logger.debug("The hourly values of the workload for "
					+ sol.getProvider() + " are: ");
			for (Instance i : sol.getApplications())
				logger.debug("%d ", i.getWorkload());

			List<Tier> tierList = sol.getApplication(0).getTiers();
			String provider = sol.getProvider();

			for (Tier tier : tierList) {
				String id = tier.getId();

				ArrayList<Tier> tierByIdList = tierMap.get(id);
				if (tierByIdList == null)
					tierByIdList = new ArrayList<Tier>();

				tierByIdList.add(tier);
				tierMap.put(id, tierByIdList);

				CloudService origRes = tier.getCloudService();

				double processingRate = dataHandler.getProcessingRate(
						origRes.getProvider(), origRes.getServiceName(),
						origRes.getResourceName());
				int memory = dataHandler.getAmountMemory(origRes.getProvider(),
						origRes.getServiceName(), origRes.getResourceName());

				processingRatesMap.put(id + provider, processingRate);
				memoriesMap.put(id + provider, memory);
			}

			double[] rates = new double[24];
			for (int i = 0; i < 24; ++i)
				rates[i] = sol.getPercentageWorkload(i);

			ratesMap.put(provider, rates);
		}

		for (int i = 0; i < 24; ++i) {

			for (String id : tierMap.keySet()) {
				logger.debug("Hour: %d, Tier ID: %s\n", i, id);

				ArrayList<Tier> tierByIdList = tierMap.get(id);
				double processingRates[] = new double[tierByIdList.size()];
				int memories[] = new int[tierByIdList.size()];

				int x = 0;
				for (Tier tier : tierByIdList) {
					IaaS service = (IaaS) tier.getCloudService();
					String provider = service.getProvider();
					processingRates[x] = service.getReplicas()
							* processingRatesMap.get(id + provider);
					memories[x] = service.getReplicas()
							* memoriesMap.get(id + provider);
					x++;
				}

				int idMaxProcs = 0, idMaxMemory = 0;

				for (int j = 1; j < processingRates.length; ++j) {
					if (processingRates[j] > processingRates[idMaxProcs])
						idMaxProcs = j;
					if (memories[j] > memories[idMaxMemory])
						idMaxMemory = j;
				}

				String providerMaxProcs = tierByIdList.get(idMaxProcs)
						.getCloudService().getProvider();
				String providerMaxMemory = tierByIdList.get(idMaxMemory)
						.getCloudService().getProvider();
				String providerRandom = tierByIdList
						.get((int) Math.floor(Math.random() * sols.size()))
						.getCloudService().getProvider();
				boolean doRandom = (Math.random() > 0.4);

				for (Solution sol : sols.getAll()) {
					double[] rates = ratesMap.get(sol.getProvider());

					if (sol.getProvider().equals(providerMaxProcs)) {
						rates[i] = rates[i] + 0.05 * (sols.size() - 1);
					} else {
						rates[i] = rates[i] - 0.05;
					}

					if (sol.getProvider().equals(providerMaxMemory)) {
						rates[i] = rates[i] + 0.05 * (sols.size() - 1);
					} else {
						rates[i] = rates[i] - 0.05;
					}

					if (doRandom) {
						if (sol.getProvider().equals(providerRandom)) {
							rates[i] = rates[i] + 0.03 * (sols.size() - 1);
						} else {
							rates[i] = rates[i] - 0.03;
						}
					}

					logger.debug("\t" + sol.getProvider() + ": ");
					for (double rate : rates) {
						logger.debug((int) (rate * 100) + " ");
					}


					ratesMap.put(sol.getProvider(), rates);
				}

				int bad = 0;
				for (Solution sol : sols.getAll()) {
					// for (int k = 0; k < sols.size(); ++k) {
					// Solution sol = sols.get(k);
					double[] rates = ratesMap.get(sol.getProvider());

					if (rates[i] < 0.1) {
						double diff = (0.1 - rates[i]) / (sols.size() - ++bad);
						rates[i] = 0.1;
						// for (Solution sol2 : sols.getAll()) {
						for (int l = 0; l < sols.size(); ++l) {
							Solution sol2 = sols.get(l);
							if (!sol.getProvider().equals(sol2.getProvider())) {
								double[] rates2 = ratesMap.get(sol2
										.getProvider());
								rates2[i] -= diff;

								if (rates2[i] < 0.1) {
									diff += (0.1 - rates[i])
											/ (sols.size() - ++bad);
									rates2[i] = 0.1;
									l = 0;
								}

								ratesMap.put(sol2.getProvider(), rates2);
							}
						}

						System.out.print("Fixed: " + sol.getProvider() + ": ");
						for (double rate : rates) {
							System.out.print((int) (rate * 100) + " ");
						}

						ratesMap.put(sol.getProvider(), rates);
					}
				}

			}

		}

		for (Solution sol : sols.getAll()) {
			MoveChangeWorkload move = new MoveChangeWorkload(sol);

			try {
				move.modifyWorkload(new File(Configuration.USAGE_MODEL_EXTENSION),
						ratesMap.get(sol.getProvider()));
				logger.debug("Done! The new values for "
						+ sol.getProvider() + " are: ");
				for (Instance i : sol.getApplications())
					logger.debug("%d ", i.getWorkload());

			} catch (ParserConfigurationException | SAXException | IOException
					| JAXBException e) {
				logger.debug("error!\n");
				e.printStackTrace();
				logger.error("Error performing the change of the workload.\n"
						+ e.getMessage());
				// return;
			}
		}

		evalServer.EvaluateSolution(sols);
		updateBestSolution(sols);

	}

	/**
	 * @param sol
	 * 
	 */
	protected void descentOptimize(Solution sol) {

		// UniformScaleInLS(sol);
		/* inicialization array of moves */
		/* turning back one step for feasibility */
		// makeFeasible(sol);
		// logger.warn("Descent Optimization first phase - feasibility");
		// updateBestSolution(sol);

		/* phase 2: scaling In one res at time */
		IteratedRandomScaleInLS(sol);
	}

	@Override
	protected Void doInBackground() throws Exception {
		if (initialSolution.size() > 1)
			optimizeMultiprovider();
		else
			optimize();
		return null;
	}

	public void evaluate() {

		timer.start();
		timer.split();
		evalServer.EvaluateSolution(initialSolution);
		logger.trace(initialSolution.showStatus());
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
	protected Tier findResource(Instance application, String id,
			Policy policy) {

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
				// if the cloud service hostin the application tier is a IaaS
				if (t.getCloudService() instanceof IaaS &&
						// and it has more than one replica
						((IaaS) t.getCloudService()).getReplicas() > 1)
					// add it to the list of resources that can be scaled in
					resMemory.add(t);

			vettResTot.add(resMemory);
		}
		return vettResTot;
	}

	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}

	public Logger2JFreeChartImage getConstraintsLogger() {
		return logConstraints;
	}

	public Logger2JFreeChartImage getCostLogger() {
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

	public Logger2JFreeChartImage getVMLogger() {
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
	 */
	protected void InternalOptimization(Solution sol) {

		/*
		 * 
		 * Innanzitutto dobbiamo implementare almeno 2 spazi di ricerca. Ogni
		 * spazio � definito da una mossa specifica la mossa 1 consiste
		 * nell'aumentare o diminuire in numero di repliche di una certa risorsa
		 * cloud (tipicamente saranno VM) la mossa 2 consiste nel ribilanciare
		 * gli arrival rates tra i vari provider.
		 * 
		 * Dobbiamo chiederci quindi: 1) con quale forza applichiamo una certa
		 * mossa. (es. di quanto aumentiamo o diminuiamo il numero di VM) 2) in
		 * che ordine eseguiamo le mosse 1 e 2. (potremmo per esempio
		 * implementare una roulette e assegnare una certa propriet� p ad una
		 * mossa e una propriet� 1-p all'altra)
		 * 
		 * -Si potrebbe cercare di stimare l'impatto delle mosse e decidere
		 * quale attuare. -Si deve cercare di capire dove attuare quando c'� un
		 * vincolo che non � soddisfatto.
		 * 
		 * - ragionando alla lavagna con gibbo ci siamo resi conto che una mossa
		 * di bilanciamento ha gli stessi effetti di una di variazione. per
		 * questo non � necessario far seguire ad una mossa di bilanciamento una
		 * serie di mosse di variazione per far assestare i risultati.
		 */

		// if the solution is unfeasible there is a first fase in which the
		// solution is forced to became feasible

		optimLogger.trace("feasibility phase");
		makeFeasible(sol);
		// optimLogger.trace("feasible solution: "+sol.showStatus());
		// If the current solution is better than the best one it becomes the
		// new best solution.
		updateBestSolution(sol);
		updateLocalBestSolution(sol);

		optimLogger.info("costReduction phase");
		descentOptimize(sol);
		// optimLogger.trace("optimized solution"+sol.showStatus());

	}

	protected void InternalOptimization(SolutionMulti sol) {
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

	protected void IteratedRandomScaleInLS(Solution sol) {
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
						if (((IaaS) vettResTot.get(i).get(j).getCloudService())
								.getReplicas() == 1)
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


				evalServer.EvaluateSolution(sol);
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
	protected void done(){ 		
		evalServer.terminateServer();
		try {
			get();
		} catch (InterruptedException e) {
			logger.error("Interrupted ending of optimization engine",e);			
		} catch (ExecutionException e) {
			logger.error("Execution exception occurred in the optimization engine",e);
		} catch (CancellationException e){
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
	public void loadInitialSolution() throws ParserConfigurationException,
	SAXException, IOException, JAXBException {
		loadInitialSolution(null,null);
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
	public void loadInitialSolution(File generatedInitialSolution,
			File generatedInitialMce) throws ParserConfigurationException,
			SAXException, IOException, JAXBException {
		// initialSolution = new Solution();
		this.initialSolution = new SolutionMulti();

		// parse the extension file
		ResourceEnvironmentExtensionParser resourceEnvParser = new ResourceEnvironmentExtentionLoader(Paths.get(Configuration.RESOURCE_ENVIRONMENT_EXTENSION).toFile());
		UsageModelExtensionParser usageModelParser = new UsageModelExtensionLoader(Paths.get(Configuration.USAGE_MODEL_EXTENSION).toFile());

		// get the PCM from the launch configuration
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = org.eclipse.core.runtime.Path.fromOSString(Paths.get(
				Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY, Configuration.LAUNCH_CONFIG).toString());
		IFile ifile = workspace.getRoot().getFileForLocation(location);
		ILaunchConfiguration launchConfig = DebugPlugin.getDefault()
				.getLaunchManager().getLaunchConfiguration(ifile);

		@SuppressWarnings("deprecation")
		PCMInstance pcm = new PCMInstance(launchConfig);
		// Since we are working on a single cloud solution a hourly solution
		// is just an instance (no multi cloud, no load balancer, non
		// exposed component)

		// create a tier for each resource container
		EList<ResourceContainer> resourceContainers = pcm
				.getResourceEnvironment()
				.getResourceContainer_ResourceEnvironment();

		// we need to create a Solution object for each one of the providers!
		ArrayList<String> providers = new ArrayList<String>();
		for (String s : resourceEnvParser.getProviders().values()) {
			if (s != null && !providers.contains(s))
				providers.add(s);
		}

		// if no provider has been selected pick one form the database
		boolean defaultProvider = false;
		if (providers.size() == 0) {
			defaultProvider = true;
			String defaultProviderName = null;
			Iterator<String> iter = dataHandler.getCloudProviders().iterator();

			do {
				defaultProviderName = iter.next();
				// skip the generic provider whose data might not be relevant
				// and those that do not offer Compute services.
				if (defaultProviderName.equals("Generic")
						|| dataHandler.getServices(defaultProviderName,
								"Compute").size() == 0)
					defaultProviderName = null;
			} while (defaultProviderName == null && iter.hasNext());
			if (defaultProviderName == null)
				logger.error("No provider with services of type Compute has been found");
			providers.add(defaultProviderName);
			logger.info("No provider specified in the extension, defaulting on "
					+ defaultProviderName);
		}

		for (String provider : providers) {

			Solution initialSolution = new Solution();

			initialSolution.buildFolderStructure(provider);

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
								provider,
								Configuration.FOLDER_PREFIX + i).toFile()
								.listFiles(new FilenameFilter() {
									@Override
									public boolean accept(File dir, String name) {
										return name.endsWith(".xml");
									}
								});
				// suppose there is just 1 model
				Path lqnModelPath = models[0].toPath();
				application.initLqnHandler(lqnModelPath);

				// add population and think time from usage model extension
				int population = -1;
				double thinktime = -1;
				if (usageModelParser.getPopulations().size() == 1)
					population = usageModelParser.getPopulations().values()
					.iterator().next()[i];
				if (usageModelParser.getThinkTimes().size() == 1)
					thinktime = usageModelParser.getThinkTimes().values()
					.iterator().next()[i];

				double percentage = (double) 1 / providers.size();

				population = (int) Math.ceil(population * percentage);

				for (int hour = 0; hour < 24; ++hour)
					initialSolution.setPercentageWorkload(hour, percentage);

				application.getLqnHandler().setPopulation(population);
				application.getLqnHandler().setThinktime(thinktime);
				application.getLqnHandler().saveToFile();
				application.setWorkload(population);

				// // create a tier for each resource container
				// EList<ResourceContainer> resourceContainers = pcm
				// .getResourceEnvironment()
				// .getResourceContainer_ResourceEnvironment();

				// STEP 1: load the resource environment
				for (ResourceContainer c : resourceContainers) {

					CloudService service = null;
					// switch over the type of cloud service
					// String cloudProvider =
					// resourceEnvParser.getProviders().get(
					// c.getId()); // provider

					// associated
					// to
					// the
					// resource
					String serviceType = resourceEnvParser.getServiceType()
							.get(c.getId() + (defaultProvider ? "" : provider)); // Service
					String resourceSize = resourceEnvParser.getInstanceSize()
							.get(c.getId() + (defaultProvider ? "" : provider));
					String serviceName = resourceEnvParser.getServiceName()
							.get(c.getId() + (defaultProvider ? "" : provider));
					int replicas = resourceEnvParser.getInstanceReplicas().get(
							c.getId() + (defaultProvider ? "" : provider))[i];

					// // pick a cloud provider if not specified by the
					// extension
					// if (cloudProvider == null)
					// cloudProvider =
					// dataHandler.getCloudProviders().iterator()
					// .next();

					// pick a service if not specified by the extension
					if (serviceName == null)
						logger.info("provider: " + provider + " default: "
								+ defaultProvider);
					logger.info("serviceType: " + serviceType);
					for (String st : dataHandler.getServices(provider, // cloudProvider,
							serviceType))
						logger.info("\tService Name: " + st);
					serviceName = dataHandler.getServices(provider, // cloudProvider,
							serviceType).get(0);
					// if the resource size has not been decided pick one
					if (resourceSize == null)
						resourceSize = dataHandler
						.getCloudResourceSizes(provider,/*
						 * cloudProvider,
						 */serviceName)
						 .iterator().next();

					double speed = dataHandler.getProcessingRate(provider, // cloudProvider,
							serviceName, resourceSize);

					int ram = dataHandler.getAmountMemory(provider, // cloudProvider,
							serviceName, resourceSize);

					int numberOfCores = dataHandler.getNumberOfReplicas(
							provider, /* cloudProvider, */serviceName,
							resourceSize);

					/*
					 * each tier has a certain kind of cloud resource and a
					 * number of replicas of that resource
					 */
					Tier t = new Tier(c.getId(), c.getEntityName()
							+ "_CPU_Processor");

					/* creation of a Compute type resource */
					service = new Compute(provider, /* cloudProvider, */
							serviceType, serviceName, resourceSize, replicas,
							numberOfCores, speed, ram);

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
				HashMap<String, Functionality> functionalities = new HashMap<>();
				for (AllocationContext context : allocations) {
					String containerId = context
							.getResourceContainer_AllocationContext().getId();
					RepositoryComponent repositoryComp = context
							.getAssemblyContext_AllocationContext()
							.getEncapsulatedComponent__AssemblyContext();

					// create the component
					Component comp = new Component(repositoryComp.getId());

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

				// use the initial evaluation to initialize parser and
				// structures
				try {
					evalServer.evaluateInstance(application);
				} catch (AnalysisFailureException e) {
					logger.error("Error evaluating an instance",e);
				}
				// initialSolution.showStatus();
			}

			this.initialSolution.add(initialSolution);
		}

		this.initialSolution.setFrom(generatedInitialSolution,
				generatedInitialMce);
		logger.info(this.initialSolution.showStatus());
	}

	public void loadInitialSolutionObject(File file) {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(file);
			in = new ObjectInputStream(fis);
			initialSolution = (SolutionMulti) in.readObject();
			in.close();
			fis.close();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
		}
		logger.info("Deserialized: " + initialSolution);
	}

	/**
	 * Make the solution feseable by performing scale out operations
	 * 
	 * @param sol
	 */
	protected void makeFeasible(Solution sol) {
		final double MAX_FACTOR = 5;
		final double MIN_FACTOR = 1.2;
		numberOfFeasibilityIterations = 0;
		while (!sol.isFeasible() && !isMaxNumberOfFesibilityIterations()) {
			logger.info("\tFeasibility iteration: "
					+ numberOfFeasibilityIterations);
			for (int i = 0; i < 24; i++) {
				// this part can be turned into a multithread section
				// we need a list of all the resources involved
				Map<Constraint, Double> constraintsEvaluation = sol
						.getEvaluation().get(i);
				Set<Tier> tierMemory = new HashSet<>();

				// for each constraint find the IaaS affected resource and add
				// it to the set. Multiple constraints affecting the same
				// resource are ignored since we use a Set as memory
				for (Constraint c : constraintsEvaluation.keySet())
					if (c.hasNumericalRange())
						if (constraintsEvaluation.get(c) > 0)
							tierMemory.add(findResource(sol.getApplication(i),
									c.getResourceID()));
				// this is the list of the
				// resouces that doesn't
				// satisfy the constraints

				// now we will scaleout the resources
				double factor = MAX_FACTOR - (MAX_FACTOR - MIN_FACTOR)
						* numberOfFeasibilityIterations
						/ MAXFEASIBILITYITERATIONS;
				if (tierMemory.size() > 0) {
					MoveOnVM moveVM_i = new MoveOnVM(sol, i);
					for (Tier t : tierMemory)
						moveVM_i.scaleOut(t, factor);
				}

			}
			// it shouldn't be necessary to set the solution to unEvaluated.
			evalServer.EvaluateSolution(sol);
			numberOfFeasibilityIterations += 1;
		}
		// optimLogger.trace(sol.showStatus());
		if (sol.isFeasible())
			logger.info("\n\t Solution made feasible");
		else
			logger.info("Max number of feasibility iterations reached");
	}

	/**
	 * This is the bulk of the optimization process in case of a single provider
	 * problem.
	 * 
	 * @return the integer -1 an error has happened.
	 */
	public Integer optimize() {	

		// 1: check if an initial solution has been set
		if (this.initialSolution == null)
			return -1;
		optimLogger.trace("starting the optimization");
		timer.start();
		timer.split();
		evalServer.EvaluateSolution(initialSolution);// evaluate the current
		// solution
		// initialSolution.showStatus();
		// Debugging constraintHandler

		bestSolution = initialSolution.clone();
		localBestSolution = initialSolution.clone();
		bestSolutionSerieHandler = costLogImage.newSeries("Best Solution");
		localBestSolutionSerieHandler = costLogImage
				.newSeries("Local Best Solution");
		costLogImage.addPoint2Series(localBestSolutionSerieHandler,
				TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
				localBestSolution.getCost());
		costLogImage.addPoint2Series(bestSolutionSerieHandler,
				TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
				bestSolution.getCost());
		logger.warn("" + bestSolution.getCost() + ", 1 " + ", "
				+ bestSolution.isFeasible());

		numberOfIterations = 1;
		currentSolution = initialSolution.clone(); // the best solution is the

		// evalSvr.EvaluateSolution(currentSolution);

		// this is one possibility, I however prefer using the execution time as
		// a stopping criterion. Mich
		boolean solutionChanged = true;
		while (!isMaxNumberOfIterations()) {

			optimLogger.info("Iteration: " + numberOfIterations);
			// optimLogger.trace( currentSolution.showStatus());

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
					optimLogger.info("\tTier: " + t.getName() + " Size: "
							+ longTermFrequencyMemory.get(t.getId()).size());
				}
				localBestSolution = currentSolution.clone();
			}			
			setProgress((numberOfIterations*100/MAX_SCRUMBLE_ITERS));
			// increment the number of iterations
			numberOfIterations += 1;

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

		exportSolution();
		evalServer.showStatistics();		

		// ////
		// evalProxy.showStatistics();
		// System.out.println("End!");
		//
		// try {
		// Files.copy(Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
		// "generated-solution.xml") , Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
		// "solution.xml"));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// ////

		return -1;

	}

	/**
	 * Builds a solution from the less used components in the long term memory
	 * in order to increase diversification
	 * 
	 * @param solMulti
	 *            the corrent solution
	 * @return the changed solution
	 */
	private SolutionMulti longTermMemoryRestart(SolutionMulti solMulti) {
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
	 */
	private Solution longTermMemoryRestart(Solution sol) {

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

			IaaS newRes = null;
			List<IaaS> resources = dataHandler.getSameServiceResource(
					t.getCloudService(), sol.getRegion());
			String newResType = SolutionHelper
					.getResourceNameFromTierTypeID(lowestID);
			for (IaaS res : resources) {
				if (res.getResourceName().equals(newResType)) {
					newRes = res;
					break;
				}
			}
			moveVM.changeMachine(t.getId(), (Compute) newRes);
		}
		evalServer.EvaluateSolution(sol);
		return sol;

	}

	/**
	 * This is the bulk of the optimization process in case of a multi-provider
	 * problem.
	 * 
	 * @return the integer -1 an error has happened.
	 */
	public Integer optimizeMultiprovider() {

		logger.getName();
		// 1: check if an initial solution has been set
		if (this.initialSolution == null)
			return -1;

		timer.start();
		timer.split();
		evalServer.EvaluateSolution(initialSolution);// evaluate the current
		// solution
		// initialSolution.showStatus();
		// Debugging constraintHandler

		logger.info("first evaluation");
		bestSolution = initialSolution.clone();
		bestSolutionSerieHandler = costLogImage.newSeries("Best solution");
		costLogImage.addPoint2Series(bestSolutionSerieHandler,
				TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
				bestSolution.getCost());
		logger.warn("" + bestSolution.getCost() + ", 1 " + ", "
				+ bestSolution.isFeasible());

		numberOfIterations = 1;
		currentSolution = initialSolution.clone(); // the best solution is the

		// evalSvr.EvaluateSolution(currentSolution);

		// this is one possibility, I however prefer using the execution time as
		// a stopping criterion. Mich
		while (!isMaxNumberOfIterations()) {

			logger.info("Iteration: " + numberOfIterations + "cost: "
					+ currentSolution.getCost());
			// 2: Internal Optimization process

			InternalOptimization(currentSolution);

			// 3: check whether the best solution has changed
			// If the current solution is better than the best one it becomes
			// the new best solution.
			updateBestSolution(currentSolution);

			// 3b: clone the best solution to start the scruble from it
			currentSolution = bestSolution.clone();
			// ogSolution(bestSolution); // a dire la verit� questo � un po'
			// restrittivo.

			// 4 Scrambling the current solution.
			scramble(currentSolution);
			setProgress(numberOfIterations/MAX_SCRUMBLE_ITERS);
			// increment the number of iterations
			numberOfIterations += 1;

		}

		try {
			costLogImage.save2png();
			logVm.save2png();
			logConstraints.save2png();
		} catch (IOException e) {
			logger.error("Unable to create charts", e);
		}

		logger.warn(bestSolution.showStatus());
		logger.info(bestSolution.showStatus());
		bestSolution.exportLight(Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.SOLUTION_FILE_NAME));
		bestSolution.exportCSV(Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.SOLUTION_CSV_FILE_NAME));
		evalServer.showStatistics();
		evalServer.terminateServer();

		// //////
		// evalProxy.showStatistics();
		// System.out.println("End!");
		//
		// try {
		// Files.copy(Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
		// "generated-solution.xml") , Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
		// "solution.xml"));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// //////

		return -1;

	}

	protected void resetNoImprovementCounter() {

		this.numIterNoImprov = 0;
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
	 */
	protected boolean scramble(Solution sol) {
		/*
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
		MoveTypeVM moveVM = new MoveTypeVM(sol);
		boolean done = false; // tells if something has changed
		int iterations = 0; // number of iterations without a change in the
		// solution
		scrambleLogger.debug("iteration: " + numberOfIterations);

		// try to performe the change until something has actually changed or we
		// give up
		while (!done && iterations < MAX_SCRAMBLE_NO_CHANGE) {
			// Phase1: Select the resource to change Policy: RANDOM
			List<Tier> tierList = sol.getApplication(0).getTiers();
			Tier selectedTier = tierList.get(random.nextInt(tierList.size()));
			scrambleLogger.debug("Selected Tier:" + selectedTier.getName());
			// Phase2: Retrieve resources that can be exchanged with the current
			// one
			CloudService origRes = selectedTier.getCloudService();
			List<IaaS> resList = dataHandler.getSameServiceResource(origRes,
					sol.getRegion());
			// filter resources according to architectural constraints
			constraintHandler.filterResources(resList, selectedTier);
			// if no resource can substitute the current one increase the number
			// of iterations without a change and try again
			if (resList.size() == 0) {
				scrambleLogger
				.warn("No resource found for scramble after constraint check, iteration: "
						+ iterations);
				iterations++;
				continue;
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
				iterations++;
				continue;
			}

			// Phase3: choose the new resource. Policy: roulette selection
			// 3.1Calculate the cumulative fitness
			double[] cumulativeFitnesses = new double[resList.size()];
			cumulativeFitnesses[0] = getEfficiency(resList.get(0),
					sol.getRegion());
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
			IaaS newRes = resList.get(index);

			// debugging
			scrambleLogger.debug("Sorted Resources");
			for (IaaS res : resList) {
				if (newRes.equals(res))
					scrambleLogger.debug("Index: " + resList.indexOf(res)
							+ " res: " + res.getResourceName() + " value: "
							+ getEfficiency(res, sol.getRegion())
							+ " <--- SELECTED");
				else
					scrambleLogger.debug("Index: " + resList.indexOf(res)
							+ " res:" + res.getResourceName() + " value: "
							+ getEfficiency(res, sol.getRegion()));
			}
			scrambleLogger.debug("Selected index:" + resList.indexOf(newRes));

			// Phase4: performs the change and evaluate the solution
			// add the new configuration in the memory
			moveVM.changeMachine(selectedTier.getId(), (Compute) newRes);
			evalServer.EvaluateSolution(sol);
			tabuSolutionList.put(SolutionHelper.buildSolutionTypeID(sol), null);
			scrambleLogger.debug("Memory size " + tabuSolutionList.size());
			done = true;
		}

		if (iterations >= MAX_SCRAMBLE_NO_CHANGE)
			return false;

		return true;
	}

	/**
	 * Calculates the efficiency of the reousce. The efficiency is calculated by
	 * numberOfCores*speed/*averageCost
	 * 
	 * @param resource
	 * @param region
	 * @return
	 */
	private double getEfficiency(IaaS resource, String region) {
		if (resource instanceof Compute) {
			Compute computeRes = (Compute) resource;
			double cost = evalServer.getCostEvaulator().getResourceAverageCost(
					resource, region);
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
	private List<IaaS> filterByMemory(List<IaaS> resourceList, Solution sol,
			Tier selectedTier) {
		// Filter resources that have been already used

		final String token = "jfhbvwiuahj038h9nvlbv93vbie";
		String solutionTypeID = SolutionHelper.buildSolutionTypeID(sol,
				selectedTier, token);
		List<IaaS> newList = new ArrayList<IaaS>();
		for (CloudService resource : resourceList) {
			// Complete the id generated before
			String newSolID = solutionTypeID.replace(token,
					resource.getResourceName());
			if (!tabuSolutionList.containsKey(newSolID)) {
				newList.add((IaaS) resource);
			} else {
				logger.debug("Memory Hit");
			}
		}
		return newList;

	}

	protected boolean scramble(SolutionMulti sol) {
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
	 * Checks wether the solution is "better" with respect to the current
	 * optimal solution. Better here means that the solution is Evaluated,
	 * Feasible and cost less
	 * 
	 * @param the
	 *            new solution
	 * @return true if sol has become the new best solution
	 */
	protected boolean updateBestSolution(Solution sol) {
		if (sol.greaterThan(bestSolution)) {
			Solution clone = sol.clone();
			bestSolution.add(clone);
			logger.warn("" + bestSolution.getCost() + ", "
					+ TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime())
					+ ", " + bestSolution.isFeasible());
			costLogImage.addPoint2Series(bestSolutionSerieHandler,
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					bestSolution.getCost());
			optimLogger.info("updated best solution");
			sol.setGenerationIteration(numberOfIterations);
			timer.split();
			sol.setGenerationTime(timer.getSplitTime());
			return true;
		}
		return false;
	}

	protected boolean updateLocalBestSolution(Solution sol) {
		if (sol.greaterThan(localBestSolution)) {

			// updating the best solution
			// bestSolution = sol.clone();
			// String filename = bestTmpSol + bestTmpSolIndex+".xml";
			// sol.exportLight(filename);
			localBestSolution.add(sol.clone());
			this.numIterNoImprov = 0;
			logger.warn("" + localBestSolution.getCost() + ", "
					+ TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime())
					+ ", " + localBestSolution.isFeasible());
			costLogImage.addPoint2Series(localBestSolutionSerieHandler,
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					localBestSolution.getCost());
			optimLogger.info("updated local best solution");
			return true;
		}
		return false;
	}

	protected void updateLocalBestSolution(SolutionMulti sol) {
		for (Solution s : sol.getAll())
			updateLocalBestSolution(s);
	}

	protected void updateBestSolution(SolutionMulti sol) {
		for (Solution s : sol.getAll())
			updateBestSolution(s);
	}

	protected void loadConfiguration() {
		MAXFEASIBILITYITERATIONS = Configuration.FEASIBILITY_ITERS;
		MAX_SCRUMBLE_ITERS = Configuration.SCRUMBLE_ITERS;
		MAXMEMORYSIZE = Configuration.TABU_MEMORY_SIZE;
		SELECTION_POLICY = Configuration.SELECTION_POLICY;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getSource().equals(evalServer) && evt.getPropertyName().equals("EvaluationError")){
			logger.error("Optimizaiton ending due to evaluation error");
			cancel(true);
		}
	}

	public void exportSolution() {
		logger.info(bestSolution.showStatus());
		bestSolution.exportLight(Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.SOLUTION_LIGHT_FILE_NAME+Configuration.SOLUTION_FILE_EXTENSION));
		for(Solution sol:bestSolution.getAll())
			sol.exportAsExtension(Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.SOLUTION_FILE_NAME+sol.getProvider()+Configuration.SOLUTION_FILE_EXTENSION));
		bestSolution.exportCSV(Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.SOLUTION_CSV_FILE_NAME));
	}

}
