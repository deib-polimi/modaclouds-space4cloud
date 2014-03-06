package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.IResponseTimeConstrainable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Tier implements Cloneable, IResponseTimeConstrainable{

	/** The cloud resource. */
	private CloudService cloudService;	

	/** The list of components deployed on the cloud resource. */
	private ArrayList<Component> components= new ArrayList<>();

	/** The id cloud resource. */
	private String idCloudResource;


	public void addComponent(Component component) {
		this.components.add(component);
		component.setContainer(this);
	}
	
	public Tier clone(){
		
		Tier t;
		try {
			t = (Tier) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			t = new Tier();
		}		
		
		//clone Cloud Resource
		try {
			t.setCloudService(getCloudService().clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// cloning the component list
		t.setComponents(new ArrayList<Component>());		
		for (Component c : this.getComponents()) {
			t.addComponent(c.clone());
		}
				
		t.setIdCloudResource(new String(this.getIdCloudResource()));
		
		return t;
		
	}



	private void setComponents(ArrayList<Component> components) {
		this.components = components;
		
	}

	public CloudService getCloudService() {
		return cloudService;
	}

	public List<Component> getComponents(){
		return components;
	}

	public Map<String, ? extends IConstrainable> getConstrinableResources() {
		HashMap<String, IConstrainable> resources = new HashMap<>();
		if(cloudService instanceof IConstrainable) //should be true by construction...
			resources.put(cloudService.getId(), cloudService);
		for(Component c:components){
			if(c instanceof IConstrainable)
				resources.put(c.getId(), c);
			resources.putAll(c.getConstrainableResources());
		}
		
		return resources;
	}

	@Override
	public String getId() {		
		return cloudService.getName();
	}

	/**
	 * @return the idCloudResource
	 */
	public String getIdCloudResource() {
		return idCloudResource;
	}

	@Override
	public double getResponseTime() {
		double avg=0;
		for(Component c:components)
			avg += c.getResponseTime();
		return avg/components.size();
	}

	public void setCloudService(CloudService cloudService) {
		this.cloudService = cloudService;
	}
	
	/**
	 * @param idCloudResource the idCloudResource to set
	 */
	public void setIdCloudResource(String idCloudResource) {
		this.idCloudResource = idCloudResource;
	}

	public void setService(CloudService cloudService) {		
		this.cloudService = cloudService;		
		this.setIdCloudResource(cloudService.getId());
	}
	
	public String showStatus(String prefix) {
		return cloudService.showStatus(prefix);
	}

	@Override
	public void update(LqnResultParser results) {
		//update resource
		cloudService.update(results);
		
		//update components
		for(Component c:components)
			c.update(results);
	}

	public List<Functionality> getFunctionalities() {
		ArrayList<Functionality> functionalities = new ArrayList<Functionality>();
		for(Component c:components)
			functionalities.addAll(c.getFunctionalities());			
		return functionalities;
	}


}
