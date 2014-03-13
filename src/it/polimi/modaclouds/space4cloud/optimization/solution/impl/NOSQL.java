/*******************************************************************************
 * Copyright 2014 Giovanni Paolo Gibilisco
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;

/**
 * The Class NOSQL.
 * @author Giovanni Gibilisco, Michele Ciavotta
 *
 */
public class NOSQL extends Database {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1597143518905843001L;

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
