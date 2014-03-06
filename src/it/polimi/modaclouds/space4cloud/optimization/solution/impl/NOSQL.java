package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;

/**
 * The Class NOSQL.
 * @author Giovanni Gibilisco, Michele Ciavotta
 *
 */
public class NOSQL extends Database {

	
	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService#clone()
	 */
	public NOSQL clone(){
		
	NOSQL nosql = new NOSQL(new String(this.getName()),
						  new String(this.getId()),
						  new String(this.getProvider()),
						  new String(this.getServiceType()),
						  new String(this.getServiceName()),
						  new String(this.getResourceName()));
		
		return nosql;
	}
	
	/**
	 * Instantiates a new nosql.
	 *
	 * @param name the name
	 * @param id the id
	 * @param provider the provider
	 * @param serviceType the service type
	 * @param serviceName the service name
	 * @param resourceName the resource name
	 */
	public NOSQL( String name, String id, String provider, String serviceType,String serviceName, String resourceName) {
		super(name,  id, provider,serviceType, serviceName, resourceName);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable#update(it.polimi.modaclouds.space4cloud.lqn.LqnResultParser)
	 */
	@Override
	public void update(LqnResultParser parser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getResponseTime() {
		// TODO Auto-generated method stub
		return 0;
	}


}
