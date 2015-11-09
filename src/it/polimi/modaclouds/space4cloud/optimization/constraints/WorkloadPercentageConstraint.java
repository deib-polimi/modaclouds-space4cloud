package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;
import it.polimi.modaclouds.space4cloud.utils.Configuration;
import it.polimi.modaclouds.space4cloud.utils.Rounder;

public class WorkloadPercentageConstraint extends QoSConstraint {

	public WorkloadPercentageConstraint(Constraint constraint) {
		super(constraint);
		id = Configuration.APPLICATION_ID;
		Float min = Rounder.round(this.range.getHasMinValue());

		if (min > 1)
			this.range.setHasMinValue(Rounder.round(min / 100));
	}

	// Workload percentage are used only to set MILP and the optimization, never
	// checked.
	@Override
	protected double checkConstraintDistance(IConstrainable resource) throws ConstraintEvaluationException {
		return -1;
	}

	@Override
	protected boolean checkConstraintSet(IConstrainable resource) throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a Workload Percentage constraint on a set");
	}

	/**
	 * @return the minimum number of replicas
	 */
	public double getMin() {
		return Rounder.round(this.range.getHasMinValue());
	}

	// TODO: add methods to derive the value fromt he resource then call the
	// evaluation

}
