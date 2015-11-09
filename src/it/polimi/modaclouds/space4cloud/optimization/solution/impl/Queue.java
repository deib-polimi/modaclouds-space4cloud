package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Queue extends PaaS implements DelayCenter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -838586291140506813L;
	
	private static final Logger logger = LoggerFactory.getLogger(Queue.class);

	public Queue(String provider, String serviceType, String serviceName,
			String resourceName,
			int requestSize, boolean orderPreserving,
			int maxConnections, int dataReplicas, boolean multiAzReplicas, int maxRequests, double multiplyingFactor, double delay,
			Compute compute) {
	super(provider, serviceType, serviceName, resourceName, Queue.DEFAULT_REPLICAS, dataReplicas,
			Queue.DEFAULT_REPLICAS_CHANGEABLE, Queue.DEFAULT_REPLICAS_PAYED_SINGULARLY, compute);
		
		this.requestSize = requestSize;
		this.orderPreserving = orderPreserving;
		this.maxConnections = maxConnections;
		this.multiAzReplicas = multiAzReplicas;
		this.maxRequests = maxRequests;
		this.multiplyingFactor = multiplyingFactor;
		this.delay = delay;
	}
	
	private int requestSize;
	
	private boolean orderPreserving;
	
	private int maxConnections;
	
	private boolean multiAzReplicas;
	
	private int maxRequests;
	
	private double multiplyingFactor;
	
	private double delay;

	public static final int DEFAULT_REQUEST_SIZE = 64; // KB
	public static final boolean DEFAULT_ORDER_PRESERVING = false;
	public static final int DEFAULT_MAX_CONNECTIONS = Integer.MAX_VALUE;
	public static final int DEFAULT_REPLICAS = 1;
	public static final int DEFAULT_DATA_REPLICAS = 1;
	public static final boolean DEFAULT_MULTI_AZ_REPLICAS = false;
	public static final int DEFAULT_MAX_REQUESTS = Integer.MAX_VALUE;
	public static final double DEFAULT_MULTIPLYING_FACTOR = 3.0;
	public static final double DEFAULT_DELAY = 10.0;
	public static final boolean DEFAULT_REPLICAS_CHANGEABLE = false;
	public static final boolean DEFAULT_REPLICAS_PAYED_SINGULARLY = false;

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

	public void setMultiAzReplicas(boolean multiAzReplicas) {
		this.multiAzReplicas = multiAzReplicas;
	}
	
	@Override
	public CloudService clone() {
		Queue queue = null;
		
		try {
			queue = (Queue) super.clone();
			queue.setRequestSize(this.getRequestSize());
			queue.setOrderPreserving(this.isOrderPreserving());
			queue.setMaxConnections(this.getMaxConnections());
			queue.setMultiAzReplicas(this.isMultiAzReplicas());
			queue.setMaxRequests(this.getMaxRequests());
			queue.setMultiplyingFactor(this.getMultiplyingFactor());
			queue.setDelay(this.getDelay());
		} catch (Exception e) {
			logger.error("Error while cloning the Queue instance. Creating a new instance.");
			queue = new Queue(
					new String(getProvider()),
					new String(getServiceType()),
					new String(getServiceName()),
					new String(getResourceName()),
					this.getRequestSize(),
					this.isOrderPreserving(),
					this.getMaxConnections(),
					this.getDataReplicas(),
					this.isMultiAzReplicas(),
					this.getMaxRequests(),
					this.getMultiplyingFactor(),
					this.getDelay(),
					this.getCompute().clone()
					);
		}
		
		return queue;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof PaaS))
			return false;

		Queue tmp = (Queue) obj;

		return new EqualsBuilder()
				.append(requestSize, tmp.requestSize)
				.append(orderPreserving, tmp.orderPreserving)
				.append(maxConnections, tmp.maxConnections)
				.append(multiAzReplicas, tmp.multiAzReplicas)
				.append(maxRequests, tmp.maxRequests)
				.append(multiplyingFactor, tmp.multiplyingFactor)
				.append(delay, tmp.delay)
				.appendSuper(super.equals(obj)).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
				// two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
				.appendSuper(super.hashCode())
				.append(requestSize)
				.append(orderPreserving)
				.append(maxConnections)
				.append(multiAzReplicas)
				.append(maxRequests)
				.append(multiplyingFactor)
				.append(delay).toHashCode();

	}
	
	@Override
	public double getDataConsumed(int storage, int requests, int providers) {
		return requests * requestSize / 1024.0 / 1024.0; // Data in GB
	}

}
