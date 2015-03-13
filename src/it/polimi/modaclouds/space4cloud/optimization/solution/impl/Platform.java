package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;

import java.util.List;

public abstract class Platform extends PaaS {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4422544682299953719L;

	public Platform(String provider, String serviceType, String serviceName,
			String resourceName,
			int replicas, boolean multiAzReplicas,
			List<String> supportedPlatforms, PlatformType platformType, Compute iaasResources, int storage, int maxConnections) {
		super(provider, serviceType, serviceName, resourceName, replicas);
		this.multiAzReplicas = multiAzReplicas;
		this.supportedPlatforms = supportedPlatforms;
		this.platformType = platformType;
		this.iaasResources = iaasResources;
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

	@Override
	public double getResponseTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update(LqnResultParser parser) {
		// TODO Auto-generated method stub
		
	}
	
	private List<String> supportedPlatforms;
	
	private PlatformType platformType;
	
	private Compute iaasResources;
	
	private boolean multiAzReplicas;
	
	private int storage;
	
	private int maxConnections;
	
	public static final int DEFAULT_MAX_CONNECTIONS = Integer.MAX_VALUE;
	public static final int DEFAULT_REPLICAS = 1;
	public static final boolean DEFAULT_MULTI_AZ_REPLICAS = false;
	public static final int DEFAULT_STORAGE = Integer.MAX_VALUE;

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

	public Compute getIaasResources() {
		return iaasResources;
	}

	public void setIaasResources(Compute iaasResources) {
		this.iaasResources = iaasResources;
	}
	
	public void setIaasResources(String provider, String serviceType, String serviceName,
			String resourceName, int replicas,
			int numberOfCores, double speed, int ram) {
		iaasResources = new Compute(provider, serviceType, serviceName, 
				resourceName, replicas, numberOfCores, speed, ram);
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

}
