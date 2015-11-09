package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class AvailabilityConstraint extends QoSConstraint {

	public AvailabilityConstraint(Constraint constraint) {
		super(constraint);
		id=Configuration.APPLICATION_ID;
	}

	
	
	/**
	 * @return the minimum availability
	 */
	public double getMin(){
		return range.getHasMinValue();
	}



	@Override
	protected double checkConstraintDistance(IConstrainable resource)
			throws ConstraintEvaluationException {
		// availability constraints are used only to set milp and never checked (the optmization can not brake this kind of constraints)
		return -1;
	}



	@Override
	protected boolean checkConstraintSet(IConstrainable resource)
			throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a Availability constraint on a set");
	}
	
	//TODO: add methods to derive the value fromt he resource then call the evaluation
}
