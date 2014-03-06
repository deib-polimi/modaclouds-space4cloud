package it.polimi.modaclouds.space4cloud.optimization.solution.impl;

import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;
import it.polimi.modaclouds.space4cloud.optimization.solution.IResponseTimeConstrainable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Functionality implements Cloneable, IResponseTimeConstrainable {
	private String name;
	private String id;
	private String entryLevelCallID=null;
	private double responseTime;
	private Component container;
	private HashMap<String,Functionality> externalCalls = new HashMap<>();

	private void setExternalCalls(HashMap<String, Functionality> externalCalls) {
		this.externalCalls = externalCalls;
	}
	public Functionality clone(){
		Functionality f;
		try {
			f = (Functionality)super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			f = new Functionality(name, id, entryLevelCallID);
		}
		f.setContainer(null);
		
		f.setExternalCalls(new HashMap<String,Functionality>(getExternalCalls().size()));
		for(String s:externalCalls.keySet())
			f.addExternalCall(s, externalCalls.get(s));		
		return f;
	}
	public Functionality(String name, String id,String entryLevelCallID){
		this.name = name;
		this.id = id;
		this.entryLevelCallID = entryLevelCallID;
	}	
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
	public void show(String prefix) {
		System.out.println(prefix+"Functionality name: "+getName()+" id: "+getId()+" callId: "+getEntryLevelCallID()+" response time: "+getResponseTime());
	}
	public String getEntryLevelCallID() {
		return entryLevelCallID;
	}
	public void setEntryLevelCallID(String entryLevelCallID) {
		this.entryLevelCallID = entryLevelCallID;
	}
	@Override
	public double getResponseTime() {
		return responseTime;
	}
	public void setResponseTime(double responseTime) {
		this.responseTime = responseTime;
	}
	@Override
	public void update(LqnResultParser parser) {
		if(parser.getResponseTime(name)> 0)
			responseTime = parser.getResponseTime(name);
		else
			responseTime = -1;
			//System.err.println("Functionality "+getName()+" not found in the results");
		
	}
	
	public void setContainer(Component comp){
		this.container = comp;
	}
	
	public Component getContainer() {
		return container;
		
	}
	
	public void addExternalCall(String id, Functionality fun){
		externalCalls.put(id, fun);
	}
	
	/**
	 * 
	 * @return a hashmap with the functionality called DIRECTLY
	 */
	public HashMap<String, Functionality> getExternalCalls(){
		return externalCalls;
	}
	/**
	 * 
	 * @return a list with all the functionalities called for the execution. 
	 */
	public List<Functionality> getExternalCallTrace(){
		ArrayList<Functionality> calls = new ArrayList<>();
		for(Functionality f:externalCalls.values()){
			//add the direct external call
			calls.add(f);
			//add the indirect calls
			calls.addAll(f.getExternalCallTrace());
		}
		return calls;		
	}
}
