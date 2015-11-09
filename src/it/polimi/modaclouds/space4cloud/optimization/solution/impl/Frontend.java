package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Frontend extends Platform {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9015012342295080115L;
	
	private static final Logger logger = LoggerFactory.getLogger(Frontend.class);

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
	public CloudService clone() {
		ArrayList<String> supportedPlatformsClone = new ArrayList<String>();
		for (String s : getSupportedPlatforms())
			supportedPlatformsClone.add(new String(s));
		
		Frontend f = null;
		
		try {
			f = (Frontend) super.clone();
			f.setMultiAzReplicas(this.isMultiAzReplicas());
			f.setSupportedPlatforms(supportedPlatformsClone);
			f.setPlatformType(this.getPlatformType());
			f.setMaxConnections(this.getMaxConnections());
		} catch (Exception e) {
			logger.error("Error while cloning the Frontend instance. Creating a new instance.");
			f = new Frontend(
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
		
		return f;
	}

}
