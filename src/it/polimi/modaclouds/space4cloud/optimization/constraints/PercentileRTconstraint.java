package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.qos_models.schema.Parameter;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.optimization.solution.impl.IPercentileRTConstrainable;

public class PercentileRTconstraint extends RTConstraint {

	public static final String PARAMETER_NAME = "thPercentile";
	int level;


	public PercentileRTconstraint(Constraint constraint) {
		super(constraint);
		//We have just one parameter for constraint
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
	public double checkConstraintDistance(IConstrainable resource) throws ConstraintEvaluationException {
		//if the resource type is correct
		if(resource instanceof IPercentileRTConstrainable){
			//if the constraint is not defined on the resource then it is ok
			if(!sameId(resource))
				return Double.NEGATIVE_INFINITY;
			IPercentileRTConstrainable constResource = (IPercentileRTConstrainable) resource;
			//if constraints have been evaluated
			if(constResource.getRtPercentiles() != null){
				//if the desired percentile has been produced
				if(constResource.getRtPercentiles().containsKey(level)){
					double percentileValue = ((IPercentileRTConstrainable)resource).getRtPercentiles().get(level);					
					logger.trace("Percentile found for functionality: "+constResource.getId());
					return checkConstraintDistance(percentileValue);
				}else{
					String errorMessage = "The evaluation of the resource did not produce the desired percentile, the percentile level specified in the constraint is: "+level+"\n";
					errorMessage += "Available percentiles are: ";
					errorMessage += "\n";
					for(Integer i:((IPercentileRTConstrainable)resource).getRtPercentiles().keySet())
						errorMessage += "\t"+i;
					throw new ConstraintEvaluationException(errorMessage);
				}				
			}else{				
				logger.warn("No percentile was derived by the LQN evaluation, ignoring percentile response time constraint for functionality: "+constResource.getId());
				return Double.NEGATIVE_INFINITY;
			}
		}else{
			throw new ConstraintEvaluationException("Evaluating a percentile ResponseTime constraint on the wrong type of resouce"+resource);
		}

	}


	@Override
	protected boolean checkConstraintSet(IConstrainable resource) throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a Percentile response time constraint as a set constraint");
	}


	public double getMax() {
		return range.getHasMaxValue();
	}

}
