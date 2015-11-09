package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Backend extends Platform {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1946469142329927184L;
	
	private static final Logger logger = LoggerFactory.getLogger(Backend.class);

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
	public CloudService clone() {
		ArrayList<String> supportedPlatformsClone = new ArrayList<String>();
		for (String s : getSupportedPlatforms())
			supportedPlatformsClone.add(new String(s));
		
		Backend b = null;
		
		try {
			b = (Backend) super.clone();
			b.setMultiAzReplicas(this.isMultiAzReplicas());
			b.setSupportedPlatforms(supportedPlatformsClone);
			b.setPlatformType(this.getPlatformType());
			b.setMaxConnections(this.getMaxConnections());
		} catch (Exception e) {
			logger.error("Error while cloning the Backend instance. Creating a new instance.");
			b = new Backend(
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
		}
		
		return b;
	}

}
