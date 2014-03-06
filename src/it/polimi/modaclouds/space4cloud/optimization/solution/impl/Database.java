package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;

public abstract class Database extends PaaS {

	public Database(String name, String id, String provider,
			String serviceType, String serviceName, String resourceName) {
		super(name, id, provider, serviceType, serviceName, resourceName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void update(LqnResultParser parser) {
		// TODO Auto-generated method stub

	}

	@Override
	public CloudService clone() {
		// TODO Auto-generated method stub
		return null;
	}

}
