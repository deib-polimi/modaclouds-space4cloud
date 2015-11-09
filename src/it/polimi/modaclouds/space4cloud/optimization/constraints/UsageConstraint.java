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

import java.util.UUID;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.qos_models.schema.QosMetricAggregation;
import it.polimi.modaclouds.qos_models.schema.Range;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.IUtilizationConstrainable;

public class UsageConstraint extends QoSConstraint {

	public UsageConstraint(
			it.polimi.modaclouds.qos_models.schema.Constraint constraint) {
		super(constraint);
		//convert 0.x into x over 100
		// too bad that the value is already expressed in %, so this blows it up
		// or wait, we need it in the form 0.x, because getUtilization() method on a resource return is in that range
//		this.range.setHasMaxValue(this.range.getHasMaxValue());
//		this.range.setHasMaxValue(this.range.getHasMaxValue()*100);
		Float max = this.range.getHasMaxValue();
		if (max > 1)
			this.range.setHasMaxValue(max/100);

	}
	
	public static UsageConstraint getStandardUsageConstraint(String tierId) {
		
		it.polimi.modaclouds.qos_models.schema.Constraint cons = new Constraint();
		
		String id = UUID.randomUUID().toString();
		
		cons.setId(id);
		cons.setName("Utilization " + id);
		cons.setMetric(Metric.CPU.getXmlTag());
		QosMetricAggregation a = new QosMetricAggregation();
		a.setAggregateFunction("Average");
		cons.setMetricAggregation(a);
		Range r = new Range();
		r.setHasMaxValue(1.0f);
		cons.setRange(r);
		cons.setTargetClass("VM");
		cons.setTargetResourceIDRef(tierId);
		
		return new UsageConstraint(cons);
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
