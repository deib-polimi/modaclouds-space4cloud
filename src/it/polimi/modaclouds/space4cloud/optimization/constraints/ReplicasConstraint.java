package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IaaS;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.Tier;

public class ReplicasConstraint extends ArchitecturalConstraint {

	/**
	 * Builds the constraint on the allowed number of replicas of a IaaS resource
	 * @param constraint
	 */
	public ReplicasConstraint(Constraint constraint) {
		super(constraint);		
	}

	
	
	@Override
	public double checkConstraintDistance(IConstrainable resource) throws ConstraintEvaluationException {
		//if the resource is a Tier with a Compute then get inside and check the resource ram
		if(resource instanceof Tier){
			//if the constraint is not defined on the resource then it is ok
			if(!sameId(resource))
				return Double.NEGATIVE_INFINITY;
			return checkConstraintDistance(((Tier) resource).getCloudService());
			//if the tier is hosted on a compute resource							
		}else if(resource instanceof IaaS){
			IaaS iaasResource = (IaaS) resource;
			return checkConstraintDistance(iaasResource.getReplicas());			
		}else{
			throw new ConstraintEvaluationException("Evaluating a replica constraint on a wrong resource with id: "+resource.getId()+
					" replica constraints should be evaluated against "+Tier.class+" hosted on a IaaS resource or "+IaaS.class+
					"resources, the specified resource is of type: "+resource.getClass());	
		}
	
	}
	/**
	 * @return the minimum number of replicas
	 */
	public int getMin(){
		return Math.round(range.getHasMinValue());
	}
	/**
	 * @return the maximum number of replicas
	 */
	public int getMax(){
		return Math.round(range.getHasMaxValue());
	}
	
	public boolean hasMaxReplica(IaaS resource){
		if(range.getHasMaxValue() == null)
			return false;
		return resource.getReplicas() >= range.getHasMaxValue();
	}
	
	public boolean hasMinReplica(IaaS resource){
		if(range.getHasMinValue() == null)
			return false;
		return resource.getReplicas() <= range.getHasMinValue();
	}

	@Override
	protected boolean checkConstraintSet(IConstrainable measurement) throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a Replica constraint as a set constraint");
	}

}
