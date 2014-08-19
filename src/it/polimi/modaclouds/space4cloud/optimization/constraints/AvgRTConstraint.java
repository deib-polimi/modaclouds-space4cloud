package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.IResponseTimeConstrainable;

public class AvgRTConstraint extends RTConstraint {

	public AvgRTConstraint(Constraint constraint) {
		super(constraint);
	}


	@Override
	public double checkConstraintDistance(IConstrainable resource) {
		if(!(resource instanceof IResponseTimeConstrainable)){
			logger.error("Evaluating a RAM constraint on a wrong resource with id: "+resource.getId()+
					" RAM constraints should be evaluated against "+IResponseTimeConstrainable.class+
					", the specified resource is of type: "+resource.getClass());
			return -1;
			}
			return super.checkConstraintDistance(((IResponseTimeConstrainable)resource).getResponseTime());
	}
}
