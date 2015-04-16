package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.ArrayList;
import java.util.List;

public class Backend extends Platform {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1946469142329927184L;

	public Backend(String provider, String serviceType, String serviceName,
			String resourceName, int replicas, int dataReplicas, boolean multiAzReplicas,
			List<String> supportedPlatforms,
			Compute iaasResources, int storage, int maxConnections,
			boolean replicasChangeable, boolean replicasPayedSingularly) {
		super(provider, serviceType, serviceName, resourceName, replicas, dataReplicas,
				multiAzReplicas, supportedPlatforms, PlatformType.Backend, iaasResources,
				storage, maxConnections, replicasChangeable, replicasPayedSingularly);
	}

	@Override
	public Backend clone() {
		ArrayList<String> supportedPlatformsClone = new ArrayList<String>();
		for (String s : getSupportedPlatforms())
			supportedPlatformsClone.add(new String(s));
		
		Backend b = new Backend(
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
		
		return b;
	}
}
