package it.polimi.modaclouds.space4cloud.optimization.constraints;

public class UsageConstraint extends QoSConstraint {

	public UsageConstraint(String id, Metric metric, int priority, Unit unit) {
		super(id, metric, priority, unit);
		// TODO Auto-generated constructor stub
	}
	
	public double checkConstraintDistance(Object measurement) {		
		//in the constraint the value is expressed in percentage
		return super.checkConstraintDistance(((Double)measurement)*100);				
	}

}
