package it.polimi.modaclouds.space4cloud.optimization.solution.impl;


public class BlobDatastore extends NOSQL {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7145407469255982629L;

	public BlobDatastore(String provider, String serviceType,
			String serviceName, String resourceName,
			boolean ssdOptimized, int storage, int replicas, boolean multiAzReplicas, int maxEntrySize, Compute compute) {
		super(provider, serviceType, serviceName, resourceName, DatabaseTechnology.BlobDatastore.getName(), ssdOptimized, storage, replicas, multiAzReplicas, maxEntrySize, compute);
	}

}
