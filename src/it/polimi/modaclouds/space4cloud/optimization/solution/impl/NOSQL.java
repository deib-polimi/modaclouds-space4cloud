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

/**
 * The Class NOSQL.
 * 
 * @author Giovanni Gibilisco, Michele Ciavotta
 * 
 */
public class NOSQL extends Database {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1597143518905843001L;
	
	private static final Logger logger = LoggerFactory.getLogger(NOSQL.class);

	/**
	 * Instantiates a new nosql.
	 * 
	 * @param name
	 *            the name
	 * @param id
	 *            the id
	 * @param provider
	 *            the provider
	 * @param serviceType
	 *            the service type
	 * @param serviceName
	 *            the service name
	 * @param resourceName
	 *            the resource name
	 */
	public NOSQL(String provider, String serviceType,
			String serviceName, String resourceName, String technology,
			boolean ssdOptimized, int storage, int replicas, boolean multiAzReplicas, int maxEntrySize, Compute compute) {
		super(provider, serviceType, serviceName, resourceName, DatabaseType.NoSQL, technology, ssdOptimized, storage, replicas, multiAzReplicas, compute);
		
		this.maxEntrySize = maxEntrySize;
	}

	private int maxEntrySize;
	
	public static final int DEFAULT_MAX_ENTRY_SIZE = 400;
	
	public int getMaxEntrySize() {
		return maxEntrySize;
	}

	public void setMaxEntrySize(int maxEntrySize) {
		this.maxEntrySize = maxEntrySize;
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
		NOSQL nosql = null;
		
		try {
			nosql = (NOSQL) super.clone();
			nosql.setMaxEntrySize(this.getMaxEntrySize());
		} catch (Exception e) {
			logger.error("Error while cloning the NOSQL instance. Creating a new instance.");
			nosql = new NOSQL(new String(this.getProvider()), new String(
					this.getServiceType()), new String(this.getServiceName()),
					new String(this.getResourceName()),
					new String(getTechnology()),
					this.isSsdOptimized(), this.getStorage(), this.getReplicas(), this.isMultiAzReplicas(), this.getMaxEntrySize(),
					this.getCompute().clone()
					);
		}

		return nosql;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof NOSQL))
			return false;

		NOSQL tmp = (NOSQL) obj;

		return new EqualsBuilder()
				.append(maxEntrySize, tmp.maxEntrySize)
				.appendSuper(super.equals(obj)).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
				// two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
				.appendSuper(super.hashCode())
				.append(maxEntrySize).toHashCode();

	}
	
	public static enum DatabaseTechnology {
		TableDatastore("table"), BlobDatastore("document");
		
		private String name;
		
		private DatabaseTechnology(String name) {
			this.name = name;
		}
		
		public static DatabaseTechnology getById(int id) {
			DatabaseTechnology[] values = DatabaseTechnology.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}
		
		public static DatabaseTechnology getByName(String name) {
			DatabaseTechnology[] values = DatabaseTechnology.values();
			for (DatabaseTechnology pt : values) {
				if (pt.name.equals(name))
					return pt;
			}
			return null;
		}

		public static int size() {
			return DatabaseTechnology.values().length;			
		}
		
		public String getName() {
			return name;
		}
	}

}
