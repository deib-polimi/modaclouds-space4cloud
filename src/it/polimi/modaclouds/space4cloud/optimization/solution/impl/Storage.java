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


public class Storage extends IaaS {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6172296990102061040L;
	private int size;

	public Storage(String provider, String serviceType,
			String serviceName, String resourceName, int size) {
		super(provider, serviceType, serviceName, resourceName);
		this.size = size;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService
	 * #clone()
	 */
	@Override
	public Storage clone() {
		Storage st = new Storage(new String(this.getProvider()), new String(
				this.getServiceType()), new String(this.getServiceName()),
				new String(this.getResourceName()), this.size);
		st.setReplicas(this.getReplicas());
		return st;
	}

	public int getSpeed() {
		return size;
	}

}
