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

public class ResponseTimeConstraint extends QoSConstraint {

	public ResponseTimeConstraint(String id, Metric metric, int priority,Unit unit) {
		super(id, metric, priority, unit);
	}

		
	@Override
	public void setMax(double maxValue) {
		if(unit.equals(Unit.MILLISECONDS)){
			setUnit(Unit.SECONDS);
			setMax(maxValue/1000);
		}else			
		super.setMax(maxValue);
	}
	
}
