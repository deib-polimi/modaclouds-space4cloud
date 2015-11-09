package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.utils.Configuration;

public class NumberProvidersConstraint extends QoSConstraint {

	public NumberProvidersConstraint(Constraint constraint) {
		super(constraint);
		id=Configuration.APPLICATION_ID;
	}

	//Number of provider are used only to set MILP and the optimization, never checked.
	@Override	
	protected double checkConstraintDistance(IConstrainable resource)
			throws ConstraintEvaluationException {		
		return -1;
	}

	@Override
	protected boolean checkConstraintSet(IConstrainable resource)
			throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a Number Provider constraint on a set");
	}
	
	/**
	 * @return the minimum number of replicas
	 */
	public int getMin(){
		Float f = range.getHasMinValue(); 
		if (f != null)
			return f.intValue();
		else
			return 1;
	}
	
	
	//TODO: add methods to derive the value fromt he resource then call the evaluation
}
