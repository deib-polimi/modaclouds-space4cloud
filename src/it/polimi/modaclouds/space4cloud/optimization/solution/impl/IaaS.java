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


public abstract class IaaS extends CloudService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 849403988955004940L;
	/** The number of replicas of the cloud resource. */
	int replicas;

	public IaaS(String provider, String serviceType,
			String serviceName, String resourceName) {
		super(provider, serviceType, serviceName, resourceName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public CloudService clone() throws CloneNotSupportedException {
		IaaS clone = (IaaS) super.clone();
		clone.setReplicas(this.getReplicas());
		return clone;
	}

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}

	@Override
	public String showStatus(String prefix) {
		return super.showStatus(prefix) + "\tReplicas: " + replicas;
	}
}
