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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Storage extends IaaS {

	private static final Logger logger = LoggerFactory.getLogger(Storage.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 6172296990102061040L;
	private int size;

	public Storage(String provider, String serviceType, String serviceName,
			String resourceName, int size) {
		super(provider, serviceType, serviceName, resourceName);
		this.size = size;

	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
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
	public CloudService clone() {
		Storage st = null;

		try {
			st = (Storage) super.clone();
			st.setReplicas(this.getReplicas());
			st.setSize(this.getSize());
		} catch (Exception e) {
			logger.error("Error while cloning the Storage instance. Creating a new instance.");
			st = new Storage(new String(this.getProvider()), new String(
					this.getServiceType()), new String(this.getServiceName()),
					new String(this.getResourceName()), this.size);
			st.setReplicas(this.getReplicas());
		}

		return st;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof PaaS))
			return false;

		Storage tmp = (Storage) obj;

		return new EqualsBuilder().append(size, tmp.size)
				.appendSuper(super.equals(obj)).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
		// two randomly chosen prime numbers
		// if deriving: appendSuper(super.hashCode()).
				.appendSuper(super.hashCode()).append(size).toHashCode();
	}

}
