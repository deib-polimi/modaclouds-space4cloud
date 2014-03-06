package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.ArrayList;

public class Provider {
	private String name;
	private String id;
	private ArrayList<CloudService> services = new ArrayList<>();
	
	public Provider(String name, String id){
		this.name = name;
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public String getId() {
		return id;
	}
	
	public void addService(CloudService service){
		this.services.add(service);
	}
	
	public ArrayList<CloudService> getServices(){
		return services;
	}
}

