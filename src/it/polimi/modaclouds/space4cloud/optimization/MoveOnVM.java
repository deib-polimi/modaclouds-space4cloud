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
/**
 * 
 */
package it.polimi.modaclouds.space4cloud.optimization;

import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;

import java.util.ArrayList;

/**
 * @author Michele Ciavotta
 *
 */
public class MoveOnVM extends AbsMoveHour {

	protected IaaS resource = null;
	private int numberOfReplicas  = 1;
	//Instance app;

	
	/**
	 * Sets the cloud resource on which the move will be performed.
	 *
	 * @param idCloudResource the id cloud resource
	 * @return the object itself 
	 */
	protected MoveOnVM setCloudResource(IaaS idCloudResource) {		
		this.resource = idCloudResource;
		return this;
	}	
	
	/**
	 * provides handles to modify a IaaS resource
	 * @param sol - The Solution in which the resource is located
	 * @param i the hour of the solution
	 */
	public MoveOnVM(Solution sol, int i) {
		super();
		//this kind of move only changes the number of VM 
		this.propertyNames = new ArrayList<>();
		this.propertyValues = new ArrayList<>();
		
		setSolution(sol);
		setHour(i);

	}

	/**
	 * initialize the reflection utility with the replicas property and the specified value
	 * @param numOfReplicas
	 * @return the move
	 */
	private MoveOnVM setNumberOfReplicas(int numOfReplicas){
		this.numberOfReplicas = numOfReplicas;
		propertyNames.add("replicas");
		propertyValues.add(numberOfReplicas);
		return this;
	}

	/**
	 * Increases by 1 the number of replicas of the specified resource
	 * @param res
	 */
	public void scaleOut(IaaS res) {			
//		//perform the move
//		setCloudResource(res);
//		setNumberOfReplicas(resource.getReplicas()+1);
//		apply();
		
		scale(res, res.getReplicas()+1);
	}
	
	
	/**
	 * Modify the number of replicas of the specified resource reducing it multiplying it by the provided factor and ceiling the result. 
	 * If the resulting number of replicas is equal or lower than  the original one (factor is <= 1) it falls back to the scaleOut function that adds just 1 replica
	 * @param res
	 * @param factor
	 */
	public void scaleOut(IaaS res, double factor) {
//		//perform the move
//		setCloudResource(res);		
//		int newReplicas = (int)Math.ceil(resource.getReplicas()*factor);		
//		if(newReplicas <= res.getReplicas())
//			scaleOut(res);
//		else
//			setNumberOfReplicas(newReplicas);
//		apply();
		
		int newReplicas = (int)Math.ceil(res.getReplicas()*factor);		
		if(newReplicas <= res.getReplicas())
			scaleOut(res);
		else
			scale(res, newReplicas);
	}
	


	/**
	 * Modify the number of replicas of the specified resource
	 * @param res
	 * @param numberOfReplicas
	 */
	public void scale(IaaS res, int numberOfReplicas){
		//perform the move
//		setCloudResource(resource);
		setCloudResource(res);
		setNumberOfReplicas(numberOfReplicas);
		apply();
	}
	
	/**
	 * Reduces by 1 the number of replicas of the specified resources
	 * @param res
	 */
	public void scaleIn(IaaS res) {
//		//perform the move
//		setCloudResource(res);
//		setNumberOfReplicas(resource.getReplicas()-1);
//		apply();
		
		scale(res, res.getReplicas()-1);
	}
	
	
	/**
	 * Modify the number of replicas of the specified resource reducing it dividing it by the provided factor and ceiling the result. 
	 * If the resulting number of replicas is equal to the original one it falls back to the scaleIn function that removes just 1 replica
	 * @param res
	 * @param factor
	 */
	public void scaleIn(IaaS res, double factor) {
//		//perform the move
//		setCloudResource(res);		
//		int newReplicas = (int)Math.ceil(resource.getReplicas()*(1/factor));		
//		if(newReplicas == res.getReplicas() || newReplicas==0)
//			scaleIn(res);
//		else
//			setNumberOfReplicas(newReplicas);
//		apply();
		
		int newReplicas = (int)Math.ceil(res.getReplicas()*(1/factor));		
		if(newReplicas == res.getReplicas() || newReplicas==0)
			scaleIn(res);
		else
			scale(res, newReplicas);
	}
	
	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.AbsMove#apply()
	 */
	@Override
	public Solution apply() {
		
		// TODO: questa funzione potrebbe andare anche in AbsMove
		// TODO Auto-generated method stub
		
		/*When we apply the move it's better to not evaluate the whole solution in that moment
		 * it is more efficient to make a set of moves and then call the evaluation.*/
		
		// 1: identify the cloud resource.
		
		// 2: Modify the Solution and LQN
		application.changeValues(resource.getId(), this.propertyNames, this.propertyValues);		
	
		
		return this.currentSolution;
	}
	
	
}
