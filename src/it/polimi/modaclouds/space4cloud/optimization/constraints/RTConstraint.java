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

public abstract class RTConstraint extends QoSConstraint {

	public RTConstraint(
			it.polimi.modaclouds.qos_models.schema.Constraint constraint) {
		super(constraint);
		//transform constraints on milliseconds to seconds
		if(range.getHasMaxValue() != null)
			range.setHasMaxValue(range.getHasMaxValue()/1000);
		if(range.getHasMinValue() != null)
			range.setHasMinValue(range.getHasMinValue()/1000);
	}
	
	//TODO: if the responsetime constrainable resource is a funcitonality and it has not been evaluated 
	//(because it is not present in the result output of the evaluation tool) we should warn the user that we will not consider the constraint. 
	// by default response time of those functionalities are lower than zero so the constraint will aslways be true. Neveretheless, a warning should be raised.
	public abstract double checkConstraintDistance(IConstrainable resource);

}
