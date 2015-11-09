package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.IResponseTimeConstrainable;

public class AvgRTConstraint extends RTConstraint {

	public AvgRTConstraint(Constraint constraint) {
		super(constraint);
	}


	@Override
	public double checkConstraintDistance(IConstrainable resource) throws ConstraintEvaluationException {
		//if the resource is response time constrainable check the response time 
		if(resource instanceof IResponseTimeConstrainable){
			//if the constraint is not defined on the resource then it is ok
			if(!sameId(resource))
				return Double.NEGATIVE_INFINITY;
			return checkConstraintDistance(((IResponseTimeConstrainable)resource).getResponseTime());			
		}
		throw new ConstraintEvaluationException("Evaluating an average response time constraint on something that is not repsonse time constrainable"+resource);

	}


	@Override
	protected boolean checkConstraintSet(IConstrainable resource) throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a RT constraint on a set");		
	}


	public double getMax() {
		return range.getHasMaxValue();
	}
}
