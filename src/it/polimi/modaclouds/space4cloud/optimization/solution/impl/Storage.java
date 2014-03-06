package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;

public class Storage extends IaaS {
	private int size;
	
	
	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService#clone()
	 */
	public Storage clone(){
		Storage st = new Storage(new String(this.getName()), 
								 new String(this.getId()),
								 new String(this.getProvider()),
								 new String(this.getServiceType()),
								 new String(this.getServiceName()),
								 new String(this.getResourceName()),
								 this.size);
		st.setReplicas(this.getReplicas());
		return st;
	}
		
	public Storage(String name, String id,  String provider,String serviceType, String serviceName,String resourceName, int size) {
		super( name, id, provider, serviceType,serviceName,resourceName);
		this.size = size;

	}

	public int getSpeed() {
		return size;
	}

	@Override
	public void update(LqnResultParser parser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getUtilization() {
		// TODO Auto-generated method stub
		return 0;
	}

}
