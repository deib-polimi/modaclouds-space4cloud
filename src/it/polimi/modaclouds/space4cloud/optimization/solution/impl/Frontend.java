package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.ArrayList;
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
	
	@Override
	public Frontend clone() {
		ArrayList<String> supportedPlatformsClone = new ArrayList<String>();
		for (String s : getSupportedPlatforms())
			supportedPlatformsClone.add(new String(s));
		
		Frontend f = new Frontend(
				new String(getProvider()),
				new String(getServiceType()),
				new String(getServiceName()),
				new String(getResourceName()),
				getReplicas(),
				getDataReplicas(),
				isMultiAzReplicas(),
				supportedPlatformsClone,
				getCompute().clone(),
				getStorage(),
				getMaxConnections(),
				areReplicasChangeable(),
				areReplicasPayedSingularly()
				);
		
		return f;
	}

}
