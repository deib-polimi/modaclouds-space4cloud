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
import java.util.Map;

/**
 * @author MODAClouds
 * The class that defines a Component
 */
public class Component implements Cloneable, IResponseTimeConstrainable, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3466902523628946119L;
	private String id;
	private ArrayList<Functionality> functionalities = new ArrayList<Functionality>();
	private Tier container;

	public Component(String id) {
		this.id = id;
	}
	
	public void addFunctionality(Functionality functionality){
		functionality.setContainer(this);
		functionalities.add(functionality);
	}

	public Component clone(){
		
		Component c;
		try {
			c = (Component) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			c = new Component(this.getId());
		}
		
		// now we have to clone the list of Functionality
		c.setFunctionalities(new ArrayList<Functionality>(getFunctionalities().size()));	
		for (Functionality f : this.functionalities) {
			c.addFunctionality(f.clone());
		}
		
		c.setContainer(null);
		
		return c;
	}
	
	public Map<String, ? extends IConstrainable> getConstrainableResources() {
		HashMap<String, IConstrainable> resources = new HashMap<>();		
		for(Functionality f:functionalities){
			if(f instanceof IConstrainable)
				resources.put(f.getId(), f);
		}
		
		return resources;
	}
	
	public Tier getContainer() {
		return container;
	}	
	
	public ArrayList<Functionality> getFunctionalities(){
		return functionalities;
	}

	public String getId() {
		return id;
	}

	@Override
	public double getResponseTime(){
		double avg = 0;
		for(Functionality f:functionalities)
			avg +=f.getResponseTime();
		return avg/functionalities.size();
	}
	
	public void setContainer(Tier tier) {
		container = tier;		
	}

	private void setFunctionalities(ArrayList<Functionality> functionalities) {
		this.functionalities = functionalities;
	}

	public void showStatus(String prefix) {
		System.out.println(prefix+"Component id: "+getId()+" avgResponsetime "+getResponseTime());
		for(Functionality f: functionalities)
			f.show(prefix+"\t");
		
	}

	@Override
	public void update(LqnResultParser parser) {
		for(Functionality f:functionalities)
			f.update(parser);
	}
}
