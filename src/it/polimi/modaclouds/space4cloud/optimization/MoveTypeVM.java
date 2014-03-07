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

import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

import java.util.ArrayList;


/**
 * @author Michele Ciavotta
 *  This move changes the type of VM
 */
public class MoveTypeVM extends AbsMove {

	private String resId = null; 
	/**
	 *  Constructor
	 */
	public MoveTypeVM(Solution sol) {

		setSolution(sol);
	}

	/*TODO: rivedere questa funzione.*/
	/**
	 * Change machine.
	 *
	 * @param id the id
	 * @param cr the CloudService
	 * @return the move itself
	 */
	public IMove changeMachine(String id, Compute vm){		
		setProperties(vm);
		setResId(id);
		apply();
		return this;
		
	}

	/**
	 * Change machine.
	 *
	 * @param res the res
	 * @param cr the cr
	 * @return the i move
	 */
	public IMove changeMachine(ArrayList<IaaS> res, CloudResource cr ){
		
		
		return this;
	}
	public IMove setProperties(Compute vm){
		propertyNames.clear();
		propertyValues.clear();
		propertyNames.add("resourceName");
		propertyNames.add("speed");
		propertyNames.add("ram");
		propertyNames.add("numberOfCores");
		propertyValues.add(vm.getResourceName()); 
		propertyValues.add(vm.getSpeed()); 
		propertyValues.add(vm.getRam()); 
		propertyValues.add(vm.getNumberOfCores());
		return this;
	}
	
	
	/* (non-Javadoc)
	 * @see it.polimi.modaclouds.space4cloud.optimization.AbsMove#apply()
	 */
	@Override
	public Solution apply() {

		currentSolution.changeValues(resId, this.propertyNames, this.propertyValues);
		return currentSolution;
	}
	
	@SuppressWarnings("unused")
	private ArrayList<IaaS> findResourceList(String id){
		ArrayList<IaaS> resultList = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			Instance application = this.currentSolution.getApplication(i);
			IConstrainable constrainedResource = application.getConstrainableResources().get(id);
			IaaS resource = null;
			if(constrainedResource instanceof IaaS)
				resource = (IaaS) constrainedResource;
			//if the constraint is on a functionality we have to build the list of affected components
			else if(constrainedResource instanceof Functionality){

				constrainedResource = ((Functionality)constrainedResource).getContainer();
			}

			//if it is a component
			if(constrainedResource instanceof Component){
				for(Tier t:application.getTiersByResourceName().values())
					if(t.getComponents().contains(constrainedResource)){
						resource = (IaaS) t.getCloudService();
						id = resource.getName();
						break;
					}
			}
			resultList.add(resource);
		}
		
		return resultList;

	}

	/**
	 * @return the resId
	 */
	public String getResId() {
		return resId;
	}

	/**
	 * @param resId the resId to set
	 */
	public void setResId(String resId) {
		this.resId = resId;
	}
	
}
