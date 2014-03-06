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
/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;

import java.util.Set;


/**
 * @author Michele Ciavotta
 *
 */
public abstract class ArchitecturalConstraint extends Constraint {

	/**
	 * @param id
	 * @param metric
	 * @param priority
	 * @param unit
	 */
	public ArchitecturalConstraint(String id, Metric metric, int priority,
			Unit unit) {
		super(id, metric, priority, unit);		
		// TODO Auto-generated constructor stub
	}

	public void setInSet(Set<String> set) {
		// TODO Auto-generated method stub
		
	}

	public String getInSet() {
		// TODO Auto-generated method stub
		return null;
	}
	public void setOutSet(Set<String> set) {
		// TODO Auto-generated method stub
		
	}
	public String getOutSet() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public abstract boolean checkConstraint(CloudService resource);




}
