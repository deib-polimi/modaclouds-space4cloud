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

import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

public class RamConstraint extends ArchitecturalConstraint {

	public RamConstraint(
			it.polimi.modaclouds.qos_models.schema.Constraint constraint) {
		super(constraint);
	}

	public double getMax() {
		return range.getHasMaxValue();
	}

	public double getMin() {
		return range.getHasMinValue();
	}
	
	@Override
	protected boolean checkConstraint(IConstrainable resource) {
		
		return super.checkConstraint(resource);
	}

	@Override
	public double checkConstraintDistance(IConstrainable resource) {
		if(!(resource instanceof Tier && (((Tier)resource).getCloudService()) instanceof Compute)){
			logger.error("Evaluating a RAM constraint on a wrong resource with id: "+((Tier)resource).getId()+
					" RAM constraints should be evaluated against "+Tier.class+" with a "+Compute.class+
					"resource, the specified resource is of type: "+resource.getClass());
			return Double.POSITIVE_INFINITY;
			}
			return super.checkConstraintDistance(((Compute)((Tier)resource).getCloudService()).getRam());
	}

	@Override
	protected boolean checkConstraintSet(IConstrainable measurement) {
		logger.error("Evaluating a ram constraint with an inset or outset");
		return false;
	}
}
