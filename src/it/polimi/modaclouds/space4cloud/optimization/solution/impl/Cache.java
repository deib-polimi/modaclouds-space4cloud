package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;

public class Cache extends PaaS {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4884178997316107198L;

	public Cache(String provider, String serviceType, String serviceName,
			String resourceName,
			String engine, int replicas, boolean multiAzReplicas, int maxConnections, int storage,
			Compute compute) {
		super(provider, serviceType, serviceName, resourceName, replicas);
		this.engine = engine;
		this.multiAzReplicas = multiAzReplicas;
		this.maxConnections = maxConnections;
		this.storage = storage;
		this.compute = compute;
	}
	
	private String engine;
	
	private boolean multiAzReplicas;
	
	private int maxConnections;
	
	private int storage;
	
	private Compute compute;
	
	public static final int DEFAULT_STORAGE = Integer.MAX_VALUE;
	public static final int DEFAULT_MAX_CONNECTIONS = Integer.MAX_VALUE;
	public static final int DEFAULT_REPLICAS = 1;
	public static final boolean DEFAULT_MULTI_AZ_REPLICAS = false;

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}
	
	public boolean isMultiAzReplicas() {
		return multiAzReplicas;
	}

	public void setMultiAzReplicas(boolean multiAzReplicas) {
		this.multiAzReplicas = multiAzReplicas;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public int getStorage() {
		return storage;
	}

	public void setStorage(int storage) {
		this.storage = storage;
	}

	public Compute getCompute() {
		return compute;
	}

	public void setCompute(Compute compute) {
		this.compute = compute;
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

}
