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

import java.util.ArrayList;

public class HourlySolution {
	private ArrayList<Instance> instances;

	private ArrayList<Double> balancing;

	private Component exposedComponent;

	public HourlySolution(ArrayList<Instance> instances,
			Component exposedComponent) {
		this.instances = instances;
		this.exposedComponent = exposedComponent;
	}

	public void evaluateApplication() {

	}

	public ArrayList<Double> getBalancing() {
		return balancing;
	}

	public Component getExposedComponent() {
		return exposedComponent;
	}

	public ArrayList<Instance> getInstances() {
		return instances;
	}

	public void setBalancing(ArrayList<Double> balancing) {
		this.balancing = balancing;
	}

}
