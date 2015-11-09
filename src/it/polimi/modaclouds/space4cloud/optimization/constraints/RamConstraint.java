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
package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Platform;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

public class RamConstraint extends ArchitecturalConstraint {

	public RamConstraint(
			it.polimi.modaclouds.qos_models.schema.Constraint constraint) {
		super(constraint);
	}

	public int getMax() {
		return Math.round(range.getHasMaxValue());
	}

	public int getMin() {
		return Math.round(range.getHasMinValue());
	}
	

	@Override
	public double checkConstraintDistance(IConstrainable resource) throws ConstraintEvaluationException {
		//if the resource is a Tier with a Compute then get inside and check the resource ram
		if(resource instanceof Tier){
			//if the constraint is not defined on the resource then it is ok
			if(!sameId(resource))
				return Double.NEGATIVE_INFINITY;
			return checkConstraintDistance(((Tier) resource).getCloudService());
			//if the tier is hosted on a compute resource							
		} else if(resource instanceof Compute) {
			Compute computeResource = (Compute) resource;
			return checkConstraintDistance(computeResource.getRam());			
		} else if(resource instanceof Platform) {
			Compute computeResource = ((Platform)resource).getCompute();
			return checkConstraintDistance(computeResource.getRam());			
		} else{
			throw new ConstraintEvaluationException("Evaluating a RAM constraint on a wrong resource with id: "+resource.getId()+
					" RAM constraints should be evaluated against "+Tier.class+" hosted on a Compute resource or "+Compute.class+
					" resources, the specified resource is of type: "+resource.getClass());	
		}
	}

	@Override
	protected boolean checkConstraintSet(IConstrainable measurement) throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a Ram constraint as a set constraint");
	}
}
