package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.qos_models.schema.Parameter;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IPercentileRTConstrainable;

public class PercentileRTconstraint extends RTConstraint {

	public static final String PARAMETER_NAME = "thPercentile";
	int level;


	public PercentileRTconstraint(Constraint constraint) {
		super(constraint);
		//We have just 1 parameter for constraint
		Parameter parameter = constraint.getMetricAggregation().getParameters().get(0); 
		if(parameter.getName().equals(PARAMETER_NAME))
			try{
				level = Integer.parseInt(parameter.getValue());
			}catch(NumberFormatException e){
				logger.error("Error converting the value of the "+PARAMETER_NAME+" of constraint with id: "+constraint.getId()+"to an integer.");
			}
		else
			logger.error("Wrong parameter specified for percentile response time constraint: "+constraint.getId()+" the allowed parameter is "+PARAMETER_NAME+" while "+parameter.getName()+" was specified.");
	}


	public int getLevel() {
		return level;
	}


	@Override
	public double checkConstraintDistance(IConstrainable resource) {
		if(!(resource instanceof IPercentileRTConstrainable)){
			logger.error("Evaluating a RAM constraint on a wrong resource with id: "+resource.getId()+
					" RAM constraints should be evaluated against "+IPercentileRTConstrainable.class+
					", the specified resource is of type: "+resource.getClass());
			return -1;
		}
		if(((IPercentileRTConstrainable)resource).getRtPercentiles() == null){
			logger.warn("No percentile was derived by the LQN evaluation, ignoring percentile response time constraint");
			return -1;
		}
		if(!((IPercentileRTConstrainable)resource).getRtPercentiles().containsKey(level)){
			String errorMessage = "The evaluation of the resource did not produce the desired percentile, the percentile level specified in the constraint is: "+level+"\n";
			errorMessage += "Available percentiles are: ";
			errorMessage += "\nIgnoring percentile response time constraint";
			for(Integer i:((IPercentileRTConstrainable)resource).getRtPercentiles().keySet())
				errorMessage += "\t"+i;
			logger.warn(errorMessage);
			return -1;
		}

		double percentileValue = ((IPercentileRTConstrainable)resource).getRtPercentiles().get(level);
		return super.checkConstraintDistance(percentileValue);
	}

}
