package it.polimi.modaclouds.space4cloud.utils;

import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.lqn.LINEResultParser;
import it.polimi.modaclouds.space4cloud.lqn.LQNSResultParser;
import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationServer;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4clouds.chart.Logger2JFreeChartImage;
import it.polimi.modaclouds.space4clouds.chart.SeriesHandle;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.emf.common.util.EList;

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
import de.uka.ipd.sdq.pcmsolver.runconfig.MessageStrings;

public class SimpleEvaluator {

	protected Constants c = Constants.getInstance();
	protected DataHandler dataHandler = DataHandlerFactory.getHandler();
	protected LqnResultParser parser;
	protected Solution initialSolution;
	private EvaluationServer evalServer = new EvaluationServer();

	private Logger2JFreeChartImage vmLogger;
	private Logger2JFreeChartImage rtLogger;
	private Logger2JFreeChartImage utilLogger;

	public SimpleEvaluator() {

	}
	public void eval(){
		//set the solver to use
		evalServer.setSolver(c.SOLVER);
		evalServer.EvaluateSolution(initialSolution);

	}

	public void loadSolution(File extensionFile) {
		initialSolution = new Solution();
		// parse the resource environment extension file
		ExtensionParser parser = new ExtensionParser(extensionFile);

		// load the 24 instances.
		for (int i = 0; i < 24; i++) {
			//logger.warn("\tLoading hour {}", i);

			// get the PCM from the launch configuration
			Path path = new Path(c.REL_WORKING_DIRECTORY + "/"
					+ c.LAUNCH_CONFIGS_FOLDER + "/hour_" + i + ".launch");
			IFile ifile = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(path);

			ILaunchConfiguration launchConfiguration = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfiguration(ifile);

			@SuppressWarnings("deprecation")
			PCMInstance pcm = new PCMInstance(launchConfiguration);
			// Since we are working on a single cloud solution a hourly solution
			// is just an instance (no multi cloud, no load balancer, non
			// exposed component)
			Instance application = new Instance();
			application.initLqnHandler(launchConfiguration);

			// add population and think time from extension
			application.getLqnHandler().setPopulation(
					parser.getPopulations()[i]);
			application.getLqnHandler().setThinktime(parser.getThinktimes()[i]);
			application.getLqnHandler().saveToFile();
			application.setWorkload(parser.getPopulations()[i]);
			// create a tier for each resource container
			EList<ResourceContainer> resourceContainers = pcm
					.getResourceEnvironment()
					.getResourceContainer_ResourceEnvironment();

			// STEP 1: load the resource environment
			for (ResourceContainer c : resourceContainers) {

				CloudService service = null;
				// switch over the type of cloud service
				String cloudProvider = parser.getProviders().get(c.getId()); // provider
				// associated
				// to
				// the
				// resource
				String serviceType = parser.getServiceType().get(c.getId()); // Service
				String resourceName = parser.getInstanceSize().get(c.getId());
				String serviceName = parser.getServiceName().get(c.getId());				
				int replicas = parser.getInstanceReplicas().get(c.getId())[i];

				double speed = dataHandler.getProcessingRate(cloudProvider,
						serviceName, resourceName);

				int ram = dataHandler.getAmountMemory(cloudProvider,
						serviceName, resourceName);

				int numberOfCores = dataHandler.getNumberOfReplicas(
						cloudProvider, serviceName, resourceName);

				/*
				 * each tier has a certain kind of cloud resource and a number
				 * of replicas of that resource
				 */
				Tier t = new Tier();

				/* creation of a Compute type resource */
				service = new Compute(c.getEntityName() + "_CPU_Processor",
						c.getId(), cloudProvider, serviceType, serviceName,
						resourceName, replicas, numberOfCores, speed, ram);				

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

			//concurrent modification, use a temporary list
			// link the functionalities toghether using their ids

			for (Functionality f : functionalities.values()){
				HashMap<String,Functionality> tempCalls = new HashMap<>();			
				//fill the temporary hashmap
				for (String s : f.getExternalCalls().keySet()) 
					tempCalls.put(functionalities.get(s).getId(), functionalities.get(s));
				//clear the ids in the hashmap of the functionality
				f.getExternalCalls().clear();

				//add the mappings of the temporary hashmap
				f.getExternalCalls().putAll(tempCalls);
			}

			// initialize the constrinable hashmap
			application.initConstrainableResources();
			// add the instance to the solution
			initialSolution.addApplication(application);
			// initialSolution.showStatus();
		}

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
	public void parseResults() {

		//code for the demo

		HashMap<Integer,HashMap<String, Double>> utilizations = new HashMap<>();
		HashMap<Integer,HashMap<String, Double>> responseTimes= new HashMap<>();
		File resultFolder = new File(c.ABSOLUTE_WORKING_DIRECTORY+System.getProperty("file.separator")+c.PERFORMANCE_RESULTS_FOLDER);
		File[] subFolder = resultFolder.listFiles();
		final String resultFileIdentifier;
		LqnResultParser parser; 
		if(c.SOLVER.equals(MessageStrings.LQNS_SOLVER))
			resultFileIdentifier = ".lqxo";
		else
			resultFileIdentifier = "_res.xml";

		//fill the lists and plot the data
		for(File f:subFolder)
			if(f.isDirectory()){			
				String name = f.getName();
				System.out.println("name: "+name);
				String number = name.substring(name.indexOf("hour")+5,name.length());
				int hour = Integer.parseInt(number);				

				File[] resultFiles = f.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {					
						if(name.endsWith(resultFileIdentifier))
							return true;
						return false;
					}
				} );

				System.out.println("Parsing hour: "+hour+" file: "+resultFiles[0].getAbsolutePath());
				if(c.SOLVER.equals(MessageStrings.LQNS_SOLVER))
					parser = new LQNSResultParser(resultFiles[0].getAbsolutePath());			
				else
					parser = new LINEResultParser(resultFiles[0].getAbsolutePath());		
				utilizations.put(hour,parser.getUtilizations());				
				responseTimes.put(hour,parser.getResponseTimes());
			}	

		//init vm Logger
		vmLogger = new Logger2JFreeChartImage("vmCount.properties");
		HashMap<String, SeriesHandle> vmSeriesHandlers = new HashMap<>();
		for(Tier t:initialSolution.getApplication(0).getTiersByResourceName().values())
			if(t.getId().contains("CPU"))
				vmSeriesHandlers.put(t.getId(), vmLogger.newSeries(t.getId()));
		for(int i=0;i<24;i++)
			for(Tier t:initialSolution.getApplication(i).getTiersByResourceName().values())
				if(t.getId().contains("CPU"))
					vmLogger.addPoint2Series(vmSeriesHandlers.get(t.getId())
							,i
							,((IaaS)t.getCloudService()).getReplicas());				


		//init response time logger
		rtLogger = new Logger2JFreeChartImage("responseTime.properties");
		HashMap<String, SeriesHandle> rtSeriesHandlers = new HashMap<>();
		
		ArrayList<String> functionalities = new ArrayList<>();
		 for(Tier t:initialSolution.getApplication(0).getTiersByResourceName().values())
			 for(Component c:t.getComponents())
				 for(Functionality f:c.getFunctionalities())
					 functionalities.add(f.getName());

		for(String s:responseTimes.get(0).keySet())
			if(functionalities.contains(s) || c.SOLVER.equals(MessageStrings.PERFENGINE_SOLVER))
				rtSeriesHandlers.put(s, rtLogger.newSeries(s));

		for(int i=0;i<24;i++){
			System.out.println("responseTime: "+i);
			for(String s:responseTimes.get(i).keySet())
				if(functionalities.contains(s) || c.SOLVER.equals(MessageStrings.PERFENGINE_SOLVER))
					rtLogger.addPoint2Series(rtSeriesHandlers.get(s), i, responseTimes.get(i).get(s));
		}


		//init utilization logger
		utilLogger = new Logger2JFreeChartImage("utilization.properties");
		HashMap<String, SeriesHandle> utilSeriesHandlers = new HashMap<>();

		for(String s:utilizations.get(0).keySet())
			if(s.contains("CPU"))
				utilSeriesHandlers.put(s, utilLogger.newSeries(s));

		for(int i=0;i<24;i++)
			for(String s:utilizations.get(i).keySet())
				if(s.contains("CPU"))
					utilLogger.addPoint2Series(utilSeriesHandlers.get(s), i, utilizations.get(i).get(s));


		System.out.println("Utilizations");
		for(int i=0;i<24;i++){			
			System.out.println("Hour "+i);			
			for(String k:utilizations.get(i).keySet())
				if(k.contains("CPU"))
					System.out.println("\tID: "+k+"\tVal: "+utilizations.get(i).get(k));
		}
		
		System.out.println("Response Times");
		for(int i=0;i<24;i++){			
			System.out.println("Hour "+i);			
			for(String k:responseTimes.get(i).keySet())
				if(k.contains("CPU"))
					System.out.println("\tID: "+k+"\tVal: "+responseTimes.get(i).get(k));
		}

		initialSolution.exportLight(c.ABSOLUTE_WORKING_DIRECTORY+"solution.xml");
	}
	public Logger2JFreeChartImage getVMLogger() {		
		return vmLogger;		
	}

	public Logger2JFreeChartImage getRTLogger() {
		return rtLogger;		
	}

	public Logger2JFreeChartImage getUtilLogger() {
		return utilLogger;		
	}


}
