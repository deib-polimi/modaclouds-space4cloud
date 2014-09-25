package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
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
	public double checkConstraintDistance(IConstrainable resource) {
		if(!(resource instanceof Tier && ((Tier)resource).getCloudService() instanceof IaaS)){
			logger.error("Error evaluating replica constraint on a non IaaS resource");
			return Double.POSITIVE_INFINITY;
		}
		
		IaaS iaasResource = (IaaS) ((Tier)resource).getCloudService();
		return super.checkConstraintDistance(iaasResource.getReplicas());

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
	protected boolean checkConstraintSet(IConstrainable measurement) {
		logger.error("Evaluating a replica constraint with an inset or outset");
		return false;
	}

}
