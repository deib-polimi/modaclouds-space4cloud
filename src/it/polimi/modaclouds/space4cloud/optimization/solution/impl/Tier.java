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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tier implements Cloneable, IResponseTimeConstrainable,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2057134756249273580L;

	/** The cloud resource. */
	private CloudService cloudService;

	/** The list of components deployed on the cloud resource. */
	private ArrayList<Component> components = new ArrayList<Component>();

	/** The id cloud resource. */
	private String idCloudResource;

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
			t = new Tier();
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

		t.setIdCloudResource(new String(this.getIdCloudResource()));

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
		if (cloudService instanceof IConstrainable) // should be true by
													// construction...
			resources.put(cloudService.getId(), cloudService);
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
		return cloudService.getName();
	}

	/**
	 * @return the idCloudResource
	 */
	public String getIdCloudResource() {
		return idCloudResource;
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

	/**
	 * @param idCloudResource
	 *            the idCloudResource to set
	 */
	public void setIdCloudResource(String idCloudResource) {
		this.idCloudResource = idCloudResource;
	}

	public void setService(CloudService cloudService) {
		this.cloudService = cloudService;
		this.setIdCloudResource(cloudService.getId());
	}

	public String showStatus(String prefix) {
		return cloudService.showStatus(prefix);
	}

	@Override
	public void update(LqnResultParser results) {
		// update resource
		cloudService.update(results);

		// update components
		for (Component c : components)
			c.update(results);
	}

}
