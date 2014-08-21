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

import it.polimi.modaclouds.qos_models.schema.AggregateFunction;
import it.polimi.modaclouds.qos_models.schema.Range;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Constraint {

	protected String id;
	protected String name;
	protected String resourceId;
	protected Metric metric;
	protected AggregateFunction metricAcggregationFunction;
	protected int priority;
	protected Range range;
	protected static final Logger logger = LoggerFactory
			.getLogger(Constraint.class);

	public Constraint(
			it.polimi.modaclouds.qos_models.schema.Constraint constraint) {
		this.id = constraint.getId();
		this.name = constraint.getName();
		this.resourceId = constraint.getTargetResourceIDRef();
		this.metric = Metric.getMetricFromTag(constraint.getMetric());
		this.priority = (constraint.getPriority() == null ? 0 : constraint
				.getPriority()).intValue();
		this.range = constraint.getRange();
	}

	public Constraint(String id, String name, String resourceId, Metric metric,
			int priority) {
		this.id = id;
		this.name = name;
		this.resourceId = resourceId;
		this.metric = metric;
		this.priority = priority;
	}
	/**
	 * Checks the fulfillment of the constraint against the specified resource. The id of the resource should be the same as the one specified in the constraint and the resource should have been evaluated
	 * @param measurement
	 * @return Positive distance in case the constraint was not fulfilled, negative otherwise.
	 */
	public abstract double checkConstraintDistance(IConstrainable resource);	

	/**
	 * Checks the distance between the constraint and the provided value. Positive distance if the constraint has not been fulfilled, negative if it has been fulfilled
	 * @param measurement
	 * @return
	 */
	protected double checkConstraintDistance(Object measurement) {
		double value = 0;
		if (measurement instanceof Double)
			value = (Double) measurement;
		else if (measurement instanceof Integer)
			value = ((Integer) measurement).doubleValue();
		else if (measurement instanceof Float)
			value = ((Float) measurement).doubleValue();
		else
			logger.error("Error in casting the value to check");
		double upper = -1; //if upper is negative the constraint of max is fulfilled
		double lower = -1; //if lower is negative the constraint of min is fulfilled
		if (range.getHasMaxValue() != null)
			upper = value - range.getHasMaxValue();
		if(range.getHasMinValue() != null)
			lower = range.getHasMinValue() - value;
		//if the value is over the max return the difference
		if(upper>0)
			return upper;
		
		//if the value is under the min return the difference
		if(lower>0)
			return lower;
		
		//if the value is in the range return the distance to the closest bound (max since these are negative numbers)
		return Math.max(upper, lower);
		
		
	}

	public Metric getMetric() {
		return metric;
	}

	public int getPriority() {
		return priority;
	}

	public String getResourceID() {
		return resourceId;
	}

	public boolean hasNumericalRange() {
		if (range.getHasMaxValue() != null || range.getHasMinValue() != null)
			return true;
		return false;
	}

}
