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
package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.qos_models.schema.HourValueType;
import it.polimi.modaclouds.qos_models.schema.Location;
import it.polimi.modaclouds.qos_models.schema.ObjectFactory;
import it.polimi.modaclouds.qos_models.schema.Performance;
import it.polimi.modaclouds.qos_models.schema.Performance.Seffs;
import it.polimi.modaclouds.qos_models.schema.Performance.Seffs.Seff;
import it.polimi.modaclouds.qos_models.schema.Performance.Tiers;
import it.polimi.modaclouds.qos_models.schema.ReplicaElement;
import it.polimi.modaclouds.qos_models.schema.ResourceContainer;
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;
import it.polimi.modaclouds.qos_models.schema.SeffType.Percentiles;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// TODO: Auto-generated Javadoc
/**
 * The Class Solution.
 */
public class Solution implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6116921591578286173L;

	/** The hour application. */
	ArrayList<Instance> hourApplication = new ArrayList<Instance>();

	private static Logger logger = LoggerFactory.getLogger(Solution.class);
	
	private int generationIteration = 0;
	
	private long generationTime =0;

	/**
	 * if the solution has been evaluated or not.
	 */
	private boolean evaluated = false;

	/** if the solution is feasible or not. */
	private boolean feasible = false;

//	/** The Cost. */
//	private double cost = 0;
	
	private double hourlyCosts[] = new double[24];

	/** The Region. */
	private String region;

	/** The evaluation. */
	private List<HashMap<Constraint, Boolean>> evaluation;

	private double[] percentageWorkload = new double[24];

	/**
	 * Instantiates a new solution.
	 */
	public Solution() {
		this.hourApplication = new ArrayList<Instance>();

		for (int i = 0; i < 24; ++i) {
			percentageWorkload[i] = 1.0;
			hourlyCosts[i] = 0.0;
		}
	}

	/**
	 * Instantiates a new solution.
	 * 
	 * @param applications
	 *            the applications
	 */
	public Solution(ArrayList<Instance> applications) {
		this.hourApplication = applications;
		setRegion(applications.get(0).getRegion());
		for (Instance app : applications)
			app.setFather(this);

		for (int i = 0; i < 24; ++i)
			percentageWorkload[i] = 1.0;
	}

	/**
	 * Adds the application.
	 * 
	 * @param application
	 *            the application
	 * @return true, if successful
	 */
	public boolean addApplication(Instance application) {
		if (hourApplication.size() < 24) {
			hourApplication.add(application);
			application.setRegion(getRegion());
			application.setFather(this);
			return true;
		} else {
			System.err.println("Solution already contains 24 applications");
			return false;
		}
	}

	public void buildFolderStructure() throws IOException {
		buildFolderStructure(getProvider());
	}

	public void buildFolderStructure(String folder) throws IOException {

		// Build the folder structure to host results and copy the LQN model in
		// those folders
		File resultDirPath = Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.PERFORMANCE_RESULTS_FOLDER).toFile();
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
			logger.error("The first initialization run has encounter some problem during the generation of the first solution");
			logger.error("SPACE4CLOUD will now exit.");
			return;
		}

		// there should be just 1 palladio model
		Path lqnModelPath = modelFiles[0].toPath();
		// with the corresponding evaluation
		Path resultModelPath = resultFiles[0].toPath();

		Files.createDirectories(Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.PERFORMANCE_RESULTS_FOLDER, folder));

		for (int i = 0; i < 24; i++) {
			Path tmpFolderPath = Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.PERFORMANCE_RESULTS_FOLDER, folder, Configuration.FOLDER_PREFIX + i);			
			Files.createDirectories(tmpFolderPath);
			Path tmpLqnPath = Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.PERFORMANCE_RESULTS_FOLDER, folder, Configuration.FOLDER_PREFIX + i,
					lqnModelPath.getFileName().toString());
			Files.copy(lqnModelPath, tmpLqnPath);
			Path tmpResultPath = Paths.get(Configuration.PROJECT_BASE_FOLDER,Configuration.WORKING_DIRECTORY,Configuration.PERFORMANCE_RESULTS_FOLDER, folder, Configuration.FOLDER_PREFIX + i,
					resultModelPath.getFileName().toString());
			Files.copy(resultModelPath, tmpResultPath);
		}
	}

	/**
	 * Change values of a certain resource far all the instances/applications.
	 * 
	 * @param resId
	 *            the res id
	 * @param propertyNames
	 *            the property names
	 * @param propertyValues
	 *            the property values
	 */
	public void changeValues(String resId, ArrayList<String> propertyNames,
			ArrayList<Object> propertyValues) {
		for (Instance appl : this.getApplications())
			appl.changeValues(resId, propertyNames, propertyValues);
		updateEvaluation();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Solution clone() {

		Solution cloneSolution;
		try {
			cloneSolution = (Solution) super.clone();
		} catch (CloneNotSupportedException e) {
			cloneSolution = new Solution();
		}

		cloneSolution.setHourApplication(new ArrayList<Instance>());
		for (Instance instance : this.getHourApplication())
			cloneSolution.addApplication(instance.clone());

		List<HashMap<Constraint, Boolean>> clonedEval = new ArrayList<HashMap<Constraint, Boolean>>();
		// fill the evaluation cloning the maps
		for (Map<Constraint, Boolean> m : evaluation) {
			HashMap<Constraint, Boolean> map = new HashMap<Constraint, Boolean>();
			for (Constraint c : m.keySet())
				map.put(c, new Boolean(m.get(c)));
			clonedEval.add(map);
		}
		cloneSolution.setEvaluation(clonedEval);

		if (getRegion() != null)
			cloneSolution.setRegion(new String(this.getRegion()));
		
		cloneSolution.percentageWorkload = new double[24];
		for (int h = 0; h < 24; ++h)
			cloneSolution.setPercentageWorkload(h,percentageWorkload[h]);
		
		cloneSolution.hourlyCosts = new double[24];
		for (int h = 0; h < 24; ++h)
			cloneSolution.setCost(h, hourlyCosts[h]);

		return cloneSolution;

	}

	public void copyApplication(Instance application, int i) {
		hourApplication.set(i, application.clone());
		hourApplication.get(i).setRegion(getRegion());
		hourApplication.get(i).setFather(this);
		hourApplication.get(i).setEvaluated(false);
	}
	
	public boolean usesPaaS() {
		for (Tier t : hourApplication.get(0).getTiers())
			if (t.getCloudService() instanceof PaaS)
				return true;
		return false;
	}

	public int getReplicas(Tier t) {
		CloudService serv = t.getCloudService();
		
		if (serv instanceof IaaS) {
			IaaS res = (IaaS) t.getCloudService();
			return res.getReplicas();
		} else if (serv instanceof PaaS) {
			return 1;
		}
		return 0;
	}
	
	public void exportCSV(String filename) {
		String text = "";
		text += "cost: " + getCost() + "\n";
		for (Tier t : hourApplication.get(0).getTiers())
			text += t.getId() + ",";
		text += "\n";
		for (Instance i : hourApplication) {
			for (Tier t : i.getTiers())
				text += getReplicas(t) + ",";
			text += "\n";
		}

		for (Tier t : hourApplication.get(0).getTiers())
			for (Component c : t.getComponents())
				text += c.getId() + ",";
		text += "\n";

		for (Instance i : hourApplication) {
			for (Tier t : i.getTiers())
				for (Component c : t.getComponents())
					text += c.getResponseTime() + ",";

			text += "\n";
		}

		try {
			PrintWriter outFile = new PrintWriter(filename);
			outFile.println(text);
			outFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void exportLight(Path filePath) {
		if (!isEvaluated()) {
			System.err
			.println("Trying to export a solution that has not been evaluated!");
			return;
		}
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("SolutionResult");
			doc.appendChild(rootElement);

			// set cost
			rootElement.setAttribute("cost", "" + getCost());
			// set generation time
			rootElement.setAttribute("time", "" + getGenerationTime());
			// set generation iteration
			rootElement.setAttribute("iteration", "" + getGenerationIteration());
			// set feasibility
			rootElement.setAttribute("feasibility", "" + isFeasible());

			// create tier container element
			Element tiers = doc.createElement("Tiers");
			rootElement.appendChild(tiers);

			for (Tier t : hourApplication.get(0).getTiers()) {
				// create the tier
				Element tier = doc.createElement("Tier");
				tiers.appendChild(tier);

				// set id, name, provider name, service name, resource name,
				// service type
				tier.setAttribute("id", t.getId());
				tier.setAttribute("name", t.getName());

				CloudService cs = t.getCloudService();
				tier.setAttribute("providerName", cs.getProvider());
				tier.setAttribute("serviceName", cs.getServiceName());
				tier.setAttribute("resourceName", cs.getResourceName());
				tier.setAttribute("serviceType", cs.getServiceType());

				if(cs instanceof IaaS || (cs instanceof PaaS && ((PaaS)cs).areReplicasChangeable())){
					for (int i = 0; i < 24; i++) {
						// create the allocation element
						Element hourAllocation = doc.createElement("HourAllocation");
						tier.appendChild(hourAllocation);
						hourAllocation.setAttribute("hour", "" + i);
						hourAllocation.setAttribute("allocation", ""
								+ getReplicas(hourApplication.get(i).getTierById(t.getId())));
					}
				}
			}

			// create the element with the response times
			Element functionalities = doc.createElement("functionalities");
			rootElement.appendChild(functionalities);

			HashMap<String, Functionality> funcList = new HashMap<>();
			for (Tier t : hourApplication.get(0).getTiers())
				for (Component c : t.getComponents())
					for (Functionality f : c.getFunctionalities())
						funcList.put(f.getId(), f);

			for (String id : funcList.keySet()) {
				// create the tier
				Element functionality = doc.createElement("Functionality");
				functionalities.appendChild(functionality);

				// set id, name, provider name, service name, resource name,
				// service type
				functionality.setAttribute("id", id);
				functionality.setAttribute("name", funcList.get(id).getName());

				for (int i = 0; i < 24; i++) {
					// create the allocation element
					Element hourlyRT = doc.createElement("HourlyRT");
					functionality.appendChild(hourlyRT);
					hourlyRT.setAttribute("hour", "" + i);
					for (Tier t : hourApplication.get(i).getTiers())
						for (Component c : t.getComponents())
							for (Functionality fun : c.getFunctionalities())
								if (fun.getId().equals(id))
									hourlyRT.setAttribute("responseTime", ""
											+ fun.getResponseTime());

				}
			}

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			File file = filePath.toFile();
			StreamResult result = new StreamResult(file);
			logger.info("Exported in: " + file.getAbsolutePath());

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

		} catch (ParserConfigurationException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Gets the application.
	 * 
	 * @param hour
	 *            the hour
	 * @return the application
	 */
	public Instance getApplication(int hour) {

		if (hour >= 0 & hour < hourApplication.size()) {
			return hourApplication.get(hour);
		}
		return null;
	}

	/**
	 * Gets the applications.
	 * 
	 * @return the applications
	 */
	public ArrayList<Instance> getApplications() {
		return hourApplication;
	}

	/**
	 * Gets the cost.
	 * 
	 * @return the cost
	 */
	public double getCost() {
		double cost = 0;
		for (int h = 0; h < hourlyCosts.length; ++h)
			cost += hourlyCosts[h];
		return cost;
	}
	
	/**
	 * Gets the cost for a single hour.
	 * 
	 * @return the cost
	 */
	public double getCost(int h) {
		return hourlyCosts[h];
	}

	/**
	 * Gets the evaluation.
	 * 
	 * @return the evaluation
	 */
	public List<HashMap<Constraint, Boolean>> getEvaluation() {
		return evaluation;
	}

	/**
	 * Gets the hour application.
	 * 
	 * @return the hour application
	 */
	public ArrayList<Instance> getHourApplication() {
		return hourApplication;
	}

	public int getNumberOfViolatedConstraints() {
		int temp = 0;
		for (Instance inst : hourApplication)
			temp += inst.getNumerOfViolatedConstraints();
		return temp;
	}

	public double getPercentageWorkload(int hour) {
		if (hour < 0)
			hour = 0;
		else if (hour > 23)
			hour = 23;
		return percentageWorkload[hour];
	}

	public String getProvider() {
		try {
			return getApplication(0).getTiers().get(0).getCloudService()
					.getProvider();
		} catch (Exception e) {
			return "Error";
		}
	}

	public String getRegion() {
		return region;
	}

	/**
	 * Retrieves the total number of VMs used for the entire solution
	 * @return
	 */
	public int getTotalVms() {
		int vms = 0;
		for (Instance inst : hourApplication)
			for (Tier t : inst.getTiers())
				vms += getReplicas(t);

		return vms;
	}

	/**
	 * Retireves the total number of VMs used for the specified tier
	 * @param tierId the id of the Tier
	 * @return
	 */
	public int getVmNumberPerTier(String tierId) {
		int vms = 0;
		for (Instance inst : hourApplication)			
				vms += getReplicas(inst.getTierById(tierId));
		return vms;
	}

	/**
	 * @return the number of the scramble iteration in which the solution have been generated
	 */
	public int getGenerationIteration() {
		return generationIteration;
	}

	public void setGenerationIteration(int generationIteration) {
		this.generationIteration = generationIteration;
	}

	/**
	 * @return the time elapsed from the starting of the optimization and the generation of the solution
	 */
	public long getGenerationTime() {
		return generationTime;
	}

	public void setGenerationTime(long generationTime) {
		this.generationTime = generationTime;
	}

	/**
	 * Greater than.
	 * 
	 * @param sol
	 *            the solution to test against this
	 * @return true, if successful
	 */
	public boolean greaterThan(Solution sol) {

		boolean condition = false;

		condition = isFeasible() && sol.isFeasible();

		if (condition) {
			/* if both are feasible */
			return getCost() < sol.getCost();

		} else if (!isFeasible() && !sol.isFeasible()) {

			/*
			 * here we  consider as better the solution with the
			 * minimum number of violated constraints 
			 */

			if (getNumberOfViolatedConstraints() <= sol.getNumberOfViolatedConstraints())
				return true;
			else
				return false;

		} else {

			return this.isFeasible();
		}

	}

	public boolean greaterThan(SolutionMulti sol) {
		if(sol==null)
			return true;
		Solution s = sol.get(getProvider());
		if (s == null)
			return true;
		return greaterThan(s);
	}

	/**
	 * Checks if is evaluated.
	 * 
	 * @return true, if is evaluated
	 */
	public boolean isEvaluated() {
		return evaluated;
	}

	/**
	 * Checks if is feasible.
	 * 
	 * @return the feasible
	 */
	public boolean isFeasible() {

		for (Instance tmp : hourApplication)
			if (!tmp.isFeasible() && feasible)
				System.err.println("Inconsistent feasibility");

		return feasible;
	}

	public int numberOfUnfeasibleHours() {
		int counter = 0;
		for (Instance i : hourApplication)
			if (!i.isFeasible())
				counter++;
		return counter;
	}

	/**
	 * Sets the cost.
	 * 
	 * @param totalCost
	 *            the new cost
	 */
	public void setCost(int h, double totalCost) {
//		this.cost = totalCost;
		hourlyCosts[h] = totalCost;

	}

	/**
	 * Sets the evaluated.
	 * 
	 * @param b
	 *            the new evaluated
	 */
	private void setEvaluated(boolean b) {
		this.evaluated = b;

	}

	/**
	 * Sets the evaluation.
	 * 
	 * @param evaluateSolution
	 *            the evaluate solution
	 */
	public void setEvaluation(List<HashMap<Constraint, Boolean>> evaluateSolution) {
		this.evaluation = evaluateSolution;
		setFeasible(true);

		// initialize solutions as feasible and counters to 0
		for (Instance tmp : hourApplication) {
			tmp.setFeasible(true);
			tmp.resetViolatedConstraints();
		}

		int i = 0;
		//suppose that evaluations and applications are in the same order!
		for (Map<Constraint, Boolean> feasibilities : evaluation) {
			Instance app = getApplication(i);

			for (Constraint c : feasibilities.keySet())
				if (!feasibilities.get(c)) {
					app.incrementViolatedConstraints(c);
					app.setFeasible(false);
					setFeasible(false);
				}
			i++;
		}
		
		
		for (Instance tmp : hourApplication) {
			if(tmp.getWorkload()==0){
				tmp.setFeasible(true);
				setFeasible(true);
			}
		}
		
	}


	/**
	 * Sets the feasibility.
	 * 
	 * @param b
	 *            the new feasibility
	 */
	public void setFeasible(boolean b) {
		this.feasible = b;

	}

	private void setHourApplication(ArrayList<Instance> hourApplication) {
		this.hourApplication = hourApplication;
	}

	public void setPercentageWorkload(int hour, double percentage) {
		if (hour < 0)
			hour = 0;
		else if (hour > 23)
			hour = 23;
		if (percentage > 1.0)
			percentage = 1.0;
		else if (percentage < 0.0)
			percentage = 0.0;
		percentageWorkload[hour] = percentage;
	}

	public void setRegion(String region) {
		this.region = region;
		for (Instance app : hourApplication)
			app.setRegion(region);
	}
	public List<Constraint> getViolatedConstraints(){
		List<Constraint> violatedConstraints = new ArrayList<>();
		for(Instance application:hourApplication){
			violatedConstraints.addAll(application.getViolatedConstraints());
		}
		return violatedConstraints;
	}

	/**
	 * Show status.
	 */
	public String showStatus() {
		String result = "Solution Status\n";
		result += "Cost: " + getCost();
		result += "\tEvaluated: " + evaluated;
		result += "\tFeasible: " + isFeasible();

		result += "\nProvider: " + getProvider();

		if (getRegion() != null)
			result += "\nRegion: " + getRegion();

		for (Instance i : hourApplication) {
			result += "\nHour: " + hourApplication.indexOf(i);
			result += "\n\tWorkload: " + i.getWorkload() + " ("
					+ (getPercentageWorkload(hourApplication.indexOf(i)) * 100)
					+ "%)";
			result += "\n" + i.showStatus("\t");
		}
		return result;
	}

	@Override
	public String toString() {
		String result = "Solution@" + Integer.toHexString(super.hashCode());
		result += "[Provider: " + getProvider();
		result += ", Cost: " + getCost();
		result += ", Evaluated: " + evaluated;
		result += ", Feasible: " + isFeasible();
		result += "]";
		return result;
	}

	/**
	 * checks whether at least one of the instances is not evaluated, if so
	 * marks the entire solution as not evaluated
	 */
	public void updateEvaluation() {
		setEvaluated(true);
		if (hourApplication.size() < 24) {
			setEvaluated(false);
			return;
		}
		for (Instance i : hourApplication)
			if (!i.isEvaluated()) {
				setEvaluated(false);
				return;
			}
	}
	
	public ResourceModelExtension getAsExtension() {
		//Build the objects
		ObjectFactory factory = new ObjectFactory();
		ResourceModelExtension extension = factory.createResourceModelExtension();
		List<ResourceContainer> resourceContainers = extension.getResourceContainer();
		Map<String,ResourceContainer> containersByID = new HashMap<>();
		//initialize fields common to all hours by looking at the first one
		for(Tier t:hourApplication.get(0).getTiers()){
			//build the resource container that maps the tier
			ResourceContainer container = factory.createResourceContainer(); 
			container.setId(t.getId());
			containersByID.put(t.getId(), container);
			
			//take out the selected service
			CloudService service = t.getCloudService();
			container.setProvider(service.getProvider());			
			
			//in case this is a IaaS service
			if(service instanceof IaaS){
				IaaS iaaService  = (IaaS) service;
				it.polimi.modaclouds.qos_models.schema.CloudService resource = factory.createCloudService();
				resource.setServiceCategory("IaaS");
				resource.setServiceType(iaaService.getServiceType());
				resource.setServiceName(iaaService.getServiceName());
				resource.setResourceSizeID(iaaService.getResourceName());							
				Location location = factory.createLocation();
				location.setRegion(hourApplication.get(0).getRegion());
				resource.setLocation(location);				
				resource.setReplicas(factory.createReplica());
				container.setCloudElement(resource);
			}
			//if it is a Paas service
			else if (service instanceof PaaS){
				PaaS paaService  = (PaaS) service;
				it.polimi.modaclouds.qos_models.schema.CloudService resource = factory.createCloudService();
				resource.setServiceCategory("PaaS");
				resource.setServiceType(paaService.getServiceType());
				resource.setServiceName(paaService.getServiceName());
				resource.setResourceSizeID(paaService.getResourceName());							
				Location location = factory.createLocation();
				location.setRegion(hourApplication.get(0).getRegion());
				resource.setLocation(location);				
				resource.setReplicas(factory.createReplica());
				container.setCloudElement(resource);
			}			
			resourceContainers.add(container);
		}
		
		//fill the fields that depend on the time slot (e.g. VMs replicas)
		for(Instance instance:hourApplication){
			for(Tier t:instance.getTiers()){
				CloudService service = t.getCloudService();
				int replicas = -1;
				if (service instanceof IaaS) {
					IaaS iaaService  = (IaaS) service;
					replicas = iaaService.getReplicas();
				}
				else if (service instanceof PaaS && ((PaaS)service).areReplicasChangeable()) {
					PaaS paaService  = (PaaS) service;
					replicas = paaService.getReplicas();
				}
				if (replicas > -1) {
					//build the replica element
					ReplicaElement replica = factory.createReplicaElement();
					replica.setHour(hourApplication.indexOf(instance));
					replica.setValue(replicas);
					//add it to the resource container 
					containersByID.get(t.getId()).getCloudElement().getReplicas().getReplicaElement().add(replica);
				}
			}
		}
		
		return extension;
	}
	
	/**
	 * Export the solution in the format of the extension used as input for space4cloud
	 */
	public void exportAsExtension(Path fileName){
		ResourceModelExtension extension = getAsExtension();
		//serialize them		
		try {			
			XMLHelper.serialize(extension, ResourceModelExtension.class, new FileOutputStream(fileName.toFile()));
		} catch (JAXBException e) {
			logger.error("The generated solution is not valid",e);
		} catch (FileNotFoundException e) {
			logger.error("Error exporting the solution",e);
		}
		
	}

	public boolean hasAtLeastOneReplicaInOneHour() {
		for (Instance i : hourApplication) {
			for (Tier t : i.getTiers()) {
				int replicas = getReplicas(t);
				if (replicas > 0)
					return true;
			}
		}
		return false;
	}
	
	public Performance getPerformancesAsExtension() {
		Performance performances = new Performance();
		
		performances.setSolutionID(hashCode() + "");
		
		Tiers trs = new Tiers();
		Seffs sfs = new Seffs();
		
		for (Tier t : hourApplication.get(0).getTiers()) {
			
			for (Component c : t.getComponents())
				for (Functionality f : c.getFunctionalities()) {
					Seff seff = new Seff();
					
					seff.setId(f.getId());
					seff.setName(f.getName());
					
					Map<Integer, Percentiles> percentiles = new TreeMap<Integer, Percentiles>(); 
					
					for (int h = 0; h < 24; ++h) {
						Instance app = hourApplication.get(h);
						for (Tier t2 : app.getTiers())
							for (Component c2 : t2.getComponents()) {
								for (Functionality f2 : c2.getFunctionalities()) {
									if (f2.getId().equals(f.getId())) {
										{
											HourValueType hvt = new HourValueType();
											hvt.setHour(h);
											hvt.setValue((float)f2.getResponseTime());
											seff.getAvgRT().add(hvt);
										}
										{
											HourValueType hvt = new HourValueType();
											hvt.setHour(h);
											hvt.setValue((float)f2.getThroughput());
											seff.getThroughput().add(hvt);
										}
										
										Map<Integer, Double> tmp = f2.getRtPercentiles();
										
										if (tmp == null)
											continue;
										
										for (int key : tmp.keySet()) {
											Percentiles perc = percentiles.get(key);
											if (perc == null) {
												 perc = new Percentiles();
												 perc.setLevel(key);
												 percentiles.put(key, perc);
											}
											
											HourValueType hvt = new HourValueType();
											hvt.setHour(h);
											hvt.setValue(tmp.get(key).floatValue());
											
											perc.getPercentile().add(hvt);
										}
									}
								}
							}
					}
					
					for (int key : percentiles.keySet())
						seff.getPercentiles().add(percentiles.get(key));
					
					sfs.getSeff().add(seff);
				}
			
			it.polimi.modaclouds.qos_models.schema.Performance.Tiers.Tier tier = new it.polimi.modaclouds.qos_models.schema.Performance.Tiers.Tier();
			
			tier.setId(t.getId());
			tier.setName(t.getName());
			
			for (int h = 0; h < 24; ++h) {
				Instance app = hourApplication.get(h);
				Tier t2 = app.getTierById(t.getId());
				
				HourValueType hvt = new HourValueType();
				hvt.setHour(h);
				hvt.setValue((int)Math.round(100 * t2.getUtilization()));
				
				tier.getUtilization().add(hvt);
			}
			
			trs.getTier().add(tier);
			
		}
		

		performances.setTiers(trs);
		performances.setSeffs(sfs);
		
		return performances;
	}
	
	public void exportPerformancesAsExtension(Path fileName){
		Performance performances = getPerformancesAsExtension();
		//serialize them
		try {
			XMLHelper.serialize(performances, Performance.class, new FileOutputStream(fileName.toFile()));
		} catch (JAXBException e) {
			logger.error("The generated solution is not valid",e);
		} catch (FileNotFoundException e) {
			logger.error("Error exporting the solution",e);
		}
		
	}

}
