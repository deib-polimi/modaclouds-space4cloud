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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.space4cloud.lqn.LINEResultParser;
import it.polimi.modaclouds.space4cloud.lqn.LqnResultParser;

public class Functionality implements Cloneable, IPercentileRTConstrainable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5621349760322110134L;
	private boolean evaluated = true;
	private String name;
	private String id;
	private String lqnProcessorId;
	private String entryLevelCallID=null;
	private double responseTime;
	private double throughput;
	private Map<Integer,Double> rtPercentiles;
	private Component container;
	private HashMap<String,Functionality> externalCalls = new HashMap<String,Functionality>();
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Functionality.class);
	
	private double demand;

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
			f = new Functionality(name, id, entryLevelCallID, demand);
		}
		f.setContainer(null);
		
		f.setExternalCalls(new HashMap<String,Functionality>(getExternalCalls().size()));
		for(String s:externalCalls.keySet())
			f.addExternalCall(s, externalCalls.get(s));		
		return f;
	}
	public Functionality(String name, String id,String entryLevelCallID, String uuu){
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
		logger.info(prefix+"Functionality name: "+getName()+" id: "+getId()+" callId: "+getEntryLevelCallID()+" evaluated: "+isEvaluated()+" response time: "+getResponseTime());
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
	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable#update(it.polimi.modaclouds.space4cloud.lqn.LqnResultParser)
	 * Updates the average and percentile response times of the functionality according to the information available in the parser
	 */
	@Override
	public void update(LqnResultParser parser) {
		//copy the average response time
		if(parser.getResponseTime(id)> 0){
			responseTime = parser.getResponseTime(id);
			evaluated = true;
		}
		else{
			responseTime = -1;
			evaluated = false;
			//System.err.println("Functionality "+getName()+" not found in the results");
		}

		//copy the percentile response time and the throughput
		//Only LINE generate this information
		if(parser instanceof LINEResultParser){
			rtPercentiles = ((LINEResultParser)parser).getPercentiles(id);
			throughput = ((LINEResultParser)parser).getThroughput(id);
		}
		
	}
	
	public boolean isEvaluated() {
		return evaluated;
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
	
	@Override
	public Map<Integer, Double> getRtPercentiles() {
		return rtPercentiles;
	}
	public String getLqnProcessorId() {
		return lqnProcessorId;
	}
	public void setLqnProcessorId(String lqnProcessorId) {
		this.lqnProcessorId = lqnProcessorId;
	}
	
	public double getThroughput() {
		return throughput;
	}
	public void setThroughput(double throughput) {
		this.throughput = throughput;
	}
	
	public int getRequests() {
		return (int)Math.round(throughput * 60 * 60);
	}
	
	public double getDemand() {
		return demand;
	}
	
	public Functionality(String name, String id,String entryLevelCallID, double demand){
		this(name, id, entryLevelCallID, "aaa");
		this.demand = demand;
	}
}
