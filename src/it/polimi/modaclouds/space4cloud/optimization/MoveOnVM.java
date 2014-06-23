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
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

import java.util.ArrayList;

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
	 * Reduces by 1 the number of replicas of the specified resources
	 * 
	 * @param res
	 */
	public void scaleIn(Tier tier) {
		if(tier.getCloudService() instanceof IaaS)
			scale(tier, ((IaaS)tier.getCloudService()).getReplicas() - 1);
		//TODO: add an else with a warning or raise an exception
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
		IaaS cloudService = null;
		if(tier.getCloudService() instanceof IaaS)
			cloudService = (IaaS) tier.getCloudService();
		//TODO: again we should add an else with a warning or raise an exception
		int newReplicas = (int) Math.ceil(cloudService.getReplicas() * (1 / factor));
		if (newReplicas == cloudService.getReplicas() || newReplicas == 0)
			scaleIn(tier);
		else
			scale(tier, newReplicas);
	}

	/**
	 * Increases by 1 the number of replicas of the specified resource
	 * 
	 * @param res
	 */
	public void scaleOut(Tier tier) {
		
		if(tier.getCloudService() instanceof IaaS)			
			scale(tier, ((IaaS)tier.getCloudService()).getReplicas() + 1);
		//TODO:here too we should add an else with a warning or raise an exception
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
		// //perform the move
		// setCloudResource(res);
		// int newReplicas = (int)Math.ceil(resource.getReplicas()*factor);
		// if(newReplicas <= res.getReplicas())
		// scaleOut(res);
		// else
		// setNumberOfReplicas(newReplicas);
		// apply();
		IaaS cloudService = null;
		if(tier.getCloudService() instanceof IaaS)
			cloudService = (IaaS) tier.getCloudService();
		//TODO: even more.. we should add an else with a warning or raise an exception
		int newReplicas = (int) Math.ceil(cloudService.getReplicas() * factor);
		if (newReplicas <= cloudService.getReplicas())
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
