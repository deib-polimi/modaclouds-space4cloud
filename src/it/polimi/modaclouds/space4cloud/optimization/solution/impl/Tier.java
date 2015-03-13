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

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.IResponseTimeConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.IUtilizationConstrainable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tier implements Cloneable, IResponseTimeConstrainable, IUtilizationConstrainable,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2057134756249273580L;

	
	
	/** The cloud resource. */
	private CloudService cloudService;

	/** The list of components deployed on the cloud resource. */
	private ArrayList<Component> components = new ArrayList<Component>();

	/** The id of the Tier*/
	private String id;
	
	/** The name of the Tier*/
	private String name;
	
	public Tier(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public void addComponent(Component component) {
		this.components.add(component);
		component.setContainer(this);
	}

	@Override
	public Tier clone() {

		Tier t;
		try {
			t = (Tier) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			t = new Tier(this.id,this.name);
		}

		// clone Cloud Resource
		try {
			t.setCloudService(getCloudService().clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// cloning the component list
		t.setComponents(new ArrayList<Component>());
		for (Component c : this.getComponents()) {
			t.addComponent(c.clone());
		}

		return t;

	}

	public CloudService getCloudService() {
		return cloudService;
	}

	public List<Component> getComponents() {
		return components;
	}

	public Map<String, ? extends IConstrainable> getConstrinableResources() {
		HashMap<String, IConstrainable> resources = new HashMap<>();		
		for (Component c : components) {
			if (c instanceof IConstrainable)
				resources.put(c.getId(), c);
			resources.putAll(c.getConstrainableResources());
		}

		return resources;
	}

	public List<Functionality> getFunctionalities() {
		ArrayList<Functionality> functionalities = new ArrayList<Functionality>();
		for (Component c : components)
			functionalities.addAll(c.getFunctionalities());
		return functionalities;
	}

	@Override
	public String getId() {		
		return id;
	}

	public String getName() {
		return name;
	}


	@Override
	public double getResponseTime() {
		double avg = 0;
		for (Component c : components)
			avg += c.getResponseTime();
		return avg / components.size();
	}

	public void setCloudService(CloudService cloudService) {
		this.cloudService = cloudService;
	}

	private void setComponents(ArrayList<Component> components) {
		this.components = components;

	}


	public void setService(CloudService cloudService) {
		this.cloudService = cloudService;
	}

	public String showStatus(String prefix) {
		return cloudService.showStatus(prefix);
	}

	@Override
	public void update(LqnResultParser results) {
		// update resource
		
		if(cloudService instanceof Compute)
			((Compute)cloudService).updateUtilization(results.getUtilization(getName()));
		else if(cloudService instanceof Platform)
			((Platform)cloudService).updateUtilization(results.getUtilization(getName()));
	

		// update components
		for (Component c : components)
			c.update(results);
	}

	@Override
	public double getUtilization() {
		if(cloudService instanceof Compute)
			return ((Compute)cloudService).getUtilization();
		else if (cloudService instanceof Platform)
			return ((Platform)cloudService).getUtilization();
			//TODO: we should raise some kind of warning here
			return -1;
	}
	
	public int getTotalRequests() {
		int total = 0;
		for (Component c : components)
			for (Functionality f : c.getFunctionalities())
				total += f.getRequests();
		
		return total;
	}

}
