package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.CloudService;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

public class MachineTypeConstraint extends ArchitecturalConstraint {

	public MachineTypeConstraint(Constraint constraint) {
		super(constraint);
	}

	@Override
	protected boolean checkConstraintSet(IConstrainable resource) throws ConstraintEvaluationException {
		
		//if the resource is a cloud service get the ccloud service and restart
		if(resource instanceof Tier){
			//if the constraint is not defined on the resource then it is ok
			if(!sameId(resource))
				return true;
			return checkConstraintSet(((Tier)resource).getCloudService());
		}
		//if the resource is a cloud service evaluate the name
		else if((CloudService.class.isAssignableFrom(resource.getClass()))){
			CloudService resourceAsService = (CloudService) resource;
			return checkConstraintSet(resourceAsService.getResourceName());
		}else{		
			throw new ConstraintEvaluationException("Evaluating a Machine type constraint on a wrong resource with id: "+resource.getId()+
					" MachineType constraints should be evaluated against "+Tier.class+" or "+CloudService.class+", the specified resource is of type: "+resource.getClass());
		}
		
	}

	@Override
	protected double checkConstraintDistance(IConstrainable resource) throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a Machine type constraint as a numerical range constraint");
	}

}
