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
import it.polimi.modaclouds.space4cloud.gui.OptimizationConfigurationFrame;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationProxy;
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
import it.polimi.modaclouds.space4cloud.utils.Constants;
import it.polimi.modaclouds.space4cloud.utils.LoggerHelper;
import it.polimi.modaclouds.space4cloud.utils.ResourceEnvironmentExtensionParser;
import it.polimi.modaclouds.space4cloud.utils.ResourceEnvironmentExtentionLoader;
import it.polimi.modaclouds.space4cloud.utils.UsageModelExtensionParser;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
public class OptEngine extends SwingWorker<Void, Void> {

	private static String bestTmpSol;

	protected SolutionMulti initialSolution = null;
	// protected Solution initialSolution = null;

	protected SolutionMulti bestSolution = null;
	// protected Solution bestSolution = null;

	protected SolutionMulti currentSolution = null;
	// protected Solution currentSolution = null;

	protected ConstraintHandler constraintHandler;

	protected DataHandler dataHandler;

	protected Constants c = Constants.getInstance();

	protected SelectionPolicies SELECTION_POLICY;

	protected int numberOfIterations;

	protected int numberOfFeasibilityIterations;

	protected int MAXMEMORYSIZE = 10;

	protected int MAXITERATIONS = 20; /*
									 * 40 Now it is a constant in the future it
									 * might become a parameter
									 */

	protected int MAXFEASIBILITYITERATIONS = 10; // 20

	/* Memory for the Scramble method */
	protected Cache<String, String> Memory;

	protected SeriesHandle seriesHandler;

	protected Random random = new Random();
	protected int numIterNoImprov = 0;

	protected int numTotImpr = 0;
	protected StopWatch timer = new StopWatch();

	protected EvaluationProxy evalProxy;

	protected Logger logger = LoggerHelper.getLogger(OptEngine.class);
	protected Logger optimLogger = LoggerFactory.getLogger("optimLogger");

	protected Logger2JFreeChartImage costLogImage;

	protected Logger2JFreeChartImage logVm;
	protected Logger2JFreeChartImage logConstraints;

	private int bestTmpSolIndex;

	public OptEngine(ConstraintHandler handler) throws DatabaseConnectionFailureExteption {
		this(handler, false);
	}

	/**
	 * Instantiates a new opt engine.
	 * 
	 * @param handler
	 *            : the constraint handler
	 * @throws DatabaseConnectionFailureExteption 
	 */
	public OptEngine(ConstraintHandler handler, boolean batch) throws DatabaseConnectionFailureExteption {
		this(handler, null, batch);
	}
	/**
	 * Instantiates a new opt engine.
	 * 
	 * @param handler
	 *            : the constraint handler
	 * @throws DatabaseConnectionFailureExteption 
	 */
	public OptEngine(ConstraintHandler handler, File configurationFile, boolean batch) throws DatabaseConnectionFailureExteption {

		try {
			costLogImage = new Logger2JFreeChartImage();
			logVm = new Logger2JFreeChartImage("vmCount.properties");
			logConstraints = new Logger2JFreeChartImage(
					"constraints.properties");
		} catch (NumberFormatException | IOException e) {
			logger.error("Unable to create chart loggers", e);

		}
		
		loadConfiguration(configurationFile, batch); // false = show gui, true = batch mode

		showConfiguration();
		
		Memory = new Cache<>(MAXMEMORYSIZE);
		constraintHandler = handler;

		/* this handle manage the data loaded from the database */
		dataHandler = DataHandlerFactory.getHandler();

		/* this object is a server needed to evaluate the solutions */
		this.evalProxy = new EvaluationProxy(c.SOLVER);
		this.evalProxy.setConstraintHandler(handler);
		this.evalProxy.setLog2png(costLogImage);
		this.evalProxy.setMachineLog(logVm);
		this.evalProxy.setConstraintLog(logConstraints);
		this.evalProxy.setTimer(timer);

		// this.evalProxy.setEnabled(false);
	}

	protected void showConfiguration() {
		logger.info("Running the optimization with parameters:");
		logger.info("Max Memory Size: " + MAXMEMORYSIZE);
		logger.info("Max Scrumble Iterations: " + MAXITERATIONS);
		logger.info("Max Feasibility Iterations: " + MAXFEASIBILITYITERATIONS);
		logger.info("Selection Policy: " + SELECTION_POLICY);
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
		System.out.printf("The hourly values of the workload are: ");
		for (Instance i : sol.getApplications())
			System.out.printf("%d ", i.getWorkload());
		System.out.printf("\nTrying the change them using a rate of %f...",
				rate);

		MoveChangeWorkload move = new MoveChangeWorkload(sol);

		try {
			move.modifyWorkload(new File(c.USAGE_MODEL_EXT_FILE), rate);
			System.out.printf("done!\nThe new values are: ");
			for (Instance i : sol.getApplications())
				System.out.printf("%d ", i.getWorkload());
			System.out.println();

		} catch (ParserConfigurationException | SAXException | IOException
				| JAXBException e) {
			System.out.printf("error!\n");
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
			System.out.printf("The hourly values of the workload for "
					+ sol.getProvider() + " are: ");
			for (Instance i : sol.getApplications())
				System.out.printf("%d ", i.getWorkload());
			System.out.println();

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
				System.out.printf("Hour: %d, Tier ID: %s\n", i, id);

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

					System.out.print("\t" + sol.getProvider() + ": ");
					for (double rate : rates) {
						System.out.print((int) (rate * 100) + " ");
					}
					System.out.println();

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
						System.out.println();

						ratesMap.put(sol.getProvider(), rates);
					}
				}

			}

		}

		for (Solution sol : sols.getAll()) {
			MoveChangeWorkload move = new MoveChangeWorkload(sol);

			try {
				move.modifyWorkload(new File(c.USAGE_MODEL_EXT_FILE),
						ratesMap.get(sol.getProvider()));
				System.out.printf("Done! The new values for "
						+ sol.getProvider() + " are: ");
				for (Instance i : sol.getApplications())
					System.out.printf("%d ", i.getWorkload());
				System.out.println();

			} catch (ParserConfigurationException | SAXException | IOException
					| JAXBException e) {
				System.out.printf("error!\n");
				e.printStackTrace();
				logger.error("Error performing the change of the workload.\n"
						+ e.getMessage());
				// return;
			}
		}

		evalProxy.EvaluateSolution(sols);
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
		evalProxy.EvaluateSolution(initialSolution);
		logger.trace(initialSolution.showStatus());
	}

	protected IaaS findResource(Instance application, String id) {
		return findResource(application, id, SELECTION_POLICY);
	}

	/**
	 * Find the resource to scale given the id of the constraint
	 * 
	 * @param id
	 *            of the resurce in the constraint
	 * @return a IaaS resource on which to perform a scale operation.
	 */
	protected IaaS findResource(Instance application, String id,
			SelectionPolicies policy) {

		IaaS resource = null;
		IConstrainable constrainedResource = application
				.getConstrainableResources().get(id);

		// if the constraint is on a IaaS resource we do nothing
		if (constrainedResource instanceof IaaS)
			resource = (IaaS) constrainedResource;
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
			if (policy == SelectionPolicies.RANDOM)
				selectedFun = functionalityChain.get(new Random()
						.nextInt(functionalityChain.size()));

			// just the first of the list
			else if (policy == SelectionPolicies.RANDOM)
				selectedFun = functionalityChain.get(0);

			// the functionality that takes more time in the chain
			else if (policy == SelectionPolicies.LONGEST) {
				selectedFun = functionalityChain.get(0);
				for (Functionality f : functionalityChain)
					if (f.isEvaluated() && f.getResponseTime() > selectedFun.getResponseTime())
						selectedFun = f;
			}

			// the functionality whose resource has higher utilization
			else if (policy == SelectionPolicies.UTILIZATION) {
				selectedFun = functionalityChain.get(0);
				resource = (IaaS) selectedFun.getContainer().getContainer()
						.getCloudService();
				for (Functionality f : functionalityChain) {
					IaaS affectedRes = (IaaS) f.getContainer().getContainer()
							.getCloudService();
					if (affectedRes.getUtilization() > resource
							.getUtilization()) {
						selectedFun = f;
						resource = (IaaS) selectedFun.getContainer()
								.getContainer().getCloudService();
					}
				}
			}

			// retreive the IaaS resource from the selected functionality
			resource = (IaaS) selectedFun.getContainer().getContainer()
					.getCloudService();

		}

		// if the constraint is on a component
		if (constrainedResource instanceof Component)
			resource = (IaaS) ((Component) constrainedResource).getContainer()
					.getCloudService();

		// We need to find which resource to scale out in case the constraint is
		// not directly on the resource

		return resource;

	}

	public ArrayList<IaaS> findResourceList(Solution sol, String id) {
		ArrayList<IaaS> resultList = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			Instance application = sol.getApplication(i);
			IConstrainable constrainedResource = application
					.getConstrainableResources().get(id);
			IaaS resource = null;
			if (constrainedResource instanceof IaaS)
				resource = (IaaS) constrainedResource;
			// if the constraint is on a functionality we have to build the list
			// of affected components
			else if (constrainedResource instanceof Functionality) {

				constrainedResource = ((Functionality) constrainedResource)
						.getContainer();
			}

			// if it is a component
			if (constrainedResource instanceof Component) {
				for (Tier t : application.getTiersByResourceName().values())
					if (t.getComponents().contains(constrainedResource)) {
						resource = (IaaS) t.getCloudService();
						id = resource.getName();
						break;
					}
			}
			resultList.add(resource);
		}

		return resultList;

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
	protected ArrayList<ArrayList<IaaS>> generateVettResTot(Solution sol) {
		ArrayList<ArrayList<IaaS>> vettResTot = new ArrayList<ArrayList<IaaS>>(
				24);
		// list of lists of Iass

		for (Instance i : sol.getApplications()) {
			ArrayList<IaaS> resMemory = new ArrayList<>();
			for (Tier t : i.getTiers())
				// if the cloud service hostin the application tier is a IaaS
				if (t.getCloudService() instanceof IaaS &&
				// and it has more than one replica
						((IaaS) t.getCloudService()).getReplicas() > 1)
					// add it to the list of resources that can be scaled in
					resMemory.add((IaaS) t.getCloudService());

			vettResTot.add(resMemory);
		}
		return vettResTot;
	}

	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}

	public Logger2JFreeChartImage getConstraintsLogger() {
		// TODO Auto-generated method stub
		return logConstraints;
	}

	public Logger2JFreeChartImage getCostLogger() {
		return costLogImage;
	}

	public EvaluationProxy getEvalProxy() {
		return evalProxy;
	}

	public SolutionMulti getInitialSolution() {
		return initialSolution;
	}

	public int getMaxIterations() {
		return MAXITERATIONS;
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

		if (numberOfIterations <= MAXITERATIONS) {
			return false;
		}
		return true;
	}

	protected void IteratedRandomScaleInLS(Solution sol) {
		MoveOnVM[] moveArray = generateArrayMoveOnVM(sol);

		ArrayList<ArrayList<IaaS>> vettResTot = generateVettResTot(sol);
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
		IaaS res;
		IaaS resArray[] = new IaaS[24];
		while (this.numIterNoImprov < 5) {
			done = false;
			while (!done) {
				res = null;
				boolean noScaleIn = true;
				for (int i = 0; i < 24; i++) {

					for (int j = 0; j < vettResTot.get(i).size(); j++)
						if (vettResTot.get(i).get(j).getReplicas() == 1)
							vettResTot.get(i).remove(j);

					if (vettResTot.get(i).size() > 0) {
						res = vettResTot.get(i).get(
								random.nextInt(vettResTot.get(i).size()));
						moveArray[i].scaleIn(res);
						resArray[i] = res;
						noScaleIn = false;
					} else
						resArray[i] = null;

				}

				// without any scaleIn the solution is stuck
				if (noScaleIn) {
					numIterNoImprov++;
					break;

				}

				evalProxy.EvaluateSolution(sol);
				updateBestSolution(sol);

				if (!sol.isFeasible()) {
					int totSize = 0;

					for (int i = 0; i < 24; i++) {
						if (!sol.getApplication(i).isFeasible()) {
							moveArray[i].scaleOut(resArray[i]);
							vettResTot.get(i).remove(resArray[i]);
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

	protected void loadConfiguration(File configurationFile, boolean batch) {

		OptimizationConfigurationFrame optLoader = new OptimizationConfigurationFrame();
		// set the default configuration file
		if(configurationFile!= null)
			optLoader.setPreferenceFile(configurationFile.getAbsolutePath());
		else
			optLoader.setPreferenceFile("/config/OptEngine.properties");

		if (!batch) {
			// show the frame and ask let the user interact
			optLoader.setVisible(true);
			while (!optLoader.isSaved())
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error("Error in loading the configuration", e);
				}
		}
		MAXMEMORYSIZE = optLoader.getMaxMemorySize();
		MAXITERATIONS = optLoader.getMaxIterations();
		MAXFEASIBILITYITERATIONS = optLoader.getMaxFeasIter();
		SELECTION_POLICY = optLoader.getPolicy();

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
    public void loadInitialSolution(File resourceEnvExtension,
            File usageModelExtension) throws ParserConfigurationException,
            SAXException, IOException, JAXBException {
        loadInitialSolution(resourceEnvExtension, usageModelExtension, null, null);
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
	public void loadInitialSolution(File resourceEnvExtension,
			File usageModelExtension, File generatedInitialSolution,
			File generatedInitialMce) throws ParserConfigurationException,
			SAXException, IOException, JAXBException {
		// initialSolution = new Solution();
		this.initialSolution = new SolutionMulti();

		// parse the extension file
		ResourceEnvironmentExtensionParser resourceEnvParser = new ResourceEnvironmentExtentionLoader(
				resourceEnvExtension);
		UsageModelExtensionParser usageModelParser = new UsageModelExtensionLoader(
				usageModelExtension);

		// get the PCM from the launch configuration
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = org.eclipse.core.runtime.Path.fromOSString(Paths.get(
				c.ABSOLUTE_WORKING_DIRECTORY, c.LAUNCH_CONFIG).toString());
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
            if (s!= null && !providers.contains(s))
                providers.add(s);
        }
        
        //if no provider has been selected pick one form the database
        boolean defaultProvider = false;
        if(providers.size() == 0){
        	defaultProvider = true;
        	String defaultProviderName=null;
        	Iterator<String> iter = dataHandler.getCloudProviders().iterator();
        	
        	do{
        		defaultProviderName = iter.next();
        		//skip the generic provider whose data might not be relevant and those that do not offer Compute services.
        		if(defaultProviderName.equals("Generic") || dataHandler.getServices(defaultProviderName, "Compute").size()==0)
        			defaultProviderName=null;
        	}while(defaultProviderName==null && iter.hasNext());
        	if(defaultProviderName == null)
        		logger.error("No provider with services of type Compute has been found");
        	providers.add(defaultProviderName);
        	logger.info("No provider specified in the extension, defaulting on "+defaultProviderName);
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
						.get(c.ABSOLUTE_WORKING_DIRECTORY,
								c.PERFORMANCE_RESULTS_FOLDER, provider,
								c.FOLDER_PREFIX + i).toFile()
						.listFiles(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								// TODO Auto-generated method stub
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
					String serviceType = resourceEnvParser.getServiceType().get(
							c.getId() + (defaultProvider?"":provider)); // Service
					String resourceSize = resourceEnvParser.getInstanceSize().get(
							c.getId() + (defaultProvider?"":provider));
					String serviceName = resourceEnvParser.getServiceName().get(
							c.getId() + (defaultProvider?"":provider));
					int replicas = resourceEnvParser.getInstanceReplicas().get(
							c.getId() + (defaultProvider?"":provider))[i];

					// // pick a cloud provider if not specified by the
					// extension
					// if (cloudProvider == null)
					// cloudProvider =
					// dataHandler.getCloudProviders().iterator()
					// .next();

					// pick a service if not specified by the extension
                    if (serviceName == null)
                    	logger.info("provider: "+provider+" default: "+defaultProvider);
                    	logger.info("serviceType: "+serviceType);
                    	for(String st:dataHandler.getServices(provider, //cloudProvider,
                                serviceType))
                    		logger.info("\tService Name: "+st);
                        serviceName = dataHandler.getServices(provider, //cloudProvider,
                                serviceType).get(0);
                    // if the resource size has not been decided pick one
                    if (resourceSize == null)
                        resourceSize = dataHandler
                                .getCloudResourceSizes(provider,/* cloudProvider,*/ serviceName)
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
					Tier t = new Tier();

					/* creation of a Compute type resource */
					service = new Compute(c.getEntityName() + "_CPU_Processor",
							c.getId(), provider, /* cloudProvider, */
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
					for (Tier t : application.getTiersByResourceName().values()) {
						if (t.getCloudService().getId().equals(containerId))
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
				evalProxy.evaluateInstance(application, c.SOLVER);
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
				HashSet<IaaS> resMemory = new HashSet<>();

				// for each constraint find the IaaS affected resource and add
				// it to the set. Multiple constraints affecting the same
				// resource are ignored since we use a Set as memory
				for (Constraint c : constraintsEvaluation.keySet())
					if (c.hasNumericalRange())
						if (constraintsEvaluation.get(c) > 0)
							resMemory.add(findResource(sol.getApplication(i),
									c.getResourceID()));
				// this is the list of the
				// resouces that doesn't
				// satisfy the constraints

				// now we will scaleout the resources
				double factor = MAX_FACTOR - (MAX_FACTOR - MIN_FACTOR)
						* numberOfFeasibilityIterations / MAXFEASIBILITYITERATIONS;
				if (resMemory.size() > 0) {
					MoveOnVM moveVM_i = new MoveOnVM(sol, i);
					for (IaaS res : resMemory)
						moveVM_i.scaleOut(res, factor);
				}

			}
			// it shouldn't be necessary to to set the solution to unEvaluated.
			evalProxy.EvaluateSolution(sol);
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
		bestTmpSol = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY + "OptimalSolutionTraces"+"OptimSol").toString();
		optimLogger.trace("starting the optimization");
		timer.start();
		timer.split();
		evalProxy.EvaluateSolution(initialSolution);// evaluate the current
		// solution
		// initialSolution.showStatus();
		// Debugging constraintHandler

		bestSolution = initialSolution.clone();
		seriesHandler = costLogImage.newSeries("Best solution");
		costLogImage.addPoint2Series(seriesHandler,
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
			
			
			optimLogger.info("Iteration: " + numberOfIterations);
			//optimLogger.trace( currentSolution.showStatus());

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
			setProgress(numberOfIterations);
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

		logger.info(bestSolution.showStatus());
		bestSolution.exportLight(c.ABSOLUTE_WORKING_DIRECTORY + "solution.xml");
		bestSolution.exportCSV(c.ABSOLUTE_WORKING_DIRECTORY + "results.csv");
		evalProxy.showStatistics();
		evalProxy.terminateServer();

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
		evalProxy.EvaluateSolution(initialSolution);// evaluate the current
		// solution
		// initialSolution.showStatus();
		// Debugging constraintHandler

		logger.info("first evaluation");
		bestSolution = initialSolution.clone();
		seriesHandler = costLogImage.newSeries("Best solution");
		costLogImage.addPoint2Series(seriesHandler,
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
			setProgress(numberOfIterations);
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
		bestSolution.exportLight(c.ABSOLUTE_WORKING_DIRECTORY + "solution.xml");
		bestSolution.exportCSV(c.ABSOLUTE_WORKING_DIRECTORY + "results.csv");
		evalProxy.showStatistics();
		evalProxy.terminateServer();

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
	 * Changes the type of virtual machines used. 
	 * This change is more distruptive than acting on the number of virtual machines and the optimal type of VM is harder to find 
	 * This move does not preserve the feasibility of the solution. 
	 * 
	 * @param the
	 *            current solution
	 */
	protected void scramble(Solution sol) {
		/*
		 * TODO:
		 * 1) Choose the candidate tier for the VM change (possible strategies: RANDOM, cost)
		 * 2) Retreive all the VM types that can substitute the current vm (Same provider/ region / service type and fulfilling architectural constraints)
		 * 3) Choose the new type of VM to use. (possible strategies: RANDOM, efficiency=cores*processingrate/cost, randomDistribution(efficiency)
		 * 		The proposed strategy uses a random variable whose probability distribution depends on the efficiency of the machines so to prefer machines that have a higher core*processingrate/cost
		 * 4) Change the machine and invalidate the solution. 
		 */

		// let's select the resource to change.
		List<Tier> tierList = sol.getApplication(0).getTiers();

		MoveTypeVM moveVM = new MoveTypeVM(sol); /* the move */

		boolean done = false;
		int memoryHit = 0;
		while (!done) {
			Tier selectedTier = tierList.get(random.nextInt(tierList
					.size()));
			CloudService origRes = selectedTier.getCloudService();
			List<IaaS> resList = dataHandler.getSameServiceResource(origRes,
					sol.getRegion());
			constraintHandler.filterResources(resList, origRes);
			if (resList.size() == 0) {
				done = true;
				logger.warn("No resource found for scramble");
			}
			CloudService newRes = resList.get(random.nextInt(resList.size()));

			if (!Memory.containsKey(origRes.getId() + newRes.getId())) {
				Memory.put(origRes.getId() + newRes.getResourceName(), "MOVE");
				moveVM.changeMachine(origRes.getId(), (Compute) newRes);
				evalProxy.EvaluateSolution(sol);
				done = true;
				System.out.printf("\t\t\t Memory size %s \n", Memory.size());
				memoryHit++;
			}
			if (memoryHit >= 200) {
				done = true;
				logger.warn("scramble memory hitted 200 times, skipping move");
			}
		}

	}

	protected void scramble(SolutionMulti sol) {
		for (Solution s : sol.getAll())
			scramble(s);
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

	// now this LS is not used
	protected void UniformScaleInLS(Solution sol) {
		MoveOnVM[] moveArray = generateArrayMoveOnVM(sol);

		ArrayList<ArrayList<IaaS>> vettResTot = generateVettResTot(sol);
		/* first phase: overall descent optimization */
		boolean done = false;
		while (!done) {
			// let's decrease the number of machine for each resource of each
			// hour
			int numUnFeasibles = 0;
			for (int i = 0; i < 24; i++) {
				if (sol.getApplication(i).isFeasible()
						&& vettResTot.get(i).size() != 0) {

					// reduction of unity the nuber of resources associated to
					// each tiers
					for (int j = 0; j < vettResTot.get(i).size(); j++) {
						if (vettResTot.get(i).get(j).getReplicas() > 1)
							moveArray[i].scaleIn(vettResTot.get(i).get(j));

					}
				} else
					numUnFeasibles += 1; // don't do anything just increase the
				// counter
			}
			if (numUnFeasibles == 24)
				done = true;
			else {
				evalProxy.EvaluateSolution(sol); // evaluate
				vettResTot = generateVettResTot(sol);
			}
		}
		// logger.warn("Descent Optimization first phase - minimized");
		// for(Instance i:sol.getApplications())
		// if(!i.isFeasible())
		// logger.warn("\thour: "+sol.getApplications().indexOf(i)+" violated constraints: "+i.getNumerOfViolatedConstraints());

	}

	
	/**
	 * Checks wether the solution is "better" with respect to the current optimal solution. 
	 * Better here means that the solution is Evaluated, Feasible and cost less 
	 * @param the new solution
	 * @return true if sol has become the new best solution
	 */
	protected boolean updateBestSolution(Solution sol) {
		if (sol.greaterThan(bestSolution)) {

			// updating the best solution
			// bestSolution = sol.clone();
			bestTmpSolIndex++;
//			String filename = bestTmpSol + bestTmpSolIndex+".xml";			
//			sol.exportLight(filename);
			bestSolution.add(sol.clone());
			this.numIterNoImprov = 0;
			this.numTotImpr += 1;
			logger.warn("" + bestSolution.getCost() + ", "
					+ TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime())
					+ ", " + bestSolution.isFeasible());
			costLogImage.addPoint2Series(seriesHandler,
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					bestSolution.getCost());
			//logger.info("updated best solution" + sol.showStatus());
			return true;
		}
		return false;
		

	}

	protected void updateBestSolution(SolutionMulti sol) {
		for (Solution s : sol.getAll())
			updateBestSolution(s);
	}

}
