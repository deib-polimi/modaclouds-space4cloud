package it.polimi.modaclouds.space4cloud.optimization;

import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.mainProgram.Space4Cloud;
import it.polimi.modaclouds.space4cloud.optimization.evaluation.EvaluationServer;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.UsageModelExtensionParser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class PrivateCloud {
	private int cpus; 			// number
	private double cpuPower; 	// MHz
	private int ram; 			// MB
	private int storage; 		// GB
	
	private Solution solution;
	private double[] rates;
	
	private static final Logger logger = LoggerFactory.getLogger(PrivateCloud.class);
	
	private DataHandler dataHandler;
//	private ConstraintHandler constraintHandler;
	private SolutionMulti solutionMulti;
	private EvaluationServer evalServer;
	
	// As a first approximation, we'll consider the Amazon instance sizes.
	private final static String PROVIDER = "Amazon";
	private final static String SERVICE_TYPE ="Compute";
	private final static String SERVICE_NAME = "Elastic Compute Cloud (EC2)";
	private final static String REGION = "eu-ireland";
	
	public static Solution getSolution(SolutionMulti solutionMulti, DataHandler dataHandler, EvaluationServer evalServer) {
		PrivateCloud pc = new PrivateCloud(solutionMulti, dataHandler, evalServer,
				Configuration.PRIVATE_CPUS, Configuration.PRIVATE_CPUPOWER, Configuration.PRIVATE_RAM, Configuration.PRIVATE_STORAGE);
		
		Solution s = pc.getSolution();
		
		return s;
	}
	
	public PrivateCloud(SolutionMulti solutionMulti,
			/*ConstraintHandler constraintHandler,*/ DataHandler dataHandler, EvaluationServer evalServer,
			int cpus, double cpuPower, int ram, int storage) {
		this.cpus = cpus;
		this.cpuPower = cpuPower;
		this.ram = ram;
		this.storage = storage;
		logger.info("Private cloud with %d cpus of %d MHz each, with %d MB of RAM and %d GB of storage initialized.", cpus, cpuPower, ram, storage);
		
		this.solutionMulti = solutionMulti;
		this.dataHandler = dataHandler;
//		this.constraintHandler = constraintHandler;
		this.evalServer = evalServer;
		
		reset();
	}
	
	public void reset() {
		solution = null;
	}
	
	private void initSolution() throws IOException, ParserConfigurationException, SAXException, JAXBException {
		Solution origSolution = solutionMulti.get(PROVIDER);
		
		String fakeProvider = "PrivateCloud";
		
		if (origSolution == null)
			origSolution = solutionMulti.get(0);
		
//		solution = new Solution();
		
		solution = origSolution.clone();
		
		String resourceSize = dataHandler.getCloudResourceSizes(PROVIDER, SERVICE_NAME).iterator().next();
		solution.setRegion(REGION);
		
		solution.buildFolderStructure(fakeProvider);
		
		rates = new double[24];
		
		UsageModelExtensionParser usageModelParser = new UsageModelExtensionLoader(Paths.get(Configuration.USAGE_MODEL_EXTENSION).toFile());
		
		for (int hour = 0; hour < 24; ++hour) {
//			Instance application = new Instance();
//			solution.addApplication(application);
			
			Instance application = solution.getApplication(hour);
			
			File[] models = Paths
					.get(Configuration.PROJECT_BASE_FOLDER, 
							Configuration.WORKING_DIRECTORY, 
							Configuration.PERFORMANCE_RESULTS_FOLDER, 
							fakeProvider,
							Configuration.FOLDER_PREFIX + hour).toFile()
							.listFiles(new FilenameFilter() {
								@Override
								public boolean accept(File dir, String name) {
									return name.endsWith(".xml");
								}
							});
			// suppose there is just 1 model
			Path lqnModelPath = models[0].toPath();
			application.initLqnHandler(lqnModelPath);
			
			Instance origApplication = origSolution.getApplication(hour);
			
			int population = (int)Math.round(origApplication.getWorkload() / origSolution.getPercentageWorkload(hour));
			double thinkTime = -1.0;
			if (usageModelParser.getThinkTimes().size() == 1)
				thinkTime = usageModelParser.getThinkTimes().values()
				.iterator().next()[hour];
			
			application.setWorkload(population);
			application.getLqnHandler().setPopulation(population);
			application.getLqnHandler().setThinktime(thinkTime);
			solution.setPercentageWorkload(hour, 1.0);
			rates[hour] = 1.0;
			
			application.getLqnHandler().saveToFile();
			
			if (!origSolution.getProvider().equals(PROVIDER)) {
				for (Tier origT : origApplication.getTiers()) {
					CloudService origService = origT.getCloudService();
					if (!origService.getServiceType().equals(SERVICE_TYPE))
						continue;
					
					Resource r = new Resource(dataHandler, resourceSize);
					
					double speed = r.getSpeed(); //dataHandler.getProcessingRate(PROVIDER, SERVICE_NAME, resourceSize);
					int ram = r.getRam(); // dataHandler.getAmountMemory(PROVIDER, SERVICE_NAME, resourceSize);
					int numberOfCores = r.getNumberOfCores(); // dataHandler.getNumberOfReplicas(PROVIDER, SERVICE_NAME, resourceSize);
					int replicas = r.getReplicas(); //1;
	
//					Tier t = new Tier(origT.getId(), origT.getName());
					
					Tier t = application.getTierById(origT.getId());
					
					CloudService service = new Compute(PROVIDER, SERVICE_TYPE, SERVICE_NAME, resourceSize, replicas, numberOfCores, speed, ram);
					t.setService(service);
//					application.addTier(t);
					
//					for (Component comp : origT.getComponents())
//						t.addComponent(comp);
					
				}
			}
		}
	}
	
	private class Resource {
		private double resourceSpeed;
		private int resourceRam;
		private int resourceNumberOfCores;
		private int resourceReplicas;
		
		public Resource(DataHandler dataHandler, String resourceSize) {
			resourceSpeed = dataHandler.getProcessingRate(PROVIDER, SERVICE_NAME, resourceSize);
			resourceRam = dataHandler.getAmountMemory(PROVIDER, SERVICE_NAME, resourceSize);
			resourceNumberOfCores = dataHandler.getNumberOfReplicas(PROVIDER, SERVICE_NAME, resourceSize);
			resourceReplicas = 1;
		}
		
		public Resource(Compute compute) {
			resourceSpeed = compute.getSpeed() * compute.getSpeedFactor();
			resourceRam = compute.getRam();
			resourceNumberOfCores = compute.getNumberOfCores();
			resourceReplicas = compute.getReplicas();
		}
		
		public double getTotalSpeed() {
			return resourceSpeed * resourceNumberOfCores * resourceReplicas;
		}
		public int getTotalRam() {
			return resourceRam * resourceReplicas * resourceReplicas;
		}
		
		public double getSpeed() {
			return resourceSpeed;
		}
		public int getNumberOfCores() {
			return resourceNumberOfCores;
		}
		public int getRam() {
			return resourceRam;
		}
		public int getReplicas() {
			return resourceReplicas;
		}
		public void setReplicas(int replicas) {
			if (replicas > 1)
				this.resourceReplicas = replicas;
		}
		
		public double remainingResources() {
			double res = (cpus * cpuPower) - getTotalSpeed() + ram - getTotalRam();
			if (res < 0.0)
				res = -1.0;
			return res;
		}
	}
	
	private int maxNumberOfReplicas(String resourceSize) {
		double totalSpeed[] = new double[24];
		int totalRam[] = new int[24];
		double maxTotalSpeed = Double.MIN_VALUE;
		int maxTotalRam = Integer.MIN_VALUE;
		
		for (int hour = 0; hour < 24; ++hour) {
			Instance app = solution.getApplication(hour);
			totalSpeed[hour] = 0.0;
			totalRam[hour] = 0;
			
			for (Tier t : app.getTiers()) {
//				Compute compute = (Compute)t.getCloudService();
//				
//				totalSpeed[hour] += compute.getReplicas() * compute.getSpeed() * compute.getSpeedFactor() * compute.getNumberOfCores();
//				totalRam[hour] += compute.getReplicas() * compute.getRam();
				
//				Resource r = new Resource((Compute)t.getCloudService());
				Resource r = new Resource(dataHandler, resourceSize);
				
				totalSpeed[hour] += r.getTotalSpeed();
				totalRam[hour] += r.getTotalRam();
			}
			
			if (maxTotalSpeed < totalSpeed[hour])
				maxTotalSpeed = totalSpeed[hour];
			if (maxTotalRam < totalRam[hour])
				maxTotalRam = totalRam[hour];
			
		}
		
		return (int)Math.min(Math.floor(cpus * cpuPower / maxTotalSpeed), Math.floor(ram / maxTotalRam));
	}
	
	private int maxNumberOfReplicas() {
		Tier t = solution.getApplication(0).getTiers().get(0);
		Compute compute = (Compute)t.getCloudService();
		
		return maxNumberOfReplicas(compute.getResourceName());
	}
	
	private final static int MAX_STEPUP_ITERATIONS = 10;
	
	private void fixWorkload() {
		double stepDown = 0.1, stepUp = 0.01;
		boolean goOn = true;
		
		for (int iteration = 1; goOn; ++iteration) {
			evalServer.EvaluateSolution(solution);
			if (solution.isFeasible() || iteration > MAX_STEPUP_ITERATIONS * 2) {
				goOn = false;
				continue;
			}
			
			double[] rates = new double[24];
			for (int i = 0; i < rates.length; ++i)
				rates[i] = 1.0;
			
			for (int hour = 0; hour < 24; ++hour) {
				Instance app = solution.getApplication(hour);
				if (app.isFeasible()) {
					if (iteration <= MAX_STEPUP_ITERATIONS) {
						rates[hour] += stepUp;
						if (rates[hour] > 1.0)
							rates[hour] = 1.0;
					}
				} else {
					rates[hour] -= stepDown;
					if (rates[hour] < 0.0)
						rates[hour] = 0.0;
				}
			}
			
			MoveChangeWorkload move = new MoveChangeWorkload(solution);

			try {
				move.modifyWorkload(rates);
				logger.debug("done!\nThe new values are: ");
				for (Instance i : solution.getApplications())
					logger.debug("%d ", i.getWorkload());

			} catch (ParserConfigurationException | SAXException | IOException
					| JAXBException e) {
				logger.debug("error!\n");
				e.printStackTrace();
				logger.error("Error performing the change of the workload.\n"
						+ e.getMessage());
			}
		}
	}
	
	private void maximizeUsage() throws Exception {
		Instance app = solution.getApplication(0);
		
		IaaS newRes = null;
		List<Tier> tiers = app.getTiers();
			
		for (Tier t : tiers) {
			
			if (newRes == null) {
				List<IaaS> resources = dataHandler.getSameServiceResource(t.getCloudService(), solution.getRegion());
				
				Resource maxR = null;
//				String newResType = null;
				
				List<String> resourceSizes =
						new ArrayList<>(new LinkedHashSet<>(dataHandler.getCloudResourceSizes(PROVIDER, SERVICE_NAME)));
//						dataHandler.getCloudResourceSizes(PROVIDER, SERVICE_NAME);
				for (String resourceSize : resourceSizes) {
					Resource r = new Resource(dataHandler, resourceSize);
					r.setReplicas(maxNumberOfReplicas(resourceSize));
					
					if ((r.remainingResources() > -1.0 && r.getReplicas() >= tiers.size()) &&
							(maxR == null || maxR.remainingResources() > r.remainingResources())) {
//						maxR = r;
//						newResType = resourceSize;
						
						for (IaaS res : resources) {
							if (res.getResourceName().equals(resourceSize)) {
								newRes = res;
								maxR = r;
//								newResType = resourceSize;
								break;
							}
						}
					}
				}
	
//				for (IaaS res : resources) {
//					if (res.getResourceName().equals(newResType)) {
//						newRes = res;
//						break;
//					}
//				}
			}
			
			if (newRes == null)
				throw new Exception("Impossible to find a solution.");
			
			MoveOnVM moveArray[] = new MoveOnVM[24];
			MoveTypeVM moveVM = new MoveTypeVM(solution);
			
			for (int hour = 0; hour < 24; ++hour) {
				for (Tier tier : solution.getApplication(hour).getTiers()) {
					moveVM.changeMachine(tier.getId(), (Compute) newRes);
				}
			}
			
			int replicas = maxNumberOfReplicas();
			
			for (int hour = 0; hour < 24; ++hour) {
				
				moveArray[hour] = new MoveOnVM(solution, hour);
				
//				List<Tier> tiers = solution.getApplication(hour).getTiers();
				int usedReplicas = 0;
				
				for (Tier tier : tiers) {
					int replicasTier = (int)Math.round(replicas / (double)tiers.size());
					
					if (usedReplicas + replicasTier > replicas) {
						replicasTier = replicas - usedReplicas;
						if (replicasTier < 0)
							replicasTier = 0;
					}
					usedReplicas += replicasTier;
					
					moveArray[hour].scale(tier, replicasTier);
				}
			}
		}
	}
	
	public Solution getSolution() {
		if (solution != null)
			return solution;
		
		logger.info("Computing the solution that will be allocated in the private cloud...");
		
		if (solutionMulti.size() < 1) {
			logger.error("The SolutionMulti object has no Solution! Aborting.");
			return null;
		}
		
		int maxPopulation = Space4Cloud.getMaxPopulation(Paths.get(Configuration.USAGE_MODEL_EXTENSION).toFile());
		if (maxPopulation == -1) {
			logger.error("Error met while reading the usage model extension file! Aborting.");
			return null;
		}
		
		try {
			initSolution();
			maximizeUsage();
			fixWorkload();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		logger.info("Solution computed!");
		logger.info(solution.showStatus());
		return solution;
	}
	
	
}