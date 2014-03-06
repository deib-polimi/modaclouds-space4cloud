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
package it.polimi.modaclouds.space4cloud.optimization.constraints;


public abstract class Constraint {

	protected String resourceId;
	protected Metric metric;
	protected Unit unit;
	protected int priority;
	//protected String unit;
	protected IRange range;
		
	public abstract double checkConstraintDistance(Object masurement);
	
	
	
	public Constraint(String resourceId, Metric metric, int priority, Unit unit) {
		this.resourceId = resourceId;
		this.metric = metric;
		this.priority = priority;
		this.unit = unit;
	}
	
	public int getPriority(){
		return priority; 
	}
	
	public String getResourceID(){
		return resourceId;
	}

	public Metric getMetric(){
		return metric;
	}
	
	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}	

	public IRange getRange() {
		return range;
	}

	public void setRange(IRange range) {
		this.range = range;
	}
	

	
}
