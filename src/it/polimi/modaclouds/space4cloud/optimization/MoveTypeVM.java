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

import it.polimi.modaclouds.resourcemodel.cloud.CloudResource;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Cache;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Component;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Database;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Functionality;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Instance;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.NOSQL;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.PaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Platform;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Queue;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.SQL;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Solution;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

/**
 * @author Michele Ciavotta This move changes the type of VM
 */
public class MoveTypeVM extends AbsMove {

	private String resId = null;

	/**
	 * Constructor
	 */
	public MoveTypeVM(Solution sol) {

		setSolution(sol);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.modaclouds.space4cloud.optimization.AbsMove#apply()
	 */
	@Override
	public Solution apply() {

		currentSolution.changeValues(resId, this.propertyNames,
				this.propertyValues);
		return currentSolution;
	}

	/**
	 * Change machine.
	 * 
	 * @param res
	 *            the res
	 * @param cr
	 *            the cr
	 * @return the i move
	 */
	public IMove changeMachine(ArrayList<IaaS> res, CloudResource cr) {

		return this;
	}

	/* TODO: rivedere questa funzione. */
	/**
	 * Change machine.
	 * 
	 * @param id
	 *            the id
	 * @param cr
	 *            the CloudService
	 * @return the move itself
	 */
	public IMove changeMachine(String id, CloudService vm) {
		setProperties(vm);
		setResId(id);
		apply();
		return this;

	}

	/**
	 * Retrieves all the IaaS resources whose performance affect the resource identified by the constrained resource id
	 * @param constrainedResourceId
	 * @return
	 */
	@SuppressWarnings("unused")
	private ArrayList<IaaS> findResourceList(String constrainedResourceId) {
		ArrayList<IaaS> resultList = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			Instance application = this.currentSolution.getApplication(i);
			IConstrainable constrainedResource = application.getConstrainableResources().get(constrainedResourceId);
			IaaS resource = null;
			//if the resource is the iaas itself
			if (constrainedResource instanceof IaaS)
				resource = (IaaS) constrainedResource;
			// if the constraint is on a functionality we have to build the list
			// of affected components
			else if (constrainedResource instanceof Functionality) {
				constrainedResource = ((Functionality) constrainedResource)
						.getContainer();
			}

			// if it is a component
			if (constrainedResource instanceof Component) {
				for (Tier t : application.getTiers())
					if (t.getComponents().contains(constrainedResource)) {
						resource = (IaaS) t.getCloudService();
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

	public IMove setProperties(CloudService cs) {
		propertyNames.clear();
		propertyValues.clear();
		
		propertyNames.add("resourceName");
		propertyValues.add(cs.getResourceName());
		
		if (cs instanceof Compute) {
			Compute vm = (Compute) cs;
			
			propertyNames.add("speed");
			propertyNames.add("ram");
			propertyNames.add("numberOfCores");
			propertyValues.add(vm.getSpeed());
			propertyValues.add(vm.getRam());
			propertyValues.add(vm.getNumberOfCores());
		} else if (cs instanceof PaaS) {
			PaaS p = (PaaS) cs;
			
			propertyNames.add("replicas");
			propertyNames.add("dataReplicas");
			propertyNames.add("replicasChangeable");
			propertyNames.add("replicasPayedSingularly");
			propertyValues.add(p.getReplicas());
			propertyValues.add(p.getDataReplicas());
			propertyValues.add(p.areReplicasChangeable());
			propertyValues.add(p.areReplicasPayedSingularly());
			
			if (p instanceof Platform) {
				Platform paas = (Platform) p;
				
				propertyNames.add("compute");
				propertyNames.add("multiAzReplicas");
				propertyNames.add("supportedPlatforms");
				propertyNames.add("platformType");
				propertyNames.add("storage");
				propertyNames.add("maxConnections");
				propertyValues.add(paas.getCompute());
				propertyValues.add(paas.isMultiAzReplicas());
				propertyValues.add(paas.getSupportedPlatforms());
				propertyValues.add(paas.getPlatformType());
				propertyValues.add(paas.getStorage());
				propertyValues.add(paas.getMaxConnections());
			} else if (p instanceof Database) {
				Database paas = (Database) p;
				
				propertyNames.add("compute");
				propertyNames.add("multiAzReplicas");
				propertyNames.add("ssdOptimized");
				propertyNames.add("type");
				propertyNames.add("storage");
				propertyNames.add("technology");
				propertyValues.add(paas.getCompute());
				propertyValues.add(paas.isMultiAzReplicas());
				propertyValues.add(paas.isSsdOptimized());
				propertyValues.add(paas.getType());
				propertyValues.add(paas.getStorage());
				propertyValues.add(paas.getTechnology());
				
				if (paas instanceof NOSQL) {
					propertyNames.add("maxEntrySize");
					propertyValues.add(((NOSQL)paas).getMaxEntrySize());
				} else {
					SQL sql = (SQL) paas;
					
					propertyNames.add("maxConnections");
					propertyNames.add("maxRollbackHours");
					propertyValues.add(sql.getMaxConnections());
					propertyValues.add(sql.getMaxRollbackHours());
				}
			} else if (p instanceof Cache) {
				Cache paas = (Cache) p;
				
				propertyNames.add("compute");
				propertyNames.add("multiAzReplicas");
				propertyNames.add("engine");
				propertyNames.add("maxConnections");
				propertyNames.add("storage");
				propertyValues.add(paas.getCompute());
				propertyValues.add(paas.isMultiAzReplicas());
				propertyValues.add(paas.getEngine());
				propertyValues.add(paas.getMaxConnections());
				propertyValues.add(paas.getStorage());
			} else if (p instanceof Queue) {
				Queue paas = (Queue) p;
				
				propertyNames.add("compute");
				propertyNames.add("multiAzReplicas");
				propertyNames.add("requestSize");
				propertyNames.add("maxConnections");
				propertyNames.add("orderPreserving");
				propertyNames.add("maxRequests");
				propertyNames.add("multiplyingFactor");
				propertyNames.add("delay");
				propertyValues.add(paas.getCompute());
				propertyValues.add(paas.isMultiAzReplicas());
				propertyValues.add(paas.getRequestSize());
				propertyValues.add(paas.getMaxConnections());
				propertyValues.add(paas.isOrderPreserving());
				propertyValues.add(paas.getMaxRequests());
				propertyValues.add(paas.getMultiplyingFactor());
				propertyValues.add(paas.getDelay());
			}
		}
		return this;
	}

	/**
	 * @param resId
	 *            the resId to set
	 */
	public void setResId(String resId) {
		this.resId = resId;
	}

}
