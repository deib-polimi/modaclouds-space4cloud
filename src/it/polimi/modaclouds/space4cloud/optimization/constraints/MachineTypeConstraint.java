package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.resourcemodel.cloud.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Compute;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

public class MachineTypeConstraint extends ArchitecturalConstraint {

	public MachineTypeConstraint(Constraint constraint) {
		super(constraint);
	}

	@Override
	protected boolean checkConstraintSet(IConstrainable resource) {
		if(!(CloudService.class.isAssignableFrom(resource.getClass()))){
			logger.error("Evaluating a RAM constraint on a wrong resource with id: "+((Tier)resource).getId()+
					" RAM constraints should be evaluated against "+Tier.class+" with a "+Compute.class+
					"resource, the specified resource is of type: "+resource.getClass());
			return false;
		}
		return super.checkConstraintSet(((Tier)resource).getCloudService().getResourceName());
	}

	@Override
	protected double checkConstraintDistance(IConstrainable resource) {
		logger.error("Evaluating machine type constraints on a numerical range  constraint");
		return Double.POSITIVE_INFINITY;
	}

}
