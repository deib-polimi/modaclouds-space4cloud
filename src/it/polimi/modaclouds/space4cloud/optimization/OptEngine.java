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
import it.polimi.modaclouds.space4cloud.gui.OptimizationConfigurationFrame;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandler;
import it.polimi.modaclouds.space4cloud.optimization.constraints.NumericalRange;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationProxy;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
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

	protected Solution initialSolution = null;

	protected Solution bestSolution = null;

	protected Solution currentSolution = null;

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

	protected Random random = new Random(0);
	protected int numIterNoImprov = 0;

	protected int numTotImpr = 0;
	protected StopWatch timer = new StopWatch();

	protected EvaluationProxy evalProxy;

	protected Logger logger = LoggerHelper.getLogger(OptEngine.class);
	protected Logger optimLogger = LoggerFactory.getLogger("optimLogger");


	protected Logger2JFreeChartImage costLogImage;

	protected Logger2JFreeChartImage logVm;
	protected Logger2JFreeChartImage logConstraints;
	/**
	 * Instantiates a new opt engine.
	 * 
	 * @param handler
	 *            : the constraint handler
	 */
	public OptEngine(ConstraintHandler handler) {

		try {
			costLogImage = new Logger2JFreeChartImage();
			logVm = new Logger2JFreeChartImage("vmCount.properties");
			logConstraints = new Logger2JFreeChartImage(
					"constraints.properties");
		} catch (NumberFormatException | IOException e) {
			logger.error("Unable to create chart loggers", e);

		}

		loadConfiguration(Constants.CONFIGURATIONGUI); //false = show gui, true = batch mode

		logger.info("Running the optimization with parameters:");
		logger.info("Max Memory Size: "+MAXMEMORYSIZE);
		logger.info("Max Iterations: "+MAXITERATIONS);
		logger.info("Max Feasibility Iterations: "+MAXFEASIBILITYITERATIONS);
		logger.info("Selection Policy: "+SELECTION_POLICY);
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

	}

	private void loadConfiguration(boolean showgui) {

		OptimizationConfigurationFrame optLoader = new OptimizationConfigurationFrame();
		//set the default configuration file
		optLoader.setPreferenceFile("/config/OptEngine.properties");		

		if(showgui){
			//show the frame and ask let the user interact
			optLoader.setVisible(true);
			while(!optLoader.isSaved())
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error("Error in loading the configuration",e);
				}
		}
		MAXMEMORYSIZE = optLoader.getMaxMemorySize();
		MAXITERATIONS = optLoader.getMaxIterations();
		MAXFEASIBILITYITERATIONS = optLoader.getMaxFeasIter();
		SELECTION_POLICY = optLoader.getPolicy();


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
	protected IaaS findResource(Instance application, String id, SelectionPolicies policy) {

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
			else if (policy  == SelectionPolicies.LONGEST) {
				selectedFun = functionalityChain.get(0);
				for (Functionality f : functionalityChain)
					if (f.getResponseTime() > selectedFun.getResponseTime())
						selectedFun = f;
			}

			// the functionality whose resource has higher utilization
			else if (policy ==SelectionPolicies.UTILIZATION) {
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

	public Solution getInitialSolution() {
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
		 * spazio è definito da una mossa specifica la mossa 1 consiste
		 * nell'aumentare o diminuire in numero di repliche di una certa risorsa
		 * cloud (tipicamente saranno VM) la mossa 2 consiste nel ribilanciare
		 * gli arrival rates tra i vari provider.
		 * 
		 * Dobbiamo chiederci quindi: 1) con quale forza applichiamo una certa
		 * mossa. (es. di quanto aumentiamo o diminuiamo il numero di VM) 2) in
		 * che ordine eseguiamo le mosse 1 e 2. (potremmo per esempio
		 * implementare una roulette e assegnare una certa proprietà p ad una
		 * mossa e una proprietà 1-p all'altra)
		 * 
		 * -Si potrebbe cercare di stimare l'impatto delle mosse e decidere
		 * quale attuare. -Si deve cercare di capire dove attuare quando c'è un
		 * vincolo che non è soddisfatto.
		 * 
		 * - ragionando alla lavagna con gibbo ci siamo resi conto che una mossa
		 * di bilanciamento ha gli stessi effetti di una di variazione. per
		 * questo non è necessario far seguire ad una mossa di bilanciamento una
		 * serie di mosse di variazione per far assestare i risultati.
		 */

		// if the solution is unfeasible there is a first fase in which the
		// solution is forced to became feasible

		optimLogger.trace("feasibility phase");
		makeFeasible(sol);
		optimLogger.trace("feasible solution: "+sol.showStatus());
		// If the current solution is better than the best one it becomes the
		// new best solution.
		updateBestSolution(sol);

		optimLogger.info("costReduction phase");
		descentOptimize(sol);
		optimLogger.trace("optimized solution"+sol.showStatus());

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
		initialSolution = new Solution();
		// parse the extension file		
		ResourceEnvironmentExtensionParser resourceEnvParser= new ResourceEnvironmentExtentionLoader(
				resourceEnvExtension);
		UsageModelExtensionParser usageModelParser = new UsageModelExtensionLoader(
				usageModelExtension);

		// set the region
		initialSolution.setRegion(resourceEnvParser.getRegion());

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

		for (int i = 0; i < 24; i++) {
			logger.info("Initializing hour " + i);
			Instance application = new Instance();
			initialSolution.addApplication(application);
			File[] models = Paths
					.get(c.ABSOLUTE_WORKING_DIRECTORY,
							c.PERFORMANCE_RESULTS_FOLDER, c.FOLDER_PREFIX + i)
							.toFile().listFiles(new FilenameFilter() {
								@Override
								public boolean accept(File dir, String name) {
									// TODO Auto-generated method stub
									return name.endsWith(".xml");
								}
							});
			// suppose there is just 1 model
			Path lqnModelPath = models[0].toPath();
			application.initLqnHandler(lqnModelPath);

			// add population and think time from usage model extension considering a single usage scenario
			int population = -1;
			double thinktime = -1;
			if (usageModelParser.getPopulations().size() == 1)
				population = usageModelParser.getPopulations().values()
				.iterator().next()[i];
			if (usageModelParser.getThinkTimes().size() == 1)
				thinktime = usageModelParser.getThinkTimes().values()
				.iterator().next()[i];
			application.getLqnHandler().setPopulation(population);
			application.getLqnHandler().setThinktime(thinktime);
			application.setWorkload(population);
			application.getLqnHandler().saveToFile();


			// create a tier for each resource container
			EList<ResourceContainer> resourceContainers = pcm
					.getResourceEnvironment()
					.getResourceContainer_ResourceEnvironment();

			// STEP 1: load the resource environment
			for (ResourceContainer c : resourceContainers) {

				CloudService service = null;
				// switch over the type of cloud service
				String cloudProvider = resourceEnvParser.getProviders().get(
						c.getId()); // provider
				// associated
				// to
				// the
				// resource
				String serviceType = resourceEnvParser.getServiceType().get(
						c.getId()); // Service
				String resourceSize = resourceEnvParser.getInstanceSize().get(
						c.getId());
				String serviceName = resourceEnvParser.getServiceName().get(
						c.getId());
				int replicas = resourceEnvParser.getInstanceReplicas().get(
						c.getId())[i];

				// pick a cloud provider if not specified by the extension
				if (cloudProvider == null)
					cloudProvider = dataHandler.getCloudProviders().iterator()
					.next();

				// pick a service if not specified by the extension
				if (serviceName == null)
					serviceName = dataHandler.getServices(cloudProvider,
							serviceType).get(0);
				// if the resource size has not been decided pick one
				if (resourceSize == null)
					resourceSize = dataHandler
					.getCloudResourceSizes(cloudProvider, serviceName)
					.iterator().next();

				double speed = dataHandler.getProcessingRate(cloudProvider,
						serviceName, resourceSize);

				int ram = dataHandler.getAmountMemory(cloudProvider,
						serviceName, resourceSize);

				int numberOfCores = dataHandler.getNumberOfReplicas(
						cloudProvider, serviceName, resourceSize);

				/*
				 * each tier has a certain kind of cloud resource and a number
				 * of replicas of that resource
				 */
				Tier t = new Tier();

				/* creation of a Compute type resource */
				service = new Compute(c.getEntityName() + "_CPU_Processor",
						c.getId(), cloudProvider, serviceType, serviceName,
						resourceSize, replicas, numberOfCores, speed, ram);

				t.setService(service);

				application.addTier(t);

			}

			// STEP 2: parse the usage model to get the reference between calls
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
					String signatureID = s.getDescribedService__SEFF().getId();
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
					functionalities.put(((Entity) s.getDescribedService__SEFF()
							.eContainer()).getEntityName()
							+ "_"
							+ s.getDescribedService__SEFF().getEntityName(),
							function);
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

			// use the initial evaluation to initialize parser and structures
			evalProxy.evaluateInstance(application, c.SOLVER);
			// initialSolution.showStatus();
		}
	}

	public void loadInitialSolutionObject(File file) {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(file);
			in = new ObjectInputStream(fis);
			initialSolution = (Solution) in.readObject();
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
					if (c.getRange() instanceof NumericalRange)
						if (constraintsEvaluation.get(c) > 0)
							resMemory.add(findResource(sol.getApplication(i),
									c.getResourceID()));
				// this is the list of the
				// resouces that doesn't
				// satisfy the constraints

				// now we will scaleout the resources
				double factor = MAX_FACTOR - (MAX_FACTOR - MIN_FACTOR)
						* numberOfFeasibilityIterations / MAXITERATIONS;
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
		optimLogger.trace(sol.showStatus());
		if (sol.isFeasible())
			logger.info("\n\t Solution made feasible");
		else
			logger.info("Max number of feasibility iterations reached");
	}

	/**
	 * This is the bulk of the optimization process.
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
			setProgress(numberOfIterations);
			optimLogger.info("Iteration: " + numberOfIterations + "solution: "+ currentSolution.showStatus());
			
			// 2: Internal Optimization process
			
			InternalOptimization(currentSolution);

			// 3: check whether the best solution has changed
			// If the current solution is better than the best one it becomes
			// the new best solution.
			updateBestSolution(currentSolution);

			// 3b: clone the best solution to start the scruble from it
			currentSolution = bestSolution.clone();
			// ogSolution(bestSolution); // a dire la verità questo è un po'
			// restrittivo.

			// 4 Scrambling the current solution.
			scramble(currentSolution);

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

		return -1;

	}

	protected void resetNoImprovementCounter() {

		this.numIterNoImprov = 0;
	}

	/**
	 * Scramble. the aim of this method is to change some the type of some VM,
	 * let's say a 5-10% of them and re-optimize by means of the mathematical
	 * model.
	 * 
	 * @param the
	 *            current solution
	 */
	protected void scramble(Solution sol) {
		/*
		 * Se non abbiamo le cose del russo a disposizione questa parte si fa +
		 * complicata. diventa una specie di ricerca locale/mutation. dobbiamo
		 * modificare il tipo di alcune macchine virtuali possiamo scegliere
		 * macchine + potenti se i vincoli non sono soddisfatti e macchine meno
		 * potenti altrimenti.
		 */

		// the rationale is to borrow some ideas from tabu methods
		// as for example the use of a memory structure.

		// let's select the resource to change.
		List<Tier> tierList = sol.getApplication(0).getTiers();

		MoveTypeVM moveVM = new MoveTypeVM(sol); /* the move */

		boolean done = false;
		int memoryHit = 0;
		while (!done) {
			Tier selectedTier = (Tier) tierList.get(random.nextInt(tierList
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
	public void setInitialSolution(Solution initialSolution) {
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
	 * 
	 */
	protected void updateBestSolution(Solution sol) {		
		if (sol.greaterThan(bestSolution)) {

			// updating the best solution
			bestSolution = sol.clone();
			this.numIterNoImprov = 0;
			this.numTotImpr += 1;
			logger.warn("" + bestSolution.getCost() + ", "
					+ TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime())
					+ ", " + bestSolution.isFeasible());
			costLogImage.addPoint2Series(seriesHandler,
					TimeUnit.MILLISECONDS.toSeconds(timer.getSplitTime()),
					bestSolution.getCost());
			logger.info("updated best solution"+sol.showStatus());

		} else {
			this.numIterNoImprov += 1;
		}

	}

}
