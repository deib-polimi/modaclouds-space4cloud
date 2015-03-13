package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.List;

public class Backend extends Platform {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1946469142329927184L;

	public Backend(String provider, String serviceType, String serviceName,
			String resourceName, int replicas, boolean multiAzReplicas,
			List<String> supportedPlatforms,
			Compute iaasResources, int storage, int maxConnections) {
		super(provider, serviceType, serviceName, resourceName, replicas,
				multiAzReplicas, supportedPlatforms, PlatformType.Backend, iaasResources,
				storage, maxConnections);
		// TODO Auto-generated constructor stub
	}

}
