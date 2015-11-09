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
package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polimi.modaclouds.space4cloud.lqn.LqnHandler;
import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;
import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.utils.ReflectionUtility;

/**
 * @author Michele Ciavotta
 * 
 */
public class Instance implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8622188724510939131L;

	/**
	 * The lqn document in DOM format associated to a certain hour of our
	 * solution. there must be a 1 to 1 relation between the lqn document and
	 * the tiers
	 */
	private LqnHandler lqnHandler;

	private boolean evaluated = false;

	private boolean feasible = false;

	private boolean closedWorkload = true;

	private Map<String, IConstrainable> constrainableResources = new HashMap<String, IConstrainable>();

	private List<Constraint> violatedConstraints= new ArrayList<Constraint>();

	private LqnResultParser resultParser;

	private int workload;

	private ArrayList<Tier> tiers = new ArrayList<Tier>();

	private String region;

	private Solution father;

	public Tier getTierByName(String name){
		for(Tier t:tiers)
			if(t.getPcmName().equals(name))
				return t;
		return null;

	}

	public Tier getTierById(String id){
		for(Tier t:tiers)
			if(t.getId().equals(id))
				return t;
		return null;

	}

	public void addTier(Tier tier) {
		tiers.add(tier);
	}


	public boolean changeValues(String tierId,
			List<String> propertyNames, List<Object> propertyValues) {

		if (tierId != null) {
			try {
				// 1: find the resource
				Tier tier = getTierById(tierId);

				CloudService service = tier.getCloudService();

				// 2: modify the resource
				// modifications may involve both resource and tier.
				for (int i = 0; i < propertyNames.size(); i++) {
					ReflectionUtility.set(service, propertyNames.get(i),
							propertyValues.get(i));
					ReflectionUtility.set(tier, propertyNames.get(i),
							propertyValues.get(i));

				}
				// update the LQN for the modified reources/tiers (Not needed
				// since we rewrite it before evaluation)
				// lqnHandler.updateElement(resource);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else {
			try {
				for (int i = 0; i < propertyNames.size(); i++) {
					ReflectionUtility.set(this, propertyNames.get(i),
							propertyValues.get(i));
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		// if we changed something we invalidate the evaluation of the solution
		setEvaluated(false);
		return true;
	}

	@Override
	public Instance clone() {

		// skip cloning the result parser since if the instance changes and is
		// re-evaluated it will get a new one after the evaluation

		Instance cloneInst;
		try {
			cloneInst = (Instance) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			cloneInst = new Instance();
		}

		// the information about the workload should be copied in the new
		// instance
		cloneInst.setWorkload(getWorkload());

		// clone the lqnHandler
		cloneInst.setLqnHandler(this.getLqnHandler().clone());

		// clone the tiers
		cloneInst.setTiers(new ArrayList<Tier>());

		for (Tier tier : this.getTiers())
			cloneInst.addTier(tier.clone());
		// we have to fix the chain of external calls updating the references to
		// new functionalities
		cloneInst.updateCallChain();

		cloneInst.initConstrainableResources();
		if (getRegion() != null)
			cloneInst.setRegion(new String(this.getRegion()));
		cloneInst.setFather(null);

		//clone the constraints
		cloneInst.violatedConstraints = new ArrayList<Constraint>(getNumerOfViolatedConstraints());
		for(Constraint c:violatedConstraints)
			cloneInst.incrementViolatedConstraints(c);

		return cloneInst;

	}

	public Map<String, IConstrainable> getConstrainableResources() {
		return constrainableResources;
	}

	public Solution getFather() {
		return father;
	}

	/**
	 * Our custom HashString
	 * 
	 * @return HashString
	 */
	public String getHashString() {
		String str = Integer.toString(workload);
		if (tiers.size() > 0)
			str += tiers.get(0).getCloudService().getProvider();
		for(Tier t:tiers){
			CloudService res = t.getCloudService();
			str = str + res.getResourceName() + getReplicas(t);
		}
		return str;
	}
	
	private int getReplicas(Tier t) {
		return t.getCloudService().getReplicas();
	}

	/**
	 * @return the lqnHandler
	 */
	public LqnHandler getLqnHandler() {
		return lqnHandler;
	}

	public int getNumerOfViolatedConstraints() {
		return violatedConstraints.size();
	}

	public String getRegion() {
		return region;
	}

	public LqnResultParser getResultParser() {
		return resultParser;
	}

	public List<Tier> getTiers() {
		return tiers;
	}


	public int getWorkload() {
		return workload;
	}

	public void incrementViolatedConstraints(Constraint violatedConstraint) {
		violatedConstraints.add(violatedConstraint);		
	}

	public void initConstrainableResources() {
		constrainableResources = new HashMap<String, IConstrainable>();
		for (Tier t : tiers) {
			if (t instanceof IConstrainable)
				constrainableResources.put(t.getId(), t);
			constrainableResources.putAll(t.getConstrinableResources());
		}
	}

	/**
	 * Build a new lqn handler from the provided file
	 * @param lqnFilePath
	 */
	public void initLqnHandler(Path lqnFilePath) {

		// evaluate the model to build the result
		lqnHandler = new LqnHandler(lqnFilePath);

	}

	/**
	 * @return the evaluated
	 */
	public boolean isEvaluated() {
		return evaluated;
	}

	/**
	 * @return the feasible
	 */
	public boolean isFeasible() {
		return feasible;
	}

	public void resetViolatedConstraints() {
		violatedConstraints.clear();
	}

	/**
	 * @param evaluated
	 *            the evaluated to set
	 */
	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;

		father.updateEvaluation();

	}

	public void setFather(Solution father) {
		this.father = father;
	}

	public boolean isClosedWorkload() {
		return closedWorkload;
	}

	public void setClosedWorkload(boolean closedWorkload) {
		this.closedWorkload = closedWorkload;
	}

	/**
	 * @param feasible
	 *            the feasible to set
	 */
	public void setFeasible(boolean feasible) {
		this.feasible = feasible;
	}

	/**
	 * @param lqnHandler
	 *            the lqnHandler to set
	 */
	public void setLqnHandler(LqnHandler lqnHandler) {
		this.lqnHandler = lqnHandler;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	private void setTiers(ArrayList<Tier> tiers) {
		this.tiers = tiers;
	}


	public void setWorkload(int workload) {
		this.workload = workload;
	}

	public String showStatus(String prefix) {
		String result = prefix + "lqnFile: " + lqnHandler.getLqnFilePath();
		result += "\n\tEvaluated: " + evaluated;
		result += "\tFeasible: " + feasible;
		for (Tier t : tiers) {
			result += "\n" + prefix + "Tier: " + t.getPcmName()+"\tId: "+t.getId();
			result += "\n" + t.showStatus(prefix + "\t");

		}
		return result;
	}

	private void updateCallChain() {
		// fetch all the functionalities from the components inside the tiers
		HashMap<String, Functionality> functionalities = new HashMap<String, Functionality>();
		for (Tier t : tiers)
			for (Functionality f : t.getFunctionalities())
				functionalities.put(f.getId(), f);

		// update the links looking at the id
		for (Functionality f1 : functionalities.values()) {
			// using a temporary hash to avoid concurrent modification
			HashMap<String, Functionality> newExternalCalls = new HashMap<>();
			for (String s : f1.getExternalCalls().keySet())
				newExternalCalls.put(s, functionalities.get(s));

			// put the new calls in the old one, hashes are the same so element
			// will be overwritten
			for (String s : newExternalCalls.keySet())
				f1.getExternalCalls().put(s, newExternalCalls.get(s));
		}

	}

	/**
	 * Updates the lqn model of the application
	 * 
	 * For each resource rewrites the corresponding element in the lqn model in
	 * the dom
	 */
	public void updateLqn() {

		for (Tier t : tiers) {			
			lqnHandler.updateElement(t);
		}

		if(isClosedWorkload())
			lqnHandler.setPopulation(workload);			
		else
			lqnHandler.setArrivalRate(workload);		

	}

	/**
	 * Updates performance values in the solution according to the evaluation
	 * @param results
	 */
	public void updateResults(LqnResultParser results) {
		this.resultParser = results; /*
		 * here we save the result to pass the
		 * result to the proxy later on
		 */
		for (Tier t : tiers)
			t.update(results);

	}

	public List<Constraint> getViolatedConstraints() {
		return violatedConstraints;
	}

}
