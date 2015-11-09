package it.polimi.modaclouds.space4cloud.optimization.bursting;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import it.polimi.modaclouds.resourcemodel.cloud.CloudProvider;
import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.space4cloud.db.DataHandler;
import it.polimi.modaclouds.space4cloud.db.DataHandlerFactory;
import it.polimi.modaclouds.space4cloud.db.DatabaseConnectionFailureExteption;
import it.polimi.modaclouds.space4cloud.optimization.MoveChangeWorkload;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SolutionMulti;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.UsageModelExtensionParser;

public class PrivateCloud implements CloudProvider {
	
	public List<Host> owns;
	
	private SolutionMulti solution;
	
	private static final Logger logger = LoggerFactory.getLogger(PrivateCloud.class);
	
	private SolutionMulti startingSolution;
	
	private DataHandler dataHandler;
	
	public static final String BASE_PROVIDER_NAME = "PrivateCloud"; // "PrivateHost";
	
	public static PrivateCloud instance = null;
	
	public static PrivateCloud getInstance() {
		if (instance == null) {
			try {
				return new PrivateCloud(null);
			} catch (Exception e) {
				return null;
			}
		}
		return instance;
	}
	
	public static PrivateCloud getInstance(SolutionMulti solutionMulti) throws DatabaseConnectionFailureExteption {
		if (solutionMulti.usesPaaS()) {
			logger.error("The solution uses PaaS! Exiting the PrivateCloud computation...");
			return null;
		}
		
		instance = new PrivateCloud(solutionMulti);
		
		return instance;
	}
	
	public static SolutionMulti getSolution(SolutionMulti solutionMulti) throws Exception {
		PrivateCloud pc = getInstance(solutionMulti);
		
		if (pc == null)
			return solutionMulti;
		
		logger.info("PrivateCloud initialized.");
		
		SolutionMulti s = pc.getSolution();
		
		return s;
	}
	
	private PrivateCloud(SolutionMulti solutionMulti) throws DatabaseConnectionFailureExteption {
		this.startingSolution = solutionMulti;
		
		this.owns = new ArrayList<Host>();
		
		if (Configuration.USE_PRIVATE_CLOUD) {
			this.owns = Host.getFromFile(new File(Configuration.PRIVATE_CLOUD_HOSTS)); //_TMP));
		}
		
		this.dataHandler = DataHandlerFactory.getHandler();
		
		reset();
		
	}
	
	public void reset() {
		solution = null;
	}
	
	public boolean isEvaluated() {
		return solution != null;
	}
	
	public static final int MAX_ATTEMPTS = 2;
	
	public SolutionMulti getSolution() throws Exception {
		if (solution != null) {
			logger.trace("Solution already computed, returned that.");
			return solution;
		}
		
		logger.info("Computing the solution that will be allocated in the private cloud...");
		
		if (this.startingSolution.size() < 1) {
			logger.error("The SolutionMulti object has no Solution! Aborting.");
			return null;
		} else if (this.startingSolution.size() > 1) {
			
			Solution solutionMax = this.startingSolution.get(0);
			double costMax = solutionMax.getCost();
			
			for (int i = 1; i < this.startingSolution.size(); ++i) {
				Solution s = this.startingSolution.get(i);
				if (s.getCost() > costMax) {
					costMax = s.getCost();
					solutionMax = s;
				}
			}
			
			solution = new SolutionMulti();
			solution.add(solutionMax);
			
		} else {
			solution = this.startingSolution.clone();
		}
		
		Path tempSol = Files.createTempFile("solution", ".xml"), tempConf = Files.createTempFile("conf", ".properties");
		solution.exportLight(tempSol);
		Configuration.saveConfiguration(tempConf.toString());
		
		double[] startingPercentages = new double[24];
		{
			Solution s = solution.get(0);
			for (int h = 0; h < startingPercentages.length; ++h) {
				startingPercentages[h] = s.getPercentageWorkload(h);
			}
		}
		
		List<File> solutionFiles = null;
		boolean done = false;
		
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; ++attempt) {
			it.polimi.modaclouds.space4cloud.privatecloud.PrivateCloud pc =
					new it.polimi.modaclouds.space4cloud.privatecloud.PrivateCloud(tempConf.toString(), tempSol.toString());
			
//			it.polimi.modaclouds.space4cloud.privatecloud.PrivateCloud.removeTempFiles = false;
			
			solutionFiles = pc.getSolutions(Paths.get(Configuration.PROJECT_BASE_FOLDER, Configuration.WORKING_DIRECTORY));
			
			for (int i = 0; i < solutionFiles.size() && !done; ++i) {
				if (SolutionMulti.getCost(solutionFiles.get(i)) > 0) {
					done = true;
					attempt = MAX_ATTEMPTS;
				}
			}
		}
		if (!done) {
			logger.error("Error in computing the solution!");
			reset();
			return null;
		}
		
		solution.setFrom(solutionFiles.get(0), null);
		String startingProvider = solution.get(0).getProvider();
		
//		{
//			Solution s = solution.get(0);
//			int[] hourlyMachines = getTotalHourlyMachines(s);
//			double[] rates = new double[hourlyMachines.length];
//			for (int h = 0; h < hourlyMachines.length; ++h) {
//				rates[h] = ((double)hourlyMachines[h] / startingHourlyMachines[h]) * startingPercentages[h];
////				changeWorkload(s, h, ((double)hourlyMachines[h] / startingHourlyMachines[h]) * startingPercentages[h]);
//			}
//			changeWorkload(s, rates);
//		}
		
		UsageModelExtensionParser usageModelParser = new UsageModelExtensionParser(Paths.get(Configuration.USAGE_MODEL_EXTENSION).toFile());
		
		SolutionMulti tmp = new SolutionMulti();
		tmp.add(solution.get(startingProvider).clone());
		
		for (Solution sol : tmp.getAll()) {
			for (Instance inst : sol.getApplications()) {
				for (Tier tier : inst.getTiers()) {
					IaaS res = (IaaS) tier.getCloudService();
					res.setReplicas(0);
				}
			}
		}
		
		for (int i = 1; i < solutionFiles.size(); ++i) {
			if (SolutionMulti.isEmpty(solutionFiles.get(i)))
				continue;
			
			tmp.addFrom(solutionFiles.get(i));
			
			Host h = owns.get(i - 1);
			
			for (Solution s : tmp.getAll()) {
				s.setRegion(null);
				
//				for (Tier t : s.getApplication(0).getTiers()) {
//					CloudService service = t.getCloudService();
//					
//					CloudResource res = dataHandler.getCloudResource(
//							service.getProvider(),
//							service.getServiceName(),
//							service.getResourceName());
//					
//					h.addResource(res);
//				}
			}
			
			SolutionMulti onCheck = new SolutionMulti();
			onCheck.add(solution.get(startingProvider).clone());
			
			for (Solution sol : onCheck.getAll()) {
				for (Instance inst : sol.getApplications()) {
					for (Tier tier : inst.getTiers()) {
						IaaS res = (IaaS) tier.getCloudService();
						res.setReplicas(0);
					}
				}
			}
			
			onCheck.setFrom(solutionFiles.get(i), null);
			
			for (int hour = 0; hour < 24; ++hour) {
				Instance app = onCheck.get(0).getApplication(hour);
				for (Tier t : app.getTiers()) {
					IaaS res = (IaaS) t.getCloudService();
					boolean hostHasTier = false;
					if (res.getReplicas() > 0) {
						h.isOn.set(hour, true);
						hostHasTier = true;
					}
					if (hostHasTier) {
						CloudResource cloudResource = dataHandler.getCloudResource(
								res.getProvider(),
								res.getServiceName(),
								res.getResourceName());
						
						h.addResource(cloudResource);
						
						h.tiersOn.add(t.getId());
					}
				}
			}
		}
			
		for (Solution s : tmp.getAll()) {
			s.setRegion(null);
			String provider = BASE_PROVIDER_NAME;
			
			for (Tier t : s.getApplication(0).getTiers()) {
				ArrayList<String> propertyNames = new ArrayList<String>();
				ArrayList<Object> propertyValues = new ArrayList<Object>();
				propertyNames.add("provider");
				propertyValues.add(provider);
				s.changeValues(t.getId(), propertyNames, propertyValues);
			}
			
			s.buildFolderStructure();
			for (int hour = 0; hour < 24; ++hour) {
                Instance application = s.getApplication(hour);
                File[] models = Paths
                        .get(Configuration.PROJECT_BASE_FOLDER, 
                                Configuration.WORKING_DIRECTORY, 
                                Configuration.PERFORMANCE_RESULTS_FOLDER, 
                                provider,
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

                // add population and think time from usage model extension
                int population = -1;
                double thinktime = -1;
                if (usageModelParser.getPopulations().size() == 1)
                    population = usageModelParser.getPopulations().values()
                    .iterator().next()[hour];
                if (usageModelParser.getThinkTimes().size() == 1)
                    thinktime = usageModelParser.getThinkTimes().values()
                    .iterator().next()[hour];

                double percentage = 1.0; //(double) 1 / providers.size();
                
//	                double percentage = ((double)getTotalHourlyMachines(s, hour) / startingHourlyMachines[hour]) * startingPercentages[hour];

                population = (int) Math.ceil(population * percentage);
                
                s.setPercentageWorkload(hour, percentage);

                application.getLqnHandler().setPopulation(population);
                application.getLqnHandler().setThinktime(thinktime);
                application.getLqnHandler().saveToFile();
			}
			
			solution.add(s);
		}
		
		int[] startingHourlyMachines = new int[24];
		{
			for (int i = 0; i < startingHourlyMachines.length; ++i)
				startingHourlyMachines[i] = 0;
			
			for (Solution s : solution.getAll()) {
				int[] machines = getTotalHourlyMachines(s);
				for (int i = 0; i < startingHourlyMachines.length; ++i)
					startingHourlyMachines[i] += machines[i];
			}
		}
		
		for (Solution s : solution.getAll()) {
//			Solution s = solution.get(0);
			int[] hourlyMachines = getTotalHourlyMachines(s);
			double[] rates = new double[hourlyMachines.length];
			for (int h = 0; h < hourlyMachines.length; ++h) {
				rates[h] = ((double)hourlyMachines[h] / startingHourlyMachines[h]) * startingPercentages[h];
//				changeWorkload(s, h, ((double)hourlyMachines[h] / startingHourlyMachines[h]) * startingPercentages[h]);
			}
			changeWorkload(s, rates);
			
			logger.debug(solution.showStatus());
		}
		
		for (Solution s : startingSolution.getAll()) {
			if (solution.get(s.getProvider()) == null)
				solution.add(s);
		}
		
//		solution.removeUselessSolutions();
		
		logger.info("Solution computed!");
		return solution;
	}
	
	private int[] getTotalHourlyMachines(Solution s) {
		int[] res = new int[24];
		
		for (int h = 0; h < res.length; ++h) {
			res[h] = getTotalHourlyMachines(s, h);
		}
			
		return res;
	}
	
	private int getTotalHourlyMachines(Solution s, int h) {
		int res = 0;
			
		for (Tier t : s.getApplication(h).getTiers()) {
			IaaS service = (IaaS) t.getCloudService();
			res += service.getReplicas();
		}
			
		return res;
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
        logger.debug("Trying to change the " + hour + " hour using this rate: " + rate);

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
	
	public List<Host> getUsedHosts(int hour) {
		return getUsedHostsForTier(hour, null);
	}
	
	public List<Host> getUsedHostsForTier(int hour, String tierId) {
		List<Host> res = new ArrayList<Host>();
		
		for (Host h : owns) {
			Boolean on = h.isOn.get(hour);
			if (on != null && on && (tierId == null || h.tiersOn.contains(tierId))) {
				res.add(h);
			}
		}
		
		return res;
	}
	
	public static final double PLATFORM_COST_MULTIPLIER = 2.102; 

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EClass eClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource eResource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EObject eContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EStructuralFeature eContainingFeature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EReference eContainmentFeature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EList<EObject> eContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeIterator<EObject> eAllContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean eIsProxy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EList<EObject> eCrossReferences() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object eGet(EStructuralFeature feature) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object eGet(EStructuralFeature feature, boolean resolve) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void eSet(EStructuralFeature feature, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean eIsSet(EStructuralFeature feature) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void eUnset(EStructuralFeature feature) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object eInvoke(EOperation operation, EList<?> arguments)
			throws InvocationTargetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EList<Adapter> eAdapters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean eDeliver() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eNotify(Notification notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EList<it.polimi.modaclouds.resourcemodel.cloud.CloudService> getProvidesCloudService() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
