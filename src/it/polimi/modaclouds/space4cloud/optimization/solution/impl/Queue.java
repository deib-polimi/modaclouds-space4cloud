package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;

public class Queue extends PaaS {

	/**
	 * 
	 */
	private static final long serialVersionUID = -838586291140506813L;

	public Queue(String provider, String serviceType, String serviceName,
			String resourceName,
			int requestSize, boolean orderPreserving,
			int maxConnections, int replicas, boolean multiAzReplicas, int maxRequests, double multiplyingFactor, double delay,
			Compute compute) {
		super(provider, serviceType, serviceName, resourceName, replicas);
		
		this.requestSize = requestSize;
		this.orderPreserving = orderPreserving;
		this.maxConnections = maxConnections;
		this.multiAzReplicas = multiAzReplicas;
		this.maxRequests = maxRequests;
		this.compute = compute;
		this.multiplyingFactor = multiplyingFactor;
		this.delay = delay;
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
	
	private int requestSize;
	
	private boolean orderPreserving;
	
	private int maxConnections;
	
	private boolean multiAzReplicas;
	
	private int maxRequests;
	
	private double multiplyingFactor;
	
	private double delay;

	public static final int DEFAULT_REQUEST_SIZE = 64;
	public static final boolean DEFAULT_ORDER_PRESERVING = false;
	public static final int DEFAULT_MAX_CONNECTIONS = Integer.MAX_VALUE;
	public static final int DEFAULT_REPLICAS = 1;
	public static final boolean DEFAULT_MULTI_AZ_REPLICAS = false;
	public static final int DEFAULT_MAX_REQUESTS = Integer.MAX_VALUE;
	public static final double DEFAULT_MULTIPLYING_FACTOR = 3.0;
	public static final double DEFAULT_DELAY = 10.0;

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public double getMultiplyingFactor() {
		return multiplyingFactor;
	}

	public void setMultiplyingFactor(double multiplyingFactor) {
		this.multiplyingFactor = multiplyingFactor;
	}

	public int getRequestSize() {
		return requestSize;
	}

	public void setRequestSize(int requestSize) {
		this.requestSize = requestSize;
	}

	public boolean isOrderPreserving() {
		return orderPreserving;
	}

	public void setOrderPreserving(boolean orderPreserving) {
		this.orderPreserving = orderPreserving;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public boolean isMultiAzReplicas() {
		return multiAzReplicas;
	}

	public int getMaxRequests() {
		return maxRequests;
	}

	public void setMaxRequests(int maxRequests) {
		this.maxRequests = maxRequests;
	}
	
	private Compute compute;

	public Compute getCompute() {
		return compute;
	}

	public void setCompute(Compute compute) {
		this.compute = compute;
	}

	public void setMultiAzReplicas(boolean multiAzReplicas) {
		this.multiAzReplicas = multiAzReplicas;
	}
	
	

}
