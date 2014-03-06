package it.polimi.modaclouds.space4cloud.optimization.constraints;


public abstract class Constraint {

	protected String resourceId;
	protected Metric metric;
	protected Unit unit;
	protected int priority;
	//protected String unit;
	protected IRange range;
		
	public abstract double checkConstraintDistance(Object masurement);
	
	
	
	public Constraint(String resourceId, Metric metric, int priority, Unit unit) {
		this.resourceId = resourceId;
		this.metric = metric;
		this.priority = priority;
		this.unit = unit;
	}
	
	public int getPriority(){
		return priority; 
	}
	
	public String getResourceID(){
		return resourceId;
	}

	public Metric getMetric(){
		return metric;
	}
	
	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}	

	public IRange getRange() {
		return range;
	}

	public void setRange(IRange range) {
		this.range = range;
	}
	

	
}
