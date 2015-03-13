package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;

public class DBTypeConstraint extends ArchitecturalConstraint {

	public DBTypeConstraint(Constraint constraint) {
		super(constraint);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double checkConstraintDistance(IConstrainable resource)
			throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a DB type constraint as a numerical range constraint");
	}

	@Override
	protected boolean checkConstraintSet(IConstrainable resource)
			throws ConstraintEvaluationException {
		// TODO Auto-generated method stub
		return false;
	}

}
