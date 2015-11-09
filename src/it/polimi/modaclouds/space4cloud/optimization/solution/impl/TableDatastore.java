package it.polimi.modaclouds.space4cloud.optimization.solution.impl;


public class TableDatastore extends NOSQL {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4760618858688214898L;

	public TableDatastore(String provider, String serviceType,
			String serviceName, String resourceName,
			boolean ssdOptimized, int storage, int replicas, boolean multiAzReplicas, int maxEntrySize, Compute compute) {
		super(provider, serviceType, serviceName, resourceName, DatabaseTechnology.TableDatastore.getName(), ssdOptimized, storage, replicas, multiAzReplicas, maxEntrySize, compute);
	}

}
