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
import it.polimi.modaclouds.space4cloud.optimization.solution.IUtilizationConstrainable;

public class UsageConstraint extends QoSConstraint {

	public UsageConstraint(
			it.polimi.modaclouds.qos_models.schema.Constraint constraint) {
		super(constraint);
		//convert 0.x into x over 100
		this.range.setHasMaxValue(this.range.getHasMaxValue()*100);

	}

	@Override
	public double checkConstraintDistance(IConstrainable resource) throws ConstraintEvaluationException {
		///if the resource is utilization constrainbale then get  the utilization
		if(resource instanceof IUtilizationConstrainable){
			//if the constraint is not defined on the resource then it is ok
			if(!sameId(resource))
				return Double.NEGATIVE_INFINITY;
			return checkConstraintDistance(((IUtilizationConstrainable)resource).getUtilization());			
		}else  {						
			throw new ConstraintEvaluationException("Evaluating a Utilization constraint on a wrong resource with id: "+((IUtilizationConstrainable)resource).getId()+
					" Utilization constraints should be evaluated against "+IUtilizationConstrainable.class+
					", the specified resource is of type: "+resource.getClass()); 
		}

	}

	@Override
	protected boolean checkConstraintSet(IConstrainable resource) throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a Usage constraint as a set constraint");
	}

	public double getMax() {
		return range.getHasMaxValue();
	}

}
