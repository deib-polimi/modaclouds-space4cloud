package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.optimization.solution.IUtilizationConstrainable;


public abstract class IaaS extends CloudService implements IUtilizationConstrainable {

	/** The number of replicas of the cloud resource. */
	int replicas; 	


	public IaaS(String name, String id, String provider, String serviceType,String serviceName, String resourceName) {		
		super(name, id, provider, serviceType,serviceName, resourceName);
		// TODO Auto-generated constructor stub
	}

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}

	@Override
	public String showStatus(String prefix) {
		return super.showStatus(prefix)+"\t Replicas: "+replicas;		
	}

	@Override
	public CloudService clone() throws CloneNotSupportedException {
		IaaS clone =  (IaaS) super.clone();
		clone.setReplicas(this.getReplicas());
		return clone;
	}
}
