package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class Platform extends PaaS {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4422544682299953719L;

	public Platform(String provider, String serviceType, String serviceName,
			String resourceName,
			int replicas, int dataReplicas, boolean multiAzReplicas,
			List<String> supportedPlatforms, PlatformType platformType, Compute compute, int storage, int maxConnections,
			boolean replicasChangeable, boolean replicasPayedSingularly) {
		super(provider, serviceType, serviceName, resourceName, replicas, dataReplicas, replicasChangeable, replicasPayedSingularly, compute);
		this.multiAzReplicas = multiAzReplicas;
		this.supportedPlatforms = supportedPlatforms;
		this.platformType = platformType;
		this.storage = storage;
		this.maxConnections = maxConnections;
	}
	
	private double utilization;

	public double getUtilization() {
		return utilization;
	}

	public void updateUtilization(double util) {
		// update the utilization
		if (util >= 0)
			utilization = util;
		else
			utilization = -1;
		// System.err.println("Processor name "+getName()+" not found in the results");

	}
	
	private List<String> supportedPlatforms;
	
	private PlatformType platformType;
	
	private boolean multiAzReplicas;
	
	private int storage;
	
	private int maxConnections;
	
	public static final int DEFAULT_MAX_CONNECTIONS = Integer.MAX_VALUE;
	public static final int DEFAULT_REPLICAS = 1;
	public static final int DEFAULT_DATA_REPLICAS = 1;
	public static final boolean DEFAULT_MULTI_AZ_REPLICAS = false;
	public static final int DEFAULT_STORAGE = Integer.MAX_VALUE;
	public static final boolean DEFAULT_REPLICAS_CHANGEABLE = true;
	public static final boolean DEFAULT_REPLICAS_PAYED_SINGULARLY = true;

	public List<String> getSupportedPlatforms() {
		return supportedPlatforms;
	}

	public void setSupportedPlatforms(List<String> supportedPlatforms) {
		this.supportedPlatforms = supportedPlatforms;
	}

	public PlatformType getPlatformType() {
		return platformType;
	}

	public void setPlatformType(PlatformType platformType) {
		this.platformType = platformType;
	}
	
	public void setIaasResources(String provider, String serviceType, String serviceName,
			String resourceName, int replicas,
			int numberOfCores, double speed, int ram) {
		setCompute(new Compute(provider, serviceType, serviceName, 
				resourceName, replicas, numberOfCores, speed, ram));
	}

	public boolean isMultiAzReplicas() {
		return multiAzReplicas;
	}

	public void setMultiAzReplicas(boolean multiAzReplicas) {
		this.multiAzReplicas = multiAzReplicas;
	}

	public int getStorage() {
		return storage;
	}

	public void setStorage(int storage) {
		this.storage = storage;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}
	
	public static enum PlatformType {
		Frontend("frontend"), Backend("backend");
		
		private String name;
		
		private PlatformType(String name) {
			this.name = name;
		}
		
		public static PlatformType getById(int id) {
			PlatformType[] values = PlatformType.values();
			if (id < 0)
				id = 0;
			else if (id >= values.length)
				id = values.length - 1;
			return values[id];
		}
		
		public static PlatformType getByName(String name) {
			PlatformType[] values = PlatformType.values();
			for (PlatformType pt : values) {
				if (pt.name.equals(name))
					return pt;
			}
			return null;
		}

		public static int size() {
			return PlatformType.values().length;			
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Platform))
			return false;

		Platform tmp = (Platform) obj;
		
		boolean sameSupportedPlatforms = true;
		
		if (supportedPlatforms.size() != tmp.supportedPlatforms.size() || supportedPlatforms == tmp.supportedPlatforms) {
			sameSupportedPlatforms = false;
		} else {
			for (int i = 0; i < supportedPlatforms.size() && sameSupportedPlatforms; ++i) {
				boolean found = false;
				for (int j = 0; j < tmp.supportedPlatforms.size() && !found; ++j) {
					if (supportedPlatforms.get(i).equals(tmp.supportedPlatforms.get(j)))
						found = true;
				}
				if (!found)
					sameSupportedPlatforms = false;
			}
		}

		return new EqualsBuilder()
				.appendSuper(sameSupportedPlatforms)
				.append(platformType, tmp.platformType)
				.append(multiAzReplicas, tmp.multiAzReplicas)
				.append(storage, tmp.storage)
				.append(maxConnections, tmp.maxConnections)
				.appendSuper(super.equals(obj)).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
				// two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
				.appendSuper(super.hashCode())
				.append(supportedPlatforms)
				.append(platformType)
				.append(multiAzReplicas)
				.append(storage)
				.append(maxConnections).toHashCode();

	}

}
