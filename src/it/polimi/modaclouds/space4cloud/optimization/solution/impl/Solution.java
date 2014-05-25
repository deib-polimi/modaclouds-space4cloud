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

import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.utils.Constants;
import it.polimi.modaclouds.space4cloud.utils.LoggerHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
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

	private static Logger logger = LoggerHelper.getLogger(Solution.class);

	/**
	 * if the solution has been evaluated or not.
	 */
	private boolean evaluated = false;

	/** if the solution is feasible or not. */
	private boolean feasible = false; 

	/** The Cost. */
	private int cost = 0;

	/** The Cost. */
	private String region;

	/** The evaluation. */
	private ArrayList<HashMap<Constraint, Double>> evaluation; 

	private long evaluationTime;

	/**
	 * Instantiates a new solution.
	 */
	public Solution() {
		this.hourApplication = new ArrayList<Instance>();

		for (int i = 0; i < 24; ++i)
			percentageWorkload[i] = 1.0;
	}

	/**
	 * Instantiates a new solution.
	 *
	 * @param applications the applications
	 */
	public Solution(ArrayList<Instance> applications) {
		this.hourApplication = applications;
		setRegion(applications.get(0).getRegion());
		for(Instance app:applications)
			app.setFather(this);
		
		for (int i = 0; i < 24; ++i)
			percentageWorkload[i] = 1.0;
	}

	/**
	 * Adds the application.
	 *
	 * @param application the application
	 * @return true, if successful
	 */
	public boolean addApplication(Instance application){		
		if(hourApplication.size()<24){
			hourApplication.add(application);
			application.setRegion(getRegion());
			application.setFather(this);
			return true;
		}
		else{
			System.err.println("Solution already contains 24 applications");
			return false;
		} 
	}




	/**
	 * Change values of a certain resource far all the instances/applications.
	 *
	 * @param resId the res id
	 * @param propertyNames the property names
	 * @param propertyValues the property values
	 */
	public void changeValues(String resId, ArrayList<String> propertyNames,
			ArrayList<Object> propertyValues) {
		for (Instance appl : this.getApplications()) 
			appl.changeValues(resId, propertyNames, propertyValues);
		updateEvaluation();

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Solution clone(){


		Solution cloneSolution;
		try {
			cloneSolution = (Solution) super.clone();
		} catch (CloneNotSupportedException e) {
			cloneSolution = new Solution();
		}


		cloneSolution.setHourApplication(new ArrayList<Instance>());
		for (Instance instance : this.getHourApplication()) 
			cloneSolution.addApplication(instance.clone());

		ArrayList<HashMap<Constraint, Double>> clonedEval = new ArrayList<HashMap<Constraint, Double>>();
		//fill the evaluation cloning the maps
		for(Map<Constraint,Double> m:evaluation){
			HashMap<Constraint, Double> map = new HashMap<Constraint, Double>();
			for(Constraint c:m.keySet())
				map.put(c, new Double(m.get(c)));
			clonedEval.add(map);
		}
		cloneSolution.setEvaluation(clonedEval);

		if(getRegion() != null)
			cloneSolution.setRegion(new String(this.getRegion()));

		return cloneSolution;

	}

	public void exportCSV(String filename) {
		String text="";
		text +="cost: "+getCost()+"\n";
		for(Tier t:hourApplication.get(0).getTiers())
			text +=t.getId()+",";
		text+="\n";
		for(Instance i:hourApplication){
			for(Tier t:i.getTiers())
				text+=((IaaS)t.getCloudService()).getReplicas()+",";
			text+="\n";
		}

		for(Tier t:hourApplication.get(0).getTiers())
			for(Component c:t.getComponents())
				text+=c.getId()+",";
		text+="\n";

		for(Instance i:hourApplication){
			for(Tier t:i.getTiers())
				for(Component c:t.getComponents())
					text+=c.getResponseTime()+",";

			text+="\n";
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


	public void exportLight(String filename){
		if(!isEvaluated()){
			System.err.println("Trying to export a solution that has not been evaluated!");
			return;
		}
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("SolutionResult");
			doc.appendChild(rootElement);

			// set cost
			rootElement.setAttribute("cost",""+getCost()/1000);
			//set evaluationtime
			rootElement.setAttribute("time",""+getEvaluationTime());
			//set feasibility
			rootElement.setAttribute("feasibility",""+isFeasible());

			//create tier container element
			Element tiers = doc.createElement("Tiers");
			rootElement.appendChild(tiers);

			for(String s:hourApplication.get(0).getTiersByResourceName().keySet()){
				//create the tier
				Element tier = doc.createElement("Tier");
				tiers.appendChild(tier);			

				//set id, name, provider name, service name, resource name, service type
				tier.setAttribute("id", hourApplication.get(0).getTiersByResourceName().get(s).getCloudService().getId());
				tier.setAttribute("name", hourApplication.get(0).getTiersByResourceName().get(s).getCloudService().getName());
				tier.setAttribute("providerName", hourApplication.get(0).getTiersByResourceName().get(s).getCloudService().getProvider());
				tier.setAttribute("serviceName", hourApplication.get(0).getTiersByResourceName().get(s).getCloudService().getServiceName());
				tier.setAttribute("resourceName", hourApplication.get(0).getTiersByResourceName().get(s).getCloudService().getResourceName());
				tier.setAttribute("serviceType", hourApplication.get(0).getTiersByResourceName().get(s).getCloudService().getServiceType());

				for(int i=0;i<24;i++){					
					//create the allocation element
					Element hourAllocation = doc.createElement("HourAllocation");
					tier.appendChild(hourAllocation);
					hourAllocation.setAttribute("hour",""+i);
					hourAllocation.setAttribute("allocation",""+((IaaS)hourApplication.get(i).getTiersByResourceName().get(s).getCloudService()).getReplicas());

				}
			}


			//create the element containign the response times
			Element functionalities = doc.createElement("functionalities");
			rootElement.appendChild(functionalities);

			HashMap<String,Functionality> funcList = new HashMap<>();
			for(Tier t:hourApplication.get(0).getTiers())
				for(Component c:t.getComponents())
					for(Functionality f:c.getFunctionalities())
						funcList.put(f.getId(),f);

			for(String id:funcList.keySet()){
				//create the tier
				Element functionality = doc.createElement("Functionality");
				functionalities.appendChild(functionality);			

				//set id, name, provider name, service name, resource name, service type
				functionality.setAttribute("id", id);
				functionality.setAttribute("name", funcList.get(id).getName());

				for(int i=0;i<24;i++){					
					//create the allocation element
					Element hourlyRT = doc.createElement("HourlyRT");
					functionality.appendChild(hourlyRT);
					hourlyRT.setAttribute("hour",""+i);
					for(Tier t:hourApplication.get(i).getTiers())
						for(Component c:t.getComponents())
							for(Functionality fun:c.getFunctionalities())
								if(fun.getId().equals(id))
									hourlyRT.setAttribute("responseTime",""+fun.getResponseTime());

				}
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			File file = new File(filename);
			StreamResult result = new StreamResult(file);
			System.out.println("Exported in: "+file.getAbsolutePath());

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
	 * @param hour the hour
	 * @return the application
	 */
	public Instance getApplication(int hour){

		if (hour >=0 & hour <hourApplication.size()) {
			return hourApplication.get(hour);
		}
		return null;
	}


	/**
	 * Gets the applications.
	 *
	 * @return the applications
	 */
	public  ArrayList<Instance> getApplications() {
		return hourApplication;
	}


	/**
	 * Gets the cost.
	 *
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}


	/**
	 * Gets the evaluation.
	 *
	 * @return the evaluation
	 */
	public ArrayList<HashMap<Constraint,Double>> getEvaluation() {
		return evaluation;
	}

	public long getEvaluationTime() {
		return evaluationTime;
	}


	/**
	 * Gets the hour application.
	 *
	 * @return the hour application
	 */
	public ArrayList<Instance> getHourApplication() {
		return hourApplication;
	}


	public int getNumberOfViolatedConstraints(){
		int temp =0;
		for(Instance inst:hourApplication)
			temp += inst.getNumerOfViolatedConstraints();
		return temp;
	}

	public String getRegion() {
		return region;
	}

	public int getTotalVms() {
		int vms=0;
		for(Instance inst:hourApplication)
			for(Tier t:inst.getTiersByResourceName().values())
				vms += ((IaaS)t.getCloudService()).getReplicas();

		return vms;
	}

	public int getVmNumberPerTier(int tierNumber) {
		int vms=0;
		for(Instance inst:hourApplication)
			if(inst.getTiersByResourceName().values().size() >=  tierNumber)
				vms += ((IaaS)((Tier)inst.getTiersByResourceName().values().toArray()[tierNumber]).getCloudService()).getReplicas();
		return vms;
	}

	/**
	 * Greater than.
	 *
	 * @param sol the solution to test against this
	 * @return true, if successful
	 */
	public boolean greaterThan(Solution sol){

		boolean condition = false;

		condition = isFeasible() && sol.isFeasible() ;

		if ( condition ) {
			/*if both are feasible*/
			return this.cost < sol.getCost();

		} 
		else if (!isFeasible() && !sol.isFeasible() ){

			/*TODO: here we have to consider as better the solution with the minimum number of violated constraints or something like this, we 
			 * could also consider the constraints according to their importance*/

			ArrayList<Constraint> notVerifiedbyThis = new ArrayList<Constraint>();
			for(Map<Constraint, Double> m:evaluation)
				for(Constraint c:m.keySet())
					if(m.get(c) >0)
						notVerifiedbyThis.add(c);

			ArrayList<Constraint> notVerifiedbySol = new ArrayList<Constraint>();
			for(Map<Constraint, Double> m:sol.getEvaluation())
				for(Constraint c:m.keySet())
					if(m.get(c) >0)
						notVerifiedbySol.add(c);
			if(notVerifiedbyThis.size() > notVerifiedbySol.size())
				return false;
			else 
				return true;
			//TODO in case it is equal we should check constraints by priority or by distance 
		}
		else{

			return this.isFeasible();
		}

	}
	
	public boolean greaterThan(SolutionMulti sol) {
		Solution s = sol.get(getProvider());
		if (s == null)
			return true;
		return greaterThan(s);
	}
	
	public String getProvider() {
		try {
			return getApplication(0).getTiers().get(0).getCloudService().getProvider();
		} catch (Exception e) {
			return "Error";
		}
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

		for(Instance tmp:hourApplication)
			if(!tmp.isFeasible() && feasible)
				System.err.println("Inconsistent feasibility");

		return feasible;
	}


	public int numberOfUnfeasibleHours(){
		int counter = 0;
		for(Instance i:hourApplication)
			if(!i.isFeasible())
				counter++;
		return counter;
	}


	/**
	 * Sets the cost.
	 *
	 * @param totalCost the new cost
	 */
	public void setCost(int totalCost) {
		this.cost = totalCost;

	}

	/**
	 * Sets the evaluated.
	 *
	 * @param b the new evaluated
	 */
	private void setEvaluated(boolean b) {
		this.evaluated= b;

	}

	/**
	 * Sets the evaluation.
	 *
	 * @param evaluateSolution the evaluate solution
	 */
	public void setEvaluation(ArrayList<HashMap<Constraint, Double>> evaluateSolution) {		
		this.evaluation = evaluateSolution;
		setFeasible(true);

		//initialize solutions as feasible and counters to 0
		for(Instance tmp:hourApplication){
			tmp.setFeasible(true);
			tmp.resetConstraintCounter();
		}

		int i = 0;
		for(Map<Constraint,Double> m:evaluation){
			Instance app = getApplication(i);

			for(Constraint c:m.keySet())
				if(m.get(c)>0){				
					app.incrementViolatedConstraints();
					app.setFeasible(false);
					setFeasible(false);
				}
			i++;						
		}
	}

	public void setEvaluationTime(long splitTime) {
		evaluationTime = splitTime;
	}


	/**
	 * Sets the feasibility.
	 *
	 * @param b the new feasibility
	 */
	public void setFeasible(boolean b) {
		this.feasible= b;

	}

	private void setHourApplication(ArrayList<Instance> hourApplication) {
		this.hourApplication = hourApplication;
	}

	public void setRegion(String region) {
		this.region = region;
		for(Instance app:hourApplication)
			app.setRegion(region);
	}

	/**
	 * Show status.
	 */
	public String showStatus() {
		String result = "Solution Status\n";
		result += "Cost: "+cost;
		result += "\tEvaluated: "+evaluated;
		result += "\tFeasible: "+isFeasible();
		
		result += "\nProvider: " + getProvider();
		
		if (getRegion() != null)
			result += "\nRegion: " + getRegion();
		
		for(Instance i:hourApplication){
			result += "\nHour: "+hourApplication.indexOf(i);
			result += "\n\tWorkload: " + i.getWorkload() + " (" + (getPercentageWorkload(hourApplication.indexOf(i)) * 100) + "%)";
			result += "\n"+i.showStatus("\t");
		}
		return result;
	}
	
	public String toString() {
		String result = "Solution@" + Integer.toHexString(super.hashCode());
		result += "[Cost: "+cost;
		result += ", Evaluated: "+evaluated;
		result += ", Feasible: "+isFeasible();
		result +="]";
		return result;
	}

	/**
	 * checks whether at least one of the instances is not evaluated, if so marks the entire solution as not evaluated
	 */
	public void updateEvaluation() {
		setEvaluated(true);
		if(hourApplication.size()<24){
			setEvaluated(false);
			return;
		}
		for(Instance i:hourApplication)
			if(!i.isEvaluated()){
				setEvaluated(false);
				return;
			}
	}

	/**
	 * checks whether at least one of the instances is not feasible, if so marks the entire solution as not feasible
	 */
	private void updateFeasibility() {
		setFeasible(true);
		for(Instance i:hourApplication)
			if(!i.isFeasible()){
				setFeasible(false);
				return;
			}
	}
	
	public void buildFolderStructure() throws IOException {
		buildFolderStructure(getProvider());
	}

	public void buildFolderStructure(String folder) throws IOException {
		
		Constants c = Constants.getInstance();
		
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
			return;
		}

		// there should be just 1 palladio model
		Path lqnModelPath = modelFiles[0].toPath();
		// with the corresponding evaluation
		Path resultModelPath = resultFiles[0].toPath();
		
		Files.createDirectory(Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
				c.PERFORMANCE_RESULTS_FOLDER, folder));
		
		for (int i = 0; i < 24; i++) {
			Path tmpFolderPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
					c.PERFORMANCE_RESULTS_FOLDER, folder, c.FOLDER_PREFIX + i);
			Files.createDirectory(tmpFolderPath);
			Path tmpLqnPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
					c.PERFORMANCE_RESULTS_FOLDER, folder, c.FOLDER_PREFIX + i,
					lqnModelPath.getFileName().toString());
			Files.copy(lqnModelPath, tmpLqnPath);
			Path tmpResultPath = Paths.get(c.ABSOLUTE_WORKING_DIRECTORY,
					c.PERFORMANCE_RESULTS_FOLDER, folder, c.FOLDER_PREFIX + i,
					resultModelPath.getFileName().toString());
			Files.copy(resultModelPath, tmpResultPath);
		}
	}
	
	private double[] percentageWorkload = new double[24];
	
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
	
	public double getPercentageWorkload(int hour) {
		if (hour < 0)
			hour = 0;
		else if (hour > 23)
			hour = 23;
		return percentageWorkload[hour];
	}

}
