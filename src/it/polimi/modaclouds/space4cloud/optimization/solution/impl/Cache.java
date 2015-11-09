package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cache extends PaaS {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4884178997316107198L;
	
	private static final Logger logger = LoggerFactory.getLogger(Cache.class);

	public Cache(String provider, String serviceType, String serviceName,
			String resourceName,
			String engine, int dataReplicas, boolean multiAzReplicas, int maxConnections, int storage,
			Compute compute) {
		super(provider, serviceType, serviceName, resourceName, Cache.DEFAULT_REPLICAS, dataReplicas,
				Cache.DEFAULT_REPLICAS_CHANGEABLE, Cache.DEFAULT_REPLICAS_PAYED_SINGULARLY, compute);
		this.engine = engine;
		this.multiAzReplicas = multiAzReplicas;
		this.maxConnections = maxConnections;
		this.storage = storage;
	}
	
	private String engine;
	
	private boolean multiAzReplicas;
	
	private int maxConnections;
	
	private int storage;
	
	public static final int DEFAULT_STORAGE = Integer.MAX_VALUE;
	public static final int DEFAULT_MAX_CONNECTIONS = Integer.MAX_VALUE;
	public static final int DEFAULT_REPLICAS = 1;
	public static final int DEFAULT_DATA_REPLICAS = 1;
	public static final boolean DEFAULT_MULTI_AZ_REPLICAS = false;
	public static final boolean DEFAULT_REPLICAS_CHANGEABLE = false;
	public static final boolean DEFAULT_REPLICAS_PAYED_SINGULARLY = false;

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
	
	@Override
	public CloudService clone() {
		Cache c = null;
		
		try {
			c = (Cache) super.clone();
			c.setEngine(new String(this.getEngine()));
			c.setMultiAzReplicas(this.isMultiAzReplicas());
			c.setMaxConnections(this.getMaxConnections());
			c.setStorage(this.getStorage());
		} catch (Exception e) {
			logger.error("Error while cloning the Cache instance. Creating a new instance.");
			c = new Cache(
					new String(getProvider()),
					new String(getServiceType()),
					new String(getServiceName()),
					new String(getResourceName()),
					new String(getEngine()),
					getDataReplicas(),
					isMultiAzReplicas(),
					getMaxConnections(),
					getStorage(),
					getCompute().clone()
					);
		}
		
		return c;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Cache))
			return false;

		Cache tmp = (Cache) obj;

		return new EqualsBuilder()
				.append(engine, tmp.engine)
				.append(multiAzReplicas, tmp.multiAzReplicas)
				.append(maxConnections, tmp.maxConnections)
				.append(storage, tmp.storage)
				.appendSuper(super.equals(obj)).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
				// two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
				.appendSuper(super.hashCode())
				.append(engine)
				.append(multiAzReplicas)
				.append(maxConnections)
				.append(storage).toHashCode();

	}
}
