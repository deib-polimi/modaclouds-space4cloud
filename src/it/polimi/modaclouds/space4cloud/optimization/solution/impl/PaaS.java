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

public abstract class PaaS extends CloudService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8088032094520247525L;

	public PaaS(String provider, String serviceType,
			String serviceName, String resourceName,
			int replicas, int dataReplicas, boolean replicasChangeable, boolean replicasPayedSingularly, Compute compute) {
		super(provider, serviceType, serviceName, resourceName);
		
		this.replicas = replicas;
		this.dataReplicas = dataReplicas;
		this.replicasChangeable = replicasChangeable;
		this.replicasPayedSingularly = replicasPayedSingularly;
		
		this.compute = compute;
		
		setLQNPropertyTAG("speedFactor"); // TODO: is this thing actually used?
	}
	
	private int replicas;
	
	private int dataReplicas;
	
	private boolean replicasChangeable;
	
	private boolean replicasPayedSingularly;
	
	private Compute compute;
	
	public Compute getCompute() {
		return compute;
	}

	public void setCompute(Compute compute) {
		this.compute = compute;
	}
	
	public static final int DEFAULT_REPLICAS = 1;
	public static final int DEFAULT_DATA_REPLICAS = 1;
	public static final boolean DEFAULT_REPLICAS_CHANGEABLE = false;
	public static final boolean DEFAULT_REPLICAS_PAYED_SINGULARLY = false;
	
	@Override
	public String showStatus(String prefix) {
		return super.showStatus(prefix) + "\tReplicas: " + getReplicas() + "\tData Replicas: " + getDataReplicas();
	}
	
	public int getDataReplicas() {
		return dataReplicas;
	}

	public void setDataReplicas(int dataReplicas) {
		this.dataReplicas = dataReplicas;
	}

	public boolean areReplicasChangeable() {
		return replicasChangeable;
	}

	public void setReplicasChangeable(boolean replicasChangeable) {
		this.replicasChangeable = replicasChangeable;
	}

	public boolean areReplicasPayedSingularly() {
		return replicasPayedSingularly;
	}

	public void setReplicasPayedSingularly(boolean replicasPayedSingularly) {
		this.replicasPayedSingularly = replicasPayedSingularly;
	}

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}

	public static enum PaaSType {
		Frontend("frontend"), Middleware("middleware"), Backend("backend"), Storage("storage"), Queue("queue"), Cache("cache"), NoSQL("nosql"), Relational("relational");
		
		private String name;
		
		private PaaSType(String name) {
			this.name = name;
		}
		
		public static PaaSType getById(int id) {
			PaaSType[] values = PaaSType.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}
		
		public static PaaSType getByName(String name) {
			PaaSType[] values = PaaSType.values();
			for (PaaSType pt : values) {
				if (pt.name.equals(name))
					return pt;
			}
			return null;
		}

		public static int size() {
			return PaaSType.values().length;			
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof PaaS))
			return false;

		PaaS tmp = (PaaS) obj;

		return new EqualsBuilder()
				.append(replicas, tmp.replicas)
				.append(dataReplicas, tmp.dataReplicas)
				.append(replicasChangeable, tmp.replicasChangeable)
				.append(replicasPayedSingularly, tmp.replicasPayedSingularly)
				.appendSuper(compute.equals(tmp.compute))
				.appendSuper(super.equals(obj)).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
				// two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
				.appendSuper(super.hashCode())
				.append(replicas)
				.append(dataReplicas)
				.append(replicasChangeable)
				.appendSuper(compute.hashCode())
				.append(replicasPayedSingularly).toHashCode();

	}
	
	@Override
	public CloudService clone() throws CloneNotSupportedException {
		PaaS clone = (PaaS) super.clone();
		clone.setReplicas(this.getReplicas());
		clone.setCompute(this.getCompute().clone());
		clone.setDataReplicas(this.getDataReplicas());
		clone.setReplicasChangeable(this.areReplicasChangeable());
		clone.setReplicasPayedSingularly(this.areReplicasPayedSingularly());
		return clone;
	}

}
