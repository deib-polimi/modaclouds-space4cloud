package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.List;

public class Frontend extends Platform {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9015012342295080115L;

	public Frontend(String provider, String serviceType, String serviceName,
			String resourceName,
			int replicas, boolean multiAzReplicas,
			List<String> supportedPlatforms,
			Compute iaasResources, int storage, int maxConnections) {
		super(provider, serviceType, serviceName, resourceName,
				replicas, multiAzReplicas, supportedPlatforms, PlatformType.Frontend, iaasResources,
				storage, maxConnections);
		// TODO Auto-generated constructor stub
	}

}
