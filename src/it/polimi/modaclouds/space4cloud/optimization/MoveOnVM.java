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

import java.util.ArrayList;
import java.util.List;

import it.polimi.modaclouds.space4cloud.optimization.constraints.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ConstraintHandlerFactory;
import it.polimi.modaclouds.space4cloud.optimization.constraints.ReplicasConstraint;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.PaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

/**
 * @author Michele Ciavotta
 * 
 */
public class MoveOnVM extends AbsMoveHour {

	protected Tier tier = null;
	private int numberOfReplicas = 1;

	// Instance app;

	/**
	 * provides handles to modify a IaaS resource
	 * 
	 * @param sol
	 *            - The Solution in which the resource is located
	 * @param i
	 *            the hour of the solution
	 */
	public MoveOnVM(Solution sol, int i) {
		super();
		// this kind of move only changes the number of VM
		this.propertyNames = new ArrayList<>();
		this.propertyValues = new ArrayList<>();

		setSolution(sol);
		setHour(i);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.modaclouds.space4cloud.optimization.AbsMove#apply()
	 */
	@Override
	public Solution apply() {
		application.changeValues(tier.getId(), this.propertyNames,
				this.propertyValues);

		return this.currentSolution;
	}

	/**
	 * Modify the number of replicas of the specified resource
	 * 
	 * @param res
	 * @param numberOfReplicas
	 */
	public void scale(Tier tier, int numberOfReplicas) {
		// perform the move
		// setCloudResource(resource);
		setCloudResource(tier);
		setNumberOfReplicas(numberOfReplicas);
		apply();
	}

	/**
	 * Reduces by 1 the number of replicas of the specified resources if this does not conflict with the constraints
	 * 
	 * @param res
	 */
	public void scaleIn(Tier tier) {
		CloudService cs = tier.getCloudService();
		
		if(!(cs instanceof IaaS) && (cs instanceof PaaS && !((PaaS)cs).areReplicasChangeable() )) {
			logger.warn("Trying to scale a non scalable resource.");
			return;
		}
		
		int replicas = cs.getReplicas();
		
		//avoid breaking constraints when scaling in	
		List<Constraint> constraints =  ConstraintHandlerFactory.getConstraintHandler().getConstraintByResourceId(tier.getId(), ReplicasConstraint.class);
		for(Constraint c:constraints){
			if(replicas <= ((ReplicasConstraint)c).getMax()){
				logger.warn("Trying to scale in a resource that is already at its maximu allowed number of replicas, tier id:"+tier.getId());
				return;
			}
		}
					
		scale(tier, replicas - 1);
	}

	/**
	 * Modify the number of replicas of the specified resource reducing it
	 * dividing it by the provided factor and ceiling the result. If the
	 * resulting number of replicas is equal to the original one it falls back
	 * to the scaleIn function that removes just 1 replica
	 * 
	 * @param res
	 * @param factor
	 */
	public void scaleIn(Tier tier, double factor) {
		CloudService cs = tier.getCloudService();
		
		if(!(cs instanceof IaaS) && (cs instanceof PaaS && !((PaaS)cs).areReplicasChangeable() )) {
			logger.warn("Trying to scale a non scalable resource.");
			return;
		}
		
		int replicas = cs.getReplicas();

		int newReplicas = (int) Math.ceil(replicas * (1 / factor));
		//avoid breaking constraints when scaling in 		
		List<Constraint> constraints =  ConstraintHandlerFactory.getConstraintHandler().getConstraintByResourceId(tier.getId(), ReplicasConstraint.class);
		for(Constraint c:constraints){
			if(newReplicas < ((ReplicasConstraint)c).getMin()){
				newReplicas = ((ReplicasConstraint)c).getMin();
				break;
			}
		}
		if (newReplicas == replicas || newReplicas == 0)
			scaleIn(tier);
		else
			scale(tier, newReplicas);
	}

	/**
	 * Increases by 1 the number of replicas of the specified resource if this does not conflict with the onstraints
	 * 
	 * @param res
	 */
	public void scaleOut(Tier tier) {
		CloudService cs = tier.getCloudService();
		
		if(!(cs instanceof IaaS) && (cs instanceof PaaS && !((PaaS)cs).areReplicasChangeable() )) {
			logger.warn("Trying to scale a non scalable resource.");
			return;
		}
		
		int replicas = cs.getReplicas();
		
		//avoid breaking constraints when scaling out	
		List<Constraint> constraints =  ConstraintHandlerFactory.getConstraintHandler().getConstraintByResourceId(tier.getId(), ReplicasConstraint.class);
		for(Constraint c:constraints){
			if(replicas >= ((ReplicasConstraint)c).getMax()){
				logger.warn("Warning: trying to scale out a resource that is already at its maximu allowed number of replicas, tier id:"+tier.getId());
				return;
			}
		}
					
		scale(tier, replicas + 1);
	}

	/**
	 * Modify the number of replicas of the specified resource reducing it
	 * multiplying it by the provided factor and ceiling the result. If the
	 * resulting number of replicas is equal or lower than the original one
	 * (factor is <= 1) it falls back to the scaleOut function that adds just 1
	 * replica
	 * 
	 * @param res
	 * @param factor
	 */
	public void scaleOut(Tier tier, double factor) {
		CloudService cs = tier.getCloudService();
		
		if(!(cs instanceof IaaS) && (cs instanceof PaaS && !((PaaS)cs).areReplicasChangeable() )) {
			logger.warn("Trying to scale a non scalable resource.");
			return;
		}
		
		int replicas = cs.getReplicas();
				
		int newReplicas = (int) Math.ceil(replicas * factor);
		//avoid breaking constraints when scaling in 		
		List<Constraint> constraints =  ConstraintHandlerFactory.getConstraintHandler().getConstraintByResourceId(tier.getId(), ReplicasConstraint.class);
		for(Constraint c:constraints){
			if(newReplicas > ((ReplicasConstraint)c).getMax()){
				newReplicas = ((ReplicasConstraint)c).getMax();
				break;
			}
		}
		if (newReplicas <= replicas)
			scaleOut(tier);
		else
			scale(tier, newReplicas);
	}

	/**
	 * Sets the cloud resource on which the move will be performed.
	 * 
	 * @param idCloudResource
	 *            the id cloud resource
	 * @return the object itself
	 */
	protected MoveOnVM setCloudResource(Tier tier) {
		this.tier = tier;
		return this;
	}

	/**
	 * initialize the reflection utility with the replicas property and the
	 * specified value
	 * 
	 * @param numOfReplicas
	 * @return the move
	 */
	private MoveOnVM setNumberOfReplicas(int numOfReplicas) {
		this.numberOfReplicas = numOfReplicas;
		propertyNames.add("replicas");
		propertyValues.add(numberOfReplicas);
		return this;
	}

}
