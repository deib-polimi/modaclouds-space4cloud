package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import java.util.ArrayList;


public class HourlySolution {
	private ArrayList<Instance> instances;
	
	private ArrayList<Double> balancing;
	
	private Component exposedComponent;
	
	public HourlySolution(ArrayList<Instance> instances, Component exposedComponent){
		this.instances = instances;
		this.exposedComponent = exposedComponent;
	}
	
	public Component getExposedComponent() {
		return exposedComponent;
	}

	public ArrayList<Double> getBalancing() {
		return balancing;
	}

	public void setBalancing(ArrayList<Double> balancing) {
		this.balancing = balancing;
	}

	public ArrayList<Instance> getInstances() {
		return instances;
	}


	
	public void evaluateApplication() {
	
	}
	
	
}
