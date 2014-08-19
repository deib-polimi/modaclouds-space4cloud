package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.qos_models.schema.Parameter;

public class PercentileRTconstraint extends RTConstraint {

	public static final String PARAMETER_NAME = "thPercentile";
	int level;
	
	
	public PercentileRTconstraint(Constraint constraint) {
		super(constraint);
		//We have just 1 parameter for constraint
		Parameter parameter = constraint.getMetricAggregation().getParameters().get(0); 
		if(parameter.getName().equals(PARAMETER_NAME))
			try{
			level = Integer.getInteger(parameter.getValue());
			}catch(NumberFormatException e){
				logger.error("Error converting the value of the "+PARAMETER_NAME+" of constraint with id: "+constraint.getId()+"to an integer.");
			}
		else
			logger.error("Wrong parameter specified for percentile response time constraint: "+constraint.getId()+" the allowed parameter is "+PARAMETER_NAME+" while "+parameter.getName()+" was specified.");
	}


	public int getLevel() {
		return level;
	}

}
