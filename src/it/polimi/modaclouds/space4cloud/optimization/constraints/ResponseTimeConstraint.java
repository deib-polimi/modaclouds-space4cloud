package it.polimi.modaclouds.space4cloud.optimization.constraints;

public class ResponseTimeConstraint extends QoSConstraint {

	public ResponseTimeConstraint(String id, Metric metric, int priority,Unit unit) {
		super(id, metric, priority, unit);
	}

		
	@Override
	public void setMax(double maxValue) {
		if(unit.equals(Unit.MILLISECONDS)){
			setUnit(Unit.SECONDS);
			setMax(maxValue/1000);
		}else			
		super.setMax(maxValue);
	}
	
}
