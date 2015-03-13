package it.polimi.modaclouds.space4cloud.optimization.constraints;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.space4cloud.exceptions.ConstraintEvaluationException;
import it.polimi.modaclouds.space4cloud.optimization.solution.IConstrainable;

public class LanguageConstraint extends ArchitecturalConstraint {

	public LanguageConstraint(Constraint constraint) {
		super(constraint);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double checkConstraintDistance(IConstrainable resource)
			throws ConstraintEvaluationException {
		throw new ConstraintEvaluationException("Evaluating a Language type constraint as a numerical range constraint");
	}

	@Override
	protected boolean checkConstraintSet(IConstrainable resource)
			throws ConstraintEvaluationException {
		// TODO Auto-generated method stub
		return false;
	}

}
