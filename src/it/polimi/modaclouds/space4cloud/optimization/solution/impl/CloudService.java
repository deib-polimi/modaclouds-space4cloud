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

/**
 * @author MODAClouds The abstract class CloudService define the general Cloud
 *         Resource type.
 */
public abstract class CloudService implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9126279019777559621L;
	private String resourceName; 
	private String LQNPropertyTAG;
	private String provider;
	private String serviceType;
	private String serviceName;

	public CloudService(String provider,
			String serviceType, String serviceName, String resourceName) {
		this.provider = provider;
		this.serviceType = serviceType;
		this.serviceName = serviceName;
		this.resourceName = resourceName;
	}

	@Override
	public CloudService clone() throws CloneNotSupportedException {
		CloudService s = (CloudService) super.clone();
		// copy mutable strings
		s.setResourceName(new String(this.getResourceName()));
		s.setLQNPropertyTAG(new String(this.getLQNPropertyTAG()));
		s.setProvider(new String(this.getProvider()));
		s.setServiceName(new String(this.getServiceName()));
		s.setServiceType(new String(this.getServiceType()));
		return s;
	}



	protected String getLQNPropertyTAG() {
		return LQNPropertyTAG;
	}


	public String getProvider() {
		return provider;
	}

	public String getResourceName() {
		return resourceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getServiceType() {
		return serviceType;
	}

	protected void setLQNPropertyTAG(String lQNPropertyTAG) {
		LQNPropertyTAG = lQNPropertyTAG;
	}

	private void setProvider(String provider) {
		this.provider = provider;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	private void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	private void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String showStatus(String prefix) {
		return prefix + "\t type: "
				+ getServiceType() + "\t provider: " + getProvider()
				+ "\t service Name: " + getServiceName() + "\t resource Name: "
				+ getResourceName();
	}
}
