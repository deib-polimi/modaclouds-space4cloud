package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.List;

public class Frontend extends Platform {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9015012342295080115L;

	public Frontend(String provider, String serviceType, String serviceName,
			String resourceName,
			int replicas, int dataReplicas, boolean multiAzReplicas,
			List<String> supportedPlatforms,
			Compute iaasResources, int storage, int maxConnections, boolean replicasChangeable, boolean replicasPayedSingularly) {
		super(provider, serviceType, serviceName, resourceName,
				replicas, dataReplicas, multiAzReplicas, supportedPlatforms, PlatformType.Frontend, iaasResources,
				storage, maxConnections, replicasChangeable, replicasPayedSingularly);
		// TODO Auto-generated constructor stub
	}

}
