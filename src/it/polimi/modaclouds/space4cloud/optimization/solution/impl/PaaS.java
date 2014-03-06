package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.optimization.solution.IResponseTimeConstrainable;

public abstract class PaaS extends CloudService implements IResponseTimeConstrainable {


	public PaaS(String name, String id, String provider,  String serviceType,String serviceName, String resourceName) {
		super(name,  id, provider,serviceType,serviceName, resourceName);
		// TODO Auto-generated constructor stub
	}
	

}
