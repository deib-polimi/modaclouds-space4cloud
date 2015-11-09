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

// TODO: Auto-generated Javadoc
/**
 * The Class SQL.
 */
public class SQL extends Database {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4717640652547301426L;
	
	private static final Logger logger = LoggerFactory.getLogger(SQL.class);

	/**
	 * Instantiates a new sql.
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
	public SQL(String provider, String serviceType,
			String serviceName, String resourceName, String technology,
			boolean ssdOptimized, int storage, int maxConnections, int maxRollbackHours,
			int replicas, boolean multiAzReplicas, Compute compute) {
		super(provider, serviceType, serviceName, resourceName, DatabaseType.Relational, technology, ssdOptimized, storage, replicas, multiAzReplicas, compute);
		
		this.maxConnections = maxConnections;
		this.maxRollbackHours = maxRollbackHours;
	}
	
	private int maxConnections;
	
	private int maxRollbackHours;
	
	public static final int DEFAULT_MAX_CONNECTIONS = Integer.MAX_VALUE;
	public static final int DEFAULT_MAX_ROLLBACK_HOURS = 0;

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public int getMaxRollbackHours() {
		return maxRollbackHours;
	}

	public void setMaxRollbackHours(int maxRollbackHours) {
		this.maxRollbackHours = maxRollbackHours;
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
		SQL sql = null;
		
		try {
			sql = (SQL) super.clone();
			sql.setMaxConnections(this.getMaxConnections());
			sql.setMaxRollbackHours(this.getMaxRollbackHours());
		} catch (Exception e) {
			logger.error("Error while cloning the SQL instance. Creating a new instance.");
			sql = new SQL(
					new String(this.getProvider()),
					new String(this.getServiceType()),
					new String(this.getServiceName()),
					new String(this.getResourceName()),
					new String(this.getTechnology()),
					this.isSsdOptimized(),
					this.getStorage(),
					this.getMaxConnections(),
					this.getMaxRollbackHours(),
					this.getReplicas(),
					this.isMultiAzReplicas(),
					this.getCompute().clone()
					);
		}
		
		return sql;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof SQL))
			return false;

		SQL tmp = (SQL) obj;

		return new EqualsBuilder()
				.append(maxConnections, tmp.maxConnections)
				.append(maxRollbackHours, tmp.maxRollbackHours)
				.appendSuper(super.equals(obj)).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
				// two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
				.appendSuper(super.hashCode())
				.append(maxConnections)
				.append(maxRollbackHours).toHashCode();

	}

}
